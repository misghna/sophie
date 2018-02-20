<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
<%@ page isELIgnored="false" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<html>
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
		<title>Login page</title>
		<link href="<c:url value='/static/css/bootstrap.css' />"  rel="stylesheet"></link>
		<link href="<c:url value='/static/css/app.css' />" rel="stylesheet"></link>
		<link rel="stylesheet" type="text/css" href="//cdnjs.cloudflare.com/ajax/libs/font-awesome/4.2.0/css/font-awesome.css" />
		
		
		<script type="text/javascript">
		$( document ).ready(function() {
		    // DOM ready

		    loadProfile();
		});

		function getLocalProfile(callback){
		    var profileImgSrc      = localStorage.getItem("PROFILE_IMG_SRC");
		    var profileName        = localStorage.getItem("PROFILE_NAME");
		    var profileReAuthEmail = localStorage.getItem("PROFILE_REAUTH_EMAIL");

		    if(profileName !== null
		            && profileReAuthEmail !== null
		            && profileImgSrc !== null) {
		        callback(profileImgSrc, profileName, profileReAuthEmail);
		    }
		}

		function loadProfile() {
		    if(!supportsHTML5Storage()) { return false; }
		    // we have to provide to the callback the basic
		    // information to set the profile
		    getLocalProfile(function(profileImgSrc, profileName, profileReAuthEmail) {
		        //changes in the UI
		        $("#profile-img").attr("src",profileImgSrc);
		        $("#profile-name").html(profileName);
		        $("#reauth-email").html(profileReAuthEmail);
		        $("#username").hide();
		        $("#remember").hide();
		    });
		}

		function supportsHTML5Storage() {
		    try {
		        return 'localStorage' in window && window['localStorage'] !== null;
		    } catch (e) {
		        return false;
		    }
		}

		function testLocalStorageData() {
		    if(!supportsHTML5Storage()) { return false; }
		    localStorage.setItem("PROFILE_IMG_SRC", "//lh3.googleusercontent.com/-6V8xOA6M7BA/AAAAAAAAAAI/AAAAAAAAAAA/rzlHcD0KYwo/photo.jpg?sz=120" );
		    localStorage.setItem("PROFILE_NAME", "César Izquierdo Tello");
		    localStorage.setItem("PROFILE_REAUTH_EMAIL", "oneaccount@gmail.com");
		}
		
		</script>
	</head>
	
<%-- 	

	<body>
		<div id="mainWrapper">
			<div class="login-container">
				<div class="login-card">
					<div class="login-form">
						<c:url var="loginUrl" value="/login" />
						<form action="${loginUrl}" method="post" class="form-horizontal">
							<c:if test="${param.error != null}">
								<div class="alert alert-danger">
									<p>Invalid username and password.</p>
								</div>
							</c:if>
							<c:if test="${param.logout != null}">
								<div class="alert alert-success">
									<p>You have been logged out successfully.</p>
								</div>
							</c:if>
							<div class="input-group input-sm">
								<label class="input-group-addon" for="username"><i class="fa fa-user"></i></label>
								<input type="text" class="form-control" id="username" name="ssoId" placeholder="Enter Username" required>
							</div>
							<div class="input-group input-sm">
								<label class="input-group-addon" for="password"><i class="fa fa-lock"></i></label> 
								<input type="password" class="form-control" id="password" name="password" placeholder="Enter Password" required>
							</div>
							<div class="input-group input-sm">
                              <div class="checkbox">
                                <label><input type="checkbox" id="rememberme" name="remember-me"> Remember Me</label>  
                              </div>
                            </div>
							<input type="hidden" name="${_csrf.parameterName}"  value="${_csrf.token}" />
								
							<div class="form-actions">
								<input type="submit"
									class="btn btn-block btn-primary btn-default" value="Log in">
							</div>
						</form>
					</div>
				</div>
			</div>
		</div> --%>

    <div class="container">
        <div class="card card-container">
            <!-- <img class="profile-img-card" src="//lh3.googleusercontent.com/-6V8xOA6M7BA/AAAAAAAAAAI/AAAAAAAAAAA/rzlHcD0KYwo/photo.jpg?sz=120" alt="" /> -->
            <img id="profile-img" class="profile-img-card" src="//ssl.gstatic.com/accounts/ui/avatar_2x.png" />
            <p id="profile-name" class="profile-name-card"></p>
            <c:url var="loginUrl" value="/login" />
            <form action="${loginUrl}" method="post" class="form-signin">
        		<c:if test="${param.error != null}">
					<div class="alert alert-danger">
						<p>Invalid username and password.</p>
					</div>
				</c:if>
				<c:if test="${param.logout != null}">
					<div class="alert alert-success">
						<p>You have been logged out successfully.</p>
					</div>
				</c:if>
                <span id="reauth-email" class="reauth-email"></span>
                <input type="text" id="username" name="ssoId" class="form-control" placeholder="Username" required autofocus>
                <input type="password" id="password" name="password" class="form-control" placeholder="Password" required>
                <div id="remember" class="checkbox">
                    <label>
                        <input type="checkbox" id="rememberme" name="remember-me"> Remember me
                    </label>
                </div>
                <input type="hidden" name="${_csrf.parameterName}"  value="${_csrf.token}" />
                <button class="btn btn-lg btn-primary btn-block btn-signin" type="submit">Sign in</button>
            </form><!-- /form -->
            <a href="#" class="forgot-password">
                Forgot the password?
            </a>
        </div><!-- /card-container -->
    </div><!-- /container -->
	</body>
	
	<script type="text/javascript">
		$(function() {
	
		    $('#login-form-link').click(function(e) {
				$("#login-form").delay(100).fadeIn(100);
		 		$("#register-form").fadeOut(100);
				$('#register-form-link').removeClass('active');
				$(this).addClass('active');
				e.preventDefault();
			});
			$('#register-form-link').click(function(e) {
				$("#register-form").delay(100).fadeIn(100);
		 		$("#login-form").fadeOut(100);
				$('#login-form-link').removeClass('active');
				$(this).addClass('active');
				e.preventDefault();
			});
	
		});
	</script>
</html>