<?php

// Read Constants
require_once 'consts.php';
// Read Utils
require_once 'utils.php';
// To track usage stats
require_once 'stats.php';

// Read parameters
$phonenumber = get_param("phone");
$c2dm_id = get_param("c2dm_id");
$app_type = get_param("APP_TYPE");
$code = get_param("code");
$device_id = get_param("device_id");
$app_version = get_param("version");
error_if(is_null($phonenumber) or is_null($c2dm_id) or is_null($app_type));
if (is_null($app_version)) {
    $app_version = "9999";
}

$connection = new Mongo();
$db = $connection->Phonar;

if (!is_null($code)) {
    // First time verification; check if code is correct
    $verificationcodes = $db->verificationcode;
    $query = array("phone" => $phonenumber);
    $verificationcode = $verificationcodes->findOne($query);
    error_if(is_null($verificationcode) or $verificationcode["code"] != $code or $verificationcode["expiration"] > (intval(microtime(true)) + 10*60*1000));
}

// Track stats
stats_installed($phonenumber, $app_type);

// store in database
$phones = $db->phone;
// Remove old entry if device_id is not null
if (!is_null($device_id)) {
    $query = array("phonenumber" => $phonenumber, "app_type" => $app_type);
    $phones->remove($query);
}
$query = array("phonenumber" => $phonenumber, "app_type" => $app_type, "device_id" => $device_id);
$phone = $phones->findOne($query);
if (is_null($phone)) {
    // Only add new entry if $device_id is not null
    error_if(is_null($device_id));
    $query["c2dm_id"] = $c2dm_id;
    $query["version"] = $app_version;
    $phones->insert($query);
} else {
    // Support for only versions of the app when we did not have device_id
    $phone["c2dm_id"] = $c2dm_id;
    $phone["version"] = $app_version;
    $phones->save($phone);
}

?>
