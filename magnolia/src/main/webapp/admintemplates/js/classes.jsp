<%@ page import="org.apache.commons.io.FileUtils" %>
<%@ page import="info.magnolia.cms.core.Path" %>
<%@ page import="java.util.Collection" %>
<%@ page import="java.io.File" %>
<%@ page import="java.util.Iterator" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.regex.Pattern" %>
<%@ page import="org.apache.commons.lang.StringUtils" %>
<%@ page import="java.util.regex.Matcher" %>
<%@ page import="java.util.HashMap" %>
<%@ page import="java.util.Map" %>
<%@ page import="javax.servlet.jsp.JspWriter" %>
<%@ page import="java.io.IOException" %>
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
	Collection files = FileUtils.listFiles(new File(Path.getAppRootDir() + "/admindocroot/js/classes"), new String[]{"js"}, true );
	for(Iterator iter = files.iterator(); iter.hasNext();){
		Definition def = new Definition();
		File file = (File) iter.next();
		def.name = StringUtils.substringAfter(file.getPath(), "/classes/");
		def.name = StringUtils.removeEnd(def.name, ".js");
		def.name = StringUtils.replace(def.name, "/", ".");
		def.content = FileUtils.readFileToString(file,"UTF8");
		Matcher matcher = importPattern.matcher(def.content);
		while(matcher.find()){
			String importName = matcher.group(1);
			def.imports.add(importName);
		}
		classes.put(def.name, def);
	}
	
	Definition runtime = (Definition) classes.get("mgnl.Runtime");
	out.println(runtime.content);
	runtime.proceed = true;
	
	for(Iterator iter = classes.keySet().iterator(); iter.hasNext(); ){
		String className = (String) iter.next();
		process(className, out);
	}
%>