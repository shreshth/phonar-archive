<?php
// Read Utils
require_once 'utils.php';
// Read Constants
require_once 'consts.php';

// Read phone and password
$phone = get_param("phone");
$password = get_param("password");
error_if(is_null($phone) or is_null($password));

// Check if the device is authorized
$connection = new Mongo();
$db = $connection->Phonar;
$devices = $db->device;
$query = array( "phone" => $phone, "password" => $password );
$cursor = $devices->find($query);
error_if(!$cursor->hasNext());

// Update the device's stored numbers
$sos1 = get_param("sos1");
$sos2 = get_param("sos2");
$sos3 = get_param("sos3");

$device = $devices->findOne($query);
error_if(is_null($device));

$device["sos1"] = $sos1;
$device["sos2"] = $sos2;
$device["sos3"] = $sos3;

$devices->save($device);

// Send an SMS to the device through Twilio
send_sms($phone, "*#288#$password#$sos1#$sos2#$sos3#");
?>
