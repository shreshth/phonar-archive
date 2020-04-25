<?php require_once 'utils.php'; ?>
<meta charset="utf-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<meta name="description" content="">
<meta name="author" content="">
<link rel="icon" type="image/ico" href="favicon.ico"></link>
<link rel="shortcut icon" href="favicon.ico"></link>
<!-- Styles -->
<link rel="stylesheet" type="text/css" href="css/bootstrap-responsive.css">
<link rel="stylesheet" type="text/css" href="css/phonar-bootstrap.css" />
<link rel="stylesheet" type="text/css" href="css/mobilepage.css" />
<link rel="stylesheet" type="text/css" href="css/styles.css" />
<link href="http://fonts.googleapis.com/css?family=Open+Sans" rel="stylesheet" type="text/css">
<!-- Scripts -->
<script src="js/jquery.js"></script>
<script src="js/bootstrap.min.js"></script>
<script src="js/bootstrap-transition.js"></script>
<script src="js/navbarcollapse.js"></script>
<script src="js/jquery-css-transform.js" type="text/javascript"></script>
<script src="js/jquery-animate-css-rotate-scale.js" type="text/javascript"></script>
<!-- Le HTML5 shim, for IE6-8 support of HTML5 elements -->
<!--[if lt IE 9]>
    <script src="http://html5shim.googlecode.com/svn/trunk/html5.js"></script>
<![endif]-->
<?php if (isProd()) { ?>
<!-- Google Analytics -->
<script type="text/javascript">
    var _gaq = _gaq || [];
    _gaq.push(['_setAccount', 'UA-33631540-1']);
    _gaq.push(['_setDomainName', 'phonar.me']);
    _gaq.push(['_trackPageview']);
    (function() {
        var ga = document.createElement('script'); ga.type = 'text/javascript'; ga.async = true;
        ga.src = ('https:' == document.location.protocol ? 'https://ssl' : 'http://www') + '.google-analytics.com/ga.js';
        var s = document.getElementsByTagName('script')[0]; s.parentNode.insertBefore(ga, s);
    })();
</script>
<?php } ?>
