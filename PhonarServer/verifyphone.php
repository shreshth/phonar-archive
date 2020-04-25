<?php
// Read Utils
require_once 'utils.php';
// Import consts
require_once 'consts.php';

// Get user_id
$user_id = get_userid_or_redirect();

// Read phone number
$phone = get_param("phone");
error_if(is_null($phone));

// Read country code
$locale = get_param("locale");
error_if(is_null($locale));
$phone = formatPhone($phone, $locale);

$code = get_param("code");

if (!is_null($code)) {
	// Get the stored code
	$connection = new Mongo();
	$db = $connection->Phonar;
	$verificationcodes = $db->verificationcode;
	$query = array("phone" => $phone);
	$verificationcode = $verificationcodes->findOne($query);
	if (is_null($verificationcode)) {
		header("Location: /addphone?error");
        exit;
	} else if ($verificationcode["expiration"] > (intval(microtime(true)) + 10*60*1000)) {
		header("Location: /addphone?error");
        exit;
	} else if ($verificationcode["code"] != $code) {
		header("Location: /addphone?error");
        exit;
	} else {
		// add phone
		$query = array( "userid" => $user_id);
		$user = $db->users->findOne($query);
        error_if(is_null($user));
		$phones = $user['phones'];
        if (!in_array($phone, $phones)) {
            $phones[] = $phone;
        }
		$user['phones'] = $phones;
		$db->users->save($user);
		header("Location: /addphone");    
	}
	exit;
} else {
    // Generate a random challenge code
    $code = substr(str_shuffle(str_repeat('0123456789',100)),0,6);

    // Store the code
    $connection = new Mongo();
    $db = $connection->Phonar;
    $verificationcodes = $db->verificationcode;
    $query = array("phone" => $phone);
    $verificationcode = $verificationcodes->findOne($query);
    if (is_null($verificationcode)) {
        $verificationcode = array("phone" => $phone, "code" => $code, "expiration" => (intval(microtime(true)) + 10*60*1000));
        $verificationcodes->insert($verificationcode);
    } else {
        $verificationcode["code"] = $code;
        $verificationcode["expiration"] = intval(microtime(true)) + 10*60*1000;
        $verificationcodes->save($verificationcode);
    }

    // Send an msg to that phone number
    send_sms($phone, "Phonar Code: " . $code);
?>
	<!DOCTYPE html>
	<html lang="en">
		<head>
			<title>Phonar: Find Everything</title>
			<?php require 'head.php' ?>
            <link rel="stylesheet" type="text/css" href="css/addphone.css" />
		</head>
		<body>
			<?php require 'navbar.php' ?>

			<hr class="top-line visible-phone spaced" />

			<div class="container body-container">
                <div id="verify-phone-form">
                    <form action="/verifyphone" method="post">
                        Enter the code you received:&nbsp 
                        <input type="text" class="hidden" name="phone" value='<?php echo $phone; ?>' />
						<input type="text" class="hidden" name="locale" value='<?php echo $locale; ?>' />
                        <input autofocus type="text" name="code" />
                        <input type="submit" />
                    </form>
                </div>
			</div>

			<!-- Footer -->
			<?php require 'footer.php'; ?>
		</body>
	</html>
<?php } ?>