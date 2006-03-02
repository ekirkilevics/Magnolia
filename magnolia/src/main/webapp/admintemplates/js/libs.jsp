<%@ page import="org.apache.commons.io.FileUtils" %>
<%@ page import="info.magnolia.cms.core.Path" %>
<%@ page import="java.util.Collection" %>
<%@ page import="java.io.File" %>
<%@ page import="java.util.Iterator" %>

<%
    Collection libFiles = FileUtils.listFiles(new File(Path.getAppRootDir() + "/admindocroot/js/libs"), new String[]{"js"}, true );
    for(Iterator iter = libFiles.iterator(); iter.hasNext();){
        File file = (File) iter.next();
        out.println(FileUtils.readFileToString(file,"UTF8"));
    }
%>