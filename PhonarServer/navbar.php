<div class="container">
    <div class="navbar">
        <div class="navbar-inner nopadding">
            <div class="container">
                <a class="brand hidden-phone" href="/">
                    <img class="phonar_logo_desktop" src="img/phonar_logo_large_website.png"/>
                </a>
                <a class="brand visible-phone" href="/">
                    <img class="phonar_logo_mobile" src="img/phonar_logo_large_website.png"/>
                </a>
                <!-- toggle for collapsed navbar content -->
                <a href="#" class="navbar-collapse-btn pull-right hidden-desktop" data-toggle="collapse" data-target=".nav-collapse" onclick="navbarclick();">
                    <img id="navbar_collapse_icon_plus" name="navbar_collapse_icon_plus" src="img/plus.png" class="navbar-collapse-btn-icon" />
                </a>
                <!-- Phone navbar -->
                <div class="nav-collapse hidden-desktop">
                    <ul class="nav pull-right nav-pills">
                    <?php if (get_userid() != null) { ?>
                        <li>
                            <a href="/webapp" class="nav-link">Home</a>
                        </li>
                        <li>
                            <a href="/addphone" class="nav-link">Sync</a>
                        </li>
                        <li>
                            <a href="/logout" class="nav-link">Logout</a>
                        </li>
                    <?php } else { ?>
                        <li>
                            <a href="/download" class="nav-link">Get the App</a>
                        </li>
                        <li>
                            <a href="/preorder" class="nav-link">Pre-order Devices</a>
                        </li>
                        <li>
                            <a href="/login" class="nav-link">Login</a>
                        </li>
                    <?php } ?>
                    </ul>
                </div>
                <!-- Desktop navbar -->
                <div class="visible-desktop">
                    <ul class="nav pull-right nav-pills">
                        <li>
                            <a href="/download" class="nav-link <?php if (isset($download_nav)) echo 'active'; ?>">Get the App</a>
                        </li>
                        <li>
                            <a href="/preorder" class="nav-link <?php if (isset($preorder_nav)) echo 'active'; ?>">Pre-order Devices</a>
                        </li>
                        <li class="divider-vertical"></li>
                        <?php if (get_userid() == null) { ?>
                        <li>
                            <a href="/login" class="nav-link <?php if (isset($login_nav)) echo 'active'; ?>">Login</a>
                        </li>
                        <?php } else { ?>
                        <li>
                            <a href="/webapp" class="nav-link <?php if (isset($webapp_nav)) echo 'active'; ?>">Home</a>
                        </li>
                        <li class="divider-vertical"></li>
                        <li class="dropdown">
                            <a class="dropdown-toggle" data-toggle="dropdown" href="#">
                                <?php
                                $user_name = get_username();
                                error_if(is_null($user_name));
                                echo $user_name;
                                ?><b class="chevron"></b>
                            </a>
                            <ul class="dropdown-menu">
                                <li>
                                    <a href="/addphone" class="nav-link <?php if (isset($addphone_nav)) echo 'active'; ?>">Sync</a>
                                </li>
                                <li>
                                    <a href="/logout" class="nav-link">Logout</a>
                                </li>
                            </ul>
                        </li>
                        <?php } ?>
                    </ul>
                </div>
            </div>
        </div>
    </div>
</div>