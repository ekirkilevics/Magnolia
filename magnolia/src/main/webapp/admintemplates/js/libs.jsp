<jsp:root version="1.2" xmlns:jsp="http://java.sun.com/JSP/Page" xmlns:cms="urn:jsptld:cms-taglib"
    xmlns:cmsu="urn:jsptld:cms-util-taglib" xmlns:c="urn:jsptld:http://java.sun.com/jsp/jstl/core">
    <jsp:directive.page import="info.magnolia.cms.util.ClasspathResourcesUtil" />
    <jsp:directive.page import="org.apache.commons.io.IOUtils" />

    <jsp:declaration>
       private static String[] names;
    </jsp:declaration>

    <jsp:scriptlet>
        <![CDATA[

            // finding files in classpath is too expensive, just cache the list of paths!
            if (names ==null)
            {
                names = ClasspathResourcesUtil.findResources(new ClasspathResourcesUtil.Filter(){
                    public boolean accept(String name){
                        return name.startsWith("/js-libs/") && name.endsWith(".js");
                    }
                });
            }

            for (int j = 0; j < names.length; j++) {
                IOUtils.copy(getClass().getResourceAsStream(names[j]), out);
            }
        ]]>
    </jsp:scriptlet>

</jsp:root>



