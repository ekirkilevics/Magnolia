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

import java.io.IOException;
import java.util.Hashtable;
import java.util.Map;

import javax.servlet.jsp.JspWriter;

import org.apache.log4j.Logger;


/**
 * @author Vinzenz Wyser
 * @version 2.0
 */
public class DialogBox extends DialogSuper {

    public static final int BOXTYPE_2COLS = 0;

    public static final int BOXTYPE_1COL = 1;

    /**
     * Logger.
     */
    private static Logger log = Logger.getLogger(DialogBox.class);

    private Map iconExtensions = new Hashtable();

    private int boxType = BOXTYPE_2COLS;

    public void setBoxType(int i) {
        this.boxType = i;
    }

    public int getBoxType() {
        String configBoxType = this.getConfigValue("boxType");
        if (configBoxType.equals("1Col")) {
            return BOXTYPE_1COL;
        }
        return this.boxType;

    }

    public void drawHtmlPre(JspWriter out) throws IOException {
        if (this.getConfigValue("lineSemi", "false").equals("true")) {
            out.println(new DialogLine().getHtml(1, 1));
        }
        else if (this.getConfigValue("line", "true").equals("true")) {
            out.println(new DialogLine().getHtml());
        }
        out.println("<tr>");
        if (this.getBoxType() == BOXTYPE_2COLS) {
            out.println("<td width=\"1%\" class=\"" + CSSCLASS_BOXLABEL + "\">");
            out.println(this.getLabel());
            if (!this.getConfigValue("labelDescription").equals("")) {
                out.println("<div class=\""
                    + CSSCLASS_DESCRIPTION
                    + "\">"
                    + this.getConfigValue("labelDescription")
                    + "</div>");
            }
            out.println("</td>");
            String cssClass = CSSCLASS_BOXINPUT;
            if (this.getClass().getName().indexOf("DialogStatic") != -1
                || this.getClass().getName().indexOf("DialogButton") != -1) {
                cssClass = CSSCLASS_BOXLABEL;
            }
            out.println("<td width=\"100%\" class=\"" + cssClass + "\">");
        }
        else {
            out.println("<td width=\"100%\" colspan=\"2\" class=\"" + CSSCLASS_BOXLABEL + "\">");
            if (!this.getLabel().equals("")) {
                out.println("<div class=\"" + CSSCLASS_BOXLABEL + "\">" + this.getLabel() + "</div>");
            }
            if (!this.getConfigValue("labelDescription").equals("")) {
                out.println("<div class=\""
                    + CSSCLASS_DESCRIPTION
                    + "\">"
                    + this.getConfigValue("labelDescription")
                    + "</div>");
            }
        }
    }

    public void drawHtmlPost(JspWriter out) throws IOException {
        out.println(this.getHtmlDescription());
        out.println("</td></tr>");
    }

    public String getHtmlDescription() {
        String html = "";
        // use div to force a new line
        if (!this.getDescription().equals("")) {
            html = "<div class=\"" + CSSCLASS_DESCRIPTION + "\">" + this.getDescription() + "</div>";
        }
        return html;
    }

    public void initIconExtensions() {
        this.getIconExtensions().put("doc", "");
        this.getIconExtensions().put("eps", "");
        this.getIconExtensions().put("gif", "");
        this.getIconExtensions().put("jpg", "");
        this.getIconExtensions().put("jpeg", ICONS_PATH + "jpg.gif");
        this.getIconExtensions().put("pdf", "");
        this.getIconExtensions().put("ppt", "");
        this.getIconExtensions().put("tif", "");
        this.getIconExtensions().put("tiff", ICONS_PATH + "tif.gif");
        this.getIconExtensions().put("xls", "");
        this.getIconExtensions().put("zip", "");
    }

    public String getIconPath(String name) {
        // name might be name (e.g. "bla.gif") or extension (e.g. "gif")
        String iconPath = ICONS_PATH + ICONS_GENERAL;
        String ext = "";
        if (name.indexOf(".") != -1) {
            ext = name.substring(name.lastIndexOf(".") + 1).toLowerCase();
        }
        else {
            ext = name;
        }
        if (this.getIconExtensions().containsKey(ext)) {
            iconPath = (String) this.getIconExtensions().get(ext);
            if (iconPath.equals("")) {
                iconPath = ICONS_PATH + ext + ".gif";
            }
        }
        return iconPath;
    }

    public Map getIconExtensions() {
        return this.iconExtensions;
    }

    public void setIconExtensions(Map t) {
        this.iconExtensions = t;
    }

    public void setIconExtensions(String extension, String iconPath) {
        this.iconExtensions.put(extension, iconPath);
    }
}
