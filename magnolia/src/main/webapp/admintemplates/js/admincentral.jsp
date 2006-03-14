<%@ page contentType="text/javascript; charset=utf-8" %>
<%@ page import="org.apache.commons.io.IOUtils" %>
<%@ page import="java.io.InputStream" %>
<%@ page import="info.magnolia.cms.beans.runtime.MgnlContext" %>
<%@ page import="info.magnolia.cms.util.FactoryUtil" %>
<%@ page import="info.magnolia.cms.beans.runtime.WebContext" %>

<%
WebContext ctx = (WebContext) FactoryUtil.getInstance(WebContext.class);
ctx.init(request);
MgnlContext.setInstance(ctx);
%>

var contextPath = '<%= request.getContextPath() %>';


<%
    String[] includes = {
        "debug.js",
        "generic.js",
        "general.js",
        "controls.js",
        "tree.js",
        "i18n.js",
        "contextmenu.js",
        "inline.js"
    };

    for(int i=0; i<includes.length; i++){
        InputStream in = getClass().getResourceAsStream("/mgnl-resources/admin-js/" + includes[i]);
        IOUtils.copy(in, out);
    }
%>

<%@ include file="messages.jsp" %>

<%@ include file="libs.jsp" %>
<%@ include file="classes.jsp" %>


