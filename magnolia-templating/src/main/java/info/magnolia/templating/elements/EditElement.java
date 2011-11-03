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
package info.magnolia.templating.elements;

import info.magnolia.cms.beans.config.ServerConfiguration;
import info.magnolia.rendering.context.RenderingContext;
import info.magnolia.rendering.engine.RenderException;
import info.magnolia.rendering.template.TemplateDefinition;

import java.io.IOException;

import javax.jcr.Node;

import org.apache.commons.lang.StringUtils;


/**
 * Embeds a marker within the page that instructs the page editor to place an edit bar/button at this location.
 *
 * @version $Id$
 */
public class EditElement extends AbstractContentTemplatingElement {

    public static final String CMS_EDIT = "cms:edit";
    public static final String FORMAT_BAR = "bar";
    public static final String FORMAT_BUTTON = "button";
    public static final String DEFAULT_FORMAT = FORMAT_BAR;

    private String dialog;
    private String format = DEFAULT_FORMAT;

    public EditElement(ServerConfiguration server, RenderingContext renderingContext) {
        super(server, renderingContext);
    }

    @Override
    public void begin(Appendable out) throws IOException, RenderException {
        if (!isAdmin()) {
            return;
        }
        Node content = getTargetContent();

        TemplateDefinition templateDefinition = getRequiredTemplateDefinition();

        MarkupHelper helper = new MarkupHelper(out);
        helper.startContent(content);
        helper.openTag(CMS_EDIT);

        if(content != null) {
            helper.attribute("content", getNodePath(content));
        } else {
            //null content probably means that the area is optional and hasn't been created yet. Still we need to generate
            //the cms:edit tag in order to render the area bar
            helper.attribute("name", templateDefinition.getName());
            helper.attribute("optional", "true");
        }

        helper.attribute("format", format);
        String dialog = resolveDialog(templateDefinition);
        helper.attribute("dialog", dialog);

        helper.attribute("template", templateDefinition.getId());

        helper.closeTag(CMS_EDIT);

    }

    private TemplateDefinition getRequiredTemplateDefinition() {
        return (TemplateDefinition) getRenderingContext().getRenderableDefinition();
    }

    private String resolveDialog(TemplateDefinition component) {
        if (StringUtils.isNotEmpty(this.dialog)) {
            return this.dialog;
        }
        String dialog = component.getDialog();
        if (StringUtils.isNotEmpty(dialog)) {
            return dialog;
        }
        return null;
    }

    @Override
    public void end(Appendable out) throws IOException, RenderException {
        if (!isAdmin()) {
            return;
        }
        Node content = getTargetContent();
        if (content != null) {
            MarkupHelper helper = new MarkupHelper(out);
            helper.endContent(content);
        }
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
