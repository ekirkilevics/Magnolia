/**
 * This file Copyright (c) 2011 Magnolia International
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
package info.magnolia.rendering.generator;

import static info.magnolia.rendering.template.AutoGenerationConfiguration.NODE_TYPE;
import static info.magnolia.rendering.template.AutoGenerationConfiguration.TEMPLATE_ID;
import info.magnolia.cms.core.MetaData;
import info.magnolia.cms.security.AccessDeniedException;
import info.magnolia.jcr.util.MetaDataUtil;
import info.magnolia.jcr.util.NodeUtil;
import info.magnolia.jcr.util.PropertyUtil;
import info.magnolia.rendering.engine.RenderException;
import info.magnolia.rendering.template.AutoGenerationConfiguration;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An implementation of {@link Generator} which will create nodes and properties verbatim as found in {@link AutoGenerationConfiguration#getContent()}.
 * @version $Id$
 *
 */
public class CopyGenerator implements Generator<AutoGenerationConfiguration> {

    private static final Logger log = LoggerFactory.getLogger(CopyGenerator.class);

    private Node parent;

    public CopyGenerator(final Node parent) {
        if(parent == null) {
            throw new IllegalArgumentException("parent node cannot be null");
        }
        this.parent = parent;
    }

    @Override
    public void generate(AutoGenerationConfiguration autoGenerationConfig) throws RenderException {
        if(autoGenerationConfig == null) {
            throw new IllegalArgumentException("Expected an instance of AutoGenerationConfiguration but got null instead");
        }

        createNode(parent, autoGenerationConfig.getContent());
    }

    @SuppressWarnings("unchecked")
    private void createNode(Node parentNode, Map<String,Object> content) throws RenderException {
        if(content == null) {
            return;
        }
        for(Entry<String, Object> entry: content.entrySet()) {

            final Map<String, Object> newNodeConfig = (Map<String, Object>) entry.getValue();

            if(!newNodeConfig.containsKey(NODE_TYPE)) {
                throw new RenderException("nodeType parameter expected but not found.");
            }
            final String name = entry.getKey();

            try {
                if(parentNode != null && parentNode.hasNode(name)) {
                    log.debug("path {} was already found in repository. No need to create it.", parentNode.getPath() + "/" + name);
                    break;
                }
            } catch (RepositoryException re) {
                throw new RenderException(re);
            }

            Node newNode = null;

            try {
                newNode = NodeUtil.createPath(parentNode, name, (String)newNodeConfig.get(NODE_TYPE));

                String template = (String) newNodeConfig.get(TEMPLATE_ID);
                if (template != null) {
                    MetaData metaData = MetaDataUtil.getMetaData(newNode);
                    metaData.setTemplate(template);
                    MetaDataUtil.updateMetaData(newNode);
                }

                log.debug("creating {}", newNode.getPath());

                for(Entry<String, Object> property : newNodeConfig.entrySet()) {
                    String propertyName = property.getKey();
                    if(NODE_TYPE.equals(propertyName) || TEMPLATE_ID.equals(propertyName)) {
                        continue;
                    }
                    //a sub content
                    if(property.getValue() instanceof HashMap) {
                        Map<String,Object> map = new HashMap<String,Object>();
                        map.put(propertyName, property.getValue());
                        createNode(newNode, map);
                    } else {
                        PropertyUtil.setProperty(newNode, propertyName, property.getValue());
                    }
                }
                newNode.getSession().save();
            } catch (AccessDeniedException e) {
                throw new RenderException("An error occurred while trying to create new node " + name, e);
            } catch (PathNotFoundException e) {
                throw new RenderException("An error occurred while trying to create new node " + name, e);
            } catch (RepositoryException e) {
                throw new RenderException("An error occurred while trying to create new node " + name, e);
            }
        }
    }

}
