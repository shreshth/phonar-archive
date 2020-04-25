<?php

// Resume session
if(!isset($_SESSION)) {
    session_start();
}

// clear user_id from session
$_SESSION["user_id"] = NULL;
$_SESSION["user_name"] = NULL;
$_SESSION["redirect_url"] = NULL;

// redirect homepage
header("Location: /");

?>