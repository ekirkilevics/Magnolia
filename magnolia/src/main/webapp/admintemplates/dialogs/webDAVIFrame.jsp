<%
/**
 *
 * Magnolia and its source-code is licensed under the LGPL.
 * You may copy, adapt, and redistribute this file for commercial or non-commercial use.
 * When copying, adapting, or redistributing this document in keeping with the guidelines above,
 * you are required to provide proper attribution to obinary.
 * If you reproduce or distribute the document without making any substantive modifications to its content,
 * please use the following attribution line:
 *
 * Copyright 1993-2004 obinary Ltd. (http://www.obinary.com) All rights reserved.
 *
 * */
%><%@ page import="org.apache.webdav.lib.WebdavResource,
                 java.util.Hashtable,
                 java.util.Enumeration,
                 java.net.URLDecoder,
                 java.io.UnsupportedEncodingException,
                 org.apache.webdav.lib.methods.XMLResponseMethodBase,
                 java.io.IOException,
                 org.apache.webdav.lib.Property,
                 java.net.URLEncoder,
                 java.text.SimpleDateFormat,
                 java.util.Date,
                 info.magnolia.cms.gui.dialog.DialogWebDAV,
                 info.magnolia.cms.gui.dialog.DialogSuper"%>

<%
    DialogWebDAV dav=(DialogWebDAV) request.getSession().getAttribute(request.getParameter(DialogSuper.SESSION_ATTRIBUTENAME_DIALOGOBJECT));
    //do not remove session attribute!
    if (dav!=null) {
        if (request.getParameter("subDirectory")!=null) dav.setSubDirectory(request.getParameter("subDirectory"));
        if (request.getParameter("selectedValue")!=null) {
            dav.setValue(request.getParameter("selectedValue"));
        }
        dav.setFrameRequest(request);
        dav.drawHtmlList(out);
    }
    else {
        out.println("<i>An error occured. Unable to connect to WebDAV Server</i>");
    }



%>



