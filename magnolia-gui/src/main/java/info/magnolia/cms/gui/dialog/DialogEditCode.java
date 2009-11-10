/**
 * This file Copyright (c) 2008-2009 Magnolia International
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
package info.magnolia.cms.gui.dialog;

import info.magnolia.cms.gui.control.Edit;
import info.magnolia.cms.gui.misc.CssConstants;

import java.io.IOException;
import java.io.Writer;

import javax.jcr.PropertyType;

import org.apache.commons.lang.StringUtils;


/**
 * Turns a textarea into a basic code editor with the <a href="http://codepress.sourceforge.net/">CodePress</a> js
 * library. To handle submits correctly it is <strong>mandatory</strong> to configure the dialog with the
 * <code>saveOnclick</code> node set as <code>CodePress.submitForm()</code>. If this value is not set correctly, falls
 * back to a plain textarea. Control (optional) configuration values are:
 * <ul>
 * <li><strong>language</strong>: one of the languages (e.g. 'css', 'javascript', 'html') supported by CodePress.
 * Default value is <code>generic</code>
 * <li><strong>readOnly</strong>: make the editor read-only. Default value is <code>false</code>
 * <li><strong>lineNumbers</strong>: shows/hide line numbers. Default value is <code>true</code>
 * </ul>
 * @author tmiyar
 * @version $Id$
 */
public class DialogEditCode extends DialogBox {

    protected static final String SAVE_ONCLICK = "CodePress.submitForm()";

    /**
     * @see info.magnolia.cms.gui.dialog.DialogControl#drawHtml(Writer)
     */
    public void drawHtml(Writer out) throws IOException {
        Edit control = new Edit(this.getName(), this.getValue());
        control.setType(this.getConfigValue("type", PropertyType.TYPENAME_STRING)); //$NON-NLS-1$
        if (this.getConfigValue("saveInfo").equals("false")) { //$NON-NLS-1$ //$NON-NLS-2$
            control.setSaveInfo(false);
        }
        String saveOnclick = this.getTopParent().getConfigValue("saveOnclick");
        // use CodePress only if the saveOnclick value is correctly set,
        // else fall back to plain textarea with sans type font
        Boolean useCodePress = StringUtils.isNotEmpty(saveOnclick)
            && SAVE_ONCLICK.equals(saveOnclick.trim().replaceAll(";", ""));
        
        if (useCodePress) {
            control.setRows(this.getConfigValue("rows", "25")); //$NON-NLS-1$ //$NON-NLS-2$
            control.setCssStyles("width", this.getConfigValue("width", "100%")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            StringBuilder codePressClasses = new StringBuilder(" codepress ");
            codePressClasses.append(this.getConfigValue("language", "generic "));
            Boolean readOnly = Boolean.valueOf(this.getConfigValue("readOnly", "false"));
            Boolean lineNumbers = Boolean.valueOf(this.getConfigValue("lineNumbers", "true"));
            codePressClasses.append(readOnly ? " readonly-on " : " readonly-off ");
            codePressClasses.append(lineNumbers ? " linenumbers-on " : " linenumbers-off ");
            control.setCssClass(CssConstants.CSSCLASS_EDIT + codePressClasses.toString());
        }
        else {
            control.setCssClass(CssConstants.CSSCLASS_EDIT);
            control.setRows(this.getConfigValue("rows", "1"));
            control.setCssStyles("width", this.getConfigValue("width", "100%"));
            control.setCssStyles("font-family", "Courier New, monospace");
            control.setCssStyles("font-size", "14px");
            if (this.getConfigValue("onchange", null) != null) {
                control.setEvent("onchange", this.getConfigValue("onchange"));
            }
        }

        this.drawHtmlPre(out);
        out.write(control.getHtml());
        //on submitting the dialog put the code into this hidden field. See codepress.js#submitForm() 
        if (useCodePress) {
            out.write("<input type=\"hidden\" name=\"");
            out.write(this.getName());
            out.write("\" class=\"codepress\" />");
        }
        this.drawHtmlPost(out);
    }
}