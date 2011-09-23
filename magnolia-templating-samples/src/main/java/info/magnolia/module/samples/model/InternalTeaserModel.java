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
package info.magnolia.module.samples.model;

import info.magnolia.context.MgnlContext;
import info.magnolia.exception.RuntimeRepositoryException;
import info.magnolia.module.templating.MagnoliaTemplatingUtilities;
import info.magnolia.rendering.model.RenderingModel;
import info.magnolia.rendering.model.RenderingModelImpl;
import info.magnolia.rendering.template.RenderableDefinition;

import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.ValueFormatException;

/**
 * TODO: write javadoc.
 * @version $Id$
 *
 */
public class InternalTeaserModel extends RenderingModelImpl<RenderableDefinition> {

    public InternalTeaserModel(Node content, RenderableDefinition definition, RenderingModel<?> parent) {
        super(content, definition, parent);
    }

    public Node getTarget() throws ValueFormatException, PathNotFoundException, RepositoryException{
        String identifier = getNodeProperty("link");
        if(identifier == null){
            return null;
        }
        return MgnlContext.getJCRSession("website").getNodeByIdentifier(identifier);
    }

    public String getTeaserLink(){
        String identifier = getNodeProperty("link");
        return MagnoliaTemplatingUtilities.getInstance().createLink("website", identifier);
    }

    private String getNodeProperty(String property) {
        try {
            return content.getProperty(property).getString();
        } catch (ValueFormatException e) {
            new RuntimeRepositoryException(e);
        } catch (PathNotFoundException e) {
            new RuntimeRepositoryException(e);
        } catch (RepositoryException e) {
            new RuntimeRepositoryException(e);
        }
        return null;
    }

}
