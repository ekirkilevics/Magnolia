<%
/**
 *
 * Magnolia and its source-code is licensed under the LGPL.
 * You may copy, adapt, and redistribute this file for commercial or non-commercial use.
 * When copying, adapting, or redistributing this document in keeping with the guidelines above,
 * you are required to provide proper attribution to obinary.
 * If you reproduce or distribute the document without making any substantive modifications to its content,
 * please use the following attribution line:
 *
 * Copyright 1993-2004 obinary Ltd. (http://www.obinary.com) All rights reserved.
 *
 * */
%><%@ page import="info.magnolia.cms.util.Resource"%>
<%@ taglib uri="cms-taglib" prefix="cms" %>
<%@ taglib uri="cms-util-taglib" prefix="cmsu" %>
<%@ taglib uri="http://java.sun.com/jstl/core" prefix="c" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
	<head>
		<meta http-equiv="Content-Type" content="text/html;charset=UTF-8">
		<title>Magnolia 2.0 Samples | <cms:out nodeDataName="title"/></title>

        <%-- ################################################## --%>
        <%-- links --%>
        <%-- ################################################## --%>
		<link rel="stylesheet" type="text/css" href="/docroot/samples/css/main.css">
		<link rel="stylesheet" type="text/css" href="/docroot/samples/css/richEdit.css">
		<script type="text/javascript" src="/docroot/samples/js/form.js"></script>

        <%-- ################################################## --%>
        <%-- meta --%>
        <%-- ################################################## --%>
		<meta name="description" content="<cms:out nodeDataName="metaDescription"/>">
		<meta name="keywords" content="<cms:out nodeDataName="metaKeywords"/>">

	</head>