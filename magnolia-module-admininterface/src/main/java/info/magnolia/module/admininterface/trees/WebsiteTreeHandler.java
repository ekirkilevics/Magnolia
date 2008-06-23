/**
 * This file Copyright (c) 2008 Magnolia International
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
package info.magnolia.module.admininterface.trees;

import java.util.Iterator;

import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.beans.config.Template;
import info.magnolia.cms.beans.config.TemplateManager;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.ItemType;
import info.magnolia.cms.security.AccessDeniedException;
import info.magnolia.cms.security.AccessManager;
import info.magnolia.cms.security.Permission;
import info.magnolia.context.MgnlContext;
import info.magnolia.module.admininterface.AdminTreeMVCHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author philipp
 * @version $Id$
 *
 */
public class WebsiteTreeHandler extends AdminTreeMVCHandler {

    public WebsiteTreeHandler(String name, HttpServletRequest request, HttpServletResponse response) {
        super(name, request, response);
    }

    private static Logger log = LoggerFactory.getLogger(WebsiteTreeHandler.class);

    public String createNode() {
        String view = super.createNode();
        try {
            // todo: default template
            if (this.getCreateItemType().equals(ItemType.CONTENT.getSystemName())) {
                Content parentNode = this.getHierarchyManager().getContent(this.getPath());
                Content newNode = parentNode.getContent(this.getNewNodeName());
                Template newTemplate = getDefaultTemplate(parentNode);
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

    protected Template getDefaultTemplate(Content parentNode) {
        // default to the template used by the parent node if the user can access it
        final TemplateManager templateManager = TemplateManager.getInstance();
        final AccessManager accessManager = MgnlContext.getAccessManager(ContentRepository.CONFIG);
        final String newTemplateName = parentNode.getTemplate();
        Template newTemplate = templateManager.getInfo(newTemplateName);
        if (newTemplate == null || !accessManager.isGranted(newTemplate.getLocation(), Permission.READ)) {
            // if that fails then first template of list is taken...
            Iterator templates = templateManager.getAvailableTemplates(accessManager);
            if (templates.hasNext()) {
                newTemplate = (Template) templates.next();
            } else {
                newTemplate = null;
            }
        }
        return newTemplate;
    }
}
