<?php

// Configuration file for Apple push notifications; usage in utils.php

$config = array(
	// These are the settings for development mode
	'development' => array(

		// The APNS server that we will use
		'server' => 'gateway.sandbox.push.apple.com:2195',

		// The SSL certificate that allows us to connect to the APNS servers
		'certificate' => 'iospushsec.pem',
		'passphrase' => 'phonar',

		// Name and path of our log file
		'logfile' => 'log/push_development.log',
		),

	// These are the settings for production mode
	'production' => array(

		// The APNS server that we will use
		'server' => 'gateway.push.apple.com:2195',

		// The SSL certificate that allows us to connect to the APNS servers
		'certificate' => 'iospushsec.pem',
		'passphrase' => 'phonar',

		// Name and path of our log file
		'logfile' => 'log/push_production.log',
		),
	);
