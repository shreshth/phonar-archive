<?php

// Read Constants
require_once 'consts.php';
// Read Utils
require_once 'utils.php';
// Apple push settings
require_once('apple_push_config.php');
// Read c2dm.php
require_once('c2dm.php');
// To track usage stats
require_once 'stats.php';

// Read parameters
$userphone = get_param("userphone");     // we will send location to this guy
$targetphone = get_param("targetphone"); // the location is position of this guy
$error = get_param("error");             // error code
$lat = get_param("lat");
$lng = get_param("lng");
$count = get_param("count");             // number of packets sent before
error_if(is_null($targetphone) or is_null($userphone) or is_null($lat) or is_null($lng) or is_null($count));

// Get target phone
$connection = new Mongo();
$db = $connection->Phonar;
$phones = $db->phone;

// Make sure phone already exists
$query = array( "phonenumber" => $userphone);
$phone = $phones->findOne($query);
error_if(is_null($targetphone));

// Check device type
if ($phone["app_type"] == "android") {
    if (is_null($error)) {
        sendC2DMAndroid($phone["c2dm_id"], $ANDROID_C2DM_RESPONSE_KEY, $ANDROID_C2DM_RESPONSE_KEY . $lat . "," . $lng . "," . $targetphone . "," . $ERROR_NO_ERROR . "," . $count, 0);
        stats_locationresponse($userphone, $targetphone, false);
    } else {
        sendC2DMAndroid($phone["c2dm_id"], $ANDROID_C2DM_RESPONSE_KEY, $ANDROID_C2DM_RESPONSE_KEY . $lat . "," . $lng . "," . $targetphone . "," . $error . "," . $count, 0);
        stats_locationresponse($userphone, $targetphone, true);
    }
} else {
    try {
        $mode = 'development';
        $config = $config[$mode];

        $apns = new APNS_Push($config);
        $apns->connectToAPNS();
        if (is_null($error)) {
            $apns->sendNotification("", $phone["c2dm_id"],makePayload($lat, $lng, $targetphone, $ERROR_NO_ERROR));
        } else {
            $apns->sendNotification("", $phone["c2dm_id"],makePayload($lat, $lng, $targetphone, $error));
        }
    } catch (Exception $e) {
        error_log($e);
    }
}

function makePayload($lat, $lng, $targetphone, $error) {
    // Create the payload body
    $body['aps'] = array(
                    'type' => 'friendupdate',
                    'alert' => 'Your friend has shared their location with you',
                    'sound' => 'default',
                    'location' => array($lat, $lng),
                    'friendnumber' => $targetphone,
                    'error' => $error
    );
    return json_encode($body);
}
?>