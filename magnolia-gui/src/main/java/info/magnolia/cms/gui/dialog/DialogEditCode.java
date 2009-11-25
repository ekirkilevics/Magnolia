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
import info.magnolia.cms.util.BooleanUtil;

import java.io.IOException;
import java.io.Writer;

import javax.jcr.PropertyType;


/**
 * Turns a textarea into a basic code editor with the <a href="http://codepress.sourceforge.net/">CodePress</a> js
 * library. <strong>Warning</strong>: When the CodePress editor is active, any custom <code>onclick</code> event handler
 * attached to the <em>Save</em> button which submits this dialog will be superseded by the dialog's own specific event
 * handler.
 * <p>
 * Configuration options are:
 * <ul>
 * <li><strong>useCodeHighlighter</strong>: activate/deactivate the editor with code highlighting. Default value is
 * <code>true</code>. If set as <code>false</code>, falls back to a plain textarea.
 * <li><strong>language</strong>: one of the languages (e.g. <em>css</em>, <em>javascript</em>, <em>html</em>) supported
 * by CodePress. Default value is <code>generic</code>.
 * <li><strong>readOnly</strong>: make the editor read-only. Default value is <code>false</code>.
 * <li><strong>lineNumbers</strong>: shows/hide line numbers. Default value is <code>true</code>.
 * </ul>
 * @author tmiyar
 * @author fgrilli
 */
public class DialogEditCode extends DialogBox {

    /**
     * Used to make sure that the javascript files are loaded only once
     */
    private static final String ATTRIBUTE_CODEPRESS_LOADED = "info.magnolia.cms.gui.dialog.codepress.loaded";

    /**
     * @see info.magnolia.cms.gui.dialog.DialogControl#drawHtml(Writer)
     */
    public void drawHtml(Writer out) throws IOException {
        Edit control = new Edit(this.getName(), this.getValue());
        control.setType(this.getConfigValue("type", PropertyType.TYPENAME_STRING)); //$NON-NLS-1$
        if (this.getConfigValue("saveInfo").equals("false")) { //$NON-NLS-1$ //$NON-NLS-2$
            control.setSaveInfo(false);
        }

        boolean isBrowserSupported = false;
        String userAgent = getRequest().getHeader("user-agent");
        if (userAgent != null && !userAgent.matches(".*AppleWebKit.*|.*Opera.*")) {
            isBrowserSupported = true;
        }
        boolean useCodePress = BooleanUtil.toBoolean(this.getConfigValue("useCodeHighlighter"), true)
            && isBrowserSupported;
        if (useCodePress) {
            control.setRows(this.getConfigValue("rows", "25")); //$NON-NLS-1$ //$NON-NLS-2$
            control.setCssStyles("width", this.getConfigValue("width", "100%")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            StringBuilder codePressClasses = new StringBuilder(" codepress ");
            codePressClasses.append(this.getConfigValue("language", "generic "));
            boolean readOnly = BooleanUtil.toBoolean(this.getConfigValue("readOnly"), false);
            boolean lineNumbers = BooleanUtil.toBoolean(this.getConfigValue("lineNumbers"), true);
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
        // load the script once if there are multiple instances
        if (useCodePress && getRequest().getAttribute(ATTRIBUTE_CODEPRESS_LOADED) == null) {
            out.write("<script type=\"text/javascript\" src=\"" //$NON-NLS-1$
                + this.getRequest().getContextPath()
                + "/.resources/js/codepress/codepress.js\"></script>"); //$NON-NLS-1$
            getRequest().setAttribute(ATTRIBUTE_CODEPRESS_LOADED, "true"); //$NON-NLS-1$
        }
        out.write(control.getHtml());
        // on submit put the code into a hidden field. OK, this is really ugly.
        if (useCodePress) {
            out.write("\n<script>\n");
            out.write("MgnlDHTMLUtil.addOnLoad(function(){\n");
            out.write("    var b = document.getElementById('mgnlSaveButton');\n");
            out.write("    var existingOnClick = b.onclick;\n");
            out.write("    b.onclick=function(){\n");
            out.write("        document.getElementById('cp_hidden_"
                + this.getName()
                + "').value = eval('"
                + this.getName()
                + "').getCode();\n");
            out.write("        existingOnClick.apply(this);\n");
            out.write("    }\n});\n");
            out.write("</script>\n");
            out.write("<input type=\"hidden\" name=\"");
            out.write(this.getName());
            out.write("\" id=\"cp_hidden_" + this.getName() + "\" />\n");
        }
        this.drawHtmlPost(out);
    }
}
