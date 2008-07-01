/**
 * This file Copyright (c) 2003-2008 Magnolia International
 * Ltd.  (http://www.magnolia.info). All rights reserved.
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
 * is available at http://www.magnolia.info/mna.html
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
 * @author Vinzenz Wyser
 * @version 2.0
 */
public class Dialog extends DialogControlImpl {

    public static final String DIALOGSIZE_NORMAL_WIDTH = "800"; //$NON-NLS-1$

    public static final String DIALOGSIZE_NORMAL_HEIGHT = "650"; //$NON-NLS-1$

    public static final String DIALOGSIZE_SLIM_WIDTH = "500"; //$NON-NLS-1$

    public static final String DIALOGSIZE_SLIM_HEIGHT = "600"; //$NON-NLS-1$

    private String callbackJavascript = "opener.document.location.reload();window.close();"; //$NON-NLS-1$

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
            out.write("<script type=\"text/javascript\" src=\"" + it.next() + "\"></script>"); //$NON-NLS-1$ //$NON-NLS-2$
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
            out.write("<link rel=\"stylesheet\" type=\"text/css\" href=\"" + it.next() + "\"/>"); //$NON-NLS-1$ //$NON-NLS-2$
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

    public void drawHtmlPreSubs(Writer out) throws IOException {

        out.write("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" ");
        out.write(" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">");

        out.write("<html>"); //$NON-NLS-1$
        out.write("<head>"); //$NON-NLS-1$
        this.drawHtmlPreSubsHead(out);
        // alert if a message was set
        if (AlertUtil.isMessageSet()) {
            out.write("<script type=\"text/javascript\">mgnl.util.DHTMLUtil.addOnLoad(function(){alert('"
                + StringEscapeUtils.escapeJavaScript(AlertUtil.getMessage())
                + "');})</script>");
        }
        out.write("<script type=\"text/javascript\">mgnl.util.DHTMLUtil.addOnLoad(mgnlDialogInit);</script>");

        out.write("</head>\n"); //$NON-NLS-1$
        out.write("<body class=\"mgnlDialogBody\">\n"); //$NON-NLS-1$
        this.drawHtmlPreSubsForm(out);
        this.drawHtmlPreSubsTabSet(out);
    }

    protected void drawHtmlPreSubsHead(Writer out) throws IOException {
        out.write("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\"/>\n"); // kupu
        // //$NON-NLS-1$
        out.write("<title>" //$NON-NLS-1$
            + this.getMessage(this.getConfigValue("label", MessagesManager.get("dialog.editTitle"))) //$NON-NLS-1$ //$NON-NLS-2$
            + "</title>\n"); //$NON-NLS-1$
        out.write(new Sources(this.getRequest().getContextPath()).getHtmlJs());
        out.write(new Sources(this.getRequest().getContextPath()).getHtmlCss());
        out.write("<script type=\"text/javascript\">\n"); //$NON-NLS-1$

        out.write("window.onresize = eventHandlerOnResize;\n"); //$NON-NLS-1$
        out.write("window.resizeTo(" //$NON-NLS-1$
            + this.getConfigValue("width", DIALOGSIZE_NORMAL_WIDTH) //$NON-NLS-1$
            + "," //$NON-NLS-1$
            + this.getConfigValue("height", DIALOGSIZE_NORMAL_HEIGHT) //$NON-NLS-1$
            + ");\n"); //$NON-NLS-1$
        out.write("</script>\n"); //$NON-NLS-1$

        this.drawJavascriptSources(out);
        this.drawCssSources(out);
    }

    protected void drawHtmlPreSubsForm(Writer out) throws IOException {
        out.write("<form action=\"" //$NON-NLS-1$
            + this.getAction()
            + "\" id=\"mgnlFormMain\" method=\"post\" enctype=\"multipart/form-data\"><div>\n"); //$NON-NLS-1$
        out.write(new Hidden("mgnlDialog", this.getConfigValue("dialog"), false).getHtml()); //$NON-NLS-1$ //$NON-NLS-2$
        out.write(new Hidden("mgnlRepository", this.getConfigValue("repository"), false).getHtml()); //$NON-NLS-1$ //$NON-NLS-2$
        out.write(new Hidden("mgnlPath", this.getConfigValue("path"), false).getHtml()); //$NON-NLS-1$ //$NON-NLS-2$
        out.write(new Hidden("mgnlNodeCollection", this.getConfigValue("nodeCollection"), false).getHtml()); //$NON-NLS-1$ //$NON-NLS-2$
        out.write(new Hidden("mgnlNode", this.getConfigValue("node"), false).getHtml()); //$NON-NLS-1$ //$NON-NLS-2$
        out.write(new Hidden("mgnlJsCallback", this.getCallbackJavascript(), false).getHtml()); //$NON-NLS-1$
        out.write(new Hidden("mgnlRichE", this.getConfigValue("richE"), false).getHtml()); //$NON-NLS-1$ //$NON-NLS-2$
        out.write(new Hidden("mgnlRichEPaste", this.getConfigValue("richEPaste"), false).getHtml()); //$NON-NLS-1$ //$NON-NLS-2$
        if (this.getConfigValue("paragraph").indexOf(",") == -1) { //$NON-NLS-1$ //$NON-NLS-2$
            out.write(new Hidden("mgnlParagraph", this.getConfigValue("paragraph"), false).getHtml()); //$NON-NLS-1$ //$NON-NLS-2$
        } // else multiple paragraph selection -> radios for selection
    }

    protected void drawHtmlPreSubsTabSet(Writer out) throws IOException {
        String id = this.getId();
        out.write("<script type=\"text/javascript\">"); //$NON-NLS-1$
        out.write("mgnlControlSets['" + id + "']=new Object();"); //$NON-NLS-1$ //$NON-NLS-2$
        out.write("mgnlControlSets['" + id + "'].items=new Array();"); //$NON-NLS-1$ //$NON-NLS-2$
        out.write("mgnlControlSets['" + id + "'].resize=true;"); //$NON-NLS-1$ //$NON-NLS-2$
        out.write("</script>\n"); //$NON-NLS-1$
    }

    public void drawHtmlPostSubs(Writer out) throws IOException {
        this.drawHtmlPostSubsTabSet(out);
        this.drawHtmlPostSubsButtons(out);

        out.write("</div></form></body></html>"); //$NON-NLS-1$
    }

    protected void drawHtmlPostSubsTabSet(Writer out) throws IOException {
        // TabSet stuff
        String id = this.getId();
        out.write("<div class=\"" + CssConstants.CSSCLASS_TABSETBUTTONBAR + "\">\n"); //$NON-NLS-1$ //$NON-NLS-2$
        out.write("<table cellpadding=\"0\" cellspacing=\"0\" border=\"0\" width=\"100%\"><tr><td  class=\"" //$NON-NLS-1$
            + CssConstants.CSSCLASS_TABSETBUTTONBAR
            + "\">"); //$NON-NLS-1$
        if (this.getOptions().size() != 0) {
            ButtonSet control = new ButtonSet();
            ((Button) this.getOptions().get(0)).setState(ControlImpl.BUTTONSTATE_PUSHED);
            control.setButtons(this.getOptions());
            control.setName(this.getId());
            control.setSaveInfo(false);
            control.setButtonType(ControlImpl.BUTTONTYPE_PUSHBUTTON);
            out.write(control.getHtml());
        }
        out.write("</td></tr></table>\n</div>\n"); //$NON-NLS-1$
        out.write("<script type=\"text/javascript\">"); //$NON-NLS-1$
        out.write("mgnlDialogResizeTabs('" + id + "');"); //$NON-NLS-1$ //$NON-NLS-2$
        out.write("mgnlDialogShiftTab('" + id + "',false,0)"); //$NON-NLS-1$ //$NON-NLS-2$
        out.write("</script>\n"); //$NON-NLS-1$
        // end TabSet stuff
    }

    protected void drawHtmlPostSubsButtons(Writer out) throws IOException {
        Messages msgs = MessagesManager.getMessages();

        out.write("<div class=\"" + CssConstants.CSSCLASS_TABSETSAVEBAR + "\">\n"); //$NON-NLS-1$ //$NON-NLS-2$

        Button save = new Button();
        String saveOnclick = this.getConfigValue("saveOnclick", "mgnlDialogFormSubmit();");
        String saveLabel = this.getConfigValue("saveLabel", msgs.get("buttons.save"));
        if (StringUtils.isNotEmpty(saveOnclick) && StringUtils.isNotEmpty("saveLabel")) {
            save.setOnclick(saveOnclick);
            save.setLabel(saveLabel);
            out.write(save.getHtml());
        }
        Button cancel = new Button();
        cancel.setOnclick(this.getConfigValue("cancelOnclick", "window.close();")); //$NON-NLS-1$ //$NON-NLS-2$
        cancel.setLabel(this.getConfigValue("cancelLabel", msgs.get("buttons.cancel"))); //$NON-NLS-1$ //$NON-NLS-2$
        out.write(cancel.getHtml());

        out.write("</div>\n"); //$NON-NLS-1$
    }
}
