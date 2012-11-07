/**
 * This file Copyright (c) 2003-2012 Magnolia International
 * Ltd.  (http://www.magnolia-cms.com). All rights reserved.
 *
 *
 * This file is dual-licensed under both the Magnolia
 * Network Agreement and the GNU General Public License.
 * You may elect to use one or the other of these licenses.
 *
 * This file is distributed in the hope that it will be
 * useful, but AS-IS and WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE, TITLE, or NONINFRINGEMENT.
 * Redistribution, except as permitted by whichever of the GPL
 * or MNA you select, is prohibited.
 *
 * 1. For the GPL license (GPL), you can redistribute and/or
 * modify this file under the terms of the GNU General
 * Public License, Version 3, as published by the Free Software
 * Foundation.  You should have received a copy of the GNU
 * General Public License, Version 3 along with this program;
 * if not, write to the Free Software Foundation, Inc., 51
 * Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * 2. For the Magnolia Network Agreement (MNA), this file
 * and the accompanying materials are made available under the
 * terms of the MNA which accompanies this distribution, and
 * is available at http://www.magnolia-cms.com/mna.html
 *
 * Any modifications to this file must keep this entire header
 * intact.
 *
 */
package info.magnolia.cms.gui.dialog;

import info.magnolia.cms.gui.control.Button;
import info.magnolia.cms.gui.control.ButtonSet;
import info.magnolia.cms.gui.control.ControlImpl;
import info.magnolia.cms.gui.control.Hidden;
import info.magnolia.cms.gui.misc.CssConstants;
import info.magnolia.cms.gui.misc.Sources;
import info.magnolia.cms.i18n.Messages;
import info.magnolia.cms.i18n.MessagesManager;
import info.magnolia.cms.util.AlertUtil;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.jcr.RepositoryException;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;


/**
 * Creates a dialog.
 * @version 2.0
 */
public class Dialog extends DialogControlImpl {

    public static final String DIALOGSIZE_NORMAL_WIDTH = "800";

    public static final String DIALOGSIZE_NORMAL_HEIGHT = "650";

    public static final String DIALOGSIZE_SLIM_WIDTH = "500";

    public static final String DIALOGSIZE_SLIM_HEIGHT = "600";

    private String callbackJavascript = "opener.document.location.reload();window.close();";

    private List javascriptSources = new ArrayList();

    private List cssSources = new ArrayList();

    private String action;

    public void setCallbackJavascript(String s) {
        this.callbackJavascript = s;
    }

    public String getCallbackJavascript() {
        return this.callbackJavascript;
    }

    public void setAction(String s) {
        this.action = s;
    }

    public String getAction() {
        if (this.action == null) {
            return this.getRequest().getRequestURI();
        }

        return this.action;
    }

    public void setJavascriptSources(String s) {
        this.getJavascriptSources().add(s);
    }

    public List getJavascriptSources() {
        return this.javascriptSources;
    }

    public void drawJavascriptSources(Writer out) throws IOException {
        Iterator it = this.getJavascriptSources().iterator();
        while (it.hasNext()) {
            out.write("<script type=\"text/javascript\" src=\"" + it.next() + "\"></script>");
        }
    }

    public void setCssSources(String s) {
        this.getCssSources().add(s);
    }

    public List getCssSources() {
        return this.cssSources;
    }

    public void drawCssSources(Writer out) throws IOException {
        Iterator it = this.getCssSources().iterator();
        while (it.hasNext()) {
            out.write("<link rel=\"stylesheet\" type=\"text/css\" href=\"" + it.next() + "\"/>");
        }
    }

    public DialogTab addTab() {
        return this.addTab(StringUtils.EMPTY);
    }

    public DialogTab addTab(String label) {
        DialogTab tab = new DialogTab();
        try {
            tab.init(getRequest(), getResponse(), null, null);
        }
        catch (RepositoryException e) {
            // ignore
        }
        tab.setLabel(label);
        this.getSubs().add(tab);
        return tab;
    }

    public DialogTab getTab(int i) {
        return (DialogTab) this.getSubs().get(i);
    }

    @Override
    public void drawHtmlPreSubs(Writer out) throws IOException {

        out.write("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" ");
        out.write(" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">");

        out.write("<html>");
        out.write("<head>");
        this.drawHtmlPreSubsHead(out);
        // alert if a message was set
        if (AlertUtil.isMessageSet()) {
            out.write("<script type=\"text/javascript\">mgnl.util.DHTMLUtil.addOnLoad(function(){alert('"
                + StringEscapeUtils.escapeJavaScript(AlertUtil.getMessage())
                + "');})</script>");
        }
        out.write("<script type=\"text/javascript\">mgnl.util.DHTMLUtil.addOnLoad(mgnlDialogInit);</script>");

        out.write("</head>\n");
        out.write("<body class=\"mgnlDialogBody\">\n");
        this.drawHtmlPreSubsForm(out);
        this.drawHtmlPreSubsTabSet(out);
    }

    protected void drawHtmlPreSubsHead(Writer out) throws IOException {
        out.write("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\"/>\n"); // kupu
        //
        out.write("<title>"
            + this.getMessage(this.getConfigValue("label", MessagesManager.get("dialog.editTitle")))
            + "</title>\n");
        out.write(new Sources(this.getRequest().getContextPath()).getHtmlJs());
        out.write(new Sources(this.getRequest().getContextPath()).getHtmlCss());

        //using jQuery.ready() function to call dialog resizing functions only when DOM is ready. See MAGNOLIA-3846.
        out.write("<script type=\"text/javascript\" src=\"");
        out.write(this.getRequest().getContextPath());
        out.write("/.resources/js/jquery/jquery-latest.min.js\"></script>\n");
        out.write("<script type=\"text/javascript\">\n");
        out.write("jQuery.noConflict();\n");
        out.write("jQuery(document).ready(function($) {\n");
        out.write("  window.onresize = eventHandlerOnResize;\n");
        out.write("  window.resizeTo("
                + this.getConfigValue("width", DIALOGSIZE_NORMAL_WIDTH)
                + ","
                + this.getConfigValue("height", DIALOGSIZE_NORMAL_HEIGHT)
                + ");\n");
        out.write("  mgnlDialogResizeTabs();\n");
        out.write("  mgnlDialogShiftTab('" + this.getId() + "',false,0)\n");
        out.write("});\n");
        out.write("</script>\n");

        this.drawJavascriptSources(out);
        this.drawCssSources(out);
    }

    protected void drawHtmlPreSubsForm(Writer out) throws IOException {
        out.write("<form action=\""
            + this.getAction()
            + "\" id=\"mgnlFormMain\" method=\"post\" enctype=\"multipart/form-data\"><div>\n");
        out.write(new Hidden("mgnlDialog", this.getConfigValue("dialog"), false).getHtml());
        out.write(new Hidden("mgnlRepository", this.getConfigValue("repository"), false).getHtml());
        out.write(new Hidden("mgnlPath", this.getConfigValue("path"), false).getHtml());
        out.write(new Hidden("mgnlNodeCollection", this.getConfigValue("nodeCollection"), false).getHtml());
        out.write(new Hidden("mgnlNode", this.getConfigValue("node"), false).getHtml());
        out.write(new Hidden("mgnlLocale", this.getConfigValue("locale"), false).getHtml());
        out.write(new Hidden("mgnlJsCallback", this.getCallbackJavascript(), false).getHtml());
        out.write(new Hidden("mgnlRichE", this.getConfigValue("richE"), false).getHtml());
        out.write(new Hidden("mgnlRichEPaste", this.getConfigValue("richEPaste"), false).getHtml());
        if (this.getConfigValue("paragraph").indexOf(",") == -1) {
            out.write(new Hidden("mgnlParagraph", this.getConfigValue("paragraph"), false).getHtml());
        } // else multiple paragraph selection -> radios for selection
        if (StringUtils.isNotEmpty(this.getConfigValue("collectionNodeCreationItemType"))) {
            out.write(new Hidden("mgnlCollectionNodeCreationItemType", this.getConfigValue("collectionNodeCreationItemType"), false).getHtml());
        }
        if (StringUtils.isNotEmpty(this.getConfigValue("creationItemType"))) {
            out.write(new Hidden("mgnlCreationItemType", this.getConfigValue("creationItemType"), false).getHtml());
        }
    }

    protected void drawHtmlPreSubsTabSet(Writer out) throws IOException {
        String id = this.getId();
        out.write("<script type=\"text/javascript\">");
        out.write("mgnlControlSets['" + id + "']=new Object();");
        out.write("mgnlControlSets['" + id + "'].items=new Array();");
        out.write("mgnlControlSets['" + id + "'].resize=true;");
        out.write("</script>\n");
    }

    @Override
    public void drawHtmlPostSubs(Writer out) throws IOException {
        this.drawHtmlPostSubsTabSet(out);
        this.drawHtmlPostSubsButtons(out);
        out.write("</div></form></body></html>");
        //out.write("\n<script type=\"text/javascript\">\n");
        //calling mgnlDialogResizeTabs() as late as possible. See MAGNOLIA-3846.
        //out.write("mgnlDialogResizeTabs();");
        //out.write("</script>\n");
        //out.write("</body></html>");
    }

    protected void drawHtmlPostSubsTabSet(Writer out) throws IOException {
        // TabSet stuff
        String id = this.getId();
        out.write("<div class=\"" + CssConstants.CSSCLASS_TABSETBUTTONBAR + "\">\n");
        out.write("<table cellpadding=\"0\" cellspacing=\"0\" border=\"0\" width=\"100%\"><tr><td  class=\""
            + CssConstants.CSSCLASS_TABSETBUTTONBAR
            + "\">");
        if (this.getOptions().size() != 0) {
            ButtonSet control = new ButtonSet();
            ((Button) this.getOptions().get(0)).setState(ControlImpl.BUTTONSTATE_PUSHED);
            control.setButtons(this.getOptions());
            control.setName(this.getId());
            control.setSaveInfo(false);
            control.setButtonType(ControlImpl.BUTTONTYPE_PUSHBUTTON);
            out.write(control.getHtml());
        }
        out.write("</td></tr></table>\n</div>\n");
        //out.write("<script type=\"text/javascript\">");
        //out.write("mgnlDialogShiftTab('" + id + "',false,0)");
        //out.write("</script>\n");
        // end TabSet stuff
    }

    protected void drawHtmlPostSubsButtons(Writer out) throws IOException {
        Messages msgs = MessagesManager.getMessages();

        out.write("<div class=\"" + CssConstants.CSSCLASS_TABSETSAVEBAR + "\">\n");

        Button save = new Button();
        String saveOnclick = this.getConfigValue("saveOnclick", "mgnlDialogFormSubmit();");
        String saveLabel = this.getConfigValue("saveLabel", msgs.get("buttons.save"));
        if (StringUtils.isNotEmpty(saveOnclick) && StringUtils.isNotEmpty(saveLabel)) {
            save.setId("mgnlSaveButton");
            save.setOnclick(saveOnclick);
            save.setLabel(saveLabel);
            out.write(save.getHtml());
        }
        Button cancel = new Button();
        cancel.setId("mgnlCancelButton");
        cancel.setOnclick(this.getConfigValue("cancelOnclick", "window.close();"));
        cancel.setLabel(this.getConfigValue("cancelLabel", msgs.get("buttons.cancel")));
        out.write(cancel.getHtml());

        out.write("</div>\n");
    }
}
