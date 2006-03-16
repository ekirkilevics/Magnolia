<%@ page contentType="text/javascript; charset=utf-8" %>
<%@ page import="org.apache.commons.io.IOUtils" %>
<%@ page import="java.io.InputStream" %>
<%@ page import="info.magnolia.cms.beans.runtime.MgnlContext" %>
<%@ page import="info.magnolia.cms.util.FactoryUtil" %>
<%@ page import="info.magnolia.cms.beans.runtime.WebContext" %>

<%!
   private static boolean nocache = BooleanUtils.toBoolean(SystemProperty.getProperty("magnolia.debug"));
%>

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

<jsp:include flush="true" page="${request.contextPath}/.resources/admin-js/*.js" />

<%@ include file="messages.jsp" %>

<jsp:include flush="true" page="${request.contextPath}/.resources/js-libs/*.js" />

<%@ include file="classes.jsp" %>


