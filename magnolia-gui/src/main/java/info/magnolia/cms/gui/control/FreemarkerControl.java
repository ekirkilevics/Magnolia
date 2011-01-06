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
package info.magnolia.cms.gui.control;

import freemarker.template.TemplateException;
import info.magnolia.freemarker.FreemarkerHelper;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Class for a generic control renderered by freemarker.
 * @author Manuel Molaschi
 * @version $Id: $
 */
public class FreemarkerControl extends ControlImpl {

    /**
     * Logger.
     */
    private static Logger log = LoggerFactory.getLogger(FreemarkerControl.class);

    /**
     * Constructor
     * @param valueType ControlImpl.VALUETYPE_MULTIPLE or ControlImpl.VALUETYPE_SINGLE
     */
    public FreemarkerControl(int valueType) {
        this.setValueType(valueType);
    }

    /**
     * Draw control on writer
     * @param out writer to write on
     * @param path path of freemarker template. The template will be automatically searched in classpath or in
     * filesystem
     * @param parameters map of parameters to be used in template
     * @throws IOException exception in template loading
     * @throws TemplateException exception in template rendering
     */
    public void drawHtml(Writer out, String path, Map parameters) throws IOException, TemplateException {

        if (path.startsWith("classpath:")) {
            path = StringUtils.substringAfter(path, "classpath:");
        }

        // render
        FreemarkerHelper.getInstance().render(path, parameters, out);

        // write generic html save info
        ((PrintWriter) out).print(this.getHtmlSaveInfo());
    }
}
