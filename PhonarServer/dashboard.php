<?php
require_once 'consts.php';
require_once 'utils.php';
$password = get_param("password");
?>
<!DOCTYPE html>
<html lang="en">
<body>
    <?php
    if (!is_null($password) && $password == $INTERNAL_PASSWORD) {
        $blacklist = array("+16092168134", "+16094337777", "+17325702172", "+16097516257", "+18025484527");
        $connection = new Mongo();
        $db = $connection->Phonar;
        $installs = $db->install;
        $total_installs = 0;
        $cursor = $installs->find(array("phonenumber" => array('$nin' => $blacklist)))->sort(array("phonenumber" => 1));
        $prev_number = "impossibru";
        foreach ($cursor as $install) {
            $phonenumber = $install["phonenumber"];
            if ($phonenumber != $prev_number) {
                $total_installs = $total_installs + 1;
            }
            $prev_number = $phonenumber;
        }
        $uninstalls = $db->uninstall;
        $total_uninstalls = $uninstalls->count(array("phonenumber" => array('$nin' => $blacklist)));
        $sms = $db->smssent;
        $total_sms = $sms->count(array("userphone" => array('$nin' => $blacklist), "targetphone" => array('$nin' => $blacklist), "clicks" => array('$gt' => 0)));
        $requests = $db->locationrequest;
        $total_requests = $requests->count(array("userphone" => array('$nin' => $blacklist), "targetphone" => array('$nin' => $blacklist)));
        $responses = $db->locationresponses;
        $total_responses = $responses->count(array("userphone" => array('$nin' => $blacklist), "targetphone" => array('$nin' => $blacklist), "error" => false));
        $total_errors = $responses->count(array("userphone" => array('$nin' => $blacklist), "targetphone" => array('$nin' => $blacklist), "error" => true));
        ?>
    <h1>Stats not counting Hamza, Nitin, Shreshth, Faaez, Clem:</h1>
    <table>
        <tr>
            <td>Unique Activations:</td>
            <td><?php echo $total_installs; ?>
            </td>
        </tr>
        <tr>
            <td>Uninstalls detected:</td>
            <td><?php echo $total_uninstalls; ?>
            </td>
        </tr>
        <tr>
            <td>SMSs sent and clicked:</td>
            <td><?php echo $total_sms; ?>
            </td>
        </tr>
        <tr>
            <td>App to App location requests:</td>
            <td><?php echo $total_requests; ?>
            </td>
        </tr>
        <tr>
            <td>Total location responses:</td>
            <td><?php echo $total_responses; ?>
            </td>
        </tr>
        <tr>
            <td>Times location denied / error in location:</td>
            <td><?php echo $total_errors; ?>
            </td>
        </tr>
    </table>
    <?php } else {
        ?>
    <form action="/dashboard.php" method="post">
        <input type="text" name="password" /> <input type="submit" />
    </form>
    <?php } ?>
</body>
</html>
