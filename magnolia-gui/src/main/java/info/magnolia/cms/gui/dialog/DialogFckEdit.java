/**
 *
 * Magnolia and its source-code is licensed under the LGPL.
 * You may copy, adapt, and redistribute this file for commercial or non-commercial use.
 * When copying, adapting, or redistributing this document in keeping with the guidelines above,
 * you are required to provide proper attribution to obinary.
 * If you reproduce or distribute the document without making any substantive modifications to its content,
 * please use the following attribution line:
 *
 * Copyright 1993-2006 obinary Ltd. (http://www.obinary.com) All rights reserved.
 *
 */
package info.magnolia.cms.gui.dialog;

import info.magnolia.cms.core.Content;
import info.magnolia.cms.gui.control.ControlImpl;
import info.magnolia.cms.link.AbsolutePathTransformer;
import info.magnolia.cms.link.LinkHelper;
import info.magnolia.cms.link.UUIDLink;
import info.magnolia.cms.util.LinkUtil;
import info.magnolia.context.MgnlContext;

import java.io.IOException;
import java.io.Writer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.jcr.RepositoryException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * An Magnolia dialog for the universal usage and configuration of the fckeditor. Credits for FCKEditor:
 * http://www.fckeditor.net/<p/> The fckEditor is mainly configured in javascript files. Those files are configured
 * with the following attributes.
 * <ul>
 * <li>jsInitFile</li>
 * <li>customConfigurationPath</li>
 * </ul>
 * Where the default values are:
 * <ul>
 * <li>/admindocroot/fckeditor/custom/init/magnoliaStandard.js</li>
 * <li>/admindocroot/fckeditor/custom/config/magnoliaStandard.js</li>
 * </ul>
 * To make live simple we provide some attributes to configure the control in the magnolia configuration instead within
 * the javascript files. <table>
 * <tr>
 * <td>css</td>
 * <td>The css file to use. Default is /admindocroot/fckeditor/custom/css/magnoliaStandard.css </td>
 * </tr>
 * <tr>
 * <td>height</td>
 * <td>The height of the editor.</td>
 * </tr>
 * <tr>
 * <td>tables</td>
 * <td>The table editing features are available if true</td>
 * </tr>
 * <tr>
 * <td>lists</td>
 * <td>The list features are available if true</td>
 * </tr>
 * <tr>
 * <td>aligment</td>
 * <td>The aligment features are available if true</td>
 * </tr>
 * <tr>
 * <td>images</td>
 * <td>The image editing features including upload are available if true</td>
 * </tr>
 * <tr>
 * <td>fileUpload</td>
 * <td>The file upload features is enabled if true</td>
 * </tr>
 * <tr>
 * <td>styles</td>
 * <td>Defines the xml file defining the used styles. See
 * http://wiki.fckeditor.net/Developer%27s_Guide/Configuration/Styles for details</td>
 * </tr>
 * <tr>
 * <td>templates</td>
 * <td>Defines the xml file defining the used templates. See
 * http://wiki.fckeditor.net/Developer%27s_Guide/Configuration/Templates for details</td>
 * </tr>
 * <tr>
 * <td>fonts</td>
 * <td>A semicolon separated list of font names.</td>
 * </tr>
 * <tr>
 * <td>fontSizes</td>
 * <td>A semicolon separated list of font sizes</td>
 * </tr>
 * <tr>
 * <tr>
 * <td>colors</td>
 * <td>A comma separated list of colors. hex values without #.</td>
 * </tr>
 * <tr>
 * <td>source</td>
 * <td>Show the source button</td>
 * </tr>
 * </table>
 * @author bert schulzki
 * @author Fabrizio Giustina
 * @version
 */
public class DialogFckEdit extends DialogBox {

    /**
     * Logger.
     */
    private static Logger log = LoggerFactory.getLogger(DialogFckEdit.class);

    /**
     * The new .BasePath of the editor
     */
    public static final String FCKEDIT_PATH = "/.resources/fckeditor/"; //$NON-NLS-1$

    /**
     * Used to make sure that the javascript files are loaded only once
     */
    private static final String ATTRIBUTE_FCKED_LOADED = "info.magnolia.cms.gui.dialog.fckedit.loaded";

    /**
     * This parameter defines the startup script. This parameter is searched in the dialog configuration.
     */
    public static final String PARAM_JS_INIT_FILE = "jsInitFile"; //$NON-NLS-1$

    /**
     * This parameter defines the configuration script
     */
    public static final String PARAM_CUSTOM_CONFIGURATION_PATH = "jsConfigFile"; //$NON-NLS-1$

    public static final String PARAM_CSS = "css"; //$NON-NLS-1$

    public static final String PARAM_HEIGHT = "height"; //$NON-NLS-1$

    public static final String PARAM_TABLES = "tables"; //$NON-NLS-1$

    private static final String PARAM_LISTS = "lists";

    private static final String PARAM_ALIGNMENT = "alignment";

    public static final String PARAM_IMAGES = "images"; //$NON-NLS-1$

    public static final String PARAM_STYLES = "styles"; //$NON-NLS-1$

    public static final String PARAM_TEMPLATES = "templates"; //$NON-NLS-1$

    public static final String PARAM_FONTS = "fonts"; //$NON-NLS-1$

    public static final String PARAM_FONT_SIZES = "fontSizes"; //$NON-NLS-1$

    private static final String PARAM_COLORS = "colors";

    public static final String PARAM_SOURCE = "source"; //$NON-NLS-1$

    /**
     * Default falues
     */
    public static final String PARAM_JS_INIT_FILE_DEFAULT = "/.resources/fckeditor/custom/init/magnoliaStandard.js"; //$NON-NLS-1$

    public static final String PARAM_CUSTOM_CONFIGURATION_PATH_DEFAULT = "/.resources/fckeditor/custom/config/magnoliaStandard.js"; //$NON-NLS-1$

    public static final String PARAM_CSS_DEFAULT = "/.resources/fckeditor/custom/css/magnoliaStandard.css"; //$NON-NLS-1$

    public static final String PARAM_HEIGHT_DEFAULT = ""; //$NON-NLS-1$

    public static final String PARAM_TABLES_DEFAULT = "false"; //$NON-NLS-1$

    public static final String PARAM_IMAGES_DEFAULT = "false"; //$NON-NLS-1$

    public static final String PARAM_STYLES_DEFAULT = ""; //$NON-NLS-1$

    public static final String PARAM_TEMPLATES_DEFAULT = ""; //$NON-NLS-1$

    public static final String PARAM_FONTS_DEFAULT = ""; //$NON-NLS-1$

    public static final String PARAM_FONT_SIZES_DEFAULT = ""; //$NON-NLS-1$

    public static final String PARAM_SOURCE_DEFAULT = "false"; //$NON-NLS-1$

    private static final String PARAM_COLORS_DEFAULT = "";

    private static final String PARAM_LISTS_DEFAULT = "true";

    private static final String PARAM_ALIGNMENT_DEFAULT = "false";

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
     * @see info.magnolia.cms.gui.dialog.DialogControl#init(HttpServletRequest, HttpServletResponse, Content, Content)
     */
    public void init(HttpServletRequest request, HttpServletResponse response, Content websiteNode, Content configNode)
        throws RepositoryException {
        super.init(request, response, websiteNode, configNode);
    }

    /**
     * @see info.magnolia.cms.gui.dialog.DialogControl#drawHtml(Writer)
     */
    public void drawHtml(Writer out) throws IOException {
        // get the config values
        String jsInitFile = this.getConfigValue(PARAM_JS_INIT_FILE, PARAM_JS_INIT_FILE_DEFAULT);
        String customConfigurationPath = this.getConfigValue(PARAM_CUSTOM_CONFIGURATION_PATH, this.getConfigValue(
            "customConfigurationPath",
            PARAM_CUSTOM_CONFIGURATION_PATH_DEFAULT));
        String height = this.getConfigValue(PARAM_HEIGHT, PARAM_HEIGHT_DEFAULT);

        this.drawHtmlPre(out);

        // load the script onece: if there are multiple instances
        if (getRequest().getAttribute(ATTRIBUTE_FCKED_LOADED) == null) {
            out.write("<script type=\"text/javascript\" src=\"" //$NON-NLS-1$
                + this.getRequest().getContextPath()
                + "/.resources/fckeditor/fckeditor.js\"></script>"); //$NON-NLS-1$
            getRequest().setAttribute(ATTRIBUTE_FCKED_LOADED, "true"); //$NON-NLS-1$
        }

        String id = getName();

        if (id == null) {
            log.error("Missing id for fckEditor instance"); //$NON-NLS-1$
        }

        String var = getVarName();
        String value = convertToView(getValue());
        out.write("<script type=\"text/javascript\">"); //$NON-NLS-1$

        // make the configuration accessible to the config javascript
        writeMgnlFCKConfig(out, id);

        out.write("var " + var + " = null;"); //$NON-NLS-1$ //$NON-NLS-2$
        out.write("fckInstance = new FCKeditor( '" + id + "' );"); //$NON-NLS-1$ //$NON-NLS-2$
        out.write("fckInstance.Value = '" + escapeJsValue(value) + "';"); //$NON-NLS-1$ //$NON-NLS-2$
        out.write("fckInstance.BasePath = '" + this.getRequest().getContextPath() + FCKEDIT_PATH + "';"); //$NON-NLS-1$ //$NON-NLS-2$

        if (StringUtils.isNotEmpty(height)) {
            out.write("fckInstance.Height = '" + this.getConfigValue("height") + "';"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        }

        // now set the custom configuration path
        if (StringUtils.isNotEmpty(customConfigurationPath)) {
            out.write("fckInstance.Config['CustomConfigurationsPath'] = '" //$NON-NLS-1$
                + this.getRequest().getContextPath()
                + customConfigurationPath
                + "';"); //$NON-NLS-1$
        }

        // here we pass the parameters to the custom configuration file --> via
        // javascript

        // start the initfile
        if (jsInitFile.length() > 0) {
            out.write("</script>"); //$NON-NLS-1$
            out.write("<script type=\"text/javascript\" src=\"" //$NON-NLS-1$
                + this.getRequest().getContextPath()
                + jsInitFile
                + "\"></script>\n"); //$NON-NLS-1$
            out.write("<script type=\"text/javascript\">"); //$NON-NLS-1$
        }

        // finaly create the editor
        out.write("fckInstance.Create();"); //$NON-NLS-1$
        out.write(var + " = fckInstance;"); //$NON-NLS-1$
        out.write("</script>"); //$NON-NLS-1$

        // write the saveInfo for the writing back to the repository
        out.write("<input type='hidden' name='mgnlSaveInfo' value='" //$NON-NLS-1$
            + id
            + ",String," //$NON-NLS-1$
            + ControlImpl.VALUETYPE_SINGLE
            + "," //$NON-NLS-1$
            + ControlImpl.RICHEDIT_FCK
            + "," //$NON-NLS-1$
            + ControlImpl.ENCODING_NO
            + "' />"); //$NON-NLS-1$

        this.drawHtmlPost(out);
    }

    private void writeMgnlFCKConfig(Writer out, String id) throws IOException {
        String css = this.getConfigValue(PARAM_CSS, PARAM_CSS_DEFAULT);
        String fonts = this.getConfigValue(PARAM_FONTS, PARAM_FONTS_DEFAULT);
        String fontSizes = this.getConfigValue(PARAM_FONT_SIZES, PARAM_FONT_SIZES_DEFAULT);
        String colors = this.getConfigValue(PARAM_COLORS, PARAM_COLORS_DEFAULT);
        String styles = this.getConfigValue(PARAM_STYLES, PARAM_STYLES_DEFAULT);
        String templates = this.getConfigValue(PARAM_TEMPLATES, PARAM_TEMPLATES_DEFAULT);

        String lists = this.getConfigValue(PARAM_LISTS, PARAM_LISTS_DEFAULT);
        String alignment = this.getConfigValue(PARAM_ALIGNMENT, PARAM_ALIGNMENT_DEFAULT);
        String tables = this.getConfigValue(PARAM_TABLES, PARAM_TABLES_DEFAULT);
        String images = this.getConfigValue(PARAM_IMAGES, PARAM_IMAGES_DEFAULT);
        String source = this.getConfigValue(PARAM_SOURCE, PARAM_SOURCE_DEFAULT);

        // create the the holder of the editors configs if not yet done
        out.write("if( window.MgnlFCKConfigs == null)\n");
        out.write("    window.MgnlFCKConfigs = new Object();\n");

        // add the config for this editor

        out.write("MgnlFCKConfigs." + id + " = new Object();\n");
        // string values
        out.write("MgnlFCKConfigs." + id + ".language = '" + MgnlContext.getUser().getLanguage() + "';\n");
        out.write("MgnlFCKConfigs." + id + ".contextPath = '" + getRequest().getContextPath() + "';\n");

        out.write("MgnlFCKConfigs." + id + ".repository = '" + getTopParent().getConfigValue("repository") + "';\n");
        out.write("MgnlFCKConfigs." + id + ".path = '" + getTopParent().getConfigValue("path") + "';\n");
        out.write("MgnlFCKConfigs."
            + id
            + ".nodeCollection = '"
            + getTopParent().getConfigValue("nodeCollection")
            + "';\n");
        out.write("MgnlFCKConfigs." + id + ".node = '" + getTopParent().getConfigValue("node") + "';\n");

        out.write("MgnlFCKConfigs." + id + ".css = '" + css + "';\n");
        out.write("MgnlFCKConfigs." + id + ".fonts = '" + fonts + "';\n");
        out.write("MgnlFCKConfigs." + id + ".fontSizes = '" + fontSizes + "';\n");
        out.write("MgnlFCKConfigs." + id + ".colors = '" + colors + "';\n");
        out.write("MgnlFCKConfigs." + id + ".styles = '" + styles + "';\n");
        out.write("MgnlFCKConfigs." + id + ".templates = '" + templates + "';\n");

        // boolean values
        out.write("MgnlFCKConfigs." + id + ".lists = " + lists + ";\n");
        out.write("MgnlFCKConfigs." + id + ".alignment = " + alignment + ";\n");
        out.write("MgnlFCKConfigs." + id + ".tables = " + tables + ";\n");
        out.write("MgnlFCKConfigs." + id + ".images = " + images + ";\n");
        out.write("MgnlFCKConfigs." + id + ".source = " + source + ";\n");
    }

    /**
     * @param value
     * @return
     */
    protected String convertToView(String value) {
        if (value != null) {
            value = value.replaceAll("\r\n", "<br />"); //$NON-NLS-1$ //$NON-NLS-2$
            value = value.replaceAll("\n", "<br />"); //$NON-NLS-1$ //$NON-NLS-2$

            // we have to add the context path for images and files but not for pages!
            value = LinkUtil.convertUUIDsToLinks(value, new AbsolutePathTransformer(true, true, false){
                public String transform(UUIDLink uuidLink) {
                    // not a link to a binary
                    if(uuidLink.getNodeData() == null){
                        setAddContextPath(false);
                    }
                    String link = super.transform(uuidLink);
                    setAddContextPath(true);
                    return link;
                }
            });

            Pattern imagePattern = Pattern.compile("(<(a|img)[^>]+(src|href)[ ]*=[ ]*\")([^\"]*)(\"[^>]*>)"); //$NON-NLS-1$

            Matcher matcher = imagePattern.matcher(value);
            StringBuffer res = new StringBuffer();
            while (matcher.find()) {
                String src = matcher.group(4);

                // process only internal and relative links
                if (LinkHelper.isInternalRelativeLink(src)) {
                    String link = this.getRequest().getContextPath()
                        + this.getTopParent().getConfigValue("path")
                        + "/"
                        + StringUtils.substringAfter(src, "/");

                    matcher.appendReplacement(res, "$1" + link + "$5"); //$NON-NLS-1$
                }
            }
            matcher.appendTail(res);
            return res.toString();
        }

        return StringUtils.EMPTY;
    }

    /**
     * Escapes the given String to make it javascript friendly.
     * (escaping single quotes, double quotes, new lines, backslashes, ...)
     * @param src
     * @return escaped js String
     * @see StringEscapeUtils#escapeJavaScript(String)
     */
    protected String escapeJsValue(String src) {
        return StringEscapeUtils.escapeJavaScript(src);
    }
}