/**
  * initialize 
  */
var form_timer = 0;
function initializepayment() {
	// change handlers for quantity
	$("#quantity").bind('keydown keyup keypress', quantity_change);
	$("#quantity").focus(function() { 
		form_timer = setInterval(quantity_change, 100);
	});
	$("#quantity").blur(function() { 
		if (form_timer != 0) {
			clearInterval(form_timer);
			form_timer = 0;
		}					
	});
	
	// submit handler of form
	$("#payment-form").submit(function(event) {			  
		return submit_payment();
	});
}		
// handler for quantity changing - used only in initialize()
var price_save;						
function quantity_change() {								
	var quantity = Number($("#quantity").val());
	if (!(typeof quantity === 'number' && quantity % 1 == 0 && quantity > 0)) {
		$("#price_input").val("$0");
		$("#price_display").html("$0");
	} else {
		var new_quantity = $("#quantity").val();
		$("#price_input").val((new_quantity * price_save).toFixed(2));
		$("#price_display").html("$" + (new_quantity * price_save).toFixed(2));
	}
}
		
/**
  * on clicking any of the buy links on the main page
  */
function buy(price, name) {
	price_save = Number(price);
	
	// Trigger modal
	$("#modal_trigger").click();	

	// Visible values
	$("#item_name").html(name);
	
	// Form (hidden) values
	$("#item_input").val(name);	

    // If quantity was not entered, set it to 1
    if ($("#quantity").val() == "") {
        $("#quantity").val("1");
    }
    
	quantity_change();
}
		
					
/**
  * on submitting the payment form
  */
function submit_payment() {
	// disable the submit button to prevent repeated clicks
	$('#payment_submit').attr("disabled", "disabled");
	// clear error
	document.getElementById('payment-errors').className = "alert alert-danger hidden";

	// validate form
	if (validate_form() == true) {	
		// create Stripe token
		Stripe.createToken({
			number: $('.card-number').val(),
			cvc: $('.card-cvc').val(),
			exp_month: $('.card-expiry-month').val(),
			exp_year: $('.card-expiry-year').val()
		}, stripeResponseHandler);
	}

	// prevent the form from submitting with the default action
	return false;
}			
// on receiving response from stripe
function stripeResponseHandler(status, response) {
	if (response.error) {
		// show the errors on the form
		show_error(response.error.message);					
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
// validate form fields
function validate_form() {
	// check quantity 
	var quantity = Number($("#quantity").val());
	if (!(typeof quantity === 'number' && quantity % 1 == 0)) {
		show_error("Quantity must be a number");
		return false;
	} else if (quantity <= 0) {
		show_error("Quantity must be at least 1");
		return false;
	}
	
	return true;
}
// show error message
function show_error(message) {			
	scroll(0,0);
	var error_msg = document.getElementById('payment-errors');
	error_msg.innerHTML = message;
	error_msg.className = "alert alert-danger top-margin";
	// re-enable submit button
	$("#payment_submit").removeAttr("disabled");
}