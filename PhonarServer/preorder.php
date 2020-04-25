<?php 
	// navbar
	$preorder_nav = TRUE;

	require_once 'utils.php';
    require_once 'consts.php';
	require_once 'stripe/stripe-php-1.7.2/lib/Stripe.php';	   
    
    function check_quantity($quantity) {
        if (is_numeric($quantity)) {
            $temp = intval($quantity);
            $temp2 = floatval($quantity);
            if ($temp == $temp2) {
                if ($temp > 0) {
                    return true;
                }
            }
        }
        return false;
    }
    
	$stripeToken = get_param('stripeToken');
	if (!is_null($stripeToken)) {
		// secret key: remember to change this to live secret key in production
		Stripe::setApiKey($STRIPE_SK);

        $item = get_param('item');       
        $quantity = get_param('quantity');
        $price = get_param('price');
        $address = get_param('address');
        $email = get_param('email');      
        $special = get_param('special');
        
        // basic checks
        error_if(is_null($item) or is_null($quantity) or is_null($price) or is_null($address) or is_null($email)); // null checking
        error_if(!in_array($item, $PRODUCTS)); // if item is not one of our products
        error_if(!check_quantity($quantity)); // quantity needs to be int > 0
        error_if(!is_numeric($price));        // price needs to be numeric                                
        error_if(round(floatval($price), 2) != round($PRICES[$item] * intval($quantity), 2)); // price sent needs to match server price
        
		// create the charge on Stripe's servers - this will charge the user's card
		$charge = Stripe_Charge::create(array(
            "amount" => round(floatval($price) * 100, 0), // amount in cents
            "currency" => "usd",
            "card" => $stripeToken,
            "description" => $email)
		);
        
        // add order to db
        $connection = new Mongo();
        $db = $connection->Phonar;
        $orders = $db->orders;
        $insert = array('item' => $item,
                        'quantity' => $quantity,
                        'price' => $price,                       
                        'address' => $address,
                        'email' => $email,
                        'special' => $special,
                        'date' => date('m-d-Y g:i a'));                
        $orders -> insert($insert);
        
        // redirect to preorder page
        header("Location: /preorder?done=true");   
	} else {
?>

<!DOCTYPE html>
<html lang="en">
    <head>
        <title>Phonar: Find Everything</title>
        <?php require 'head.php' ?>
        <link rel="stylesheet" type="text/css" href="css/preorder.css" />
		
		<!-- Payment stuff -->		
		<script type="text/javascript" src="https://js.stripe.com/v1/"></script>
        <script>
            // this identifies your website in the createToken call below
            Stripe.setPublishableKey("<?php echo $STRIPE_PK; ?>");
        </script>
		<script type="text/javascript" src="js/preorder.js"></script>		
    </head>
    <body onload="initializepayment();">
        <?php require 'navbar.php' ?>
		
        <?php 
            if (!is_null(get_param('done'))) {
                echo "<div class=\"alert alert-success bottom-padded\">Your transaction was completed!</div>";
            }
        ?>
        
        <div class="container body-container">
			<div class="modal hide fade" id="buy_modal">
				<div class="modal-header">
					<div class="">
						<button class="close" data-dismiss="modal"><i class="icon-remove"></i></button>
						<h3>Enter payment details</h3>
					</div>
					<div class="alert alert-danger hidden" id="payment-errors"></div>							
				</div>							
                <div class="modal-body">					
                    <form action="/preorder" method="POST" id="payment-form" name="payment-form" class="form-horizontal">
						<fieldset>													
							<!-- Item -->
							<legend id="item_name"></legend>		
							<div class="hidden">
								<input type="text" class="hidden" id="item_input" name="item" />
							</div>
							<!-- Quantity -->
							<div class="control-group">
								<label class="control-label">Quantity</label>
								<div class="controls">
									<input type="text" size="20" class="text-input" required="True" value="1" name="quantity" id="quantity" />
								</div>
							</div>				
							<!-- Amount -->
							<div class="control-group">
								<label class="control-label">Price</label>
								<div class="controls" id="price_input_div">
									<div class="hidden">
										<input type="text" class="hidden" id="price_input" name="price" />
									</div>
									<p id="price_display"></p>
								</div>
							</div>
							
							<legend>Payment details</legend>
							<!-- Card number -->
							<div class="control-group">
								<label class="control-label">Card Number</label>
								<div class="controls">
									<input type="text" size="20" autocomplete="off" class="card-number" required="True" />
								</div>
							</div>				
							<!-- CVC -->
							<div class="control-group">
								<label class="control-label">CVC</label>
								<div class="controls">
									<input type="text" size="4" autocomplete="off" class="card-cvc" required="True" />
								</div>
							</div>
							<!-- Expiration -->
							<div class="control-group">
								<label class="control-label">Expiration (MM/YYYY)</label>
								<div class="controls">
									<select class="card-expiry-month">
										<option value="1" selected>January (1)</option>
										<option value="2">February (2)</option>
										<option value="3">March (3)</option>
										<option value="4">April (4)</option>
										<option value="5">May (5)</option>
										<option value="6">June (6)</option>
										<option value="7">July (7)</option>
										<option value="8">August (8)</option>
										<option value="9">September (9)</option>
										<option value="10">October (10)</option>
										<option value="11">November (11)</option>
										<option value="11">December (12)</option>
									</select>
									<span> / </span>
									<select class="card-expiry-year">
                                        <?php $cur_year = intval(date('Y')); ?>
										<option value="<?php echo $cur_year; ?>"><?php echo $cur_year; ?></option>
										<?php                                             
                                            for ($i = 1; $i <= 20; $i++) {
                                                echo "<option value=\"" . ($cur_year + $i) . "\">" . ($cur_year + $i) . "</option>";
                                            }
                                        ?>
									</select>									
								</div>
							</div>				
                            
							<legend>Shipping details</legend>
							<!-- Address -->
							<div class="control-group">
								<label class="control-label">Address</label>
								<div class="controls">
									<textarea class="text-input" required="True" name="address"></textarea>
								</div>
							</div>	
							<!-- Email -->
							<div class="control-group">
								<label class="control-label">Email</label>
								<div class="controls">
									<input type="text" class="text-input" required="True" name="email" />
								</div>
							</div>								
							<!-- Extras -->
							<div class="control-group">
								<label class="control-label">Special instructions</label>
								<div class="controls">
									<textarea class="text-input" name="special"></textarea>
								</div>
							</div>	
						</fieldset>		
                    </form>
                </div>
                <div class="modal-footer">
                    <div class="pull-right">
                        <button class="btn btn-modal-footer" data-dismiss="modal">Close</button>
                        <button id="payment_submit" type="submit" class="btn btn-primary btn-modal-footer" form="payment-form">Submit Payment</button>
                    </div>
                </div>				
			</div>
			<!-- hidden modal trigger -->
			<a class="hidden" data-toggle="modal" href="#buy_modal" id="modal_trigger"></a> 
			
            <h2 id="intro">
                We are currently working on developing devices that you can use to keep track of your loved ones and assets. All of these devices will be compatible with the Phonar app. Pre-order now to support our efforts.
            </h2>
			           
			<a href="#" onclick='buy("<?php echo $PRICES[$KIDS_SHOES]; ?>", "<?php echo $KIDS_SHOES; ?>"); return false;'>$<?php echo $PRICES[$KIDS_SHOES]; ?> - <?php echo $KIDS_SHOES; ?></a><br />
            <a href="#" onclick='buy("<?php echo $PRICES[$ADULT_SHOES]; ?>", "<?php echo $ADULT_SHOES; ?>"); return false;'>$<?php echo $PRICES[$ADULT_SHOES]; ?> - <?php echo $ADULT_SHOES; ?></a><br />
            <a href="#" onclick='buy("<?php echo $PRICES[$DOG_COLLAR]; ?>", "<?php echo $DOG_COLLAR; ?>"); return false;'>$<?php echo $PRICES[$DOG_COLLAR]; ?> - <?php echo $DOG_COLLAR; ?></a><br />
			<a href="#" onclick='buy("<?php echo $PRICES[$CAR_TRACKER]; ?>", "<?php echo $CAR_TRACKER; ?>"); return false;'>$<?php echo $PRICES[$CAR_TRACKER]; ?> - <?php echo $CAR_TRACKER; ?></a><br />
            <a href="#" onclick='buy("<?php echo $PRICES[$PORTABLE_TRACKER]; ?>", "<?php echo $PORTABLE_TRACKER; ?>"); return false;'>$<?php echo $PRICES[$PORTABLE_TRACKER]; ?> - <?php echo $PORTABLE_TRACKER; ?></a><br />
        </div>

		<?php require 'footer.php'; ?>		
    </body>
</html>

<?php } ?>