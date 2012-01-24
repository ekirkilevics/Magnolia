<%@ page import="java.util.HashMap" %>
<%@ taglib prefix="cms" uri="http://magnolia-cms.com/taglib/templating-components/cms" %>

<%
    HashMap<String, Object> notEmptyMap = new HashMap<String, Object>();
    notEmptyMap.put("index","component_index+1");
    pageContext.setAttribute("notEmptyMap", notEmptyMap);
    pageContext.setAttribute("emptyMap", new HashMap<String, Object>());
    pageContext.setAttribute("nullMap", null);
%>

<div id="editable">
    <cms:render template="editableTemplate" editable="true"/>
</div>

<div id="notEditable">
    <cms:render template="notEditableTemplate" editable="false"/>
</div>

<div id="existingContextAttribute">
    <cms:render template="existingContextAttributeTemplate" contextAttributes="${notEmptyMap}"/>
</div>

<div id="emptyContextAttribute">
    <cms:render template="emptyContextAttributeTemplate" contextAttributes="${emptyMap}"/>
</div>
