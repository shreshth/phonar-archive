<?php

// Read utils
require_once('utils.php');
// Read Constants
require_once 'consts.php';

// Get user_id
$user_id = get_userid_or_redirect();

// Get parameters
$phone = get_param('phone');
$password = get_param('password');
error_if(is_null($phone) or is_null($password));

// remove device
$connection = new Mongo();
$db = $connection->Phonar;
$query = array( "userid" => $user_id);
$user = $db->users->findOne($query);
error_if(is_null($user));
$devices = $user["devices"];
$i = 0;
foreach ($devices as $device) {
    if (($device["phone"] == $phone) and ($device["password"] == $password)) {
        unset($devices[$i]);
        break;
    }
    $i++;
}
$user["devices"] = $devices;
$db->users->save($user);
header("Location: /webapp");
?>