<%@ page import="java.util.Arrays" %>
<%@ page import="java.util.List" %>
<%@ taglib prefix="ui" uri="http://magnolia-cms.com/taglib/templating-components" %>

<%
    List<String> myList = Arrays.asList("abc", "def");
    pageContext.setAttribute("myList", myList);
%>
--- ${myList} ---

<ui:new container="paragraphs" paragraphs="abc,def"/>
<ui:new container="paragraphs" paragraphs="${myList}"/>
