<?php
// Read Constants
require_once 'consts.php';
// Read Utils
require_once 'utils.php';
// Read c2dm.php
require_once 'c2dm.php';

// Read phone and password
$devicenumber = get_param("devicenumber");
$password = get_param("password");
$pushtoken = get_param("pushtoken");
$apptype = get_param("APP_TYPE");
$staleIsOK = get_param("staleIsOK");

error_if(is_null($devicenumber) or is_null($password) or is_null($apptype));

// Check if the device is authorized
$connection = new Mongo();
$db = $connection->Phonar;
$devices = $db->device;
$query = array( "phone" => $devicenumber, "password" => $password );
$device = $devices->findOne($query);
error_if(is_null($device));

//add to active requests table
if (!is_null($pushtoken)) {
    $activerequests = $db->activerequests;
    $query = array("pushtoken" => $pushtoken, "apptype" => $apptype, "devicenumber" => $devicenumber, "tracking" => false);
    $activerequests->remove($query);
    $activerequests->save($query);
}

// we do different stuff depending on different models
if ($device["model"] == $MODEL_MT90) {
    send_sms($devicenumber, "FACID,$password,QUERY,Link");
} else if ($device["model"] == $MODEL_GT03B) {
    send_sms($devicenumber, "URL#");
}

// if stale data is ok, send back stale data
if (!is_null($staleIsOK) && $staleIsOK == "1" && $device["time"] != null) {
    if ($apptype == "iOS") {
        // TODO: Implement this for iOS
    } else if ($apptype == "android") {
        sendC2DMAndroid($pushtoken, $ANDROID_C2DM_DEVICE_RESPONSE_KEY, $ANDROID_C2DM_DEVICE_RESPONSE_KEY . $device["lat"] . "," . $device["lng"] . "," . $device["phone"] . "," . $device["time"] . "," . $device["battery"], 0);
    }
}

?>
