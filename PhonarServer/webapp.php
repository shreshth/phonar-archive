<?php
	// navbar
	$webapp_nav = TRUE;
    // Read utils
    require_once('utils.php');
	// Read Constants
	require_once 'consts.php';

    // Get user_id
    $user_id = get_userid_or_redirect();

    // Get user's devices
    $connection = new Mongo();
    $db = $connection->Phonar;
    $query = array( "userid" => $user_id);
    $user = $db->users->findOne($query);
    error_if(is_null($user));
    $devices = $user["devices"];
    error_if(is_null($devices));
?>
<!DOCTYPE html>
<html lang="en">
    <head>
        <title>Phonar: Find Everything</title>
        <?php require 'head.php' ?>
        <link href="/css/jquery.pnotify.default.css" media="all" rel="stylesheet" type="text/css" />
        <link href="/css/jquery.pnotify.default.icons.css" media="all" rel="stylesheet" type="text/css" />
        <link rel="stylesheet" type="text/css" href="css/webapp.css" />
		
		<script type="text/javascript" src="js/bootstrap-tooltip.js"></script>
        <script type="text/javascript" src="js/bootstrap-popover.js"></script>
        <script type="text/javascript" src="js/phonarpopover.js"></script>
        <script type="text/javascript" src="js/jquery.pnotify.min.js"></script>
        <script type="text/javascript" src="http://maps.googleapis.com/maps/api/js?sensor=false"></script>
        <script type="text/javascript" src="js/webapp.js"></script>
        <script>
            var devices = [
            <?php foreach ($devices as $device) { ?>
                ["<?php echo $device["phone"]; ?>", "<?php echo $device["password"]; ?>", "img/userimages/<?php echo $user_id . $device["phone"] . $device["password"]; ?>?dummy=<?php echo rand(0,1000000); ?>", null, null, null, null],
            <?php } ?>
            ];
        </script>
    </head>
    <body onload="initialize()">
        <?php require 'navbar.php' ?>
        <div class="container">
            <div class="row">
                <div class="span4 bottom-padded">
                    <div id="devices-container" class="container body-container">
                        <div id="devices">
                            <div id="devices-title">
                                Devices
                            </div>
                            <?php if (empty($devices)) { ?>
                                You do not have any devices yet. You can use the order page above to buy one right now.
                            <?php } else { ?>
                                <table>
                                    <?php foreach($devices as $device) { ?>
                                    <tr class="device-row">
                                        <td class="device-image-td">
                                            <img class="device-image pull-left" src="img/userimages/<?php echo $user_id . $device["phone"] . $device["password"]; ?>?dummy=<?php echo rand(0,1000000); ?>"/>
                                        </td>
                                        <td class="device-details-td">
                                            <table class="device-details-table">
                                                <tr class="device-name-row">
                                                    <td class="device-name-td" colspan="3">
                                                        <?php echo $device["name"]; ?>
                                                    </td>
                                                </tr>
                                                <tr class="device-actions-row">
                                                    <td class="device-action-td device-locate-td">
                                                        <a class="device-action-a" href="#" onclick="findonce('<?php echo $device["phone"]; ?>', '<?php echo $device["password"]; ?>')">Locate</a>
                                                    </td>
                                                    <td class="device-action-td device-edit-td">
														<a rel="popover" class="device-action-a" title="Edit <?php echo $device["name"]; ?>" href="add?phone=<?php echo urlencode($device["phone"]); ?>&password=<?php echo urlencode($device["password"]); ?>&type=<?php echo $device["type"]; ?>&name=<?php echo urlencode($device["name"]); ?>"><i class="icon-edit btn-icon"></i></a>
                                                    </td>
                                                    <td class="device-action-td device-remove-td">
                                                        <a rel="popover" class="device-action-a" title="Delete <?php echo $device["name"]; ?>" href="removedevice?phone=<?php echo urlencode($device["phone"]); ?>&password=<?php echo urlencode($device["password"]); ?>"><i class="icon-remove btn-icon"></i></a>
                                                    </td>
                                                </tr>
                                            </table>
                                        </td>
                                    </tr>
                                    <?php } ?>
                                </table>
                            <?php } ?>
                        </div>
                    </div>
                </div>
                <div class="span8">
                    <div id="map" class="container body-container"></div>
                </div>
            </div>
        </div>
		<?php require 'footer.php'; ?>
    </body>
</html>
