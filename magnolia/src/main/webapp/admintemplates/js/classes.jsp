<%@ page import="java.util.Collection" %>
<%@ page import="java.util.Iterator" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.regex.Pattern" %>
<%@ page import="java.util.HashMap" %>
<%@ page import="java.util.Map" %>
<%@ page import="javax.servlet.jsp.JspWriter" %>
<%@ page import="java.io.IOException" %>
<%@ page import="info.magnolia.cms.util.ClasspathResourcesUtil" %>
<%@ page import="org.apache.commons.io.IOUtils" %>
<%@ page import="java.util.regex.Matcher" %>
<%@ page import="org.apache.commons.lang.StringUtils" %>
<%!
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

%>

<%
    Collection files = ClasspathResourcesUtil.findResources(new ClasspathResourcesUtil.Filter(){
        public boolean accept(String name){
            return name.startsWith("/mgnl-resources/js-classes") && name.endsWith(".js");
        }
    });
    
    for(Iterator iter = files.iterator(); iter.hasNext();){
        String name = (String) iter.next();
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

%>

MgnlRuntime.loadingOn=false;

<%
    for(Iterator iter = classes.keySet().iterator(); iter.hasNext(); ){
        String className = (String) iter.next();
        process(className, out);
    }
%>

MgnlRuntime.loadingOn=true;