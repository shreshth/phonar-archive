<?php

function stats_installed($phonenumber, $app_type) {
    date_default_timezone_set(timezone_name_from_abbr("EST"));
    $connection = new Mongo();
    $db = $connection->Phonar;
    $installs = $db->install;
    $query = array( "phonenumber" => $phonenumber, "app_type" => $app_type, "date" => date('m-d-Y g:i a'));
    $installs->insert($query);
}

function stats_uninstalled($c2dm_id, $app_type) {
    date_default_timezone_set(timezone_name_from_abbr("EST"));
    $connection = new Mongo();
    $db = $connection->Phonar;
    $uninstalls = $db->uninstall;
    $query = array( "c2dm_id" => $c2dm_id, "app_type" => $app_type, "date" => date('m-d-Y g:i a'));
    $uninstalls->insert($query);
}

function stats_smssent($userphone, $targetphone, $short_code) {
    date_default_timezone_set(timezone_name_from_abbr("EST"));
    $connection = new Mongo();
    $db = $connection->Phonar;
    $smssents = $db->smssent;
    $smsent = array( "userphone" => $userphone, "targetphone" => $targetphone, "time" => date('m-d-Y g:i a'), "clicks" => 0, "short_code" => $short_code);
    $smssents->insert($smsent);
}

function stats_smsclick($short_code) {
    date_default_timezone_set(timezone_name_from_abbr("EST"));
    $connection = new Mongo();
    $db = $connection->Phonar;
    $smssents = $db->smssent;
    $query = array("short_code" => $short_code);
    $smssent = $smssents->findOne($query);
    if (!is_null($smssent)) {
        $smssent["clicks"] = $smssent["clicks"] + 1;
        $smssents->save($smssent);
    }
}

function stats_locationrequest($userphone, $targetphone) {
    date_default_timezone_set(timezone_name_from_abbr("EST"));
    $connection = new Mongo();
    $db = $connection->Phonar;
    $locationrequests = $db->locationrequest;
    $locationrequest = array( "userphone" => $userphone, "targetphone" => $targetphone, "time" => date('m-d-Y g:i a'));
    $locationrequests->insert($locationrequest);
}

function stats_locationresponse($userphone, $targetphone, $error) {
    date_default_timezone_set(timezone_name_from_abbr("EST"));
    $connection = new Mongo();
    $db = $connection->Phonar;
    $locationresponses = $db->locationresponses;
    $locationresponse = array( "userphone" => $userphone, "targetphone" => $targetphone, "error" => $error, "time" => date('m-d-Y g:i a'));
    $locationresponses->insert($locationresponse);
}

?>