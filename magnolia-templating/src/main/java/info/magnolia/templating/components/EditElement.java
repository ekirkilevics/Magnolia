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
package info.magnolia.templating.components;

import java.io.IOException;
import javax.jcr.Node;

import org.apache.commons.lang.StringUtils;

import info.magnolia.cms.beans.config.ServerConfiguration;
import info.magnolia.jcr.util.MetaDataUtil;
import info.magnolia.objectfactory.Components;
import info.magnolia.rendering.context.RenderingContext;
import info.magnolia.rendering.engine.RenderException;
import info.magnolia.rendering.template.TemplateDefinition;
import info.magnolia.rendering.template.registry.TemplateDefinitionRegistrationException;
import info.magnolia.rendering.template.registry.TemplateDefinitionRegistry;

/**
 * Outputs an edit bar.
 *
 * @version $Id$
 */
public class EditElement extends AbstractContentTemplatingElement {

    public static final String CMS_EDIT = "cms:edit";

    private String dialog;
    private String format;

    public EditElement(ServerConfiguration server, RenderingContext renderingContext) {
        super(server, renderingContext);
    }

    @Override
    public void begin(Appendable out) throws IOException, RenderException {
        Node content = getTargetContent();

        MarkupHelper helper = new MarkupHelper(out);

        helper.startContent(content);
        helper.openTag(CMS_EDIT).attribute("content", getNodePath(content));
        if (StringUtils.isNotEmpty(format)) {
            helper.attribute("format", format);
        }
        TemplateDefinition templateDefinition = getRequiredTemplateDefinition(content);
        helper.attribute("label", templateDefinition.getTitle());
        helper.attribute("dialog", resolveDialog(templateDefinition));
        helper.attribute("template", templateDefinition.getId());
        helper.closeTag(CMS_EDIT);
    }

    private TemplateDefinition getRequiredTemplateDefinition(Node content) {
        try {
            TemplateDefinitionRegistry registry = Components.getComponent(TemplateDefinitionRegistry.class);
            String template = MetaDataUtil.getMetaData(content).getTemplate();
            return registry.getTemplateDefinition(template);
        } catch (TemplateDefinitionRegistrationException e) {
            // TODO dlipp: implement consistent ExceptionHandling for these situations.
            throw new RuntimeException(e);
        }
    }

    private String resolveDialog(TemplateDefinition component) throws RenderException {
        if (StringUtils.isNotEmpty(this.dialog)) {
            return this.dialog;
        }
        String dialog = component.getDialog();
        if (StringUtils.isNotEmpty(dialog)) {
            return dialog;
        }
        throw new RenderException("Please set dialog for component id=" + component.getId() + ", name=" + component.getName());
    }

    @Override
    public void end(Appendable out) throws IOException, RenderException {
        Node content = getTargetContent();
        MarkupHelper helper = new MarkupHelper(out);
        helper.endContent(content);
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
