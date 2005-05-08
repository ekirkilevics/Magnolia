<jsp:root version="2.0" xmlns:jsp="http://java.sun.com/JSP/Page" xmlns:c="urn:jsptld:http://java.sun.com/jsp/jstl/core"
    xmlns:fmt="urn:jsptld:http://java.sun.com/jsp/jstl/fmt">
    <jsp:directive.page contentType="text/html; charset=UTF-8" />
    <html>
    <head>
    <title>Error</title>
    </head>
    <body>
    <h3>Error</h3>
    <p>${requestScope['javax.servlet.error.exception'].message}</p>

    <pre>
    <jsp:scriptlet>
		Exception ex = (Exception) request.getAttribute("javax.servlet.error.exception");
		if (ex != null)
        {
		ex.printStackTrace(new java.io.PrintWriter(out));
        }
	</jsp:scriptlet>
	</pre>

    </body>
    </html>
</jsp:root>
