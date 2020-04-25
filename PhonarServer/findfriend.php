<?php

// Read Constants
require_once 'consts.php';
// Utils
require_once 'utils.php';
// To track usage stats
require_once 'stats.php';
// Import c2dm.php
require_once 'c2dm.php';

// Read parameters
$userphone = get_param("userphone");
$targetphone = get_param("targetphone");
$c2dm_id = get_param("c2dm_id");
$app_type = get_param("APP_TYPE");
$locale = get_param("locale");
$device_id = get_param("device_id");

error_if(is_null($userphone) or is_null($c2dm_id) or is_null($app_type) or is_null($targetphone) or is_null($device_id));

// Check that user device exists
$connection = new Mongo();
$db = $connection->Phonar;
$phones = $db->phone;
$query = array("phonenumber" => $userphone, "device_id" => $device_id);
$phone = $phones->findOne($query);
error_if(is_null($phone));

// Locate url depends on whether we are in prod or dev mode
$url_locate = isProd() ? "http://phonar.me/locate.php" : "http://dev.phonar.me/locate.php";

// Check if target device exists
$query = array( "phonenumber" => $targetphone);
$phone = $phones->findOne($query);
if (is_null($phone)) {
    // Send back the short code so the user phone can send sms
    $short_code = getShortUrl($url_locate . "?targetphone=" . urlencode($targetphone) . "&userphone=" . urlencode($userphone));
    echo "?" . $short_code;
    // Track stats
    stats_smssent($userphone, $targetphone, $short_code);
} else {
    if ($phone["app_type"] == "android") {
        // Locate via c2dm
        if (sendC2DMAndroid($phone["c2dm_id"], $ANDROID_C2DM_REQUEST_KEY, $ANDROID_C2DM_REQUEST_KEY . $userphone, 0) === false) {
            // Send back the short_code so the user phone can send sms
            $short_code = "?" . getShortUrl($url_locate . "?targetphone=" . urlencode($targetphone) . "&userphone=" . urlencode($userphone));
            echo $short_code;
            stats_smssent($userphone, $targetphone, $short_code);
        } else {
            // Track stats
            stats_locationrequest($userphone, $targetphone);
        }
    } else {
        // testing: send back url for sms
        $short_code = getShortUrl($url_locate . "?targetphone=" . urlencode($targetphone) . "&userphone=" . urlencode($userphone));
        echo "?" . $short_code;
    }
}

?>
