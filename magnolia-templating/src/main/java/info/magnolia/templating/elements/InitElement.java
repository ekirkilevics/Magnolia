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
import info.magnolia.cms.gui.misc.Sources;
import info.magnolia.cms.i18n.I18nContentSupport;
import info.magnolia.cms.i18n.I18nContentSupportFactory;
import info.magnolia.context.MgnlContext;
import info.magnolia.rendering.context.RenderingContext;
import info.magnolia.rendering.engine.RenderException;
import info.magnolia.rendering.template.TemplateDefinition;

import java.io.IOException;

import javax.jcr.Node;

import org.apache.commons.lang.StringUtils;


/**
 * Embeds js and css needed while authoring pages.
 *
 * @version $Id$
 */
public class InitElement extends AbstractContentTemplatingElement {

    public static final String PAGE_EDITOR_JS_SOURCE =  MgnlContext.getContextPath() + "/.resources/editor/info.magnolia.templating.editor.PageEditor/info.magnolia.templating.editor.PageEditor.nocache.js";
    public static final String PAGE_EDITOR_CSS =  MgnlContext.getContextPath() + "/.resources/magnolia-templating-editor/css/editor.css";
    private static final String CMS_TAG = "cms:page";

    private I18nContentSupport i18nSupport = I18nContentSupportFactory.getI18nSupport();
    private String dialog;

    public InitElement(ServerConfiguration server, RenderingContext renderingContext) {
        super(server, renderingContext);
    }

    @Override
    public void begin(Appendable out) throws IOException, RenderException {
        if (!isAdmin()) {
            return;
        }

        Node content = getTargetContent();

        TemplateDefinition templateDefinition = getRequiredTemplateDefinition();

        dialog = resolveDialog(templateDefinition);

        Sources src = new Sources(MgnlContext.getContextPath());
        MarkupHelper helper = new MarkupHelper(out);
        helper.append("<!-- begin js and css added by @cms.init -->\n");
        helper.append("<meta name=\"gwt:property\" content=\"locale=" + i18nSupport.getLocale() +"\"/>\n");
        helper.append(src.getHtmlCss());
        helper.append(src.getHtmlJs());
        helper.append("<link rel=\"stylesheet\" type=\"text/css\" href=\"" + PAGE_EDITOR_CSS + "\"></link>\n");
        helper.append("<script type=\"text/javascript\" src=\"" + PAGE_EDITOR_JS_SOURCE + "\"></script>\n");



        helper.openComment(CMS_TAG);
        if(content != null) {
            helper.attribute("content", getNodePath(content));
        }
        helper.attribute("dialog", dialog);
        helper.append(" -->\n");
        helper.closeComment(CMS_TAG);

    }

    @Override
    public void end(Appendable out) throws IOException, RenderException {
        if (!isAdmin()) {
            return;
        }
        MarkupHelper helper = new MarkupHelper(out);
        helper.append("\n<!-- end js and css added by @cms.init -->\n");
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

    public void setDialog(String dialog) {
        this.dialog = dialog;
    }
}
