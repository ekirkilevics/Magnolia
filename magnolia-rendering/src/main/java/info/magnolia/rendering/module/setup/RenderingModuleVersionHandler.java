/**
 * This file Copyright (c) 2008-2011 Magnolia International
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
package info.magnolia.rendering.module.setup;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import info.magnolia.jcr.util.NodeUtil;
import info.magnolia.jcr.util.PropertyUtil;
import info.magnolia.module.DefaultModuleVersionHandler;
import info.magnolia.module.InstallContext;
import info.magnolia.module.delta.AbstractRepositoryTask;
import info.magnolia.module.delta.BootstrapSingleResource;
import info.magnolia.module.delta.DeltaBuilder;
import info.magnolia.module.delta.Task;
import info.magnolia.module.delta.TaskExecutionException;
import info.magnolia.module.delta.WarnTask;

/**
 * Rendering VersionHandler.
 * @author erichechinger
 * @version $Id$
 *
 */
public class RenderingModuleVersionHandler extends DefaultModuleVersionHandler {
    
    final Task transformSubTemplatesToVariations = new AbstractRepositoryTask("Transfrom subTemplates to variations", "Find and transfrom all subTemplates to variations."){

        final String TEMPLATES = "templates";
        final String SUBTEMPLATES = "subTemplates";
        final String VARIATION = "variations";

        @Override
        protected void doExecute(InstallContext installContext) throws RepositoryException, TaskExecutionException {
            Session session = installContext.getJCRSession("config");
            if (session == null) {
                throw new RuntimeException("Repository config not loaded");
            }

            NodeIterator modulesNodeIterator = session.getNode("/modules").getNodes();
            while(modulesNodeIterator.hasNext()){
                Node moduleNode = modulesNodeIterator.nextNode();
                if(moduleNode.hasNode(TEMPLATES)){
                    findSubtemplates(moduleNode, installContext);
                }
            }
        }
        
        /**
         * Method that find all subTemplates in templates node.
         */
        private void findSubtemplates(Node node, InstallContext installContext) throws RepositoryException, TaskExecutionException{
            if(node.hasNode(SUBTEMPLATES)){
                if(!node.hasNode(VARIATION)){
                    transformSubTemapltesToVariations(node, installContext);
                    return;
                }
                new WarnTask("Transfrom subTemplates to variations", "Can't transfrom subTemplates from" + node.getPath() + " to variations, because variations already exists.").execute(installContext);
                return;
            }
            if(node.hasNodes()){
                NodeIterator nodeIterator = node.getNodes();
                while(nodeIterator.hasNext()){
                    findSubtemplates(nodeIterator.nextNode(), installContext);
                }
            }
        }

        /**
         * Method that transfrom subTemplates to variations.
         */
        private void transformSubTemapltesToVariations(Node node, InstallContext installContext) throws RepositoryException, TaskExecutionException {
            NodeIterator subTemplatesNodeIterator = node.getNode(SUBTEMPLATES).getNodes();
            while(subTemplatesNodeIterator.hasNext()){
                Node subTemplatesNode = subTemplatesNodeIterator.nextNode();
                if(!subTemplatesNode.getName().equals("MetaData")){
                    if(subTemplatesNode.hasProperty("extension") && subTemplatesNode.hasProperty("templatePath") && subTemplatesNode.hasProperty("type")){
                        PropertyUtil.renameProperty(subTemplatesNode.getProperty("templatePath"), "templateScript");
                        PropertyUtil.renameProperty(subTemplatesNode.getProperty("type"), "renderType");
                        NodeUtil.renameNode(subTemplatesNode, subTemplatesNode.getProperty("extension").getString());
                        subTemplatesNode.getProperty("extension").remove();
                    }else{
                        new WarnTask("Transfrom subTemplates to Variations", "subTemplate from called " + subTemplatesNode.getName() + " has missing required property, can't transform this subTemplate properly. Please edit it in " + node.getPath() + "/variations.").execute(installContext);
                    }
                }
            }
            NodeUtil.renameNode(node.getNode(SUBTEMPLATES), VARIATION);
        }
    };

    public RenderingModuleVersionHandler() {
        register(DeltaBuilder.update("4.5", "")
                .addTask(new BootstrapSingleResource(
                        "Register FTL Context Attribute",
                        "Add cms and cmsfn context Attribute",
                        "/mgnl-bootstrap/rendering/config.modules.rendering.renderers.freemarker.contextAttributes.xml"))
                .addTask(new BootstrapSingleResource(
                        "Register JSP Context Attribute",
                        "Add cmsfn context Attribute",
                        "/mgnl-bootstrap/rendering/config.modules.rendering.renderers.jsp.contextAttributes.xml"))
                 );
        register(DeltaBuilder.update("4.5.3", "")
                .addTask(transformSubTemplatesToVariations)
        );
    }
}
