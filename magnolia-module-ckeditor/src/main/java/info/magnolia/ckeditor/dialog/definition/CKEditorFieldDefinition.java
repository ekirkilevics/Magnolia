/**
 * This file Copyright (c) 2010-2011 Magnolia International
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
package info.magnolia.ckeditor.dialog.definition;

import info.magnolia.context.MgnlContext;
import info.magnolia.ui.model.dialog.definition.RichEditFieldDefinition;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TODO <ul>
 * <li>clean up and adapt to our config needs.
 * <li>Configuration loading order (i.e. when custom js config is defined, should it loaded before or after inPageConfig?Should it only override default/inPageConfig opions)?
 * <li>I tried to keep old config options but must check if all of them are still supported and with which name.
 * <li>Do we want to add new config options?
 * </ul>
 *<p>
 * Configuration utility for using the CKEditorTextField.  You can use this "config javascript builder" object for our
 * tested/common options, or just set the options using a JavaScript/JSON string as you prefer.
 * <p>
 * Adapted from http://vaadin.com/directory#addon/ckeditor-wrapper-for-vaadin.
 */
public class CKEditorFieldDefinition extends RichEditFieldDefinition implements java.io.Serializable {

    private static final Logger log = LoggerFactory.getLogger(CKEditorFieldDefinition.class);

    private static final long serialVersionUID = 4360029660001644525L;

    // If this is set, we'll just use it and ignore everything else.
    private String inPageConfig;

    private String jsConfigFile;

    private String css;

    private String height;

    private String width;

    private String tables;

    private String lists;

    private String alignment;

    private String images;

    private String styles;

    private String templates;

    private String fonts;

    private String fontSizes;

    private String colors;

    private String source;

    private String enterMode;

    private String shiftEnterMode;

    private String showSpellChecker;

    private String spellChecker;

    /**
     * The new .BasePath of the editor.
     */
    public static final String FCKEDIT_PATH = "/.resources/ckeditor/";

    /**
     * This parameter defines the startup script. This parameter is searched in the dialog configuration.
     */
    public static final String PARAM_JS_INIT_FILE = "jsInitFile";

    /**
     * Default values.
     */
    public static final String PARAM_JS_INIT_FILE_DEFAULT = "/.resources/ckeditor/custom/init/magnoliaStandard.js";

    public static final String PARAM_CUSTOM_CONFIGURATION_PATH_DEFAULT = "/.resources/ckeditor/custom/config/magnoliaStandard.js";

    public static final String PARAM_CSS_DEFAULT = "/.resources/ckeditor/custom/css/magnoliaStandard.css";

    public static final String PARAM_HEIGHT_DEFAULT = "";

    public static final String PARAM_WIDTH_DEFAULT = "";

    public static final String PARAM_TABLES_DEFAULT = "false";

    public static final String PARAM_IMAGES_DEFAULT = "false";

    public static final String PARAM_STYLES_DEFAULT = "";

    public static final String PARAM_TEMPLATES_DEFAULT = "";

    public static final String PARAM_FONTS_DEFAULT = "";

    public static final String PARAM_FONT_SIZES_DEFAULT = "";

    public static final String PARAM_SOURCE_DEFAULT = "false";

    private static final String PARAM_COLORS_DEFAULT = "";

    private static final String PARAM_LISTS_DEFAULT = "true";

    private static final String PARAM_ALIGNMENT_DEFAULT = "false";

    private static final String PARAM_ENTER_MODE_DEFAULT = "p";

    private static final String PARAM_SHIFT_ENTER_MODE_DEFAULT = "br";

    private static final String PARAM_SHOW_SPELL_CHECKER_DEFAULT = "true";

    private static final String PARAM_SPELL_CHECKER_DEFAULT = ""; // 'WSC' | 'SCAYT' | 'SpellerPages' | 'ieSpell'

    /**
     * The spell checker providers supported by fckEditor.
     * @author fgrilli
     *
     */
    public enum SpellCheckerProviders {

        WSC, SCAYT, SpellerPages, ieSpell
    }

    public CKEditorFieldDefinition() {
    }

    public String getInPageConfig() {
        if ( inPageConfig != null ) {
            return inPageConfig;
        }

        String css = StringUtils.defaultString(getCss(), PARAM_CSS_DEFAULT);
        String fonts = StringUtils.defaultString(getFonts(), PARAM_FONTS_DEFAULT);
        String fontSizes = StringUtils.defaultString(getFontSizes(), PARAM_FONT_SIZES_DEFAULT);
        String colors = StringUtils.defaultString(getColors(), PARAM_COLORS_DEFAULT);
        String styles = StringUtils.defaultString(getStyles(), PARAM_STYLES_DEFAULT);
        String templates = StringUtils.defaultString(getTemplates(), PARAM_TEMPLATES_DEFAULT);

        String lists = StringUtils.defaultString(getLists(), PARAM_LISTS_DEFAULT);
        String alignment = StringUtils.defaultString(getAlignment(), PARAM_ALIGNMENT_DEFAULT);
        String tables = StringUtils.defaultString(getTables(), PARAM_TABLES_DEFAULT);
        String images = StringUtils.defaultString(getImages(), PARAM_IMAGES_DEFAULT);
        String source = StringUtils.defaultString(getSource(), PARAM_SOURCE_DEFAULT);
        String enterMode = StringUtils.defaultString(getEnterMode(), PARAM_ENTER_MODE_DEFAULT);
        String shiftEnterMode = StringUtils.defaultString(getShiftEnterMode(), PARAM_SHIFT_ENTER_MODE_DEFAULT);
        String showSpellChecker = StringUtils.defaultString(getShowSpellChecker(), PARAM_SHOW_SPELL_CHECKER_DEFAULT);
        String spellChecker = StringUtils.defaultString(getSpellChecker(), PARAM_SPELL_CHECKER_DEFAULT);

        StringBuilder config = new StringBuilder("{ ");
        appendJSONConfig(config, "contentsCss:'" + css + "'");
        appendJSONConfig(config, "language:'"+ MgnlContext.getUser().getLanguage() +"'");
        config.append("}");

        //log.debug("ckeditor config is {}",config);
        return null;
        //return config.toString();

        // create the the holder of the editors configs if not yet done
//        out.write("if( window.MgnlFCKConfigs == null)\n");
//        out.write("    window.MgnlFCKConfigs = new Object();\n");
//
//        // add the config for this editor
//
//        out.write("MgnlFCKConfigs." + id + " = new Object();\n");
//        // string values
//        out.write("MgnlFCKConfigs." + id + ".language = '" + MgnlContext.getUser().getLanguage() + "';\n");
//        out.write("MgnlFCKConfigs." + id + ".contextPath = '" + getRequest().getContextPath() + "';\n");
//
//        out.write("MgnlFCKConfigs." + id + ".repository = '" + getTopParent().getConfigValue("repository") + "';\n");
//        out.write("MgnlFCKConfigs." + id + ".path = '" + getTopParent().getConfigValue("path") + "';\n");
//        out.write("MgnlFCKConfigs."
//                + id
//                + ".nodeCollection = '"
//                + getTopParent().getConfigValue("nodeCollection")
//                + "';\n");
//        out.write("MgnlFCKConfigs." + id + ".node = '" + getTopParent().getConfigValue("node") + "';\n");
//
//        out.write("MgnlFCKConfigs." + id + ".css = '" + css + "';\n");
//        out.write("MgnlFCKConfigs." + id + ".fonts = '" + fonts + "';\n");
//        out.write("MgnlFCKConfigs." + id + ".fontSizes = '" + fontSizes + "';\n");
//        out.write("MgnlFCKConfigs." + id + ".colors = '" + colors + "';\n");
//        out.write("MgnlFCKConfigs." + id + ".styles = '" + styles + "';\n");
//        out.write("MgnlFCKConfigs." + id + ".templates = '" + templates + "';\n");
//        out.write("MgnlFCKConfigs." + id + ".enterMode = '" + enterMode + "';\n");
//        out.write("MgnlFCKConfigs." + id + ".shiftEnterMode = '" + shiftEnterMode + "';\n");
//
//        if(StringUtils.isNotBlank(spellChecker)) {
//            if(isSpellCheckerValid(spellChecker)){
//                out.write("MgnlFCKConfigs." + id + ".spellChecker = '" + spellChecker + "';\n");
//                out.write("MgnlFCKConfigs." + id + ".showSpellChecker = " + showSpellChecker + ";\n");
//            } else {
//                log.warn("Invalid spellchecker {} configured for fckEditor. Valid options are {}", spellChecker, SpellCheckerProviders.values());
//                out.write("alert('" + spellChecker + " is not a valid spell checker. Spell checking will not be available.');\n");
//            }
//        }
//
//        // boolean values
//        out.write("MgnlFCKConfigs." + id + ".lists = " + lists + ";\n");
//        out.write("MgnlFCKConfigs." + id + ".alignment = " + alignment + ";\n");
//        out.write("MgnlFCKConfigs." + id + ".tables = " + tables + ";\n");
//        out.write("MgnlFCKConfigs." + id + ".images = " + images + ";\n");
//        out.write("MgnlFCKConfigs." + id + ".source = " + source + ";\n");

//         Build the JSON config
//                StringBuilder config = new StringBuilder(1024);
//                config.append("{ "); // we assume this is 2 chars in the buffer append routines to know whether anything more has been appended yet
//
//                if ( customToolbarLines != null ) {
//
//                    StringBuilder buf = new StringBuilder();
//                    ListIterator<String> iter = customToolbarLines.listIterator();
//                    while( iter.hasNext() ) {
//                        if ( buf.length() > 0 )
//                            buf.append(",'/',");
//                        String js = iter.next();
//                        if ( js.startsWith("[") ) { // if it has a band, assume fully banded
//                            buf.append(js);
//                        } else {
//                            buf.append("[").append(js).append("]"); // put entire line in one band
//                        }
//                    }
//
//                    appendJSONConfig(config, "toolbar : 'Custom'");
//                    appendJSONConfig(config, "toolbar_Custom : [" + buf.toString() + "]");
//                }
//
//                if ( toolbarCanCollapse != null ) {
//                    appendJSONConfig(config, "toolbarCanCollapse : " + toolbarCanCollapse);
//                }
//
//                if ( resizeDir != null ) {
//                    appendJSONConfig(config, "resize_dir : '" + resizeDir + "'");
//                }
//
//                if ( extraPlugins != null ) {
//                    StringBuilder buf = new StringBuilder();
//                    ListIterator<String> iter = extraPlugins.listIterator();
//                    while( iter.hasNext() ) {
//                        if ( buf.length() > 0 )
//                            buf.append(",");
//                        buf.append(iter.next());
//                    }
//
//                    appendJSONConfig(config, "extraPlugins : '" + buf.toString() + "'");
//                }
//
//                if ( removePlugins != null ) {
//                    StringBuilder buf = new StringBuilder();
//                    ListIterator<String> iter = removePlugins.listIterator();
//                    while( iter.hasNext() ) {
//                        if ( buf.length() > 0 )
//                            buf.append(",");
//                        buf.append(iter.next());
//                    }
//
//                    appendJSONConfig(config, "removePlugins : '" + buf.toString() + "'");
//                }
//
//                if ( width != null ) {
//                    appendJSONConfig(config, "width : '" + width + "'");
//                }
//
//                if ( height != null ) {
//                    appendJSONConfig(config, "height : '" + height + "'");
//                }
//
//                if ( baseFloatZIndex != null ) {
//                    appendJSONConfig(config, "baseFloatZIndex : " + baseFloatZIndex);
//                }
//
//                if ( pasteFromWordNumberedHeadingToList != null ) {
//                    appendJSONConfig(config, "pasteFromWordNumberedHeadingToList : " + pasteFromWordNumberedHeadingToList);
//                }
//
//                if ( startupMode != null ) {
//                    appendJSONConfig(config, "startupMode : '" + startupMode + "'");
//                }
//
//                if ( contentsCssFiles != null && contentsCssFiles.length > 0 ) {
//                    if ( contentsCssFiles.length == 1 ) {
//                        appendJSONConfig(config, "contentsCss : '" + contentsCssFiles[0] + "'");
//                    } else {
//                        StringBuilder buf = new StringBuilder();
//                        for( String file : contentsCssFiles ) {
//                            if ( buf.length() > 0 )
//                                buf.append(",");
//                            buf.append("'").append(file).append("'");
//                        }
//                        appendJSONConfig(config, "contentsCss : [" + buf.toString() + "]");
//                    }
//                }
//
//                if ( bodyClass != null ) {
//                    appendJSONConfig(config, "bodyClass : '" + bodyClass + "'");
//                }
//
//                if ( disableNativeSpellChecker != null ) {
//                    appendJSONConfig(config, "disableNativeSpellChecker : " + disableNativeSpellChecker);
//                }
//
//                if ( stylesCombo_stylesSet != null ) {
//                    appendJSONConfig(config, "stylesCombo_stylesSet : '" + stylesCombo_stylesSet + "'");
//                }
//
//                if ( filebrowserBrowseUrl != null ) {
//                    appendJSONConfig(config, "filebrowserBrowseUrl : '" + filebrowserBrowseUrl + "'");
//                }
//                if ( filebrowserUploadUrl != null ) {
//                    appendJSONConfig(config, "filebrowserUploadUrl : '" + filebrowserUploadUrl + "'");
//                }
//                if ( filebrowserWindowWidth != null ) {
//                    appendJSONConfig(config, "filebrowserWindowWidth : '" + filebrowserWindowWidth + "'");
//                }
//                if ( filebrowserWindowHeight != null ) {
//                    appendJSONConfig(config, "filebrowserWindowHeight : '" + filebrowserWindowHeight + "'");
//                }
//
//                if ( filebrowserImageBrowseUrl != null ) {
//                    appendJSONConfig(config, "filebrowserImageBrowseUrl : '" + filebrowserImageBrowseUrl + "'");
//                }
//                if ( filebrowserImageUploadUrl != null ) {
//                    appendJSONConfig(config, "filebrowserImageUploadUrl : '" + filebrowserImageUploadUrl + "'");
//                }
//                if ( filebrowserImageWindowWidth != null ) {
//                    appendJSONConfig(config, "filebrowserImageWindowWidth : '" + filebrowserImageWindowWidth + "'");
//                }
//                if ( filebrowserImageWindowHeight != null ) {
//                    appendJSONConfig(config, "filebrowserImageWindowHeight : '" + filebrowserImageWindowHeight + "'");
//                }
//
//                if ( filebrowserFlashBrowseUrl != null ) {
//                    appendJSONConfig(config, "filebrowserFlashBrowseUrl : '" + filebrowserFlashBrowseUrl + "'");
//                }
//                if ( filebrowserFlashUploadUrl != null ) {
//                    appendJSONConfig(config, "filebrowserFlashUploadUrl : '" + filebrowserFlashUploadUrl + "'");
//                }
//                if ( filebrowserLinkBrowseUrl != null ) {
//                    appendJSONConfig(config, "filebrowserLinkBrowseUrl : '" + filebrowserLinkBrowseUrl + "'");
//                }
//
//                config.append(" }");
//                return config.toString();
    }

    private StringBuilder appendJSONConfig(StringBuilder configBuf, String oneOptions) {
        if ( configBuf.length() > 2 ) {
            configBuf.append(", ");
        }
        configBuf.append(oneOptions);
        return configBuf;
    }

    /**
     * You can use this to just set the JavaScript/JSON notation for setting the 'in page config' option when the editor is
     * created.  If you don't use this, you can use all of the other builder routines that set options that are then used
     * to generate the JSON notation to use
     * @param js the String JSON 'config' for the new editor instance.
     */
    public void setInPageConfig(String js) {
        inPageConfig = js;
    }

    public String getJsConfigFile() {
        return jsConfigFile;
    }

    public void setJsConfigFile(String jsConfigFile) {
        this.jsConfigFile = jsConfigFile;
    }

    public String getCss() {
        return css;
    }

    public void setCss(String css) {
        this.css = css;
    }

    public String getHeight() {
        return height;
    }

    public void setHeight(String height) {
        this.height = height;
    }

    public String getWidth() {
        return width;
    }

    public void setWidth(String width) {
        this.width = width;
    }

    public String getTables() {
        return tables;
    }

    public void setTables(String tables) {
        this.tables = tables;
    }

    public String getLists() {
        return lists;
    }

    public void setLists(String lists) {
        this.lists = lists;
    }

    public String getAlignment() {
        return alignment;
    }

    public void setAlignment(String alignment) {
        this.alignment = alignment;
    }

    public String getImages() {
        return images;
    }

    public void setImages(String images) {
        this.images = images;
    }

    public String getStyles() {
        return styles;
    }

    public void setStyles(String styles) {
        this.styles = styles;
    }

    public String getTemplates() {
        return templates;
    }

    public void setTemplates(String templates) {
        this.templates = templates;
    }

    public String getFonts() {
        return fonts;
    }

    public void setFonts(String fonts) {
        this.fonts = fonts;
    }

    public String getFontSizes() {
        return fontSizes;
    }

    public void setFontSizes(String fontSizes) {
        this.fontSizes = fontSizes;
    }

    public String getColors() {
        return colors;
    }

    public void setColors(String colors) {
        this.colors = colors;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getEnterMode() {
        return enterMode;
    }

    public void setEnterMode(String enterMode) {
        this.enterMode = enterMode;
    }

    public String getShiftEnterMode() {
        return shiftEnterMode;
    }

    public void setShiftEnterMode(String shiftEnterMode) {
        this.shiftEnterMode = shiftEnterMode;
    }

    public String getShowSpellChecker() {
        return showSpellChecker;
    }

    public void setShowSpellChecker(String showSpellChecker) {
        this.showSpellChecker = showSpellChecker;
    }

    public String getSpellChecker() {
        return spellChecker;
    }

    public void setSpellChecker(String spellChecker) {
        this.spellChecker = spellChecker;
    }
}
