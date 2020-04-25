<?php
// Read Constants
require_once 'consts.php';
// Read Utils
require_once 'utils.php';
// Read c2dm.php
require_once 'c2dm.php';
//Apple push settings
require_once('apple_push_config.php');

$password = get_param("password");
?>
<!DOCTYPE html>
<html lang="en">
<body>
    <?php
    if (!is_null($password) && $password == $INTERNAL_PASSWORD) {
        $connection = new Mongo();
        $db = $connection->Phonar;
        $phones = $db->phone->find();
        // Iterate over phones
        $i = 0;
        foreach ($phones as $phone) {
            $i++;
            // Only for old versions of the android app
            if (($phone["app_type"] == "android") && ((!array_key_exists("version", $phone)) || ($phone["version"] == null) || ($phone["version"] == 9999) || ($phone["version"] == "9999"))) {
                // Send bogus locations response
                sendC2DMAndroid($phone["c2dm_id"], $ANDROID_C2DM_RESPONSE_KEY, $ANDROID_C2DM_RESPONSE_KEY . 0 . "," . 0 . "," . "+11234567890" . "," . $ERROR_NO_ERROR . "," . 1, 0);
            }
            error_log($i);
        }
    } else {
        ?>
    <form action="/detectuninstalls.php" method="post">
        <input type="text" name="password" /> <input type="submit" />
    </form>
    <?php } ?>
</body>
</html>
