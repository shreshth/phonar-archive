<?php
	// navbar
	$download_nav = TRUE;
?>

<!DOCTYPE html>
<html lang="en">
    <head>
        <title>Phonar: Find Everything</title>
        <?php require 'head.php' ?>
        <link rel="stylesheet" type="text/css" href="css/download.css" />
    </head>
    <body>
        <?php require 'navbar.php' ?>
        <div class="container body-container">
            <div class="span12">
                <table>
                    <tr>
                        <td class="download_table_divider download_table_item">
                            <a href="https://market.android.com/details?id=com.phonar" target="_blank"><img class="download_phone" src="img/phones/android.png"/></a>
                        </td>
                        <td class="download_table_item">
                            <img class="download_phone" src="img/phones/iphone.png"/>
                        </td>
                        <td class="download_table_item">
                            <img class="download_phone" src="img/phones/wp.png"/>
                        </td>
                        <td class="download_table_item">
                            <img class="download_phone" src="img/phones/blackberry.png"/>
                        </td>
                    </tr>
                    <tr>
                        <td class="download_table_item">
                            <a href="https://market.android.com/details?id=com.phonar" target="_blank"><img class="download_phone_logo" src="img/phones/android_logo.png"/></a>
                        </td>
                        <td colspan="3" class="download_coming_soon download_table_item">
                            <span class="download_coming_soon">Coming soon to other platforms...</span>
                        </td>
                    </tr>
                </table>
            </div>
        </div>
		<?php require 'footer.php'; ?>		
    </body>
</html>