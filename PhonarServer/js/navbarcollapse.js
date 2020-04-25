// Changing the button for navbar collapse and retract
function navbarclick() {	
	var anim_duration = 200;		
	var btn_plus = document.getElementById('navbar_collapse_icon_plus');
	$('#navbar_collapse_icon_plus').animate({rotate: '+=45deg'}, {queue: false, duration: anim_duration});	
}