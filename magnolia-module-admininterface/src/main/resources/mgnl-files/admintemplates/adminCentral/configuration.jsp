<%@ page import="info.magnolia.cms.beans.config.Server"%>
<%@ page import="info.magnolia.cms.beans.config.ShutdownManager"%>
<%@ page import="info.magnolia.cms.beans.config.VirtualURIManager"%>
<%@ page import="info.magnolia.cms.security.SecureURI"%>
<%@ page import="info.magnolia.cms.util.DateUtil"%>
<%@ page import="java.util.Date"%>
<%@ page import="java.util.Iterator"%>
<%@ page import="java.util.List"%>
<%@ page import="java.util.Map"%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head><title>Server configuration page</title>
    <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/.resources/configPage/configuration.css" />
</head>

<h1>Server configuration page</h1>

<!--
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    General Server Data
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
-->
<table id="mytable" cellspacing="0" summary="Shutdown tasks">
    <caption>General Magnolia Server Data</caption>
    <tr>
        <th scope="col" class="nobg">Configuration</th>
        <th scope="col" >Value</th>
    </tr>
    <tr>
        <td>Server up since...</td><td><%=DateUtil.formatDateTime(new Date(Server.getUptime()))%></td>
    </tr>
    <tr>
        <td>Is admin server</td><td><%=Server.isAdmin()%></td>
    </tr>

    <tr>
        <td>Default Mail Server</td><td><%=Server.getDefaultMailServer()%>&nbsp;</td>
    </tr>
    <tr>
        <td>Visible to Obinary</td><td><%=Server.isVisibleToObinary()%></td>
    </tr>
    <tr>
        <td>Basic Realm</td><td><%=Server.getBasicRealm()%></td>
    </tr>
    <tr>
        <td>404 URI</td><td><%=Server.get404URI()%></td>
    </tr>

</table>


<br/><br/><br/>

<!--
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    Virtual URIs
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
-->
<%
    VirtualURIManager vm = VirtualURIManager.getInstance();
    Map map = vm.getURIMappings();
    Iterator iter2 = map.values().iterator();
%>
<table id="mytable" cellspacing="0" summary="Shutdown tasks">
    <caption>Virtual URIs</caption>
    <tr>
        <th scope="col" class="nobg">From URI (URL Pattern)</th>
        <th scope="col" >To URI</th>
    </tr>
    <%
        int i = 0;
        String[] s;
        while(iter2.hasNext()) {
            s = (String[])iter2.next();

    %>
    <tr>
        <th scope="row" class="specalt"><%= s[1]%></th>
        <%
            if(i%2==0) {
        %>
        <td class="alt">
            <%
        }
            else {%>
        <td>
            <%}
            %>
            <%=s[0]%></td>
    </tr>
    <%
            i++;
        }
    %>
</table>


<br/><br/><br/>

<!--
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    Secure URIs
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
-->
<%
    Map map1 = SecureURI.listSecureURIs();
    Map map2 = SecureURI.listUnsecureURIs();
    Iterator i1 = map1.keySet().iterator();
    Iterator i2 = map2.keySet().iterator();
%>
<table id="mytable" cellspacing="0" summary="Shutdown tasks">
    <caption>Secure/Unsecure URIs</caption>
    <tr>
        <th scope="col" class="nobg">URL pattern</th>
        <th scope="col" >Secure</th>
        <th scope="col" >Unsecure</th>
    </tr>

    <%
        // SECURE
        while(i1.hasNext()) {
    %>
    <tr>
        <th scope="row" class="specalt"><%=i1.next()%></th>
        <td><b>x</b></td>
        <td>&nbsp;</td>
    </tr>
    <%
        }
    %>

    <%
        // UNSECURE
        while(i2.hasNext()) {
    %>
    <tr>
        <th scope="row"><%=i2.next()%></th>
        <td>&nbsp;</td>
        <td><b>x</b></td>
    </tr>
    <%
        }
    %>

</table>

<br/><br/><br/>

<!--
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    Shutdown Tasks
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
-->
<%
    List list = ShutdownManager.listShutdownTasks();
    Iterator iter = list.iterator();
%>
<table id="mytable" cellspacing="0" summary="Shutdown tasks">
    <caption>Shutdown tasks</caption>
    <tr>
        <th scope="col" class="nobg">Task number(Lowest executed first)</th>
        <th scope="col" >Java string description</th>
    </tr>
    <%
        int j = 0;
        while(iter.hasNext()) {
    %>
    <tr>
        <th scope="row" class="specalt"><%=j%></th>
        <%
            if(j%2==0) {
        %>
        <td class="alt">
            <%
        }
            else {%>
        <td>
            <%}
            %>
            <%=iter.next()%></td>
    </tr>
    <%
            j++;
        }
    %>
</table>


</html>