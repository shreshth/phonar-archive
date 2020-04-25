<?php

// Twilio constants
$TWILIO_SID = "AC37a51731a7414fa2a83c1206e43804aa";
$TWILIO_AUTH_TOKEN = "f56f33e9284947d607dade6658d83e56";
$TWILIO_PHONE_NUMBER = (!($_SERVER["SERVER_NAME"] == "dev.phonar.me")) ? "+12345426925" : "+16096812631";

// Phonar email and password
$EMAIL = "phonar@phonar.me";
$EMAIL_PASSWORD = "LJLRglVkTERbLo6";

// Android C2DM stuff
$ANDROID_C2DM_API_KEY = "AIzaSyBJ3N0-6Isn2dmxiH66dtu--T9KTd_8F88";
$ANDROID_C2DM_TIME_TO_LIVE = 0;
$ANDROID_C2DM_REQUEST_KEY = "1";
$ANDROID_C2DM_RESPONSE_KEY = "2";
$ANDROID_C2DM_DEVICE_RESPONSE_KEY = "3";
$ANDROID_C2DM_UPDATE_KEY = "4";
$ANDROID_C2DM_MAX_SEND_COUNT = 4; // max number of times to send C2DM request
$ANDROID_C2DM_EXP_BACKOFF = 1; // exponential backoff
$ANDROID_C2DM_BACKOFF_SLEEP_TIME = 0.1; // sleep time for exponential backoff
// C2DM error codes
$ANDROID_C2DM_DEVICE_ERROR_1 = "Error=InvalidRegistration";
$ANDROID_C2DM_DEVICE_ERROR_2 = "Error=NotRegistered";
// location error codes
$ERROR_NO_ERROR = 0; // no error
$ERROR_USER_DENY_LOCATION = 1; // Replicate in Phonar:RequestLocationDenied.java
$FIRST_UPDATE = 0; // replicate in Phonar:RequestLocationActivity.java
$LATER_UPDATE = 1; // replicate in Phonar:RequestLocationActivity.java

// secret stuff
$INTERNAL_PASSWORD = 'EwAaPHqSY6KZ758G';

// Device types
$DEVICE_GENERIC = "generic";
$DEVICE_KIDS_SHOE = "kids_shoe";
$DEVICE_ADULT_SHOE = "adult_shoe";
$DEVICE_CAR = "car";
$DEVICE_PET = "pet";

// Device model types
$MODEL_MT90 = "mt-90";
$MODEL_GT03B = "gt-03b";

// Android Update Notification Stuff
$ANDROID_UPDATE_NOTIFICATION_MIN_VERSION = 13;
$ANDROID_UPDATE_NOTIFICATION_LATEST_VERSION = 15;

// Stripe stuff
$STRIPE_PK_TEST = "pk_09JFXkQClaquaqnV4h8cDWhU5Hr6u";
$STRIPE_SK_TEST = "sk_09JFvHaRg9fF5EFiiu8UrKpHLUGze";
$STRIPE_PK_LIVE = "pk_09JFtZ9pfJsiLak853hCBg3PQr6lx";
$STRIPE_SK_LIVE = "sk_09JF8eLRbEcok615NK0o0MkyK6Rtj";
$STRIPE_PK = $STRIPE_PK_TEST;
$STRIPE_SK = $STRIPE_SK_TEST;

// Preorder page stuff
$KIDS_SHOES = "Kids Shoes";
$ADULT_SHOES = "Adult Shoes";
$DOG_COLLAR = "Dog Collar";
$CAR_TRACKER = "Car Tracker"; 
$PORTABLE_TRACKER = "Portable Tracker";
$PRODUCTS = array($KIDS_SHOES, $ADULT_SHOES, $DOG_COLLAR, $CAR_TRACKER, $PORTABLE_TRACKER);
$PRICES = array($KIDS_SHOES => 60.00, 
                $ADULT_SHOES => 60.00, 
                $DOG_COLLAR => 50.00, 
                $CAR_TRACKER => 50.00,
                $PORTABLE_TRACKER => 50.00);
?>
