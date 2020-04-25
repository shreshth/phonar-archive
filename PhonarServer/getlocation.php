<?php
// Read Constants
require_once 'consts.php';
// Read Utils
require_once 'utils.php';

$devicenumber = get_param("devicenumber");
$password = get_param("password");
$apptype = get_param("APP_TYPE");
error_if(is_null($devicenumber) or is_null($password) or is_null($apptype));

// return location
$connection = new Mongo();
$db = $connection->Phonar;
$devices = $db->device;
$query = array( "phone" => $devicenumber, "password" => $password );
$device = $devices->findOne($query);
error_if(is_null($device));
echo $device["lat"] . "," . $device["lng"] . "," . $device["time"];
?>