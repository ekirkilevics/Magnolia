/**
 * This file Copyright (c) 2008-2011 Magnolia International
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

import info.magnolia.cms.core.NodeData;
import info.magnolia.cms.gui.control.Edit;
import info.magnolia.cms.gui.misc.CssConstants;
import info.magnolia.cms.util.BooleanUtil;

import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

import javax.jcr.PropertyType;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Turns a textarea into a basic code editor using <a href="http://codemirror.net/">CodeMirror</a>. If no <code>language</code> option is explicitly configured, it will try to guess
 * the correct syntax highlighter by either looking at the resource extension or the <code>mgnl:template</code> metadata property. Defaults to a <code>generic</code> syntax parser which
 * will highlight html, javascript and css.
 * <p>
 * Configuration options are:
 * <ul>
 * <li><strong>useCodeHighlighter</strong>: activate/deactivate the editor with code highlighting. Default value is
 * <code>true</code>. If set as <code>false</code>, falls back to a plain textarea.
 * <li><strong>language</strong>: one of the supported languages (e.g. <em>css</em>, <em>javascript</em> or <em>js</em>, <em>html</em>, <em>freemarker</em> or <em>ftl</em>, <em>groovy</em>) supported.
 *  Default value is <code>generic</code>.
 * <li><strong>readOnly</strong>: make the editor read-only. Default value is <code>false</code>.
 * <li><strong>lineNumbers</strong>: shows/hide line numbers. Default value is <code>true</code>.
 * </ul>
 * @author tmiyar
 * @author fgrilli
 */
public class DialogEditCode extends DialogBox {

    /**
     * Used to make sure that the javascript files are loaded only once.
     */
    private static final String ATTRIBUTE_CODEMIRROR_LOADED = "info.magnolia.cms.gui.dialog.codemirror.loaded";

    private static final Logger log = LoggerFactory.getLogger(DialogEditCode.class);
    /**
     * Valid values are <code>js, javascript, processedJs, css, processedCss, html, freemarker, ftl, groovy, generic</code>.
     */
    public static final Map<String,String> availableParsers = new HashMap<String,String>();

    static {
        availableParsers.put("js", "JSParser");
        availableParsers.put("javascript", "JSParser");
        availableParsers.put("processedJs", "JSParser");
        availableParsers.put("css", "CSSParser");
        availableParsers.put("processedCss", "CSSParser");
        availableParsers.put("html", "HTMLMixedParser");
        availableParsers.put("freemarker", "FreemarkerParser");
        availableParsers.put("ftl", "FreemarkerParser");
        availableParsers.put("groovy", "GroovyParser");
        availableParsers.put("generic", "HTMLMixedParser");
    }

    /**
     * @see info.magnolia.cms.gui.dialog.DialogControl#drawHtml(Writer)
     */
    @Override
    public void drawHtml(Writer out) throws IOException {
        final Edit control = new Edit(this.getName(), this.getValue());
        control.setType(this.getConfigValue("type", PropertyType.TYPENAME_STRING));
        control.setCssClass(CssConstants.CSSCLASS_EDIT);
        if (this.getConfigValue("saveInfo").equals("false")) {
            control.setSaveInfo(false);
        }

        boolean useHighlighter = BooleanUtil.toBoolean(this.getConfigValue("useCodeHighlighter"), true);
        log.debug("useHighlighter? {}",useHighlighter);
        if(!useHighlighter){
            drawSimpleEditor(out, control);
            return;
        }

        final String language = this.getConfigValue("language");
        String parser = null;
        if(StringUtils.isNotBlank(language)){
            parser = getAvailableParser(language.trim());
            if(parser == null){
                log.warn("no suitable parser found for language {}. No syntax highlighting will be available. Look at the documentation for the supported languages. ", language);
                drawSimpleEditor(out, control);
                return;
            }
        }
        drawEditorWithSyntaxHighligher(out, control, parser);
    }

    private void drawSimpleEditor(Writer out, Edit control) throws IOException {
        control.setRows(this.getConfigValue("rows", "1"));
        control.setCssStyles("width", this.getConfigValue("width", "100%"));
        control.setCssStyles("font-family", "Courier New, monospace");
        control.setCssStyles("font-size", "14px");
        if (this.getConfigValue("onchange", null) != null) {
            control.setEvent("onchange", this.getConfigValue("onchange"));
        }
        this.drawHtmlPre(out);
        out.write(control.getHtml());
        this.drawHtmlPost(out);
    }

    private void drawEditorWithSyntaxHighligher(Writer out, Edit control, String parser) throws IOException{
        final String pathToCodeMirror =  this.getRequest().getContextPath() + "/.resources/js/codemirror/";
        if(parser == null) {
            //let's try to guess the language by the resource extension
            final NodeData extNodeData = this.getStorageNode().getNodeData("extension");

            if(extNodeData.isExist()){
                final String ext = extNodeData.getString();
                parser = getAvailableParser(ext);
            } else {
                //try with the template property
                final String template = this.getStorageNode().getMetaData().getTemplate();
                parser = getAvailableParser(template);
            }
            if(parser == null){
                log.debug("No suitable parser found. Falling back to generic.");
                parser = getAvailableParser("generic");
            }
        }

        control.setRows(this.getConfigValue("rows", "25"));
        control.setRows(this.getConfigValue("cols", "100"));

        this.drawHtmlPre(out);
        // load the script once if there are multiple instances
        if (getRequest().getAttribute(ATTRIBUTE_CODEMIRROR_LOADED) == null) {
            out.write("<script type=\"text/javascript\" src=\""+ pathToCodeMirror + "codemirror-min.js\"></script>");
            getRequest().setAttribute(ATTRIBUTE_CODEMIRROR_LOADED, true);
        }

        final StringBuilder inlineStyle = new StringBuilder("<style>\n");
        inlineStyle.append(".CodeMirror-line-numbers {\n");
        inlineStyle.append("background-color: #eee;\n");
        inlineStyle.append("text-align: right;\n");
        inlineStyle.append("font-family: monospace;\n");
        inlineStyle.append("font-size: 10pt;\n");
        inlineStyle.append("color: #aaa;\n");
        inlineStyle.append("line-height: 16px;\n");
        inlineStyle.append("padding: .4em;\n");
        inlineStyle.append("width: 2.2em;\n");
        inlineStyle.append("</style>\n");
        out.write(inlineStyle.toString());

        out.write("<div class=\"editorWrapper\" style=\"border: 1px solid #999; padding: 3px;\">");
        out.write(control.getHtml());

        final boolean lineNumbers = BooleanUtil.toBoolean(this.getConfigValue("lineNumbers"), true);
        final boolean readOnly = BooleanUtil.toBoolean(this.getConfigValue("readOnly"), false);
        final String editorVar = "editor"+this.getName();

        out.write("\n<script>\n");
        out.write("MgnlDHTMLUtil.addOnLoad(function(){\n");
        String codeMirrorEditor = "var " + editorVar +" = CodeMirror.fromTextArea(\""+this.getName()+"\", {\n"+
        "     path: \"" + pathToCodeMirror + "\",\n" +
        "     textWrapping: false,\n" +
        "     height: \"420px\",\n" +
        "     basefiles: [\"codemirror-base.min.js\"],\n" +
        "     parserfile: [\"allinone.js\"],\n" +
        "     stylesheet: [\""+ pathToCodeMirror +"css/jscolors.css\",\""+ pathToCodeMirror +"css/csscolors.css\",\""+
        pathToCodeMirror +"css/xmlcolors.css\",\""+ pathToCodeMirror +"css/freemarkercolors.css\",\""+ pathToCodeMirror +"css/groovycolors.css\"],\n" +
        (lineNumbers ? "     lineNumbers:true,\n":"") +
        (readOnly ? "     readOnly:true,\n":"") +
        "     initCallback:function(e){ \n"+
        "            e.setParser('"+ parser +"');\n"+
        "            e.focus();\n"+
        "     } \n"+
        "});\n";

        out.write(codeMirrorEditor);

        out.write("    var b = document.getElementById('mgnlSaveButton');\n");
        out.write("    var existingOnClick = b.onclick;\n");
        out.write("    b.onclick=function(){\n");
        // on submit put the code into a hidden field.
        out.write("        document.getElementById('cm_hidden_"+ this.getName() + "').value = " + editorVar + ".getCode();\n");
        out.write("        existingOnClick.apply(this);\n");
        out.write("    }\n});\n");
        out.write("</script>\n");
        out.write("<input type=\"hidden\" name=\"" + this.getName() + "\" id=\"cm_hidden_" + this.getName() + "\" />\n");
        out.write("</div><!-- closing editorWrapper -->\n");

        this.drawHtmlPost(out);
    }

    private String getAvailableParser(String language){
        final String parser = availableParsers.get(language);
        log.debug("language is {}, parser is {}", language, parser);
        return parser;
    }
}
