/**
 * This file Copyright (c) 2003-2011 Magnolia International
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
package info.magnolia.module.admininterface.pages;

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.gui.control.Button;
import info.magnolia.cms.gui.misc.CssConstants;
import info.magnolia.cms.gui.misc.Sources;
import info.magnolia.cms.i18n.MessagesManager;
import info.magnolia.module.admininterface.SimplePageMVCHandler;

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
public class LinkBrowserDialogPage extends SimplePageMVCHandler {

    public LinkBrowserDialogPage(String name, HttpServletRequest request, HttpServletResponse response) {
        super(name, request, response);
    }

    /**
     * Stable serialVersionUID.
     */
    private static final long serialVersionUID = 222L;

    protected void render(HttpServletRequest request, HttpServletResponse response) throws IOException {
        PrintWriter out = response.getWriter();
        String repository = request.getParameter("repository"); //$NON-NLS-1$
        if (StringUtils.isEmpty(repository)) {
            repository = ContentRepository.WEBSITE;
        }

        String path = request.getParameter("path"); //$NON-NLS-1$
        String pathOpen = request.getParameter("pathOpen"); //$NON-NLS-1$
        String pathSelected = request.getParameter("pathSelected"); //$NON-NLS-1$

        StringBuffer src = getIFrameSrc(request, repository, path, pathOpen, pathSelected);

        StringBuffer html = new StringBuffer();
        html.append("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\">"); //$NON-NLS-1$
        html.append("<html><head>"); //$NON-NLS-1$
        html.append("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\"/>"); //$NON-NLS-1$
        html.append(new Sources(request.getContextPath()).getHtmlJs());
        html.append(new Sources(request.getContextPath()).getHtmlCss());
        html.append("<script>");
        html.append("MgnlDHTMLUtil.addOnResize(mgnlDialogLinkBrowserResize);");
        html.append("MgnlDHTMLUtil.addOnLoad(function(){mgnlDialogLinkBrowserResize();document.getElementById('mgnlDialogLinkBrowserIFrame').src='"+src+"';window.focus();})");
        html.append("</script>");

        html.append("</head>"); //$NON-NLS-1$
        html.append("<body class=\"mgnlBgDark\" >"); //$NON-NLS-1$


        html.append("<div id=\"mgnlTreeDiv\" class=\"mgnlDialogLinkBrowserTreeDiv\">"); //$NON-NLS-1$
        html.append("<iframe id=\"mgnlDialogLinkBrowserIFrame\" name=\"mgnlDialogLinkBrowserIFrame\" src=\"" //$NON-NLS-1$
            //+ src
            + "\" scrolling=\"no\" frameborder=\"0\" width=\"100%\" height=\"100%\"></iframe>"); //$NON-NLS-1$
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
        src.append("?mgnlCK=" + new Date().getTime()); //$NON-NLS-1$
        src.append("&browseMode=true"); //$NON-NLS-1$
        if (path != null) {
            src.append("&path=" + path); //$NON-NLS-1$
        }
        if (pathOpen != null) {
            src.append("&pathOpen=" + pathOpen); //$NON-NLS-1$
        }
        if (pathSelected != null) {
            src.append("&pathSelected=" + pathSelected); //$NON-NLS-1$
        }
        return src;
    }
}
