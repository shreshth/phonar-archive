<?php

require_once 'utils.php';
$phone = get_param("p");
$locale = get_param("l");

echo formatPhone($phone, $locale);

?>
