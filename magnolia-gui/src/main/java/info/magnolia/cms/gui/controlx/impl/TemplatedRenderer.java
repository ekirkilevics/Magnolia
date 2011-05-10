/**
 * This file Copyright (c) 2003-2011 Magnolia International
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
package info.magnolia.cms.gui.controlx.impl;

import info.magnolia.cms.gui.controlx.Control;
import info.magnolia.cms.gui.controlx.Renderer;
import info.magnolia.cms.i18n.Messages;
import info.magnolia.cms.i18n.MessagesManager;
import info.magnolia.freemarker.FreemarkerUtil;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;


/**
 * Used the classname by default to get the template to render.
 * @author Philipp Bracher
 * @version $Revision$ ($Author$)
 */
public class TemplatedRenderer implements Renderer {

    private String templateName;

    /**
     * Uses the controls class name to construct the template name
     */
    public TemplatedRenderer() {
    }

    /**
     * @param templateName
     */
    public TemplatedRenderer(String templateName) {
        this.templateName = templateName;
    }

    /**
     * Render the using the template. The control is passed under the name 'this' and the renderer class is passed under
     * the name 'renderer'
     */
    @Override
    public String render(Control control) {
        Map data = new HashMap();
        data.put("this", control);
        data.put("renderer", this);
        return FreemarkerUtil.process(this.getTemplateName(control), data);
    }

    /**
     * Get the message from the renderer. Uses getMessages().
     * @param key
     * @return the string found, or the key if not found
     */
    public String getMessage(String key) {
        return getMessages().getWithDefault(key, key);
    }

    /**
     * Get the messages used for the rendering. By default the standard messages are returned.
     * @return the messages
     */
    public Messages getMessages() {
        return MessagesManager.getMessages();
    }

    /**
     * @return
     */
    protected String getTemplateName(Control control) {
        if (this.templateName == null) {
            return "/" + StringUtils.replace(control.getClass().getName(), ".", "/") + ".html";
        }

        return this.templateName;
    }

    /**
     * @return Returns the templateName.
     */
    public String getTemplateName() {
        return this.templateName;
    }

    /**
     * @param templateName The templateName to set.
     */
    public void setTemplateName(String templateName) {
        this.templateName = templateName;
    }

}
