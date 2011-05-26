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
package info.magnolia.templating.model;

import javax.jcr.Node;

import info.magnolia.templating.template.definition.RenderableDefinition;


/**
 * The default concrete rendering definition used for templates and paragraphs.
 * @author pbracher
 * @param <RD> - an instance of {@link RenderableDefinition}
 * @version $Id: RenderingModelImpl.java 44893 2011-05-10 07:33:22Z dlipp $
 *
 */
public class RenderingModelImpl<RD extends RenderableDefinition> implements RenderingModel<RD> {
    protected RenderingModel<?> parentModel;
    protected final Node content;
    protected final RD definition;

    public RenderingModelImpl(Node content, RD definition, RenderingModel<?> parent) {
        this.content = content;
        this.definition = definition;
        this.parentModel = parent;
    }

    @Override
    public RenderingModel<?> getParent() {
        return this.parentModel;
    }

    public RenderingModel<?> getRoot(){
        RenderingModel<?> model = this;
        while(model.getParent() != null){
            model = model.getParent();
        }
        return model;
    }

    @Override
    public Node getContent() {
        return this.content;
    }

    /**
     * Shortname for templates: model.def.
     */
    public RD getDef() {
        return getDefinition();
    }

    @Override
    public RD getDefinition() {
        return this.definition;
    }

    @Override
    public String execute() {
        return null;
    }

}
