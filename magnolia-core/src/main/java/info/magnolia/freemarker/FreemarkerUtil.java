/**
 * This file Copyright (c) 2003-2008 Magnolia International
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
package info.magnolia.freemarker;

import freemarker.template.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.commons.lang.StringUtils;

import java.util.Map;
import java.util.HashMap;
import java.io.Writer;
import java.io.StringWriter;
import java.io.PrintWriter;

import info.magnolia.cms.util.AlertUtil;

/**
 * A bunch of utility methods to render freemarker templates into Strings.
 *
 * @see info.magnolia.freemarker.FreemarkerHelper
 *
 * @author Philipp Bracher
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class FreemarkerUtil {
    private static final Logger log = LoggerFactory.getLogger(FreemarkerUtil.class);

    /**
     * Uses the class of the object to create the templates name, passes the object under the name 'this'
     * and returns the result in a String.
     */
    public static String process(Object thisObj) {
        return process(thisObj.getClass(), thisObj);
    }

    public static String process(Object thisObj, String classifier, String ext) {
        return process(thisObj.getClass(), thisObj, classifier, ext);
    }

    /**
     * Default extension is html
     */
    public static String process(Class klass, Object thisObj) {
        return process(klass, thisObj, "html");
    }

    /**
     * Uses the class to create the templates name and passes the object under the name 'this'
     */
    public static String process(Class klass, Object thisObj, String ext) {
        final Map data = new HashMap();
        data.put("this", thisObj);
        String template = createTemplateName(klass, ext);
        return process(template, data);
    }

    public static String process(Class klass, Object thisObj, String classifier, String ext) {
        final Map data = new HashMap();
        data.put("this", thisObj);
        String template = createTemplateName(klass, classifier, ext);
        return process(template, data);
    }

    /**
     * Uses the class to create the templates name.
     *
     * @deprecated not used (only by this class)
     */
    public static String process(Class klass, Map data, String ext) {
        return process(createTemplateName(klass, ext), data);
    }

    /**
     * Process this template with the passed data and returns the result in a String.
     */
    public static String process(String name, Map data) {
        final Writer writer = new StringWriter();
        process(name, data, writer);
        return writer.toString();
    }

    /**
     * Process the template with the data and writes the result to the writer.
     * TODO : move this to FreemarkerHelper
     */
    public static void process(String name, Map data, Writer writer) {
        try {
            // add some usfull default data
            if (AlertUtil.isMessageSet()) {
                data.put("message", AlertUtil.getMessage());
            }
            FreemarkerHelper.getInstance().render(name, data, writer);
        }
        catch (Exception e) {
            e.printStackTrace(new PrintWriter(writer));
            log.error("exception in template", e);
        }
    }

    public static String createTemplateName(Class klass, String ext) {
        return "/" + StringUtils.replace(klass.getName(), ".", "/") + "." + ext;
    }

    public static String createTemplateName(Class klass, String classifier , String ext) {
        return "/" + StringUtils.replace(klass.getName(), ".", "/") + StringUtils.capitalize(classifier) + "." + ext;
    }

    /**
     * @return get default static freemarker configuration
     * @deprecated don't mess around, just use FreemarkerHelper
     */
    public static Configuration getDefaultConfiguration() {
        return FreemarkerHelper.getInstance().getConfiguration();
    }
}
