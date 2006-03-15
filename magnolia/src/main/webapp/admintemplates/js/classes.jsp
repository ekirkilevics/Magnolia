<jsp:root version="1.2" xmlns:jsp="http://java.sun.com/JSP/Page" xmlns:cms="urn:jsptld:cms-taglib"
    xmlns:cmsu="urn:jsptld:cms-util-taglib" xmlns:c="urn:jsptld:http://java.sun.com/jsp/jstl/core">
    <jsp:directive.page import="info.magnolia.cms.util.ClasspathResourcesUtil" />
    <jsp:directive.page import="org.apache.commons.io.IOUtils" />
    <jsp:directive.page import="java.util.Iterator" />
    <jsp:directive.page import="java.util.List" />
    <jsp:directive.page import="java.util.ArrayList" />
    <jsp:directive.page import="java.util.regex.Pattern" />
    <jsp:directive.page import="java.util.HashMap" />
    <jsp:directive.page import="java.util.Map" />
    <jsp:directive.page import="javax.servlet.jsp.JspWriter" />
    <jsp:directive.page import="java.io.IOException" />
    <jsp:directive.page import="info.magnolia.cms.util.ClasspathResourcesUtil" />
    <jsp:directive.page import="java.util.regex.Matcher" />
    <jsp:directive.page import="org.apache.commons.lang.StringUtils" />

    <jsp:declaration>
       private static String[] files;

       Pattern importPattern = Pattern.compile("importClass\\(\"(.*)\"\\);");
       Map classes = new HashMap();

       class Definition{
           boolean proceed = false;
           String content;
           String name;
           List imports = new ArrayList();
       }

       void process(String name, JspWriter out) throws IOException{
           Definition def = (Definition) classes.get(name);
           if(!def.proceed){
               def.proceed = true;
               for(Iterator iter = def.imports.iterator(); iter.hasNext();){
                   String importName = (String) iter.next();
                   process(importName, out);
               }
               out.println(def.content);
           }
       }

    </jsp:declaration>

    <jsp:scriptlet>
        <![CDATA[

            // finding files in classpath is too expensive, just cache the list of paths!
            if (files ==null)
            {
                files = ClasspathResourcesUtil.findResources(new ClasspathResourcesUtil.Filter(){
                    public boolean accept(String name){
                        return name.startsWith("/mgnl-resources/js-classes") && name.endsWith(".js");
                    }
                });
            }

           for (int j = 0; j < files.length; j++) {
                String name = files[j];
                Definition def = new Definition();
                def.name = StringUtils.replace(name, "\\", "/");
                def.name = StringUtils.substringAfterLast(def.name, "/js-classes/");
                def.name = StringUtils.removeEnd(def.name, ".js");
                def.name = StringUtils.replace(def.name, "/", ".");

                def.content = IOUtils.toString(getClass().getResourceAsStream(name));
                Matcher matcher = importPattern.matcher(def.content);
                while(matcher.find()){
                    String importName = matcher.group(1);
                    def.imports.add(importName);
                }
                classes.put(def.name, def);
            }

            // write first the runtime
            Definition runtime = (Definition) classes.get("mgnl.Runtime");
            out.println(runtime.content);
            runtime.proceed = true;
        ]]>
    </jsp:scriptlet>

MgnlRuntime.loadingOn=false;

    <jsp:scriptlet>
        <![CDATA[
            for(Iterator iter = classes.keySet().iterator(); iter.hasNext(); ){
                String className = (String) iter.next();
                process(className, out);
            }
        ]]>
    </jsp:scriptlet>

MgnlRuntime.loadingOn=true;

</jsp:root>

