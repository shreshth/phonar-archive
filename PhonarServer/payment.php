<?php 
	require_once 'utils.php';
	require_once 'stripe/stripe-php-1.7.2/lib/Stripe.php';
	
	$stripeToken = get_param('stripeToken');
	if (!is_null($stripeToken)) {
		// secret key: remember to change this to live secret key in production
		Stripe::setApiKey("sk_09JFvHaRg9fF5EFiiu8UrKpHLUGze");

		// create the charge on Stripe's servers - this will charge the user's card
		$charge = Stripe_Charge::create(array(
		  "amount" => 100, // amount in cents
		  "currency" => "usd",
		  "card" => $stripeToken,
		  "description" => "payinguser@example.com")
		);
	} else {
?>
<!DOCTYPE html>
<html lang="en">
    <head>
        <title>Phonar: Find Everything</title>
        <?php require 'head.php' ?>		
		<link rel="stylesheet" type="text/css" href="css/payment.css" />
		
		<!-- Payment stuff -->		
		<script type="text/javascript" src="https://js.stripe.com/v1/"></script>
		<script type="text/javascript">
			// this identifies your website in the createToken call below
			Stripe.setPublishableKey('pk_09JFXkQClaquaqnV4h8cDWhU5Hr6u');

			// on receiving response from stripe
			function stripeResponseHandler(status, response) {
				if (response.error) {
					// show the errors on the form
					var error_msg = document.getElementById('payment-errors');
					error_msg.innerHTML = response.error.message;
					error_msg.className = "alert alert-danger";
					// re-enable submit button
					$("#payment_submit").removeAttr("disabled");
				} else {
					var form$ = $("#payment-form");
					// token contains id, last4, and card type
					var token = response['id'];
					// insert the token into the form so it gets submitted to the server
					form$.append("<input type='hidden' name='stripeToken' value='" + token + "'/>");
					// and submit
					form$.get(0).submit();
				}
			}
			
			// onsubmit of payment form
			$(document).ready(function() {
			  $("#payment-form").submit(function(event) {			  
				// disable the submit button to prevent repeated clicks
				$('#payment_submit').attr("disabled", "disabled");
				// clear error
				document.getElementById('payment-errors').className = "alert alert-danger hidden";

				Stripe.createToken({
					number: $('.card-number').val(),
					cvc: $('.card-cvc').val(),
					exp_month: $('.card-expiry-month').val(),
					exp_year: $('.card-expiry-year').val()
				}, stripeResponseHandler);

				// prevent the form from submitting with the default action
				return false;
			  });
			});					
		</script>
		
    </head>
    <body>
		<?php require 'navbar.php' ?>
		
        <hr class="top-line visible-phone spaced" />

        <div class="container body-container">
			<div class="span8">
				<form action="/payment" method="POST" id="payment-form" class="form-horizontal">
					<fieldset>
					<legend>Enter payment details</legend>
					
					<div class="top-padded">
						<div class="alert alert-danger hidden" id="payment-errors"></div>
					</div>
					
					<div class="control-group">
						<label class="control-label">Card Number</label>
						<div class="controls">
							<input type="text" size="20" autocomplete="off" class="card-number" required="True" />
						</div>
					</div>				
					<div class="control-group">
						<label class="control-label">CVC</label>
						<div class="controls">
							<input type="text" size="4" autocomplete="off" class="card-cvc" required="True" />
						</div>
					</div>
					<div class="control-group">
						<label class="control-label">Expiration (MM/YYYY)</label>
						<div class="controls">
							<input type="text" size="2" class="card-expiry-month" required="True" />
							<span> / </span>
							<input type="text" size="4" class="card-expiry-year" required="True" />
						</div>
					</div>					
					<hr class="smallspace">
					<button id="payment_submit" type="submit" class="btn btn-primary pull-right">Submit Payment</button>
				</form>
			</div>
        </div>
		
		<!-- Footer -->
        <?php require 'footer.php'; ?>
		
    </body>
</html>

<?php
}
?>