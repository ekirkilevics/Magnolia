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
package info.magnolia.module.templatingcomponents.components;

import info.magnolia.cms.beans.config.ServerConfiguration;
import info.magnolia.cms.core.AggregationState;
import info.magnolia.jcr.util.MetaDataUtil;
import info.magnolia.objectfactory.Components;
import info.magnolia.templating.rendering.RenderException;
import info.magnolia.templating.template.TemplateDefinition;
import info.magnolia.templating.template.registry.TemplateDefinitionRegistrationException;
import info.magnolia.templating.template.registry.TemplateDefinitionRegistry;

import java.io.IOException;

import javax.jcr.Node;

import org.apache.commons.lang.StringUtils;

/**
 * Outputs an edit bar.
 *
 * @version $Id$
 */
public class EditComponent extends AbstractContentComponent {

    public static final String CMS_EDIT = "cms:edit";

    private String dialog;
    private String format;

    public EditComponent(ServerConfiguration server, AggregationState aggregationState) {
        super(server, aggregationState);
    }

    @Override
    protected void doRender(Appendable out) throws IOException, RenderException {
        Node content = getTargetContent();

        // TODO we need to do html attribute escaping on this

        out.append(CMS_BEGIN_CONTENT_COMMENT).append(getNodePath(content)).append(QUOTE).append(XML_END_COMMENT).append(LINEBREAK);
        out.append(LESS_THAN).append(CMS_EDIT).append(" content=").append(QUOTE).append(getNodePath(content)).append(QUOTE);
        if (StringUtils.isNotEmpty(format)) {
            out.append(" format=").append(QUOTE).append(format).append(QUOTE);
        }
        TemplateDefinition paragraph;
        try {
            paragraph = Components.getComponent(TemplateDefinitionRegistry.class).getTemplateDefinition(MetaDataUtil.getMetaData(content).getTemplate());
        } catch (TemplateDefinitionRegistrationException e) {
            // TODO dlipp: implement consistent ExceptionHandling for these Situations.
            throw new RuntimeException(e);
        }
        out.append(" label=").append(QUOTE).append(paragraph.getTitle()).append(QUOTE);

        out.append(" dialog=").append(QUOTE).append(resolveDialog(paragraph)).append(QUOTE);

        out.append(" paragraph=").append(QUOTE).append(paragraph.getName()).append(QUOTE);

        out.append(GREATER_THAN).append(LESS_THAN).append(SLASH).append(CMS_EDIT).append(GREATER_THAN).append(LINEBREAK);
    }

    // TODO this is used in a few more places - check for a better place to move to.
    private String resolveDialog(TemplateDefinition paragraph) {
        if (this.dialog != null) {
            return this.dialog;
        }
        String dialogToUse = paragraph.getDialog();
        if (dialogToUse == null) {
            return paragraph.getName();
        }
        return dialogToUse;
    }

    @Override
    public void postRender(Appendable out) throws IOException, RenderException {
        Node content = getTargetContent();

        out.append(CMS_END_CONTENT_COMMENT).append(getNodePath(content)).append(QUOTE).append(XML_END_COMMENT).append(LINEBREAK);
    }

    public String getDialog() {
        return dialog;
    }

    public void setDialog(String dialog) {
        this.dialog = dialog;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }
}