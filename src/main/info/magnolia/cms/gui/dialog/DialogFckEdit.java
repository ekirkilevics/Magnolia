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
package info.magnolia.cms.gui.dialog;

import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.ContentNode;

import java.io.IOException;

import javax.jcr.RepositoryException;
import javax.servlet.jsp.JspWriter;

import org.apache.log4j.Logger;


/**
 * An Magnolia control for the universal usage and configuration of the fckeditor. Credits for FCKEditor:
 * http://www.fckeditor.net/
 * @author bert.schulzki
 * @author Fabrizio Giustina
 * @version 1.0 11.10.2004
 */
public class DialogFckEdit extends DialogBox {

    public static final String FCKEDIT_PATH = "/admindocroot/fckeditor/";

    public static final String PARAM_JS_INIT_FILE = "jsInitFile";

    public static final String PARAM_CUSTOM_CONFIGURATION_PATH = "customConfigurationPath";

    /**
     * Logger.
     */
    private static Logger log = Logger.getLogger(DialogFckEdit.class);

    private String customConfigurationsPath = "";

    private String jsInitFile = "";

    public DialogFckEdit() {
    }

    public DialogFckEdit(ContentNode configNode, Content websiteNode) throws RepositoryException {
        super(configNode, websiteNode);
    }

    public String getVarName() {
        String id = getId();
        if (id == null) {
            id = getName();
        }
        return "fck_" + id.replace('-', '_');
    }

    public void setCustomConfigurationPath(String name) {
        if (name != null) {
            customConfigurationsPath = name;
        }
    }

    public void setJSInitFile(String name) {
        if (name != null) {
            jsInitFile = name;
        }
    }

    public void drawHtml(JspWriter out) throws IOException {
        String id = getId();
        if (id == null) {
            id = getName();
        }
        String var = getVarName();
        out.println("<script type=\"text/javascript\">\n");
        out.println("var " + var + " = null;\n");
        out.println("fckInstance = new FCKeditor( '" + id + "' );\n");
        out.println("fckInstance.Value = '" + escapeJsValue(getValue()) + "';\n");
        out.println("fckInstance.BasePath = '" + FCKEDIT_PATH + "';\n");
        if (customConfigurationsPath.length() > 0) {
            out.println("fckInstance.Config['CustomConfigurationsPath'] = '" + customConfigurationsPath + "';");
        }
        if (jsInitFile.length() > 0) {
            out.println("</script>\n");
            out.println("<script type=\"text/javascript\" src=\"" + jsInitFile + "\"></script>\n");
            out.println("<script type=\"text/javascript\">\n");
        }
        out.println("fckInstance.Create();\n");
        out.println(var + " = fckInstance;\n");
        out.println("</script>\n");
    }

    /**
     * Replacements:
     * 
     * <pre>
     * ' -> \'
     * " -> \"
     * \r\n -> \\r\\n
     * \n -> \\n
     * \ -> \\
     * </pre>
     * 
     * @param src
     * @return
     */
    public static String escapeJsValue(String src) {
        if (src != null) {
            src = src.replaceAll("\\\\", "\\\\");
            src = src.replaceAll("'", "\\\\'");
            src = src.replaceAll("\"", "\\\"");
            src = src.replaceAll("\\r\\n", "\\\\r\\\\n");
            src = src.replaceAll("\\n", "\\\\n");
        }
        return src;
    }
}
