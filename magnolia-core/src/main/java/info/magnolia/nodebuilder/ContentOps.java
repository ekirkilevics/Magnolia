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
package info.magnolia.nodebuilder;

import info.magnolia.cms.beans.runtime.FileProperties;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.ItemType;
import info.magnolia.cms.core.NodeData;

import java.io.InputStream;

import javax.jcr.RepositoryException;

import org.apache.commons.lang.StringUtils;


/**
 * A set of methods to build {@link NodeOperation}s using the same tems as the Content API.
 */
public class ContentOps {

    public static NodeOperation createContent(String name, ItemType type) {
        return Ops.addNode(name, type);
    }

    public static NodeOperation createPage(String name, String template) {
        return createContent(name, ItemType.CONTENT).then(
            setTemplate(template));
    }

    public static NodeOperation createCollectionNode(String name) {
        return createContent(name, ItemType.CONTENTNODE);
    }

    public static NodeOperation createParagraph(String name, String template) {
        return createContent(name, ItemType.CONTENTNODE).then(
            setTemplate(template));
    }

    public static NodeOperation setNodeData(final String name, final Object value) {
        return new AbstractNodeOperation() {

            @Override
            protected Content doExec(Content context, ErrorHandler errorHandler) throws RepositoryException {
                context.setNodeData(name, value);
                return context;
            }
        };
    }

    public static NodeOperation setBinaryNodeData(final String name, final String fileName, final long size, final InputStream inputStream) {
        return new AbstractNodeOperation() {

            @Override
            protected Content doExec(Content context, ErrorHandler errorHandler) throws RepositoryException {
                NodeData binary = context.setNodeData(name, inputStream);
                binary.setAttribute(FileProperties.PROPERTY_FILENAME, StringUtils.substringBeforeLast(fileName, "."));
                binary.setAttribute(FileProperties.PROPERTY_EXTENSION, StringUtils.substringAfterLast(fileName, "."));
                binary.setAttribute(FileProperties.PROPERTY_SIZE, Long.toString(size));
                return context;
            }
        };
    }

    public static NodeOperation setTemplate(final String template) {
        return new AbstractNodeOperation() {

            @Override
            protected Content doExec(Content context, ErrorHandler errorHandler) throws RepositoryException {
                context.getMetaData().setTemplate(template);
                return context;
            }
        };
    }

}
