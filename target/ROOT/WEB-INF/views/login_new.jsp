<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
<%@ page isELIgnored="false" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<!DOCTYPE html>
<html>

<head>
	<meta charset="utf-8">
	<meta name="viewport" content="width=device-width, initial-scale=1">
	<title>Fire Ball</title>
	<link rel="stylesheet" href="../static/css/themes/default/jquery.mobile-1.4.5.min.css">
	<link rel="stylesheet" href="../static/_assets/css/jqm-demos.css">
	<link rel="shortcut icon" href="fireball.ico">
	<script src="../static/js/jquery.js"></script>
	<script src="../static/_assets/js/index.js"></script>
	<script src="../static/js/jquery.mobile-1.4.5.min.js"></script>
	<script src="../static/js/Chart.min.js"></script>
	
	 <style id="textinput-controlgroup">
			.controlgroup-textinput{
				padding-top:.22em;
				padding-bottom:.22em;
			}
			table, th, td {
			    text-align: center;
			    font: normal 12px courier !important;
			    
			}
			tr:nth-child(even) {background-color: #f2f2f2}
			.blue{
				color: blue;
				font: normal 14px courier !important;
			}
			.timeTable{
				font: normal 10px courier !important;
			}
			[data-role=page]{height: 100% !important; position:relative !important;}
			[data-role=footer]{bottom:0; position:absolute !important; top: auto !important; width:100%;}  
			
	    </style>

	<script >
	$( document ).ready(function() {
		var host = document.location.host;
    	var pathname = document.location.pathname;
    	console.log(pathname);
  
	});
	</script>
</head>

<body>

<!-- Start of login page: #one -->

<div data-role="page" id="one" data-theme="a">

	<div data-role="header">
		<h1>FireBall Dashboard</h1>
	</div><!-- /header -->

	<div role="main" class="ui-content">
		<br><br><br><br>
<!-- 		 <input type="text" class="center-input" placeholder="UserName" id="usernameTxt">
		 <input type="password" class="center-input" placeholder="Password" id="passTxt">
		 <button class="center-input" id="connect" >Submit</button>
		 <label class="blue"  id="stat"></label> -->
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


</div><!-- /page two -->



</body>
</html>
