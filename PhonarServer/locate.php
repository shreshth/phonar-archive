<?php
// Read constants
require_once 'consts.php';
// Read Utils
require_once 'utils.php';
?>
<!DOCTYPE html>
<html>
    <head>
        <title>Phonar</title>

        <link rel="stylesheet" type="text/css" href="css/phonar-bootstrap.css" />
        <link rel="stylesheet" type="text/css" href="css/mobilepage.css" />
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <link href='http://fonts.googleapis.com/css?family=Open+Sans:400' rel='stylesheet' type='text/css'>

        <script type="text/javascript" src="js/jquery.js"></script>
        <script type="text/javascript" src="http://maps.googleapis.com/maps/api/js?sensor=false"></script>
        <script>
            var map;
            var marker;
            var sent = false;
            var min_accuracy = 100;
            var in_accurate = false;
            var timeoutID = 0;
            var timeoutStarted = false;

            function on_position(position) {
                if (timeoutStarted == false) {
                    var timeoutval = 20 * 1000;
                    timeoutID = setTimeout('handle_error_inaccurate()', timeoutval);
                    timeoutStarted = true;
                }

                if (sent == false) {
                    if (position.coords.accuracy < min_accuracy) {
                        handle_position(position);
                        sent = true;
                    } else {
                        in_accurate = true;
                    }
                }
            }

            function handle_position(position) {
                clearTimeout(timeoutID);
                var latlng = new google.maps.LatLng(position.coords.latitude, position.coords.longitude);
                var myOptions = {
                zoom: 15,
                center: latlng,
                mapTypeId: google.maps.MapTypeId.ROADMAP
                };
                map = new google.maps.Map(document.getElementById("map_canvas"), myOptions);
                var image = new google.maps.MarkerImage('img/30_mylocation.png');
                var marker = new google.maps.Marker({
                                                    position: latlng,
                                                    map: map,
                                                    icon: image,
                                                    title: "Your location",
                                                    });
                var data = {
                    userphone : "<?php echo get_param("userphone") ?>",
                    targetphone : "<?php echo get_param("targetphone") ?>",
                    lat : position.coords.latitude,
                    lng : position.coords.longitude,
                    count : <?php echo $FIRST_UPDATE ?>,
                }
                $.ajax({
                       type: 'POST',
                       url: "http://<?php echo $_SERVER['HTTP_HOST']?>/onfriendlocation.php",
                       data: data,
                       success: handle_success,
                       error: handle_error_sending,
                       });
            }

            function handle_success() {
                document.getElementById('locationsucceedmessage').innerHTML ='Thanks! We told your friend where you are right now. Want to know where your friend is? Scroll down to download the Phonar app!';
                document.location.hash = "sent";
            }

            function handle_error(error) {
                if ((error.code == error.TIMEOUT) && (in_accurate == true)) {
                    handle_error_inaccurate();
                } else {
                    handle_error_locating();
                }
                var data = {
                        userphone : "<?php echo get_param("userphone") ?>",
                        targetphone : "<?php echo get_param("targetphone") ?>",
                        lat : -1,
                        lng : -1,
                        error : <?php echo $ERROR_USER_DENY_LOCATION ?>,
                        count : <?php echo $FIRST_UPDATE ?>,
                    }
                $.ajax({
                       type: 'POST',
                       url: "http://<?php echo $_SERVER['HTTP_HOST'] ?>/onfriendlocation.php",
                       data: data,
                       error: handle_error_sending,
                       });
            }

            function handle_error_sending() {
                if (sent == false) {
                    document.getElementById('locationsucceedmessage').innerHTML = 'Sorry, we couldn\'t send your location. Please check your internet connection and refresh this page to try again';
                    clearTimeout(timeoutID);
                }
            }

            function handle_error_locating() {
                if (sent == false) {
                    document.getElementById('locationsucceedmessage').innerHTML = 'Sorry, we couldn\'t get your location. Make sure that your GPS and browser location services are turned on and then refresh this page.';
                    clearTimeout(timeoutID);
                }
            }

            function handle_error_inaccurate() {
                if (sent == false && in_accurate == true) {
                    document.getElementById('locationsucceedmessage').innerHTML = 'Sorry, we could not get an accurate enough location. This could be caused by poor GPS reception.';
                    clearTimeout(timeoutID);
                }
            }

            function read_position() {
                if (document.location.hash) {
                    document.getElementById('locationsucceedmessage').innerHTML ='We have already told your friend where you are right now. Want to know where your friend is? Scroll down to download the Phonar app!';
                } else if (navigator.geolocation) {
                    var timeoutval = 20 * 1000;
                    navigator.geolocation.watchPosition(on_position, handle_error, {enableHighAccuracy:true,maximumAge:0,timeout:timeoutval});
                    document.getElementById('locationsucceedmessage').innerHTML ='Searching for your location (this could take up to 20 seconds). Make sure your GPS is turned on in order to improve location accuracy.';
                }
                else {
                    handle_error_locating(null);
                }
            }
        </script>
    </head>

    <body onload="read_position()">
        <div class="container">
            <img id="header" src="img/phonar_logo_large.png" style="width:100%"></img>
            <br />
            <h4 class="span12" style="font-family: 'Open Sans', sans-serif; text-align:center" id="locationsucceedmessage"></h4>
            <br />
            <div id="map_canvas"></div>
            <br />
            <table style="text-align:center;width:100%" id="market_links">
                <tr>
                    <td style="text-align:center;width:40%">
                        <a style="text-align:center;width:100%" href="http://market.android.com/details?id=com.phonar" title="Download Phonar to your Android device">
                            <img style="text-align:center;width:100%" src="img/google_play.png" alt="Download Phonar to your Android device" />
                        </a>
                    </td>
                    <td style="text-align:center;width:40%">
                        <img style="text-align:center;width:100%" src="img/iphone_coming_soon.png" alt="Coming soon to iPhone" />
                    </td>
                </tr>
            </table>
        </div>
    </body>
</html>

