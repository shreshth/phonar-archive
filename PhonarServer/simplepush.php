<?php

// Import utils
require_once 'utils.php';
// Apple push settings
require_once('apple_push_config.php');
// Import c2dm.php
require_once('c2dm.php');

// Put your device token here (without spaces):
$deviceToken = '8a3457d85a15ad4127e939b6b8eaeb474616399e6710d96f5fbf7497db0a4dd7';

// Put your private key's passphrase here:
$passphrase = 'phonar';

// Put your alert message here:
$message = 'Trying simple push';

////////////////////////////////////////////////////////////////////////////////

$ctx = stream_context_create();
stream_context_set_option($ctx, 'ssl', 'local_cert', 'iospushsec.pem');
stream_context_set_option($ctx, 'ssl', 'passphrase', $passphrase);

echo 'Connected to APNS' . PHP_EOL;

$body['aps'] = array(
                'alert' => 'You have received a location update in Phonar',
                'sound' => 'default',
                'location' => array(20.43, 40.34),
                'device' => "+16098655796",
                'error' => $error);

// Encode the payload as JSON
$payload = json_encode($body);
try{
    $mode = 'development';
    $config = $config[$mode];

    $apns = new APNS_Push($config);
    $apns->connectToAPNS();

    $apns->sendNotification("", $deviceToken ,$payload);

} catch (Exception $e) {
    fatalError($e);
}
