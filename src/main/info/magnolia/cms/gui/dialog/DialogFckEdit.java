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
import info.magnolia.cms.gui.control.ControlSuper;
import info.magnolia.cms.util.LinkUtil;

import java.io.IOException;
import java.io.Writer;

import javax.jcr.RepositoryException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;


/**
 * An Magnolia dialog for the universal usage and configuration of the fckeditor. Credits for FCKEditor:
 * http://www.fckeditor.net/
 * @author bert schulzki
 * @author Fabrizio Giustina
 * @version 1.0 11.10.2004
 */
public class DialogFckEdit extends DialogBox {

    /**
     * Logger.
     */
    private static Logger log = Logger.getLogger(DialogFckEdit.class);

    /**
     * The new .BasePath of the editor
     */
    public static final String FCKEDIT_PATH = "/admindocroot/fckeditor/"; //$NON-NLS-1$

    /**
     * This parameter defines the startup script. This parameter is searched in the dialog configuration.
     */
    public static final String PARAM_JS_INIT_FILE = "jsInitFile"; //$NON-NLS-1$

    /**
     * This parameter defines the configuration script
     */
    public static final String PARAM_CUSTOM_CONFIGURATION_PATH = "customConfigurationPath"; //$NON-NLS-1$

    /**
     * If jsInitFile is not defined
     */
    public static final String PARAM_JS_INIT_FILE_DEFAULT = "/admindocroot/fckeditor/custom/init/magnoliaStandard.js"; //$NON-NLS-1$

    /**
     * If customConfigurationPath is not defined
     */
    public static final String PARAM_CUSTOM_CONFIGURATION_PATH_DEFAULT = "/admindocroot/fckeditor/custom/config/magnoliaStandard.js"; //$NON-NLS-1$

    /**
     * the configuration script name
     */
    private String customConfigurationsPath = PARAM_CUSTOM_CONFIGURATION_PATH_DEFAULT;

    /**
     * the initialization script name
     */
    private String jsInitFile = PARAM_JS_INIT_FILE_DEFAULT;

    /**
     * Empty constructor should only be used by DialogFactory.
     */
    protected DialogFckEdit() {
    }

    /**
     * @return The name of the variable for the editor object
     */
    public String getVarName() {
        String id = getId();
        if (id == null) {
            id = getName();
        }
        return "fck_" + id.replace('-', '_'); //$NON-NLS-1$
    }

    /**
     * @param name script name
     */
    public void setCustomConfigurationPath(String name) {
        if (name != null) {
            customConfigurationsPath = name;
        }
    }

    /**
     * @param name init file
     */
    public void setJSInitFile(String name) {
        if (name != null) {
            jsInitFile = name;
        }
    }

    /**
     * @see info.magnolia.cms.gui.dialog.DialogInterface#init(HttpServletRequest, HttpServletResponse, Content, Content)
     */
    public void init(HttpServletRequest request, HttpServletResponse response, Content websiteNode, Content configNode)
        throws RepositoryException {
        super.init(request, response, websiteNode, configNode);
        String jsInitFile = this.getConfigValue(PARAM_JS_INIT_FILE, PARAM_JS_INIT_FILE_DEFAULT);
        String customConfigurationPath = this.getConfigValue(
            PARAM_CUSTOM_CONFIGURATION_PATH,
            PARAM_CUSTOM_CONFIGURATION_PATH_DEFAULT);
        this.setJSInitFile(jsInitFile);
        this.setCustomConfigurationPath(customConfigurationPath);
    }

    /**
     * @see info.magnolia.cms.gui.dialog.DialogInterface#drawHtml(Writer)
     */
    public void drawHtml(Writer out) throws IOException {
        this.drawHtmlPre(out);

        // load the script onece: if there are multiple instances
        if (getRequest().getAttribute("__fcked_loaded") == null) { //$NON-NLS-1$
            out.write("<script type=\"text/javascript\" src=\"" //$NON-NLS-1$
                + this.getRequest().getContextPath()
                + "/admindocroot/fckeditor/fckeditor.js\"></script>"); //$NON-NLS-1$
            getRequest().setAttribute("__fcked_loaded", "true"); //$NON-NLS-1$ //$NON-NLS-2$
        }

        String id = getName();

        if (id == null) {
            log.error("Missing id for fckEditor instance"); //$NON-NLS-1$
        }

        String var = getVarName();
        String value = convertToView(getValue());
        value = LinkUtil.convertUUIDsToAbsoluteLinks(value);
        out.write("<script type=\"text/javascript\">"); //$NON-NLS-1$
        out.write("var " + var + " = null;"); //$NON-NLS-1$ //$NON-NLS-2$
        out.write("fckInstance = new FCKeditor( '" + id + "' );"); //$NON-NLS-1$ //$NON-NLS-2$
        out.write("fckInstance.Value = '" + escapeJsValue(value) + "';"); //$NON-NLS-1$ //$NON-NLS-2$
        out.write("fckInstance.BasePath = '" + this.getRequest().getContextPath() + FCKEDIT_PATH + "';"); //$NON-NLS-1$ //$NON-NLS-2$

        if (this.getConfigValue("height", null) != null) { //$NON-NLS-1$
            out.write("fckInstance.Height = '" + escapeJsValue(this.getConfigValue("height")) + "';"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        }

        if (customConfigurationsPath.length() > 0) {
            out.write("fckInstance.Config['CustomConfigurationsPath'] = '" //$NON-NLS-1$
                + this.getRequest().getContextPath()
                + customConfigurationsPath
                + "';"); //$NON-NLS-1$
        }
        if (jsInitFile.length() > 0) {
            out.write("</script>"); //$NON-NLS-1$
            out.write("<script type=\"text/javascript\" src=\"" //$NON-NLS-1$
                + this.getRequest().getContextPath()
                + jsInitFile
                + "\"></script>\n"); //$NON-NLS-1$
            out.write("<script type=\"text/javascript\">"); //$NON-NLS-1$
        }

        out.write("fckInstance.Create();"); //$NON-NLS-1$
        out.write(var + " = fckInstance;"); //$NON-NLS-1$
        out.write("</script>"); //$NON-NLS-1$

        // write the saveInfo for the writting back to the repository
        out.write("<input type='hidden' name='mgnlSaveInfo' value='" //$NON-NLS-1$
            + id
            + ",String," //$NON-NLS-1$
            + ControlSuper.VALUETYPE_SINGLE
            + "," //$NON-NLS-1$
            + ControlSuper.RICHEDIT_FCK
            + "," //$NON-NLS-1$
            + ControlSuper.ENCODING_NO
            + "' />"); //$NON-NLS-1$

        this.drawHtmlPost(out);

    }

    /**
     * @param value
     * @return
     */
    private static String convertToView(String value) {
        String tmp = value;
        if (tmp != null) {
            tmp = tmp.replaceAll("\r\n", "<br />"); //$NON-NLS-1$ //$NON-NLS-2$
            tmp = tmp.replaceAll("\n", "<br />"); //$NON-NLS-1$ //$NON-NLS-2$
            return tmp;
        }
        return StringUtils.EMPTY;
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
     * @return escaped js String
     */
    public static String escapeJsValue(String src) {
        if (src == null) {
            return null;
        }
        String escapedSrc = src.replaceAll("'", "\\\\'"); //$NON-NLS-1$ //$NON-NLS-2$
        escapedSrc = escapedSrc.replaceAll("\"", "\\\""); //$NON-NLS-1$ //$NON-NLS-2$
        escapedSrc = escapedSrc.replaceAll("\\r\\n", "\\\\r\\\\n"); //$NON-NLS-1$ //$NON-NLS-2$
        escapedSrc = escapedSrc.replaceAll("\\n", "\\\\n"); //$NON-NLS-1$ //$NON-NLS-2$

        return escapedSrc;
    }
}