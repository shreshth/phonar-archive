<?php

// Read Constants
require_once 'consts.php';
// Read Utils
require_once 'utils.php';
// c2dm.php
require_once 'c2dm.php';
//Apple push settings
require_once('apple_push_config.php');

/*
 * Make push message for iOS
*/
function makePayload($lat, $lng, $from_number, $error) {
    // Create the payload body
    $body['aps'] = array(
                    'type' => 'deviceupdate',
                    'alert' => 'You have received a location update in Phonar',
                    'sound' => 'default',
                    'location' => array($lat, $lng),
                    'device' => $from_number,
                    'error' => $error
    );
    return json_encode($body);
}

/**
 * Parse the SMS body and return lat and lng
 * @param unknown_type $text SMS body
 */
function parseSMSLocationMT90($text) {
    $matches = array();
    preg_match("/[-+.0-9]+,[-+.0-9]+/", $text, $matches);
    if (!empty($matches)) {
        return explode(",", $matches[0]);
    } else {
        return array();
    }
}

/**
 * Parse the SMS body and return lat and lng
 * @param unknown_type $text SMS body
 */
function parseSMSLocationGT03B($text) {
    $text = str_replace("N", "+", $text);
    $text = str_replace("S", "-", $text);
    $text = str_replace("E", "+", $text);
    $text = str_replace("W", "-", $text);
    $matches = array();
    preg_match("/[-+.0-9]+,[-+.0-9]+/", $text, $matches);
    if (!empty($matches)) {
        return explode(",", $matches[0]);
    } else {
        return array();
    }
}

/**
 * Parse the SMS body and return battery level
 * @param unknown_type $text SMS body
 */
function parseSMSBatteryMT90($text) {
    $matches = array();
    preg_match("/BAT=[0-9]+%/", $text, $matches);
    if (!empty($matches)) {
        return substr($matches[0], 4);
    } else {
        return "NA";
    }
}

// Check account SID
$account_sid = get_param('AccountSid');
$to_number = get_param('To');
error_if(is_null($account_sid) or is_null($to_number));
error_if($account_sid != $TWILIO_SID or $to_number != $TWILIO_PHONE_NUMBER);

// Read in phone number
$from_number = get_param('From');
error_if(is_null($from_number));

// Ensure that it's an already existing device
$connection = new Mongo();
$db = $connection->Phonar;
$devices = $db->device;
$query = array( "phone" => $from_number );
$device = $devices->findOne($query);
error_if(is_null($device));

// Parse SMS body
$sms = get_param('Body');
error_if(is_null($sms));

// do different stuff depending on which type of device
if ($device["model"] == $MODEL_MT90) {
    $latlng = parseSMSLocationMT90($sms);
    $battery = parseSMSBatteryMT90($sms);
} else if ($device["model"] == $MODEL_GT03B) {
    $latlng = parseSMSLocationGT03B($sms);
    // does not support battery level reading
    $battery = "null";
}

if (!empty($latlng)) {
    $lat = $latlng[0];
    $lng = $latlng[1];

    // for some devices, (0,0) means error, so we ignore these msgs
    if (($lat == 0) || ($lng == 0)) {
        exit;
    }

    $device['lat'] = $lat;
    $device['lng'] = $lng;
    $device['time'] = intval(microtime(true));
    $device['battery'] = $battery;

    $devices->save($device);

    //check in the activerequests table to push
    //add to active requests table
    $activerequests = $db->activerequests;
    $query = array("devicenumber" => $from_number);
    $cursor = $activerequests->find($query);
    if (!$cursor->hasNext()) {
        exit();
    }

    // future use
    $error = null;

    foreach ($cursor as $request) {
        if ($request["apptype"] == "iOS") {
            try {
                $mode = 'development';
                $config = $config[$mode];

                $apns = new APNS_Push($config);
                $apns->connectToAPNS();
                if (is_null($error)) {
                    $apns->sendNotification("", $request["pushtoken"],makePayload($lat, $lng, $from_number, $ERROR_NO_ERROR));
                } else {
                    $apns->sendNotification("", $request["pushtoken"],makePayload($lat, $lng, $from_number, $error));
                }
            } catch (Exception $e) {
                error_log($e);
                exit();
            }
        }
        else {
            sendC2DMAndroid($request["pushtoken"], $ANDROID_C2DM_DEVICE_RESPONSE_KEY, $ANDROID_C2DM_DEVICE_RESPONSE_KEY . $device["lat"] . "," . $device["lng"] . "," . $device["phone"] . "," . $device["time"] . "," . $device["battery"], 0);
        }
    }
    $query = array("devicenumber" => $device["phone"], "tracking" => false);
    $activerequests->remove($query);
}
?>
