<!DOCTYPE html>
<html lang="en">
<head>
<title>Phonar: Find What You Care About</title>
<?php require 'head.php' ?>
<link rel="stylesheet" type="text/css" href="css/home.css" />
<script src="js/bootstrap-carousel.js"></script>
<script src="js/home.js"></script>
</head>
<body onload="initialize()">
<?php require 'navbar.php' ?>

<hr class="top-line visible-phone spaced" />

<!-- Main boilerplate for desktop -->
<div class="container body-container hidden-phone">
<table>
<tr>
<td>
<h2 id="title">
Thanks for checking us out!
</h2>
<br />
<h2 id="intro">
Phonar allows you to find people and assets you care about.
<br />
Whether they be kids, friends, pets or cars, you can use Phonar's mobile apps or GPS devices to locate them.
</br></br>
This page links to resources that will help you get to know us better.
<br /></br>
You can look at our <a href="http://phonar.me/phonar_deck.pdf">pitch deck</a>, find out more about the <a href="http://phonar.me/team">team</a>, download our <a href="http://phonar.me/download">app</a> or email us
at <a href="mailto:founders@phonar.me">founders@phonar.me</a> for more information, suggestions or feedback. Want to test-drive our product? Let us know and we would be happy to send a sample over!
</h2>
</td>
<td class="title-phone-container hidden-phone">
<img class="title-phone" src="img/phones/android.png"/>
<div id="home_carousel" class="carousel slide inside-title-phone">
<div class="carousel-inner title-screenshot">
<div class="active item title-screenshot">
<img class="title-screenshot" src="img/screenshots/track.png"/>
</div>
<div class="item title-screenshot">
<img class="title-screenshot" src="img/screenshots/ar.png"/>
</div>
<div class="item title-screenshot">
<img class="title-screenshot" src="img/screenshots/map.png"/>
</div>
</div>
</div>
</td>
</tr>
</table>
</div>
<!-- Main boilerplate for phone -->
<div class="container body-container visible-phone">
<hr class="space-nothing" />
<div class="row">
<div class="span12">
<table>
<tr>
<td class="left-padded right-padded">
<span class="info-section-title">
Find Everything You Care About
</span>
</td>
</tr>
<tr>
<td class="left-padded right-padded top-padded">
<div class="info-section-text center">
Phonar allows you to find people and assets you care about.
<hr class="space-nothing" />
Whether they be kids, friends, pets or cars, you can use Phonar's mobile apps or GPS devices to locate them.
</div>
</td>
</tr>
<tr class="hidden-phone">
<td>
<hr class="separator-line-wide" />
</td>
</tr>
<tr class="hidden-phone">
<td class="title-phone-container-mobile">
<img class="title-phone-mobile" src="img/phones/android.png"/>
<div id="home_carousel_mobile" class="carousel slide inside-title-phone-mobile">
<div class="carousel-inner title-screenshot-mobile">
<div class="active item title-screenshot-mobile">
<img class="title-screenshot-mobile" src="img/screenshots/track.png"/>
</div>
<div class="item title-screenshot-mobile">
<img class="title-screenshot-mobile" src="img/screenshots/ar.png"/>
</div>
<div class="item title-screenshot-mobile">
<img class="title-screenshot-mobile" src="img/screenshots/map.png"/>
</div>
</div>
</div>
</td>
</tr>
<tr>
<td>
<hr class="separator-line-wide" />
</td>
</tr>
<tr>
<table>
<tr>
<td class="title-action-mobile center">
<a href="/download">
<img class="title-action-image-mobile" src="img/download.png"/>
<div><span class="title-action-text-mobile">Download App</span></div>
</a>
</td>
<td class="title-action-mobile center">
<a href="/preorder">
<img class="title-action-image-mobile" src="img/order.png"/>
<div><span class="title-action-text-mobile">Pre-order Devices</span></div>
</a>
</td>
</tr>
</table>
</tr>
</table>
</div>
</div>
</div>

<hr class="space hidden-phone" />
<hr class="smallspace visible-phone" />

<!-- Features -->
<div class="container body-container">
<hr class="space-nothing" />
<div class="row">
<!-- Find friends -->
<div class="span4">
<table>
<tr>
<td class="left-padded right-padded">
<table class="nocenter">
<tr>
<td>
<img src="img/friends.png " class="info-section-img" />
</td>
<td>
<span class="info-section-title">
Find Your Friends
</span>
</td>
<tr>
</table>
<div class="info-section-text">
Lost in a new city, or want to meet up with a friend at a concert? Phonar makes it quick and easy for you to share your location and get to where you need to be.
</div>
</td>
</tr>
</table>
</div>
<!-- Find kids -->
<div class="span4">
<table>
<tr>
<td class="left-padded right-padded">
<table class="nocenter">
<tr>
<td>
<img src="img/children.png " class="info-section-img" />
</td>
<td>
<span class="info-section-title">
Find Your Kids
</span>
</td>
<tr>
</table>
<div class="info-section-text">
Going to an amusement park and don't want to put your child on a leash? Phonar allows you to have peace of mind and enjoy yourself without worrying about losing your child.
</div>
</td>
</tr>
</table>
</div>
<!-- Find car -->
<div class="span4">
<table>
<tr>
<td class="left-padded right-padded">
<table class="nocenter">
<tr>
<td>
<img src="img/car.png" class="info-section-img" />
</td>
<td>
<span class="info-section-title">
Find Your Car
</span>
</td>
<tr>
</table>
<div class="info-section-text">
Ever forgotten where you've parked your car, or had your car stolen? Put in a Phonar device and you won't have to worry about losing your car again.
</div>
</td>
</tr>
</table>
</div>
</div>
</div>

<!-- Footer -->
<?php require 'footer.php'; ?>
</body>
</html>
