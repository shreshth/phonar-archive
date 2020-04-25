<?php
require_once 'utils.php';

// Get user_id
$user_id = get_userid_or_redirect();

// Read phone number
$phone = get_param("phone");
error_if(is_null($phone));

// Remove phone from user model
$connection = new Mongo();
$db = $connection->Phonar;
$query = array( "userid" => $user_id);
$user = $db->users->findOne($query);
error_if(is_null($user));
$phones = $user['phones'];
$key = array_search($phone, $phones);
if ($key !== FALSE) {
    unset($phones[$key]);
}
$user['phones'] = $phones;
$db->users->save($user);
header("Location: /addphone");
?>