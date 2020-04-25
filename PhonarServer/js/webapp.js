var map;
var user_position = new google.maps.LatLng(41.574361,-103.987792);
var user_position_marker = null;
var got_user_position = false;
$.pnotify.defaults.delay -= 5000;

function initialize() {
    var timeoutval = 20 * 1000;
    var myOptions = {
        zoom: 4,
        maxZoom: 20,
        center: user_position,
        mapTypeId: google.maps.MapTypeId.ROADMAP,
    };
    map = new google.maps.Map(document.getElementById("map"), myOptions);
    navigator.geolocation.getCurrentPosition(on_user_position, on_user_position_error, {enableHighAccuracy:true,maximumAge:0,timeout:timeoutval});
    fetch_locations();
    start_timer();
}

function on_user_position(position) {
    user_position = new google.maps.LatLng(position.coords.latitude, position.coords.longitude);
    got_user_position = true;
    adjust_position();
}

function on_user_position_error() {
    $.pnotify({
        title: 'Cannot determine your location',
        text: 'Please enable gelocation services in your browser for the best possible experience.',
        history: false,
    });
}

function adjust_position() {
    // adjust zoom and center
    var bounds = new google.maps.LatLngBounds();
    for (var i = 0; i < devices.length; i++) {
        var device = devices[i];
        if (device[3] != null) {
            var point = new google.maps.LatLng(device[3], device[4]);
            bounds.extend(point);
        }
    }
    if (got_user_position) {
        bounds.extend(user_position);
    }
    map.fitBounds(bounds);
    // show user's own position
    if (user_position_marker != null) {
        user_position_market.setMap(null);
    }
    if (got_user_position) {
        marker = new google.maps.Marker({
            position: user_position,
            map: map,
        });
    }
    // show device location
    for (var i = 0; i < devices.length; i++) {
        var device = devices[i];
        if (device[6] != null) {
            device[6].setMap(null);
        }
        if ((device[3] != null) && (device[4] != null)) {
            var point = new google.maps.LatLng(device[3], device[4]);
            device[6] = new google.maps.Marker({
                position: point,
                map: map,
                icon: new google.maps.MarkerImage(device[2], null, null, null, new google.maps.Size(64, 64)),
            });
        }
    }
}

function findonce(phone, password) {
    $.post("findonce", {
        devicenumber: phone,
        password: password,
        APP_TYPE: "webapp",
    }, function(data) {
        $.pnotify({
            title: 'Location Requested',
            text: 'You should see the updated location shortly',
            type: 'success',
            history: false,
        });
    });
}

function fetch_locations() {
    for (var i = 0; i < devices.length; i++) {
        fetch_location(i);
    }
}

function fetch_location(i) {
    var device = devices[i];
    $.get("getlocation", {
        devicenumber: device[0],
        password: device[1],
        APP_TYPE: "webapp",
    }, function(data) {
        var params = data.split(',');
        device[3] = params[0]; // lat
        device[4] = params[1]; // lng
        device[5] = params[2]; // time
        adjust_position();
    });
}

function start_timer() {
    setInterval(function() {
        fetch_locations();
    }, 60000);
}
