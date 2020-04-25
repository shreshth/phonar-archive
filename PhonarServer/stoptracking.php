<?php
// Read Constants
require_once 'consts.php';
// Read Utils
require_once 'utils.php';

// Read phone and password
$phone = get_param("devicenumber");
$apptype = get_param("APP_TYPE");
$password = get_param("password");
$pushtoken = get_param("pushtoken");

error_if(is_null($phone) or is_null($password) or is_null($pushtoken) or is_null($apptype));

// Check if the device is authorized
$connection = new Mongo();
$db = $connection->Phonar;
$devices = $db->device;
$query = array( "phone" => $phone, "password" => $password );
$device = $devices->findOne($query);
error_if(is_null($device));

// we do different stuff depending on different models
if ($device["model"] == $MODEL_MT90) {
    // don't stop tracking if other users are tracking the same device
    $activerequests = $db->activerequests;
    $query = array("devicenumber" => $phone, "tracking" => true);
    $cursor = $activerequests->find($query);
    error_if(!$cursor->hasNext());

    // Send an SMS to the device through Twilio if the only request in table
    if ($cursor->count() <= 1) {
        send_sms($phone, "FACID,$password,LOC");
    }

    // mark that no one is tracking any more
    $query = array("pushtoken" => $pushtoken, "apptype" => $apptype, "tracking" => true);
    $activerequests->remove($query);
} else if ($device["model"] == $MODEL_GT03B) {
    // This model does not support tracking
}

?>
