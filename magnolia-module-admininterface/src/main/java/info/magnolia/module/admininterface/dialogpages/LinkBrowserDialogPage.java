package info.magnolia.module.admininterface.dialogpages;

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.gui.control.Button;
import info.magnolia.cms.gui.misc.CssConstants;
import info.magnolia.cms.gui.misc.Sources;
import info.magnolia.cms.i18n.MessagesManager;
import info.magnolia.module.admininterface.DialogPageMVCHandler;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;


/**
 * @author Fabrizio Giustina
 * @version $Id$
 */
public class LinkBrowserDialogPage extends DialogPageMVCHandler {

    public LinkBrowserDialogPage(String name, HttpServletRequest request, HttpServletResponse response) {
        super(name, request, response);
    }

    /**
     * Stable serialVersionUID.
     */
    private static final long serialVersionUID = 222L;

    protected void draw(HttpServletRequest request, HttpServletResponse response) throws IOException {
        PrintWriter out = response.getWriter();
        String repository = request.getParameter("repository"); //$NON-NLS-1$
        if (StringUtils.isEmpty(repository)) {
            repository = ContentRepository.WEBSITE;
        }

        String path = request.getParameter("path"); //$NON-NLS-1$
        String pathOpen = request.getParameter("pathOpen"); //$NON-NLS-1$
        String pathSelected = request.getParameter("pathSelected"); //$NON-NLS-1$

        StringBuffer html = new StringBuffer();
        html.append("<html><head>"); //$NON-NLS-1$
        html.append("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\"/>"); //$NON-NLS-1$
        html.append(new Sources(request.getContextPath()).getHtmlJs());
        html.append(new Sources(request.getContextPath()).getHtmlCss());
        html.append("</head>"); //$NON-NLS-1$
        html.append("<body class=\"mgnlBgDark\" onload=\"mgnlDialogLinkBrowserResize();\">"); //$NON-NLS-1$

        StringBuffer src = getIFrameSrc(request, repository, path, pathOpen, pathSelected);

        html.append("<div id=\"mgnlTreeDiv\" class=\"mgnlDialogLinkBrowserTreeDiv\">"); //$NON-NLS-1$
        html.append("<iframe id=\"mgnlDialogLinkBrowserIFrame\" name=\"mgnlDialogLinkBrowserIFrame\" src=\"" //$NON-NLS-1$
            + src
            + "\" scrolling=\"no\" frameborder=\"0\" width=\"100%\" height=\"100\"></iframe>"); //$NON-NLS-1$
        html.append("</div>"); //$NON-NLS-1$

        Button bOk = new Button();
        bOk.setLabel(MessagesManager.get("buttons.ok")); //$NON-NLS-1$
        // this will call the callback command
        bOk.setOnclick("mgnlDialogLinkBrowserWriteBack()"); //$NON-NLS-1$

        Button bCancel = new Button();
        bCancel.setLabel(MessagesManager.get("buttons.cancel")); //$NON-NLS-1$
        bCancel.setOnclick("window.top.close();"); //$NON-NLS-1$

        html.append("<div class=\"" + CssConstants.CSSCLASS_TABSETSAVEBAR + "\">"); //$NON-NLS-1$ //$NON-NLS-2$
        html.append(bOk.getHtml());
        html.append(" "); //$NON-NLS-1$
        html.append(bCancel.getHtml());
        html.append("</div>"); //$NON-NLS-1$

        html.append("</body></html>"); //$NON-NLS-1$

        out.println(html);
    }

    private StringBuffer getIFrameSrc(HttpServletRequest request, String repository, String path, String pathOpen,
        String pathSelected) {
        StringBuffer src = new StringBuffer();
        src.append(request.getContextPath());
        src.append("/.magnolia/trees/" + repository + ".html"); //$NON-NLS-1$
        src.append("?&amp;mgnlCK=" + new Date().getTime()); //$NON-NLS-1$
        src.append("&amp;browseMode=true"); //$NON-NLS-1$
        if (path != null) {
            src.append("&amp;path=" + path); //$NON-NLS-1$
        }
        if (pathOpen != null) {
            src.append("&amp;pathOpen=" + pathOpen); //$NON-NLS-1$
        }
        if (pathSelected != null) {
            src.append("&amp;pathSelected=" + pathSelected); //$NON-NLS-1$
        }
        return src;
    }
}
