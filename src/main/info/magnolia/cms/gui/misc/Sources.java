/**
 *
 * Magnolia and its source-code is licensed under the LGPL.
 * You may copy, adapt, and redistribute this file for commercial or non-commercial use.
 * When copying, adapting, or redistributing this document in keeping with the guidelines above,
 * you are required to provide proper attribution to obinary.
 * If you reproduce or distribute the document without making any substantive modifications to its content,
 * please use the following attribution line:
 *
 * Copyright 1993-2005 obinary Ltd. (http://www.obinary.com) All rights reserved.
 *
 */
package info.magnolia.cms.gui.misc;

/**
 * @author Vinzenz Wyser
 * @version 2.0
 */
public class Sources {

    /**
     * Context path for the current request.
     */
    private String contextPath;

    /**
     * Instantiate a new Source for a given context path.
     * @param contextPath context path for the current request (request.getContextPath)
     */
    public Sources(String contextPath) {
        this.contextPath = contextPath;
    }

    public String getHtmlJs() {
        StringBuffer html = new StringBuffer();
        html.append("<script type=\"text/javascript\" src=\"");
        html.append(contextPath);
        html.append("/admindocroot/js/generic.js\"></script>");
        html.append("<script type=\"text/javascript\" src=\"");
        html.append(contextPath);
        html.append("/admindocroot/js/general.js\"></script>");
        html.append("<script type=\"text/javascript\" src=\"");
        html.append(contextPath);
        html.append("/admindocroot/js/controls.js\"></script>");
        html.append("<script type=\"text/javascript\" src=\"");
        html.append(contextPath);
        html.append("/admindocroot/js/dialogs/dialogs.js\"></script>");
        html.append("<script type=\"text/javascript\" src=\"");
        html.append(contextPath);
        html.append("/admindocroot/js/dialogs/acl.js\"></script>");
        html.append("<script type=\"text/javascript\" src=\"");
        html.append(contextPath);
        html.append("/admindocroot/js/dialogs/calendar.js\"></script>");
        html.append("<script type=\"text/javascript\" src=\"");
        html.append(contextPath);
        html.append("/admindocroot/js/tree.js\"></script>");
        html.append("<script type=\"text/javascript\" src=\"");
        html.append(contextPath);
        html.append("/admindocroot/js/adminCentral.js\"></script>");
        html.append("<script type=\"text/javascript\" src=\"");
        html.append(contextPath);
        html.append("/admindocroot/js/inline.js\"></script>");
        return html.toString();
    }

    public String getHtmlCss() {
        StringBuffer html = new StringBuffer();
        html.append("<link rel=\"stylesheet\" type=\"text/css\" href=\"");
        html.append(contextPath);
        html.append("/admindocroot/css/admin-all.css\" />");
        return html.toString();
    }

    public String getHtmlRichEdit() {
        StringBuffer html = new StringBuffer();
        html.append("<link rel=\"stylesheet\" type=\"text/css\" href=\"");
        html.append(contextPath);
        html.append("/admindocroot/richE/kupustyles.css\" />");
        html.append("<link rel=\"stylesheet\" type=\"text/css\" href=\"");
        html.append(contextPath);
        html.append("/admindocroot/richE/kupucustom.css\" />");
        html.append("<script type=\"text/javascript\" src=\"");
        html.append(contextPath);
        html.append("/admindocroot/richE/sarissa.js\"> </script>");
        html.append("<script type=\"text/javascript\" src=\"");
        html.append(contextPath);
        html.append("/admindocroot/richE/kupuhelpers.js\"> </script>");
        html.append("<script type=\"text/javascript\" src=\"");
        html.append(contextPath);
        html.append("/admindocroot/richE/kupueditor.js\"> </script>");
        html.append("<script type=\"text/javascript\" src=\"");
        html.append(contextPath);
        html.append("/admindocroot/richE/kupubasetools.js\"> </script>");
        html.append("<script type=\"text/javascript\" src=\"");
        html.append(contextPath);
        html.append("/admindocroot/richE/kupuloggers.js\"> </script>");
        html.append("<script type=\"text/javascript\" src=\"");
        html.append(contextPath);
        html.append("/admindocroot/richE/kupucontentfilters.js\"> </script>");
        html.append("<script type=\"text/javascript\" src=\"");
        html.append(contextPath);
        html.append("/admindocroot/richE/kupuinit.js\"> </script>");
        return html.toString();
    }

}
