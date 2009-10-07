/**
 * This file Copyright (c) 2003-2009 Magnolia International
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
package info.magnolia.module.delta;

import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.ItemType;
import info.magnolia.cms.core.Path;
import info.magnolia.cms.util.NodeDataUtil;
import info.magnolia.module.InstallContext;
import info.magnolia.module.model.ServletDefinition;
import info.magnolia.module.model.ServletParameterDefinition;

import javax.jcr.RepositoryException;

/**
 * @author philipp
 * @version $Id$
 */
public class RegisterServletTask extends AbstractTask {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(RegisterServletTask.class);

    private static final String DEFAULT_SERVLET_FILTER_PATH = "/server/filters/servlets";
    private final ServletDefinition servletDefinition;

    public RegisterServletTask(ServletDefinition servletDefinition) {
        super("Servlet " + servletDefinition.getName(), "Registers servlet" + servletDefinition.getName() + " (" + servletDefinition.getComment() + ")");
        this.servletDefinition = servletDefinition;
    }

    public void execute(InstallContext installContext) throws TaskExecutionException {
        log.debug("Registering servlet " + servletDefinition.getName() + " in servlet filter.");

        final String servletFilterPath = DEFAULT_SERVLET_FILTER_PATH;

        try {
            final Content servletNode = installContext.getConfigHierarchyManager().createContent(servletFilterPath, servletDefinition.getName(), ItemType.CONTENTNODE.getSystemName());
            NodeDataUtil.getOrCreateAndSet(servletNode, "class", "info.magnolia.cms.filters.ServletDispatchingFilter");
            NodeDataUtil.getOrCreateAndSet(servletNode, "enabled", true);
            NodeDataUtil.getOrCreateAndSet(servletNode, "servletClass", servletDefinition.getClassName());
            NodeDataUtil.getOrCreateAndSet(servletNode, "servletName", servletDefinition.getName());
            NodeDataUtil.getOrCreateAndSet(servletNode, "comment", servletDefinition.getComment());

            final Content mappingsNode = servletNode.createContent("mappings", ItemType.CONTENTNODE);
            for (String pattern : servletDefinition.getMappings()) {
                String mappingNodeName = Path.getUniqueLabel(mappingsNode, Path.getValidatedLabel(pattern));
                final Content mappingNode = mappingsNode.createContent(mappingNodeName, ItemType.CONTENTNODE);
                NodeDataUtil.getOrCreateAndSet(mappingNode, "pattern", pattern);
            }

            final Content parametersNode = servletNode.createContent("parameters", ItemType.CONTENTNODE);
            for (ServletParameterDefinition parameter : servletDefinition.getParams()) {
                NodeDataUtil.getOrCreateAndSet(parametersNode, parameter.getName(), parameter.getValue());
            }
        }
        catch (RepositoryException e) {
            log.error("Cannot create servlet node in servlet filter.", e);
        }
    }

    public ServletDefinition getServletDefinition() {
        return this.servletDefinition;
    }

}
