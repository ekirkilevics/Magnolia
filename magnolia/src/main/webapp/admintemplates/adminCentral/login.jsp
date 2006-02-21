<%@ page import="info.magnolia.cms.core.Path"%>
<%
String contextPath = request.getContextPath();
if (contextPath.equalsIgnoreCase("/")) {
    contextPath = "";
}
String referer = contextPath + Path.getURI(request);

%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN"
	"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">

<html xmlns="http://www.w3.org/1999/xhtml" lang="en" xml:lang="en">
<head>
	<title>Magnolia Login Form</title>
	<link rel="STYLESHEET" type="text/css" href="${pageContext.request.contextPath}/admindocroot/loginForm/login.css" />
	<script src="${pageContext.request.contextPath}/admindocroot/loginForm/md5.js" type="text/javascript"></script>
	<script type="text/javascript">
		function submit() {
			// crypt
			//input = document.loginForm.password.value;
			//hash = hex_md5( input );
			//document.loginForm.passwordMD5.value = hash;
			// BLANK OUT PASSWORD
			///document.loginForm.password.value = "";
			// SUBMIT FORM
			document.loginForm.submit();
		}
		
		// methods copied from magnolia admin central (here not accessible)
		function mgnlIsKeyEnter(event){
			if (window.event && window.event.keyCode == 13) 
				return true;
			else if 
				(navigator.appName=="Netscape" && event.which==13) return true;
			else 
				return false;
		}
		
		document.onkeypress = function(e){
			if(mgnlIsKeyEnter(e)){
				submit();
			}
		}
		
		onload = function(){
			document.loginForm.username.focus();
		}

		
</script>
</head>

<body>

<div id="frame">
	<div id="logo"><img src="${pageContext.request.contextPath}/admindocroot/loginForm/logo.gif" width="271" height="51" title="magnolia Content Management Suite" alt="magnolia Content Management Suite" /></div>

	<div id="form">
		<div class="form">
		<form name="loginForm" action="<%= referer %>" method="post">
			<label for="username">Username</label>
			<input id="username" name="mgnlUserId" /><br />

			<label for="pasword">Password</label>
			<input id="pasword" name="mgnlUserPSWD" type="password" /><br />

			<label for="submitButton">&nbsp;</label>
			<!-- <input id="submit" type="button" value="login" class="button" onclick="Javascript:crypt();" /><br /> -->
			<input id="submitButton" type="button" value="login" class="button" onclick="submit();" /><br />
			<input  name="passwordMD5" type="hidden" />
		</form>
		</div>
	</div>

	<div id="copyright">&copy; Copyright 2000-2006 obinary ag, Basel, Switzerland • All rights reserved.</div>

</div>

</body>
</html>

