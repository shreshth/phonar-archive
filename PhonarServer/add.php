<?php
    $UPLOAD_NO_ERROR = 0;       // no error in uploading
    $UPLOAD_MAX_SIZE = 1048576; // 1 MB
    // Read utils
    require_once('utils.php');
	// Read Constants
	require_once 'consts.php';

    // Get user_id
    $user_id = get_userid_or_redirect();
    
    // Get parameters
    $phone = get_param('phone');
    $password = get_param('password');
    $type = get_param('type');
    $name = get_param('name');
    $is_new = false;
    if (is_null($phone) or is_null($password) or is_null($name) or is_null($type)) {
        $request = explode("#", $_SERVER['REQUEST_URI']);
        $request = $request[0];
        $params = explode("?", $request);
        error_if(count($params) < 4);
        $phone = $params[1];
        $password = $params[2];
        $type = $params[3];
        $name = "";
        $is_new = $true;
    }

    // Whether this is a submission request or form request?
    if (!is_null(get_param('submission'))) {
        // check for valid data
        if ((strlen($name) == 0) or ($_FILES["image"]["error"] != $UPLOAD_NO_ERROR) or ($_FILES["image"]["size"] >= $UPLOAD_MAX_SIZE) or (strtolower(substr($_FILES["image"]["type"], 0, 5)) != "image")) {
            header("Location: /add?error&phone=" . urlencode($phone) . "&password=" . urlencode($password) . "&type=$type&name=$name");
            exit;
        }
        // add/modify device
        $connection = new Mongo();
        $db = $connection->Phonar;
        $query = array( "userid" => $user_id);
        $user = $db->users->findOne($query);
        error_if(is_null($user));
        $devices = $user["devices"];
        error_if(is_null($devices));
        // copy image file to new place
        move_uploaded_file($_FILES["image"]["tmp_name"], dirname(__FILE__) . "/img/userimages/" . $user_id . $phone . $password);
        // check if device already exists
        $exists = false;
        $i = 0;
        foreach ($devices as $device) {
            if (($device["phone"] == $phone) and ($device["password"] == $password) and ($device["type"] == $type)) {
                $device["name"] = $name;
                $devices[$i] = $device;
                $exists = true;
                break;
            }
            $i++;
        }
        if (!$exists) {
            $device = array(
                            "phone" => $phone,
                            "password" => $password,
                            "type" => $type,
                            "name" => $name,
            );
            $devices[] = $device;
        }
        $user["devices"] = $devices;
        $db->users->save($user);
        header("Location: /webapp");
    } else {
        // show form to add/modify device
?>
        <!DOCTYPE html>
        <html lang="en">
            <head>
                <title>Phonar: Find Everything</title>
                <?php require 'head.php' ?>
                <link rel="stylesheet" type="text/css" href="css/add.css" />
            </head>
            <body>
                <?php require 'navbar.php' ?>
                <div class="container body-container">
                    <h2 id="intro">
                        <?php if ($is_new) { ?>
                            Add Device
                        <?php } else { ?>
                            Edit Device
                        <?php } ?>
                    </h2>
                    <?php if (!is_null(get_param("error"))) { ?>
                        <div class="alert alert-error alert-block span3" id="add-error">
                            <a class="close" data-dismiss="alert" href="#">&times;</a>
                            We were unable to process your request. Please make sure that the name and image parameters are valid.
                        </div>
                        <script>
                        $(".alert").alert()
                        </script>
                    <?php } ?>
                    <div id="form" class="span10">
                        <form action="/add" enctype="multipart/form-data" method="post">
                            <input type="text" class="hidden" name="submission" value='1' />
                            <input type="text" class="hidden" name="phone" value='<?php echo $phone; ?>' />
                            <input type="text" class="hidden" name="password" value='<?php echo $password; ?>' />
                            <input type="text" class="hidden" name="type" value='<?php echo $type; ?>' />
                            <table id="form-table">
                                <tr id="name-row" class="form-row">
                                    <td>
                                        Name:&nbsp
                                    </td>
                                    <td>
                                        <input type="text" name="name" value='<?php echo $name; ?>' />
                                    </td>
                                </tr>
                                <tr id="image-row" class="form-row">
                                    <td>
                                        Image:&nbsp
                                    </td>
                                    <td>
                                        <input type="file" name="image" />
                                    </td>
                                </tr>
                                <tr id="submit-row" class="form-row">
                                    <td colspan="2">
                                        <input type="submit" />
                                    </td>
                                </tr>
                            </table>
                        </form>
                    </div>
                </div>
                <?php require 'footer.php'; ?>		
            </body>
        </html>
<?php } ?>