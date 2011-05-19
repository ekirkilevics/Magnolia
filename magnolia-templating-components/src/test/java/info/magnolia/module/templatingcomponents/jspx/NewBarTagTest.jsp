<%@ page import="java.util.Arrays" %>
<%@ page import="java.util.Collections" %>
<%@ page import="java.util.List" %>
<%@ taglib prefix="ui" uri="http://magnolia-cms.com/taglib/templating-components" %>

<%
    List<String> myList = Arrays.asList("abc", "def");
    pageContext.setAttribute("myList", myList);
    pageContext.setAttribute("emptyList", Collections.emptyList());
%>
--- ${myList} ---

<div id="asList">
    <ui:new container="paragraphs" paragraphs="${myList}"/>
</div>

<%--<div id="commaSeparatedString">--%>
<%--<ui:new container="paragraphs" paragraphs="abc,def"/>--%>
<%--</div>--%>

<%--<div id="single">--%>
<%--<ui:new container="paragraphs" paragraphs="abc"/>--%>
<%--</div>--%>

<div id="customLabel">
    <ui:new container="paragraphs" paragraphs="${myList}" newLabel="custom.new.label"/>
</div>

<div id="emptyParagraphList">
    <ui:new container="paragraphs" paragraphs="${emptyList}"/>
</div>
