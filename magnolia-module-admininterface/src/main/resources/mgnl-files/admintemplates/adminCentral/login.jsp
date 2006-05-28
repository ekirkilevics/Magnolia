<jsp:root version="1.2" xmlns:jsp="http://java.sun.com/JSP/Page" xmlns:c="urn:jsptld:http://java.sun.com/jsp/jstl/core"
  xmlns:cms="urn:jsptld:cms-taglib" xmlns:fmt="urn:jsptld:http://java.sun.com/jsp/jstl/fmt">
  <jsp:directive.page contentType="text/html; charset=UTF-8" />
  <jsp:text>
    <![CDATA[<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN"
    "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd"> ]]>
  </jsp:text>
  <jsp:scriptlet>
    String contextPath = request.getContextPath(); if (contextPath.equals("/")) { contextPath = ""; }
    pageContext.setAttribute("referer",contextPath + info.magnolia.cms.core.Path.getURI(request));
  </jsp:scriptlet>
  <html xmlns="http://www.w3.org/1999/xhtml" lang="en">
    <head>
      <title>Magnolia Login Form</title>
      <link rel="STYLESHEET" type="text/css" href="${pageContext.request.contextPath}/.resources/loginForm/login.css" />
      <link rel="shortcut icon" href="${pageContext.request.contextPath}/.resources/loginForm/favicon.ico"
        type="image/x-icon" />
    </head>
    <body>
      <div id="frame">
        <div id="logo">
          <img src="${pageContext.request.contextPath}/.resources/loginForm/logo.gif" width="271" height="51"
            title="magnolia Content Management Suite" alt="magnolia Content Management Suite" />
        </div>
        <div id="form">
          <div class="form">
            <form name="loginForm" action="${referer}" method="post">
              <label for="username">Username</label>
              <input id="username" name="mgnlUserId" />
              <label for="mgnlUserPSWD">Password</label>
              <input id="mgnlUserPSWD" name="mgnlUserPSWD" type="password" />
              <br />
              <label for="submitButton"><!--  --></label>
              <input id="submitButton" type="submit" value="login" class="button" />
              <br />
            </form>
          </div>
        </div>
        <div id="copyright">&amp;copy; Copyright 2000-2006 obinary ag, Basel, Switzerland - All rights reserved.</div>
      </div>
    </body>
  </html>
</jsp:root>
