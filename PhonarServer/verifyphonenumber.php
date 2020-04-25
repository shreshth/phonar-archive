<?php

// Read Constants
require_once 'consts.php';
// Read Utils
require_once 'utils.php';

// Read phone number
$phone = get_param("phone");
error_if(is_null($phone));

// Generate a random challenge code
$code = substr(str_shuffle(str_repeat('0123456789',100)),0,6);

// Store the code
$connection = new Mongo();
$db = $connection->Phonar;
$verificationcodes = $db->verificationcode;
$query = array("phone" => $phone);
$verificationcode = $verificationcodes->findOne($query);
if (is_null($verificationcode)) {
    $verificationcode = array("phone" => $phone, "code" => $code, "expiration" => (intval(microtime(true)) + 10*60*1000));
    $verificationcodes->insert($verificationcode);
} else {
    $verificationcode["code"] = $code;
    $verificationcode["expiration"] = intval(microtime(true)) + 10*60*1000;
    $verificationcodes->save($verificationcode);
}

// Send an msg to that phone number
send_sms($phone, "Phonar Code: " . $code);
?>