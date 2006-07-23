/**
 *
 * Magnolia and its source-code is licensed under the LGPL.
 * You may copy, adapt, and redistribute this file for commercial or non-commercial use.
 * When copying, adapting, or redistributing this document in keeping with the guidelines above,
 * you are required to provide proper attribution to obinary.
 * If you reproduce or distribute the document without making any substantive modifications to its content,
 * please use the following attribution line:
 *
 * Copyright 1993-2005 obinary Ltd. (http://www.obinary.com) All rights reserved.
 *
 */
package info.magnolia.cms.gui.controlx.impl;

import info.magnolia.cms.gui.controlx.Control;
import info.magnolia.cms.gui.controlx.Renderer;
import info.magnolia.cms.i18n.Messages;
import info.magnolia.cms.i18n.MessagesManager;
import info.magnolia.cms.util.FreeMarkerUtil;

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
    public String render(Control control) {
        Map data = new HashMap();
        data.put("this", control);
        data.put("renderer", this);
        return FreeMarkerUtil.process(this.getTemplateName(control), data);
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
