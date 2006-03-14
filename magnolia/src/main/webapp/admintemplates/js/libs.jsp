<%@ page import="info.magnolia.cms.util.ClasspathResourcesUtil" %>
<%@ page import="java.util.Collection" %>
<%@ page import="java.util.Iterator" %>
<%@ page import="org.apache.commons.io.IOUtils" %>

<%
    Collection names = ClasspathResourcesUtil.findResources(new ClasspathResourcesUtil.Filter(){
        public boolean accept(String name){
            return name.startsWith("/js-libs/") && name.endsWith(".js");
        }
    });
    
    for(Iterator iter = names.iterator(); iter.hasNext(); ){
        String name = (String) iter.next();
        IOUtils.copy(getClass().getResourceAsStream(name), out);
    }
%>