/**
 * This file Copyright (c) 2011-2012 Magnolia International
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
package info.magnolia.repository.definition;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.BooleanUtils;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

/**
 * Reads InputStream created out of repositories.xml into pojos using JDom.
 *
 * @version $Id$
 */
public class RepositoryMappingDefinitionReader {

    /**
     * repository element string.
     */
    private static final String ELEMENT_REPOSITORY = "Repository";

    private static final String ELEMENT_REPOSITORYMAPPING = "RepositoryMapping";

    private static final String ELEMENT_PARAM = "param";

    private static final String ELEMENT_WORKSPACE = "workspace";

    /**
     * Attribute names.
     */
    private static final String ATTRIBUTE_NAME = "name";

    private static final String ATTRIBUTE_LOAD_ON_STARTUP = "loadOnStartup";

    private static final String ATTRIBUTE_PROVIDER = "provider";

    private static final String ATTRIBUTE_VALUE = "value";

    private static final String ATTRIBUTE_REPOSITORY_NAME = "repositoryName";

    private static final String ATTRIBUTE_WORKSPACE_NAME = "workspaceName";

    private static final String DEFAULT_WORKSPACE_NAME = "default";


    /**
     * Load repository mappings and params using repositories.xml.
     */
    public RepositoryMappingDefinition read(InputStream stream) throws JDOMException, IOException {
        SAXBuilder builder = new SAXBuilder();
        Document document = builder.build(stream);

        Element root = document.getRootElement();
        RepositoryMappingDefinition definition = new RepositoryMappingDefinition();
        parseRepositoryMapping(root, definition);
        parseRepositories(root, definition);

        return definition;
    }

    /**
     * Parses &lt;Repository&gt;.
     */
    private void parseRepositories(Element root, RepositoryMappingDefinition definition) {
        @SuppressWarnings("unchecked")
        List<Element> repositoryElements = root.getChildren(ELEMENT_REPOSITORY);
        for (Element repositoryElement : repositoryElements) {
            parseRepository(repositoryElement, definition);
        }
    }

    private void parseRepository(Element repositoryElement, RepositoryMappingDefinition definition) {

        String name = repositoryElement.getAttributeValue(ATTRIBUTE_NAME);
        String loadOnStartup = repositoryElement.getAttributeValue(ATTRIBUTE_LOAD_ON_STARTUP);
        String provider = repositoryElement.getAttributeValue(ATTRIBUTE_PROVIDER);

        RepositoryDefinition repository = new RepositoryDefinition();
        repository.setName(name);
        repository.setProvider(provider);
        repository.setLoadOnStartup(BooleanUtils.toBoolean(loadOnStartup));

        // parse repository parameters
        Map<String, String> parameters = new HashMap<String, String>();
        for (Object element : repositoryElement.getChildren(ELEMENT_PARAM)) {
            Element parameterElement = (Element) element;
            String parameterName = parameterElement.getAttributeValue(ATTRIBUTE_NAME);
            String value = parameterElement.getAttributeValue(ATTRIBUTE_VALUE);
            parameters.put(parameterName, value);
        }
        // TODO : it looks like params here are not interpolated
        repository.setParameters(parameters);

        // parse workspace names
        @SuppressWarnings("unchecked")
        List<Element> workspaces = repositoryElement.getChildren(ELEMENT_WORKSPACE);
        if (workspaces == null || workspaces.isEmpty()) {
            // TODO this does not belong in the parser
            repository.addWorkspace(DEFAULT_WORKSPACE_NAME);
        } else {
            for (Element element : workspaces) {
                String workspaceName = element.getAttributeValue(ATTRIBUTE_NAME);
                repository.addWorkspace(workspaceName);
            }
        }

        definition.addRepository(repository);
    }

    /**
     * Parses &lt;RepositoryMapping&gt;.
     */
    private void parseRepositoryMapping(Element root, RepositoryMappingDefinition definition) {
        Element repositoryMappingElement = root.getChild(ELEMENT_REPOSITORYMAPPING);
        @SuppressWarnings("unchecked")
        List<Element> children = repositoryMappingElement.getChildren();
        for (Element child : children) {
            definition.addMapping(
                    child.getAttributeValue(ATTRIBUTE_NAME),
                    child.getAttributeValue(ATTRIBUTE_REPOSITORY_NAME),
                    child.getAttributeValue(ATTRIBUTE_WORKSPACE_NAME));
        }
    }

}
