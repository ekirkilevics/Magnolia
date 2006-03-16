<%@ page import="info.magnolia.cms.util.ClasspathResourcesUtil" %>
<%@ page import="org.apache.commons.io.IOUtils" %>
<%!
    private static String[] names;
%>
<%
    // finding files in classpath is too expensive, just cache the list of paths!
    if (names == null || nocache) {
        names = ClasspathResourcesUtil.findResources(new ClasspathResourcesUtil.Filter() {
            public boolean accept(String name) {
                return name.startsWith("/js-libs/") && name.endsWith(".js");
            }
        });
    }

    for (int j = 0; j < names.length; j++) {
        IOUtils.copy(ClasspathResourcesUtil.getStream(names[j]), out);
    }
%>

