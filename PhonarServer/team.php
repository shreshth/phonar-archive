<!DOCTYPE html>
<html lang="en">
    <head>
        <title>Phonar: Find Everything</title>
        <?php require 'head.php' ?>
		
		<!-- randomize team image -->
		<?php 
			$img_number = rand(2, 3);
		?>		

		<!-- Team descriptions on mobile -->
		<script>		
			function showdescription(person) {
				var faaez = document.getElementById('faaez_info');
				var hamza = document.getElementById('hamza_info');
				var nitin = document.getElementById('nitin_info');
				var shreshth = document.getElementById('shreshth_info');
				
				if (person == 'faaez') {
					if (faaez.className == "hidden") {
						faaez.className = "";
					} else {
						faaez.className = "hidden";
					}
				} else if (person == 'hamza') {
					if (hamza.className == "hidden") {
						hamza.className = "";
					} else {
						hamza.className = "hidden";
					}
				} else if (person == 'nitin') {
					if (nitin.className == "hidden") {
						nitin.className = "";
					} else {
						nitin.className = "hidden";
					}
				} else if (person == 'shreshth') {
					if (shreshth.className == "hidden") {
						shreshth.className = "";
					} else {
						shreshth.className = "hidden";
					}
				}
			}
		</script>
		
    </head>
    <body>
        <?php require 'navbar.php' ?>
        <div class="container body-container">
			<!-- Team page on desktop -->
			<div class="span11 hidden-phone">				
				<table class="searchtable">
					<tr>
						<!-- image + buttons -->
						<td class="searchtable img-large">
							<ul class="thumbnails top-padded-large">
								<li class="team-thumb-large">
								<a class="thumbnail">
									<img src="<?php echo "/img/team/faaez" . $img_number . ".JPG"; ?>"></img>
								</a>
								</li>
							</ul>
						</td>
						<!-- info -->
						<td class = "searchtable info" >
							<p class="search top-padded-large">Faaez Ul-Haq</p>
							<p class="search sub">
                                faaez@phonar.me
                                <br /><br />
                                Faaez recently graduated from Princeton, where he studied Computer Science and Public Policy. He has interned at Google, One Laptop Per Child and Goldman Sachs, and also served as the Deputy Executive Director of em[POWER] and co-founder of BeneTag.
                            </p>
						</td>
                        <!-- image + buttons -->
						<td class="searchtable img-large">
							<ul class="thumbnails top-padded-large">
								<li class="team-thumb-large">
								<a class="thumbnail">	
									<img src="<?php echo "/img/team/hamza" . $img_number . ".JPG"; ?>"></img>
								</a>
								</li>
							</ul>
						</td>
						<!-- info -->
						<td class = "searchtable info" >
							<p class="search top-padded-large">Hamza Aftab</p>
							<p class="search sub">
                                hamza@phonar.me
                                <br /><br />
                                Hamza recently graduated from Princeton studying Electrical Engineering. He has interned at Facebook as a mobile developer and served as a co-founder of BeneTag.
                            </p>
						</td>
					</tr>
					<tr>
						<!-- image + buttons -->
						<td class="searchtable img-large">
							<ul class="thumbnails top-padded-large">
								<li class="team-thumb-large">
								<a class="thumbnail">
									<img src="<?php echo "/img/team/nitin" . $img_number . ".JPG"; ?>"></img>
								</a>
								</li>
							</ul>
						</td>
						<!-- info -->
						<td class = "searchtable info" >
							<p class="search top-padded-large">Nitin Viswanathan</p>
							<p class="search sub">
                                nitin@phonar.me
                                <br /><br />
                                Nitin recently graduated from Princeton University, where he studied Computer Science. His hobbies include video games, science fiction, and Ultimate Frisbee.
                            </p>
						</td>
                        <!-- image + buttons -->
						<td class="searchtable img-large">
							<ul class="thumbnails top-padded-large">
								<li class="team-thumb-large">
								<a class="thumbnail">
									<img src="<?php echo "/img/team/shreshth" . $img_number . ".JPG"; ?>"></img>
								</a>
								</li>
							</ul>
						</td>
						<!-- info -->
						<td class = "searchtable info" >
							<p class="search top-padded-large">Shreshth Singhal</p>
							<p class="search sub">
                                shreshth@phonar.me
                                <br /><br />
                                Shreshth is a rising Princeton senior studying Computer Science. He has prior experience working at SAHApedia, and has served as a co-founder of Mitra Foundation and BeneTag.
                            </p>
						</td>
					</tr>
				</table>
			</div>
			<!-- Team page on mobile -->
			<div class="visible-phone">
				<ul class="thumbnails visible-phone top-padded">
					<li class="team-thumb-large right-padded">
						<a class="thumbnail" onclick="showdescription('faaez');">
							<img src="<?php echo "/img/team/faaez" . $img_number . ".JPG"; ?>"></img>
						</a>										
					</li>
					<div class="hidden" id="faaez_info">
						<p class="search">Faaez Ul-Haq</p>
						<p class="search sub">
							faaez@phonar.me
							<br /><br />
							Faaez recently graduated from Princeton studying Computer Science. He has worked at Google and Goldman Sachs, and also served as the Deputy Executive Director of em[POWER] and co-founder of BeneTag.
						</p>					
					</div>
				</ul>
				<ul class="thumbnails visible-phone center top-padded">
					<li class="team-thumb-large right-padded">
						<a class="thumbnail" onclick="showdescription('hamza');">
							<img src="<?php echo "/img/team/hamza" . $img_number . ".JPG"; ?>"></img>
						</a>
					</li>
					<div class="hidden" id="hamza_info">
						<p class="search">Hamza Aftab</p>
						<p class="search sub">
							hamza@phonar.me
							<br /><br />
							Hamza recently graduated from Princeton studying Electrical Engineering. He has interned at Facebook as a mobile developer and served as a co-founder of BeneTag.
						</p>
					</div>
				</ul>
				<ul class="thumbnails visible-phone center top-padded">
					<li class="team-thumb-large right-padded">
						<a class="thumbnail" onclick="showdescription('nitin');">
							<img src="<?php echo "/img/team/nitin" . $img_number . ".JPG"; ?>"></img>
						</a>
					</li>
					<div class="hidden" id="nitin_info">
						<p class="search">Nitin Viswanathan</p>
						<p class="search sub">
								nitin@phonar.me
							<br /><br />
							Nitin recently graduated from Princeton University, where he studied Computer Science. His hobbies include video games, science fiction, and Ultimate Frisbee.
						</p>
					</div>
				</ul>
				<ul class="thumbnails visible-phone center top-padded">
					<li class="team-thumb-large right-padded">
						<a class="thumbnail" onclick="showdescription('shreshth');">
							<img src="<?php echo "/img/team/shreshth" . $img_number . ".JPG"; ?>"></img>
						</a>
					</li>
					<div class="hidden" id="shreshth_info">
						<p class="search">Shreshth Singhal</p>
						<p class="search sub">
							shreshth@phonar.me
							<br /><br />
							Shreshth is a rising Princeton senior studying Computer Science. He has prior experience working at SAHApedia, and has served as a co-founder of Mitra Foundation and BeneTag.
						</p>
					</div>
				</ul>
			</div>
        </div>        
		<?php require 'footer.php'; ?>
    </body>
</html>

