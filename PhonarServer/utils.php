<?php

/**
 * Safe method for getting a parameter from $_REQUEST. Returns NULL if parameter is not present
 */
function get_param($param) {
    if (array_key_exists($param, $_REQUEST)) {
        return $_REQUEST[$param];
    } else {
        return NULL;
    }
}

/**
 * Shows an error and exits if the condition is true
 */
function error_if($condition) {
    if ($condition) {
        header('HTTP/1.0 400 Bad Request');
        exit();
    }
}

/**
 * Whether or not we are using prod_mode or dev_mode
 */
function isProd() {
    return !($_SERVER["SERVER_NAME"] == "dev.phonar.me");
}

/**
 * Helper function for getting short_url version of long url
 */
function getShortUrl($url) {
    $connection = new Mongo();
    $db = $connection->Phonar;
    $short_urls = $db->short_url;
    // create a random string
    $code = substr(str_shuffle(str_repeat('ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789',100)),0,8);
    // check if already exists
    $query = array("code" => $code);
    $short_url = $short_urls->findOne($query);
    if (is_null($short_url)) {
        $short_url = array("code" => $code, "url" => $url);
        $short_urls->insert($short_url);
    } else {
        $short_url["url"] = $url;
        $short_urls->save($short_url);
    }
    return $code;
}

/**
 * Helper function for getting long url version of short_url
 */
function getLongUrl($code) {
    $connection = new Mongo();
    $db = $connection->Phonar;
    $short_urls = $db->short_url;
    $query = array("code" => $code);
    $short_url = $short_urls->findOne($query);
    if (is_null($short_url)) {
        return NULL;
    } else {
        return $short_url["url"];
    }
}

/**
 * Send an SMS using Twilio
 */
function send_sms($to, $msg) {
    // Include Twilio API
    require_once 'Services/Twilio.php';
    // Read Constants
    require_once 'consts.php';
    global $TWILIO_SID, $TWILIO_AUTH_TOKEN, $TWILIO_PHONE_NUMBER;

    $client = new Services_Twilio($TWILIO_SID, $TWILIO_AUTH_TOKEN);
    $message = $client->account->sms_messages->create(
                    $TWILIO_PHONE_NUMBER,
                    $to,
                    $msg
    );
}

/**
 * Get the user id of the user. Redirects to login if NULL
 */
function get_userid_or_redirect() {
    $userid = get_userid();
    if (is_null($userid)) {
        $_SESSION["redirect_url"] = $_SERVER['REQUEST_URI'];
        redirect_login();
    } else {
        return $userid;
    }
}

/**
 * Get the user id of the user. Returns null if not logged in
 */
function get_userid() {
    // Resume session
    if(!isset($_SESSION)) {
        session_start();
    }
    if (array_key_exists("user_id", $_SESSION)) {
        return $_SESSION["user_id"];
    } else {
        return NULL;
    }
}

/**
 * Get the user name of the user. Returns null if not logged in
 */
function get_username() {
    // Resume session
    if(!isset($_SESSION)) {
        session_start();
    }
    if (array_key_exists("user_name", $_SESSION)) {
        return $_SESSION["user_name"];
    } else {
        return NULL;
    }
}

/**
 * Ask the user to log in
 */
function redirect_login() {
    header("Location: /login");
    exit;
}

/**
 *  Formats a phone number according to E164 rukes
 */
function formatPhone($phone, $locale) {
    $output = array();
    exec('java PhonarNumberFormatter ' . $phone . ' ' . $locale, $output);
    return $output[0];
}

?>