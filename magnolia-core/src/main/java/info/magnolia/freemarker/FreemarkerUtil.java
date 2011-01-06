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
package info.magnolia.freemarker;

import info.magnolia.cms.util.AlertUtil;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

/**
 * A bunch of utility methods to render freemarker templates into Strings.
 *
 * @author Philipp Bracher
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 * @see info.magnolia.freemarker.FreemarkerHelper
 */
public class FreemarkerUtil {
    private static final Logger log = LoggerFactory.getLogger(FreemarkerUtil.class);

    /**
     * Uses the class of the object to create the templates name, passes the object under the name 'this'
     * and returns the result in a String.
     * Used only in DialogMultiSelect - VersionCommentPopup - Inbox - SubPagesControl
     */
    public static String process(Object thisObj) {
        final Writer writer = new StringWriter();
        process(thisObj, writer);
        return writer.toString();
    }

    /**
     * @see #process(Object)
     */
    public static void process(Object thisObj, Writer out) {
        process(thisObj.getClass(), thisObj, out);
    }

    /**
     * Same as {@link #process(Object)} but adds the classifier and the extension to the template name.
     */
    public static String process(Object thisObj, String classifier, String ext) {
        final Writer writer = new StringWriter();
        process(thisObj, classifier, ext, writer);
        return writer.toString();
    }

    /**
     * @see #process(Object, String, String)
     */
    public static void process(Object thisObj, String classifier, String ext, Writer out) {
        final Map data = new HashMap();
        data.put("this", thisObj);
        String template = createTemplateName(thisObj.getClass(), classifier, ext);
        process(template, data, out);
    }

    /**
     * Uses the class to create the templates name and passes the object under the name 'this'.
     * Uses "html" as template filename extension.
     * Only used in AbstractSimpleSearchList and VersionsList.
     */
    public static String process(Class klass, Object thisObj) {
        final Writer writer = new StringWriter();
        process(klass, thisObj, writer);
        return writer.toString();
    }

    /**
     * @see #process(Class, Object)
     */
    public static void process(Class klass, Object thisObj, Writer out) {
        final Map data = new HashMap();
        data.put("this", thisObj);
        String template = createTemplateName(klass, "html");
        process(template, data, out);
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
            // add some useful default data
            if (AlertUtil.isMessageSet()) {
                data.put("message", AlertUtil.getMessage());
            }
            FreemarkerHelper.getInstance().render(name, data, writer);
        }
        catch (Exception e) {
            e.printStackTrace(new PrintWriter(writer));
            log.error("Exception while processing template " + name, e);
        }
    }

    /**
     * Creates a template name based on the class name and adds the extension.
     * If the class is org.mydomain.TheClass the returned name is /org/mydomain/TheClass.html.
     */
    public static String createTemplateName(Class klass, String ext) {
        return createTemplateName(klass, null, ext);
    }

    /**
     * Same as {@link #createTemplateName(Class, String)} but adds the classifier between
     * the template name and the extension.
     */
    public static String createTemplateName(Class klass, String classifier, String ext) {
        classifier = (classifier != null) ? StringUtils.capitalize(classifier) : "";
        return "/" + StringUtils.replace(klass.getName(), ".", "/") + classifier + "." + ext;
    }

}
