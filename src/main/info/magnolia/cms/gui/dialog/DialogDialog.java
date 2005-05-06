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

import info.magnolia.cms.gui.control.Button;
import info.magnolia.cms.gui.control.ButtonSet;
import info.magnolia.cms.gui.control.ControlSuper;
import info.magnolia.cms.gui.control.Hidden;
import info.magnolia.cms.gui.misc.CssConstants;
import info.magnolia.cms.gui.misc.Sources;
import info.magnolia.cms.i18n.Messages;
import info.magnolia.cms.i18n.MessagesManager;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;


/**
 * @author Vinzenz Wyser
 * @version 2.0
 */
public class DialogDialog extends DialogSuper {

    public static final String DIALOGSIZE_NORMAL_WIDTH = "800";

    public static final String DIALOGSIZE_NORMAL_HEIGHT = "650";

    public static final String DIALOGSIZE_SLIM_WIDTH = "500";

    public static final String DIALOGSIZE_SLIM_HEIGHT = "600";

    /**
     * Logger.
     */
    private static Logger log = Logger.getLogger(DialogDialog.class);

    private String callbackJavascript = "opener.document.location.reload();window.close();";

    private List javascriptSources = new ArrayList();

    private List cssSources = new ArrayList();

    private String action;

    /**
     * Empty constructor should only be used by DialogFactory.
     */
    protected DialogDialog() {
    }

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
            out.write("<link rel=\"stylesheet\" type=\"text/css\" href=\"" + it.next() + "\">");
        }
    }

    public DialogTab addTab() {
        return this.addTab("");
    }

    public DialogTab addTab(String label) {
        DialogTab tab = new DialogTab();
        tab.setLabel(label);
        this.getSubs().add(tab);
        return tab;
    }

    public DialogTab getTab(int i) {
        return (DialogTab) this.getSubs().get(i);
    }

    public void drawHtmlPreSubs(Writer out) throws IOException {
        
        // @todo fix html and add a good doctype. At the moment dialogs don't work in standard compliant mode
        // out.write("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" ");
        // out.write(" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">");

        out.write("<html>");
        out.write("<head>");
        out.write("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\"/>"); // kupu
        out.write("<script type=\"text/javascript\">");
        out.write("window.resizeTo("
            + this.getConfigValue("width", DIALOGSIZE_NORMAL_WIDTH)
            + ","
            + this.getConfigValue("height", DIALOGSIZE_NORMAL_HEIGHT)
            + ");");
        out.write("</script>");
        out.write("<title>" + this.getConfigValue("label", MessagesManager.get(getRequest(),"dialog.editTitle")) + "</title>");
        out.write(new Sources(this.getRequest().getContextPath()).getHtmlJs());
        out.write(new Sources(this.getRequest().getContextPath()).getHtmlCss());
        out.write(new Sources(this.getRequest().getContextPath()).getHtmlRichEdit());
        out.write("<script type=\"text/javascript\">");
        out.write("var mgnlRichEditors=new Array();"); // will be extended at each richEdit control
        out.write("var kupu = null;");
        out.write("var kupuui = null;");
        out.write("</script>");
        this.drawJavascriptSources(out);
        this.drawCssSources(out);
        out.write("</head>");
        out.write("<body class=\"mgnlDialogBody\" onload=\"mgnlDialogInit();\">");
        out.write("<form action=\""
            + this.getAction()
            + "\" name=\"mgnlFormMain\" method=\"post\" enctype=\"multipart/form-data\">");
        out.write(new Hidden("mgnlDialog", this.getConfigValue("dialog"), false).getHtml());
        out.write(new Hidden("mgnlRepository", this.getConfigValue("repository"), false).getHtml());
        out.write(new Hidden("mgnlPath", this.getConfigValue("path"), false).getHtml());
        out.write(new Hidden("mgnlNodeCollection", this.getConfigValue("nodeCollection"), false).getHtml());
        out.write(new Hidden("mgnlNode", this.getConfigValue("node"), false).getHtml());
        out.write(new Hidden("mgnlJsCallback", this.getCallbackJavascript(), false).getHtml());
        out.write(new Hidden("mgnlRichE", this.getConfigValue("richE"), false).getHtml());
        out.write(new Hidden("mgnlRichEPaste", this.getConfigValue("richEPaste"), false).getHtml());
        if (this.getConfigValue("paragraph").indexOf(",") == -1) {
            out.write(new Hidden("mgnlParagraph", this.getConfigValue("paragraph"), false).getHtml());
        } // else multiple paragraph selection -> radios for selection
        // TabSet stuff
        String id = this.getId();
        out.write("<script type=\"text/javascript\">");
        out.write("mgnlControlSets['" + id + "']=new Object();");
        out.write("mgnlControlSets['" + id + "'].items=new Array();");
        out.write("mgnlControlSets['" + id + "'].resize=true;");
        out.write("</script>");
        // end TabSet stuff
    }

    public void drawHtmlPostSubs(Writer out) throws IOException {
        Messages msgs = MessagesManager.getMessages(getRequest());
        
        // TabSet stuff
        String id = this.getId();
        out.write("<div class=\"" + CssConstants.CSSCLASS_TABSETBUTTONBAR + "\">");
        out.write("<table cellpadding=\"0\" cellspacing=\"0\" border=\"0\" width=\"100%\"><tr><td  class=\""
            + CssConstants.CSSCLASS_TABSETBUTTONBAR
            + "\">");
        if (this.getOptions().size() != 0) {
            ButtonSet control = new ButtonSet();
            ((Button) this.getOptions().get(0)).setState(ControlSuper.BUTTONSTATE_PUSHED);
            control.setButtons(this.getOptions());
            control.setName(this.getId());
            control.setSaveInfo(false);
            control.setButtonType(ControlSuper.BUTTONTYPE_PUSHBUTTON);
            out.write(control.getHtml());
        }
        out.write("</td></tr></table></div>");
        out.write("<script type=\"text/javascript\">");
        out.write("mgnlDialogResizeTabs('" + id + "');");
        out.write("mgnlDialogShiftTab('" + id + "',false,0)");
        out.write("</script>");
        // end TabSet stuff
        out.write("<div class=\"" + CssConstants.CSSCLASS_TABSETSAVEBAR + "\">");

        Button save = new Button();
        save.setOnclick(this.getConfigValue("saveOnclick", "mgnlDialogFormSubmit();"));
        save.setLabel(this.getConfigValue("saveLabel", msgs.get("buttons.save")));
        out.write(save.getHtml());
        Button cancel = new Button();
        cancel.setOnclick(this.getConfigValue("cancelOnclick", "window.close();"));
        cancel.setLabel(this.getConfigValue("cancelLabel", msgs.get("buttons.cancel")));
        out.write(cancel.getHtml());

        out.write("</div>");
        out.write("</form></body></html>");
    }
}
