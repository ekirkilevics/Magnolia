<%@ page import="java.util.HashMap" %>
<%@ taglib prefix="cms" uri="http://magnolia-cms.com/taglib/templating-components/cms" %>

<%
    HashMap<String, Object> notEmptyMap = new HashMap<String, Object>();
    notEmptyMap.put("indexString","1");
    notEmptyMap.put("useIndex",true);
    pageContext.setAttribute("notEmptyMap", notEmptyMap);
    pageContext.setAttribute("emptyMap", new HashMap<String, Object>());
    pageContext.setAttribute("nullMap", null);
%>

<div id="onlyName">
    <cms:area name="stage"/>
</div>

