<jsp:root version="1.2" xmlns:jsp="http://java.sun.com/JSP/Page">
    <jsp:directive.page contentType="text/html; charset=UTF8" />
    <jsp:directive.page import="info.magnolia.cms.gui.control.*" />
    <jsp:text>
        <![CDATA[<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" 
        "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd"> ]]>
    </jsp:text>
    <html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
    <head>
    <title>Magnolia unit test</title>
    </head>
    <body>
    <jsp:scriptlet>Button button = new Button("name", "value");</jsp:scriptlet>
    <div id="button"><jsp:expression>button.getHtmlPushbutton()</jsp:expression></div>
    </body>
    </html>
</jsp:root>
