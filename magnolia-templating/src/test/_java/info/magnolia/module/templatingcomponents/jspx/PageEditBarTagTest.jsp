<%@page import="info.magnolia.templating.template.configured.ConfiguredTemplateDefinition"%>
<%@page import="info.magnolia.templating.template.TemplateDefinition"%>
<%@ taglib prefix="ui" uri="http://magnolia-cms.com/taglib/templating-components" %>
<%
    TemplateDefinition dialogLessTpl = new ConfiguredTemplateDefinition();
    pageContext.setAttribute("dialogLessTpl", dialogLessTpl);

    ConfiguredTemplateDefinition def = new ConfiguredTemplateDefinition();
    def.setDialog("dialogFromDef");
    pageContext.setAttribute("def", def);
%>
--- ${dialogLessTpl} ---


<div id="basic">
    <ui:page dialog="myDialog"/>
</div>

<div id="customLabel">
    <ui:page editLabel="custom.foo.label" dialog="myDialog"/>
</div>

<div id="noDialog">
    <ui:page/>
</div>

<div id="dialogFromDef">
    <ui:page dialog="${def.dialog}"/>
</div>

<div id="dialogFromIncompleTpl">
    <ui:page dialog="${dialogLessTpl.dialog}"/>
</div>
