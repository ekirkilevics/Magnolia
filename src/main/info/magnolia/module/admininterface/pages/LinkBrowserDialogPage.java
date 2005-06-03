package info.magnolia.module.admininterface.pages;

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.gui.control.Button;
import info.magnolia.cms.gui.misc.CssConstants;
import info.magnolia.cms.gui.misc.Sources;
import info.magnolia.cms.i18n.MessagesManager;
import info.magnolia.cms.servlets.BasePageServlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;

import javax.jcr.RepositoryException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;


/**
 * @author Fabrizio Giustina
 * @version $Id: $
 */
public class LinkBrowserDialogPage extends BasePageServlet {

    /**
     * Stable serialVersionUID.
     */
    private static final long serialVersionUID = 222L;

    /**
     * @see info.magnolia.cms.servlets.BasePageServlet#draw(HttpServletRequest, HttpServletResponse)
     */
    public void draw(HttpServletRequest request, HttpServletResponse response) throws IOException, RepositoryException {
        PrintWriter out = response.getWriter();
        String repository = request.getParameter("repository");
        if (StringUtils.isEmpty(repository)) {
            repository = ContentRepository.WEBSITE;
        }

        String path = request.getParameter("path");
        String pathOpen = request.getParameter("pathOpen");
        String pathSelected = request.getParameter("pathSelected");

        StringBuffer html = new StringBuffer();
        html.append("<html><head>");
        html.append("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\"/>");
        html.append(new Sources(request.getContextPath()).getHtmlJs());
        html.append(new Sources(request.getContextPath()).getHtmlCss());
        html.append("</head>");
        html.append("<body class=\"mgnlBgDark\" onload=\"mgnlDialogLinkBrowserResize();\">");

        StringBuffer src = new StringBuffer();
        src.append(request.getContextPath());
        src.append("/.magnolia/dialogs/linkBrowserIFrame.html");
        src.append("?&amp;mgnlCK=" + new Date().getTime());
        src.append("&amp;repository=" + repository);
        if (path != null) {
            src.append("&amp;path=" + path);
        }
        if (pathOpen != null) {
            src.append("&amp;pathOpen=" + pathOpen);
        }
        if (pathSelected != null) {
            src.append("&amp;pathSelected=" + pathSelected);
        }

        html.append("<div id=\"mgnlTreeDiv\" class=\"mgnlDialogLinkBrowserTreeDiv\">");
        html.append("<iframe id=\"mgnlDialogLinkBrowserIFrame\" name=\"mgnlDialogLinkBrowserIFrame\" src=\""
            + src
            + "\" scrolling=\"no\" frameborder=\"0\" width=\"100%\" height=\"100\"></iframe>");
        html.append("</div>");

        Button bOk = new Button();
        bOk.setLabel(MessagesManager.get(request, "buttons.ok"));
        // this will call the callback command
        bOk.setOnclick("mgnlDialogLinkBrowserWriteBack()");
        
        /*
        bOk.setOnclick("mgnlDialogLinkBrowserWriteBack('"
            + destinationControlName
            + "','"
            + destinationExtension
            + "', "
            + addcontext
            + ");");
        */
        Button bCancel = new Button();
        bCancel.setLabel(MessagesManager.get(request, "buttons.cancel"));
        bCancel.setOnclick("window.close();");

        html.append("<div class=\"" + CssConstants.CSSCLASS_TABSETSAVEBAR + "\">");
        html.append(bOk.getHtml());
        html.append(" ");
        html.append(bCancel.getHtml());
        html.append("</div>");

        html.append("</body></html>");

        out.println(html);

    }

}
