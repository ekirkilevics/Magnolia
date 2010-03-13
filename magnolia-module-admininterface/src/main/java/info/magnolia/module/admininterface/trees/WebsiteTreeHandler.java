/**
 * This file Copyright (c) 2008-2010 Magnolia International
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
package info.magnolia.module.admininterface.trees;

import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.ItemType;
import info.magnolia.module.admininterface.AdminTreeMVCHandler;
import info.magnolia.module.templating.Template;
import info.magnolia.module.templating.TemplateManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.RepositoryException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * @author philipp
 * @version $Id$
 *
 */
public class WebsiteTreeHandler extends AdminTreeMVCHandler {
    private static final Logger log = LoggerFactory.getLogger(WebsiteTreeHandler.class);

    private final TemplateManager templateManager;

    public WebsiteTreeHandler(String name, HttpServletRequest request, HttpServletResponse response) {
        super(name, request, response);
        templateManager = TemplateManager.getInstance();
    }

    public String createNode() {
        String view = super.createNode();
        try {
            // todo: default template
            if (this.getCreateItemType().equals(ItemType.CONTENT.getSystemName())) {
                Content parentNode = this.getHierarchyManager().getContent(this.getPath());
                Content newNode = parentNode.getContent(this.getNewNodeName());
                Template newTemplate = getDefaultTemplate(newNode);
                if (newTemplate != null) {
                    newNode.getMetaData().setTemplate(newTemplate.getName());
                    newNode.save();
                }
            }
        }
        catch (RepositoryException e) {
            log.error("can't set template", e);
        }
        return view;
    }

    protected Template getDefaultTemplate(Content node) {
        return templateManager.getDefaultTemplate(node);
    }

    public String show() {
        //show start page if no templates present yet
        if (!templateManager.getAvailableTemplates().hasNext()) {
            try {
                request.getRequestDispatcher("/.magnolia/pages/quickstart.html").forward(request, response);
                return "";
            } catch (Exception e) {
                log.error("Couldn't forward to quickstart page: " + e.getMessage());
            }
        }
        return super.show();
    }
}
