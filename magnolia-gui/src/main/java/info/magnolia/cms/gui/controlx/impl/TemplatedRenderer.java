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
import info.magnolia.cms.util.FreeMarkerUtil;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;


/**
 * Used the classname by default to get the template to render.
 * @author Philipp Bracher
 * @version $Revision$ ($Author$)
 *
 */
public class TemplatedRenderer implements Renderer {

    /**
     * @see info.magnolia.cms.gui.controlx.Renderer#render(info.magnolia.cms.gui.controlx.Control)
     */
    public String render(Control control) {
        Map data = new HashMap();
        data.put("this", control);
        return FreeMarkerUtil.process(this.getTemplateName(control), data);
    }

    /**
     * @return
     */
    private String getTemplateName(Control control) {
        return "/" + StringUtils.replace(control.getClass().getName(), ".", "/") + ".html";
    }

}
