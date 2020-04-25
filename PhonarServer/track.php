<?php
// Read Constants
require_once 'consts.php';
// Read Utils
require_once 'utils.php';

// Read phone and password
$devicenumber = get_param("devicenumber");
$password = get_param("password");
$pushtoken = get_param("pushtoken");
$apptype = get_param("APP_TYPE");
$interval = get_param("interval");
$polls = get_param("polls");
error_if(is_null($devicenumber) or is_null($password) or is_null($apptype) or is_null($pushtoken) or is_null($interval) or is_null($polls));

// Check if the device is authorized
$connection = new Mongo();
$db = $connection->Phonar;
$devices = $db->device;
$query = array( "phone" => $devicenumber, "password" => $password );
$device = $devices->findOne($query);
error_if(is_null($device));

// we do different stuff depending on different models
if ($device["model"] == $MODEL_MT90) {
    //add to active requests table
    $activerequests = $db->activerequests;
    $query = array("pushtoken" => $pushtoken, "apptype" => $apptype, "devicenumber" => $devicenumber, "tracking" => true);
    $activerequests->remove($query);
    $activerequests->save($query);

    // Send an SMS to the device through Twilio
    send_sms($devicenumber, "FACID,$password,LOC,I=$interval,T=$polls,L=0");
} else if ($device["model"] == $MODEL_GT03B) {
    // This model does not support tracking
}

?>
