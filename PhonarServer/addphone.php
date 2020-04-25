<?php
// navbar
$addphone_nav = TRUE;

// Read Utils
require_once 'utils.php';

// Get user_id
$user_id = get_userid_or_redirect();

// Get user's phone numbers
$connection = new Mongo();
$db = $connection->Phonar;
$query = array("userid" => $user_id);
$user = $db->users->findOne($query);
error_if(is_null($user));
$phones = $user["phones"];
?>
<!DOCTYPE html>
<html lang="en">
    <head>
        <title>Phonar: Find Everything</title>
        <?php require 'head.php' ?>
        <link rel="stylesheet" type="text/css" href="css/addphone.css" />
        <script src="js/bootstrap-alert.js" type="text/javascript"></script>
        <script src="js/bootstrap-tooltip.js"></script>
        <script src="js/bootstrap-popover.js"></script>
        <script src="js/phonarpopover.js"></script>
    </head>
    <body>
        <?php require 'navbar.php' ?>

        <hr class="top-line visible-phone spaced" />

        <div class="container body-container">
            <h2 id="intro">
                By verifying your phone number, you can keep your Phonar Mobile Apps and the Web App in sync.
            </h2>
            <table id="phone-numbers">
                <?php foreach($phones as $phone) { ?>
                <tr class="phone-number-row">
					<td>
						<form action="/removephone" method="post" id="remove<?php echo $phone; ?>">
							<input type="text" class="hidden" name="phone" value='<?php echo $phone; ?>' />
						</form>
						<?php echo $phone; ?>
						<a rel="popover" class="left-padded" href="#" title="Delete <?php echo $phone; ?>" type="submit" onclick="document.getElementById('remove<?php echo $phone; ?>').submit();">
							<i class="icon-remove btn-icon"></i>
						</a>
                    </td>
                </tr>
                <?php } ?>
            </table>
            <?php if (!is_null(get_param("error"))) { ?>
                <div class="alert alert-error alert-block span3" id="addphone-error">
                    <a class="close" data-dismiss="alert" href="#">&times;</a>
                    We were unable to verify this phone number. Please make sure that the phone number is valid.
                </div>
                <script>
                $(".alert").alert()
                </script>
            <?php } ?>
            <div id="new-phone-form" class="span10">
                <form action="/verifyphone" method="post">
                    Enter phone number to add:&nbsp                    
					<?php require('countrycodes.php'); ?> <input type="text" name="phone" id="phonenumber_input" />	
					<input type="submit" />
                </form>
            </div>
        </div>

        <!-- Footer -->
        <?php require 'footer.php'; ?>
    </body>
</html>
