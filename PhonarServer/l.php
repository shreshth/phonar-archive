<?php

// include utils
require_once 'utils.php';
// To track usage stats
require_once 'stats.php';

if (array_key_exists('QUERY_STRING', $_SERVER)) {
    $code = $_SERVER['QUERY_STRING'];
    $url = getLongUrl($code);
    if ($url != null) {
        // Track stats
        stats_smsclick($code);

        header("Location: $url");
        exit;
    }
}
error_if(true);

?>