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
import info.magnolia.cms.gui.control.Button;
import info.magnolia.cms.gui.control.ButtonSet;
import info.magnolia.cms.gui.control.ControlSuper;
import info.magnolia.cms.gui.control.Hidden;
import info.magnolia.cms.gui.misc.Sources;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.jcr.RepositoryException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.PageContext;

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

    public DialogDialog(Content configNode, Content websiteNode, HttpServletRequest request, PageContext pageContext)
        throws RepositoryException {
        super(configNode, websiteNode, request, pageContext);
    }

    public DialogDialog() {
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

    public void drawJavascriptSources(JspWriter out) throws IOException {
        Iterator it = this.getJavascriptSources().iterator();
        while (it.hasNext()) {
            out.println("<script type=\"text/javascript\" src=\"" + it.next() + "\"></script>");
        }
    }

    public void setCssSources(String s) {
        this.getCssSources().add(s);
    }

    public List getCssSources() {
        return this.cssSources;
    }

    public void drawCssSources(JspWriter out) {
        Iterator it = this.getCssSources().iterator();
        while (it.hasNext()) {
            try {
                out.println("<link rel=\"stylesheet\" type=\"text/css\" href=\"" + it.next() + "\">");
            }
            catch (IOException ioe) {
                log.error("");
            }
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

    public void drawHtmlPreSubs(JspWriter out) throws IOException {
        out.println("<!DOCTYPE html PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\">");
        out.println("<html>");
        out.println("<head>");
        out.println("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\"/>"); // kupu
        out.println("<script type=\"text/javascript\">");
        out.println("window.resizeTo("
            + this.getConfigValue("width", DIALOGSIZE_NORMAL_WIDTH)
            + ","
            + this.getConfigValue("height", DIALOGSIZE_NORMAL_HEIGHT)
            + ");");
        out.println("</script>");
        out.println("<title>" + this.getConfigValue("label", "Magnolia Edit Dialog") + "</title>");
        out.println(new Sources().getHtmlJs());
        out.println(new Sources().getHtmlCss());
        out.println(new Sources().getHtmlRichEdit());
        out.println("<script type=\"text/javascript\">");
        out.println("var mgnlRichEditors=new Array();"); // will be extended at each richEdit control
        out.println("var kupu = null;");
        out.println("var kupuui = null;");
        out.println("</script>");
        this.drawJavascriptSources(out);
        out.println("</head>");
        out
            .println("<body class=\"mgnlDialogBody\" onload=\"mgnlDialogInit();\" marginwidth=\"0\" topmargin=\"0\" marginheight=\"0\" leftmargin=\"0\">");
        out.println("<form action=\""
            + this.getAction()
            + "\" name=\"mgnlFormMain\" method=\"post\" enctype=\"multipart/form-data\">");
        out.println(new Hidden("mgnlRepository", this.getConfigValue("repository"), false).getHtml());
        out.println(new Hidden("mgnlPath", this.getConfigValue("path"), false).getHtml());
        out.println(new Hidden("mgnlNodeCollection", this.getConfigValue("nodeCollection"), false).getHtml());
        out.println(new Hidden("mgnlNode", this.getConfigValue("node"), false).getHtml());
        out.println(new Hidden("mgnlJsCallback", this.getCallbackJavascript(), false).getHtml());
        out.println(new Hidden("mgnlRichE", this.getConfigValue("richE"), false).getHtml());
        out.println(new Hidden("mgnlRichEPaste", this.getConfigValue("richEPaste"), false).getHtml());
        if (this.getConfigValue("paragraph").indexOf(",") == -1) {
            out.println(new Hidden("mgnlParagraph", this.getConfigValue("paragraph"), false).getHtml());
        } // else multiple paragraph selection -> radios for selection
        // TabSet stuff
        String id = this.getId();
        out.println("<script type=\"text/javascript\">");
        out.println("mgnlControlSets['" + id + "']=new Object();");
        out.println("mgnlControlSets['" + id + "'].items=new Array();");
        out.println("mgnlControlSets['" + id + "'].resize=true;");
        out.println("</script>");
        // end TabSet stuff
    }

    public void drawHtmlPostSubs(JspWriter out) throws IOException {
        // TabSet stuff
        String id = this.getId();
        out.println("<div class=\"" + CSSCLASS_TABSETBUTTONBAR + "\">");
        out.println("<table cellpadding=\"0\" cellspacing=\"0\" border=\"0\" width=\"100%\"><tr><td  class=\""
            + CSSCLASS_TABSETBUTTONBAR
            + "\">");
        if (this.getOptions().size() != 0) {
            ButtonSet control = new ButtonSet();
            ((Button) this.getOptions().get(0)).setState(ControlSuper.BUTTONSTATE_PUSHED);
            control.setButtons(this.getOptions());
            control.setName(this.getId());
            control.setSaveInfo(false);
            control.setButtonType(ControlSuper.BUTTONTYPE_PUSHBUTTON);
            out.println(control.getHtml());
        }
        out.println("</td></tr></table></div>");
        out.println("<script type=\"text/javascript\">");
        out.println("mgnlDialogResizeTabs('" + id + "');");
        out.println("mgnlDialogShiftTab('" + id + "',false,0)");
        out.println("</script>");
        // end TabSet stuff
        out.println("<div class=\"" + CSSCLASS_TABSETSAVEBAR + "\">");
        // out.println("<table cellpadding=\"0\" cellspacing=\"0\" border=\"0\" width=\"100%\">");
        // out.println("<tr><td style=\"text-align:right;\">");
        Button save = new Button();
        save.setOnclick(this.getConfigValue("saveOnclick", "mgnlDialogFormSubmit();"));
        save.setLabel(this.getConfigValue("saveLabel", "Save"));
        out.println(save.getHtml());
        Button cancel = new Button();
        cancel.setOnclick(this.getConfigValue("cancelOnclick", "window.close();"));
        cancel.setLabel(this.getConfigValue("cancelLabel", "Cancel"));
        out.println(cancel.getHtml());
        // out.println("</td></tr></table>");
        out.println("</div>");
        out.println("</form></body></html>");
    }
}
