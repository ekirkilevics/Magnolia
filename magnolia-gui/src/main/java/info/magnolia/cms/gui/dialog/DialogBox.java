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

import info.magnolia.cms.gui.misc.CssConstants;

import java.io.IOException;
import java.io.Writer;
import java.util.Hashtable;
import java.util.Map;

import org.apache.commons.lang.StringUtils;


/**
 * @author Vinzenz Wyser
 * @version 2.0
 */
public class DialogBox extends DialogSuper {

    public static final int BOXTYPE_2COLS = 0;

    public static final int BOXTYPE_1COL = 1;

    public static final String ICONS_GENERAL = "general.gif"; //$NON-NLS-1$

    public static final String ICONS_FOLDER = "folder.gif"; //$NON-NLS-1$

    public static final String ICONS_PATH = "/.resources/file-icons/"; //$NON-NLS-1$

    private static Map iconExtensions = new Hashtable();

    private int boxType = BOXTYPE_2COLS;

    static {
        DialogBox.initIconExtensions();
    }

    /**
     * Empty constructor should only be used by DialogFactory.
     */
    protected DialogBox() {
    }

    public void setBoxType(int i) {
        this.boxType = i;
    }

    public int getBoxType() {
        String configBoxType = this.getConfigValue("boxType"); //$NON-NLS-1$
        if (configBoxType.equals("1Col")) { //$NON-NLS-1$
            return BOXTYPE_1COL;
        }
        return this.boxType;

    }

    public void drawHtmlPre(Writer out) throws IOException {
        if (this.getConfigValue("lineSemi", "false").equals("true")) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            out.write(new DialogLine().getHtml(1, 1));
        }
        else if (this.getConfigValue("line", "true").equals("true")) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            out.write(new DialogLine().getHtml());
        }
        out.write("<tr>"); //$NON-NLS-1$
        if (this.getBoxType() == BOXTYPE_2COLS) {
            out.write("<td width=\"1%\" class=\"" + CssConstants.CSSCLASS_BOXLABEL + "\">"); //$NON-NLS-1$ //$NON-NLS-2$
            // write the label
            out.write(this.getMessage(this.getLabel()));
            if (StringUtils.isNotEmpty(this.getConfigValue("labelDescription"))) { //$NON-NLS-1$
                String desc = this.getConfigValue("labelDescription"); //$NON-NLS-1$
                desc = this.getMessage(desc);
                out.write("<div class=\"" + CssConstants.CSSCLASS_DESCRIPTION + "\">" + desc + "</div>"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            }
            out.write("</td>"); //$NON-NLS-1$
            String cssClass = CssConstants.CSSCLASS_BOXINPUT;
            if (this.getClass().getName().indexOf("DialogStatic") != -1 //$NON-NLS-1$
                || this.getClass().getName().indexOf("DialogButton") != -1) { //$NON-NLS-1$
                cssClass = CssConstants.CSSCLASS_BOXLABEL;
            }
            out.write("<td width=\"100%\" class=\"" + cssClass + "\">"); //$NON-NLS-1$ //$NON-NLS-2$
        }
        else {
            out.write("<td width=\"100%\" colspan=\"2\" class=\"" + CssConstants.CSSCLASS_BOXLABEL + "\">"); //$NON-NLS-1$ //$NON-NLS-2$
            if (StringUtils.isNotEmpty(this.getLabel())) {
                out.write("<div class=\"" //$NON-NLS-1$
                    + CssConstants.CSSCLASS_BOXLABEL
                    + "\">" //$NON-NLS-1$
                    + this.getMessage(this.getLabel())
                    + "</div>"); //$NON-NLS-1$
            }
            if (StringUtils.isNotEmpty(this.getConfigValue("labelDescription"))) { //$NON-NLS-1$
                String desc = this.getConfigValue("labelDescription"); //$NON-NLS-1$
                out.write("<div class=\"" //$NON-NLS-1$
                    + CssConstants.CSSCLASS_DESCRIPTION
                    + "\">" //$NON-NLS-1$
                    + this.getMessage(desc)
                    + "</div>"); //$NON-NLS-1$
            }
        }
    }

    public void drawHtmlPost(Writer out) throws IOException {
        out.write(this.getHtmlDescription());
        out.write("</td></tr>"); //$NON-NLS-1$
    }

    public String getHtmlDescription() {

        // use div to force a new line
        if (StringUtils.isNotEmpty(this.getDescription())) {
            String desc = this.getDescription();
            desc = this.getMessage(desc);
            return "<div class=\"" + CssConstants.CSSCLASS_DESCRIPTION + "\">" + desc + "</div>"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        }

        return StringUtils.EMPTY;
    }

    public static void initIconExtensions() {
        getIconExtensions().put("doc", StringUtils.EMPTY); //$NON-NLS-1$
        getIconExtensions().put("eps", StringUtils.EMPTY); //$NON-NLS-1$
        getIconExtensions().put("gif", StringUtils.EMPTY); //$NON-NLS-1$
        getIconExtensions().put("jpg", StringUtils.EMPTY); //$NON-NLS-1$
        getIconExtensions().put("jpeg", ICONS_PATH + "jpg.gif"); //$NON-NLS-1$ //$NON-NLS-2$
        getIconExtensions().put("pdf", StringUtils.EMPTY); //$NON-NLS-1$
        getIconExtensions().put("ppt", StringUtils.EMPTY); //$NON-NLS-1$
        getIconExtensions().put("tif", StringUtils.EMPTY); //$NON-NLS-1$
        getIconExtensions().put("tiff", ICONS_PATH + "tif.gif"); //$NON-NLS-1$ //$NON-NLS-2$
        getIconExtensions().put("xls", StringUtils.EMPTY); //$NON-NLS-1$
        getIconExtensions().put("zip", StringUtils.EMPTY); //$NON-NLS-1$
    }

    public String getIconPath(String name) {
        // name might be name (e.g. "bla.gif") or extension (e.g. "gif")
        String iconPath = ICONS_PATH + ICONS_GENERAL;
        String ext;
        if (name.indexOf(".") != -1) { //$NON-NLS-1$
            ext = name.substring(name.lastIndexOf(".") + 1).toLowerCase(); //$NON-NLS-1$
        }
        else {
            ext = name;
        }
        if (getIconExtensions().containsKey(ext)) {
            iconPath = (String) getIconExtensions().get(ext);
            if (StringUtils.isEmpty(iconPath)) {
                iconPath = ICONS_PATH + ext + ".gif"; //$NON-NLS-1$
            }
        }
        return iconPath;
    }

    public static Map getIconExtensions() {
        return iconExtensions;
    }

    public static void setIconExtensions(Map t) {
        iconExtensions = t;
    }

    public void setIconExtensions(String extension, String iconPath) {
        iconExtensions.put(extension, iconPath);
    }
}