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
        foreach ($phones as $phone) {
            if ($phone["app_type"] == "android") {
                if (($phone["version"] >= $ANDROID_UPDATE_NOTIFICATION_MIN_VERSION) && (($phone["version"] < $ANDROID_UPDATE_NOTIFICATION_LATEST_VERSION))) {
                    // Send push notification
                    sendC2DMAndroid($phone["c2dm_id"], $ANDROID_C2DM_UPDATE_KEY, $ANDROID_C2DM_UPDATE_KEY . $ANDROID_UPDATE_NOTIFICATION_LATEST_VERSION, 0);
                }
            } else if ($phone["app_type"] == "iOS") {
                // TODO: Implement this
            }
        }
    } else {
        ?>
    <form action="/notifyupgrade.php" method="post">
        <input type="text" name="password" /> <input type="submit" />
    </form>
    <?php } ?>
</body>
</html>
