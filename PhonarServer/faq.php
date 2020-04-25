<!DOCTYPE html>
<html lang="en">
    <head>
        <title>Phonar: Find Everything</title>
        <?php require 'head.php' ?>
        <link rel="stylesheet" type="text/css" href="css/faq.css" />
    </head>
    <body>
        <?php require 'navbar.php' ?>
        <div class="container body-container">
            <h1> Frequently Asked Questions </h1><br />
            <div class="accordion top-padded-large span8" id="accordion">
				<div class="accordion-group">
					<div class="accordion-heading">
						<a class="accordion-toggle" data-toggle="collapse" data-parent="#accordion" href="#q1">
						I'm sending my location using Phonar. Are you collecting this information?
						</a>
					</div>
					<div id="q1" class="accordion-body collapse">
						<div class="accordion-inner">
						At Phonar, we care about your privacy and are very careful with your personal data. Here is the information on the data we collect and why we collect it:
						<br /><br />
						When you register your phone with Phonar, we obtain your <u>phone number</u>. We need this to identify you. We will never share your phone number with anyone.
						<br /><br />
						When you use Phonar to share a location, we store the <u>location data</u> on our servers. We will never share this location data with anyone without your permission.
						</div>
					</div>
				</div>
				<div class="accordion-group">
					<div class="accordion-heading">
						<a class="accordion-toggle" data-toggle="collapse" data-parent="#accordion" href="#q2">
						I want to find my kids inside a mall. Does Phonar work well indoors?	
						</a>
					</div>
					<div id="q2" class="accordion-body collapse">
						<div class="accordion-inner">
						Phonar relies on GPS technology to keep track of your friends or your kids. GPS doesn't work well indoors. Therefore, you are very likely to get inaccurate results on using Phonar indoors.
						</div>
					</div>
				</div>
				<div class="accordion-group">
					<div class="accordion-heading">
						<a class="accordion-toggle" data-toggle="collapse" data-parent="#accordion" href="#q3">
						Is Phonar guaranteed to find my children or my car if I lose them?
						</a>
					</div>
					<div id="q3" class="accordion-body collapse">
						<div class="accordion-inner">
						No, Phonar is just a way to make it easier to keep track of things you care about. However, it is not guaranteed to work. 
						<br /><br />
						In some situations, it will probably not work well. For example, if your child is lost indoors, or if the GPS tracker in your car runs out of battery.
						</div>
					</div>
				</div>
			</div>
        </div>
		<?php require 'footer.php'; ?>		
    </body>
</html>

