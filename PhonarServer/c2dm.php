<?php

/**
 * Send a C2DM request to a phone
 *
 * @param unknown_type $deviceRegistrationId
 * @param unknown_type $msgType all messages with same type may be "collapsed" together. If multiple are
 *                              sent, only the last will be received by phone, so this can be used to
 *                              avoid repeats
 * @param unknown_type $messageText
 * @param unknown_typ $sendCount The number of times this message has been sent (start with 0)
 * @return curl response
 */
function sendC2DMAndroid($deviceRegistrationId, $msgType, $messageText, $sendCount) {
    require_once 'stats.php';
    require_once 'consts.php';
    global $ANDROID_C2DM_MAX_SEND_COUNT, $ANDROID_C2DM_API_KEY, $ANDROID_C2DM_TIME_TO_LIVE, $ANDROID_C2DM_EXP_BACKOFF, $ANDROID_C2DM_BACKOFF_SLEEP_TIME, $ANDROID_C2DM_DEVICE_ERROR_1, $ANDROID_C2DM_DEVICE_ERROR_2;

    if ($sendCount >= $ANDROID_C2DM_MAX_SEND_COUNT) {
        return;
    }

    $headers = array('Authorization:key=' . $ANDROID_C2DM_API_KEY);
    $data = array(
                    'registration_id' => $deviceRegistrationId,
                    'collapse_key' => $msgType,
                    'data.message' => $messageText,
                    'time_to_live' => $ANDROID_C2DM_TIME_TO_LIVE
    );

    $ch = curl_init();

    curl_setopt($ch, CURLOPT_URL, "https://android.googleapis.com/gcm/send");

    if ($headers) {
        curl_setopt($ch, CURLOPT_HTTPHEADER, $headers);
    }
    curl_setopt($ch, CURLOPT_SSL_VERIFYPEER, false);
    curl_setopt($ch, CURLOPT_POST, true);
    curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
    curl_setopt($ch, CURLOPT_POSTFIELDS, $data);

    $response = curl_exec($ch);
    $http_code = curl_getinfo($ch, CURLINFO_HTTP_CODE);

    if ($http_code == 503 or $http_code == 500) {
        // server timeout - exponential backoff and retry
        $retry_after = curl_getinfo($ch, 'Retry-After');
        if (is_null($retry_after)) {
            sleep($ANDROID_C2DM_EXP_BACKOFF * $ANDROID_C2DM_BACKOFF_SLEEP_TIME);
            $ANDROID_C2DM_EXP_BACKOFF = 2 * $ANDROID_C2DM_EXP_BACKOFF;
            curl_close($ch);
            sendC2DMAndroid($deviceRegistrationId, $msgType, $messageText, $sendCount + 1);
        } else {
            sleep($ANDROID_C2DM_EXP_BACKOFF * $retry_after);
            $ANDROID_C2DM_EXP_BACKOFF = 2 * $ANDROID_C2DM_EXP_BACKOFF;
            curl_close($ch);
            sendC2DMAndroid($deviceRegistrationId, $msgType, $messageText, $sendCount + 1);
        }
    }
    if ($http_code == 200) {
        $ANDROID_C2DM_EXP_BACKOFF = 1;

        // device not using C2DM anymore (for example, removed the app)
        if ($response === $ANDROID_C2DM_DEVICE_ERROR_1 or $response === $ANDROID_C2DM_DEVICE_ERROR_2) {
            // Track uninstall
            stats_uninstalled($deviceRegistrationId, "android");

            // remove from database
            $connection = new Mongo();
            $db = $connection->Phonar;
            $query = array( "c2dm_id" => $deviceRegistrationId );
            $db->phone->remove($query);

            curl_close($ch);
            return false;
        }
    }

    curl_close($ch);

    return $response;
}

function writeToLog($message)
{
    error_log($message);
}

function fatalError($message)
{
    writeToLog('Exiting with fatal error: ' . $message);
    exit;
}

class APNS_Push {
    private $fp = NULL;
    private $server;
    private $certificate;
    private $passphrase;

    function __construct($config)
    {
        $this->server = $config['server'];
        $this->certificate = $config['certificate'];
        $this->passphrase = $config['passphrase'];
    }

    // Opens an SSL/TLS connection to Apple's Push Notification Service (APNS).
    // Returns TRUE on success, FALSE on failure.
    function connectToAPNS()
    {
        $ctx = stream_context_create();
        stream_context_set_option($ctx, 'ssl', 'local_cert', $this->certificate);
        stream_context_set_option($ctx, 'ssl', 'passphrase', $this->passphrase);

        $this->fp = stream_socket_client(
                        'ssl://' . $this->server, $err, $errstr, 60,
                        STREAM_CLIENT_CONNECT|STREAM_CLIENT_PERSISTENT, $ctx);

        if (!$this->fp)
        {
            writeToLog("Failed to connect: $err $errstr");
            return FALSE;
        }

        writeToLog('Connection OK');
        return TRUE;
    }

    // Drops the connection to the APNS server.
    function disconnectFromAPNS()
    {
        fclose($this->fp);
        $this->fp = NULL;
    }

    // Attempts to reconnect to Apple's Push Notification Service. Exits with
    // an error if the connection cannot be re-established after 3 attempts.
    function reconnectToAPNS()
    {
        $this->disconnectFromAPNS();

        $attempt = 1;

        while (true)
        {
            writeToLog('Reconnecting to ' . $this->server . ", attempt $attempt");
            if ($this->connectToAPNS())
                return;

            if ($attempt++ > 3)
                fatalError('Could not reconnect after 3 attempts');
            sleep(60);
        }
    }

    // Sends a notification to the APNS server. Returns FALSE if the connection
    // appears to be broken, TRUE otherwise.
    function sendNotification($messageId, $deviceToken, $payload)
    {
        if (strlen($deviceToken) != 64)
        {
            writeToLog("Message $messageId has invalid device token");
            return TRUE;
        }

        if (strlen($payload) < 10)
        {
            writeToLog("Message $messageId has invalid payload");
            return TRUE;
        }


        writeToLog("Sending message $messageId to '$deviceToken', payload: '$payload'");

        if (!$this->fp)
        {
            writeToLog('No connection to APNS');
            return FALSE;
        }

        // The simple format
        $msg = chr(0)                  // command (1 byte)
        . pack('n', 32)                // token length (2 bytes)
        . pack('H*', $deviceToken)     // device token (32 bytes)
        . pack('n', strlen($payload))  // payload length (2 bytes)
        . $payload;                    // the JSON payload

        $result = @fwrite($this->fp, $msg, strlen($msg));

        if (!$result)
        {
            writeToLog('Message not delivered');
            return FALSE;
        }

        writeToLog('Message successfully delivered');

        return TRUE;
    }

}
?>