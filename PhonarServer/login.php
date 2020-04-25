<?php
	// navbar
	$login_nav = TRUE;

    // Read utils
    require_once('utils.php');

    // start session
    if(!isset($_SESSION)) {
        session_start();
    }

    // check if user already logged in
    if (!is_null(get_userid())) {
        // redirect to webapp
        header("Location: /webapp");
        exit;
    }

    // get provider parameter
    $provider = get_param("provider");

    if (!is_null($provider)) {
        // try to login using chosen provider
        try {
            // initialize Hybrid_Auth with a given file
            $config = dirname(__FILE__) . '/lib/hybridauth/config.php';
            require_once( "lib/hybridauth/Hybrid/Auth.php" );
            $hybridauth = new Hybrid_Auth($config);
            // try to authenticate with the selected provider
            $adapter = $hybridauth->authenticate($provider);
            // then grab the user profile
            $user_profile = $adapter->getUserProfile();
            // get user id and username
            $user_id = $provider . "-" . $user_profile->identifier;
			$user_name = $user_profile->displayName;
            // store user data in session
            $_SESSION["user_id"] = $user_id;
			$_SESSION["user_name"] = $user_name;

            // if first login, create account for user
            $connection = new Mongo();
            $db = $connection->Phonar;
            $query = array( "userid" => $user_id);
            $user = $db->users->findOne($query);
            if (is_null($user)) {
                $user = array("userid" => $user_id, "phones" => array(), "devices" => array());
                $db->users->insert($user);
            }

            // redirect to redirect_url if set
            if (array_key_exists("redirect_url", $_SESSION) and !is_null($_SESSION["redirect_url"])) {
                header("Location: " . $_SESSION["redirect_url"]);
                exit;
            exit;
            }
            
            // redirect to webapp
            header("Location: /webapp");
            exit;
        } catch (Exception $e) {
            // redirect to webapp
            header("Location: /login?error");
            exit;
        }
        exit;
    } else {
        // show empty login page
?>
        <!DOCTYPE html>
        <html lang="en">
            <head>
                <title>Phonar: Find Everything</title>
                <?php require 'head.php' ?>
                <link rel="stylesheet" type="text/css" href="css/login.css" />
                <script src="js/bootstrap-alert.js" type="text/javascript"></script>
            </head>
            <body>
                <?php require 'navbar.php' ?>

                <hr class="top-line visible-phone spaced" />

                <div class="container body-container">                    
                    <?php if (!is_null(get_param("error"))) { ?>
                        <div class="alert alert-error alert-block span3" id="login-error">
                            <a class="close" data-dismiss="alert" href="#">&times;</a>
                            We encountered an error while trying to log you in. Please try again later.
                        </div>
                        <script>
                        $(".alert").alert()
                        </script>
                    <?php } ?>
                    <div>
                        <h2 id="intro">
                            Please use one of the options below to login
                        </h2>
                    </div>
                    <div id="login-options">
                        <a href="/login?provider=facebook"><img class="login-provider-button" src="img/facebook.png"/></a>
                        <a href="/login?provider=google"><img class="login-provider-button" src="img/google.png"/></a>
                        <a href="/login?provider=twitter"><img class="login-provider-button" src="img/twitter.png"/></a>
                    </div>
                </div>

                <!-- Footer -->
                <?php require 'footer.php'; ?>
            </body>
        </html>
<?php } ?>