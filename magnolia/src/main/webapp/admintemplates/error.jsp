<jsp:root version="2.0" xmlns:jsp="http://java.sun.com/JSP/Page" xmlns:c="urn:jsptld:http://java.sun.com/jsp/jstl/core"
    xmlns:fmt="urn:jsptld:http://java.sun.com/jsp/jstl/fmt" xmlns:cms="urn:jsptld:cms-taglib">
    <jsp:directive.page contentType="text/html; charset=UTF-8" />
    <html>
    <head>
    <title>Error</title>
    <cms:links adminOnly="false" />
    </head>
    <body class="mgnlBgLight">
    <h3>Error</h3>

    <jsp:scriptlet>
		Throwable ex = (Throwable) request.getAttribute("javax.servlet.error.exception");
		if (ex != null)
        {
    </jsp:scriptlet>

    <![CDATA[<p>]]>

    <jsp:scriptlet>
		out.println(ex.getMessage());
	</jsp:scriptlet>

    <![CDATA[</p>]]>
    <![CDATA[<pre>]]>

    <jsp:scriptlet>
		ex.printStackTrace(new java.io.PrintWriter(out));
	</jsp:scriptlet>

    <![CDATA[</pre>]]>

    <jsp:scriptlet>
        }
	</jsp:scriptlet>

    </body>
    </html>
</jsp:root>
