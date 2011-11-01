/**
 * This file Copyright (c) 2011 Magnolia International
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
package info.magnolia.templating.editor.servlet;

import info.magnolia.context.MgnlContext;
import info.magnolia.jcr.util.MetaDataUtil;
import info.magnolia.jcr.util.NodeUtil;

import java.io.IOException;
import java.io.PrintWriter;

import javax.jcr.Node;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;

/**
 * PageEditorServlet.
 * @version $Id$
 *
 */
public class PageEditorServlet extends HttpServlet{

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String action  = req.getParameter("action");
        String workspace = req.getParameter("workspace");
        String parent = req.getParameter("parent");
        String relPath = req.getParameter("relPath");
        String itemType = req.getParameter("itemType");

        try {
            checkMandatoryParameters(action, workspace, parent, relPath, itemType);
            createNode(workspace, parent, relPath, itemType);
            //TODO fgrilli: everything went fine, say it in json string, in case the client wants to know it.
        } catch (Exception e) {
            //TODO fgrilli: spit out error as json
            PrintWriter writer = resp.getWriter();
            writer.write(e.getMessage());
            writer.flush();
        }
    }

    private void createNode(String workspace, String parent, String relPath, String itemType) throws Exception {
        Node parentNode = MgnlContext.getJCRSession(workspace).getNode(parent);
        Node newNode = NodeUtil.createPath(parentNode, relPath, itemType);
        MetaDataUtil.updateMetaData(newNode);
        newNode.getSession().save();
    }

    private void checkMandatoryParameters(String... params) {
        for(String param: params) {
            if(StringUtils.isBlank(param)) {
                throw new IllegalArgumentException("No param can be null or empty");
            }
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doPost(req, resp);
    }
}
