/**
 * This file Copyright (c) 2011 Magnolia International
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

import java.text.MessageFormat;
import java.util.ArrayList;

import net.sf.json.JSONArray;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TODO write javadoc. There are still options to be ported from old configuration mechanism.
 * See commented out javascript code in class body.
 *
 * @version $Id$
 *
 */
public class CKEditorDefaultConfiguration {


    private static final Logger log = LoggerFactory.getLogger(CKEditorDefaultConfiguration.class);

    public static final String PARAM_CSS_DEFAULT = "custom/css/magnoliaStandard.css";

    public static final String PARAM_SKIN_DEFAULT = "magnolia,custom/skin/magnolia/";

    public static final String PARAM_REMOVE_DIALOG_TABS_DEFAULT = "link:advanced,image:advanced";

    public static final String PARAM_HEIGHT_DEFAULT = "";

    public static final String PARAM_WIDTH_DEFAULT = "";

    public static final String PARAM_STYLES_DEFAULT = "";

    public static final String PARAM_TEMPLATES_DEFAULT = "";

    public static final String PARAM_FONTS_DEFAULT = "";

    public static final String PARAM_FONT_SIZES_DEFAULT = "";

    public static final String PARAM_COLORS_DEFAULT = "";

    public static final String PARAM_ENTER_MODE_DEFAULT = "p";

    public static final String PARAM_SHIFT_ENTER_MODE_DEFAULT = "br";

    //public static final String PARAM_SPELL_CHECKER_DEFAULT = ""; // 'WSC' | 'SCAYT' | 'SpellerPages' | 'ieSpell'

    public static final String PARAM_TOOLBAR_DEFAULT = "Magnolia";
    /**
     * Represents a javascript array containing the minimum items for default Magnolia toolbar.
     */
    public static final String PARAM_MAGNOLIA_TOOLBAR_DEFAULT = "[[\"Bold\", \"Italic\", \"SpecialChar\", \"-\", \"Link\", \"Unlink\", \"-\", \"Copy\", \"Paste\", \"PasteWord\", \"-\", \"NumberedList\", \"BulletedList\",\"-\", \"Undo\", \"Redo\", \"-\", \"SpellChecker\"]]";

    // params are 0 = alignment, 1 = lists, 2 = tables, 3 = images, 4 = source, 5 = spellCheck
    protected static final String TOOLBAR_DEFINITION_PATTERN = "[[\"Bold\", \"Italic\", \"SpecialChar\", \"-\", \"Link\", \"Unlink\", \"-\",\"Copy\", \"Paste\", \"PasteWord\", {0} {1} {2} {3} \"-\", \"Undo\", \"Redo\" {4} {5}]]";


    private String customConfig;
    private String contentCss = PARAM_CSS_DEFAULT;
   /**
    * TODO skin must be probably rewritten from scratch due to breaking changes introduced by CKEditor. Therefore it is disabled for the time being.
    * Magnolia skin. It may be the name of the skin folder inside the editor installation path, or the name and the path separated by a comma.
    * CKEDITOR.config.skin = 'magnolia,'+ CKEDITOR.basePath + 'custom/skin/magnolia/';
    */
    private String skin;
    // Always auto detecting unless the language configuration is specified. Falls back to CKEditor.defaultLanguage.
    private String language;
    // line breaks
    private String enterMode = PARAM_ENTER_MODE_DEFAULT;
    private String shiftEnterMode = PARAM_SHIFT_ENTER_MODE_DEFAULT;
    //toolbar name: here we don't set any default. See createToolbar() and getToolbar()
    private String toolbar;
    private boolean toolbarCanCollapse = true;
    private String removeDialogTabs = PARAM_REMOVE_DIALOG_TABS_DEFAULT;
    private String filebrowserLinkBrowseUrl;
    private String filebrowserImagerowseUrl;
    private String filebrowserLinkUploadUrl;
    private String filebrowserImageUploadUrl;
    private String filebrowserFlashUploadUrl;
    private boolean alignment = false;
    private boolean lists = true;
    private boolean tables = false;
    private boolean images = false;
    private boolean source = false;
    private boolean spellChecker = true;
    private Object [] toolbar_Magnolia;

    protected Object[] createToolbar(){
        ArrayList<String> args = new ArrayList<String>();
        if(isAlignment()){
            args.add("'-', 'JustifyLeft', 'JustifyCenter', 'JustifyRight', 'JustifyFull',");
        } else {
            args.add("");
        }
        if(isLists()){
            args.add("'-', 'NumberedList', 'BulletedList',");
        } else {
            args.add("");
        }
        if(isTables()){
            args.add("'-', 'Table',");
        } else {
            args.add("");
        }
        if(isImages()){
            args.add("'-', 'Image', 'Flash',");
        } else {
            args.add("");
        }
        if(isSource()){
            args.add(",'-', 'Source',");
        } else {
            args.add("");
        }
        if(isSpellChecker()){
            args.add(",'-', 'SpellChecker',");
        } else {
            args.add("");
        }
        String toolbar = MessageFormat.format(TOOLBAR_DEFINITION_PATTERN, args.toArray());
        //no comma preceding ]
        toolbar = toolbar.replaceAll(",\\s*]", "]");
        Object[] array = JSONArray.fromObject(toolbar).toArray();
        log.debug("createToolbar returns {}", toolbar);
        return array;
    }

    /**
     * The non conventional method name with underscore is needed because CKEditor expects a javascript array named toolbar_Magnolia
     * and this in turn is built with json-lib when calling this method.
     */
    public Object[] getToolbar_Magnolia() {
        if(!StringUtils.equalsIgnoreCase(toolbar, PARAM_TOOLBAR_DEFAULT)){
            toolbar_Magnolia = createToolbar();
            toolbar = PARAM_TOOLBAR_DEFAULT;
        }
        return toolbar_Magnolia;
    }

    public String getCustomConfig() {
        return customConfig;
    }

    public void setCustomConfig(String customConfig) {
        this.customConfig = customConfig;
    }

    public String getContentCss() {
        return contentCss;
    }

    public void setContentCss(String contentCss) {
        this.contentCss = contentCss;
    }

    public String getSkin() {
        return skin;
    }

    public void setSkin(String skin) {
        this.skin = skin;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
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

    public String getToolbar() {
        if(!StringUtils.equalsIgnoreCase(toolbar, PARAM_TOOLBAR_DEFAULT)){
            toolbar_Magnolia = createToolbar();
            toolbar = PARAM_TOOLBAR_DEFAULT;
        }
        return toolbar;
    }

    public void setToolbar(String toolbar) {
        this.toolbar = toolbar;
    }

    public boolean isToolbarCanCollapse() {
        return toolbarCanCollapse;
    }

    public void setToolbarCanCollapse(boolean toolbarCanCollapse) {
        this.toolbarCanCollapse = toolbarCanCollapse;
    }

    public String getRemoveDialogTabs() {
        return removeDialogTabs;
    }

    public void setRemoveDialogTabs(String removeDialogTabs) {
        this.removeDialogTabs = removeDialogTabs;
    }

    public String getFilebrowserLinkBrowseUrl() {
        return filebrowserLinkBrowseUrl;
    }

    public void setFilebrowserLinkBrowseUrl(String filebrowserLinkBrowseUrl) {
        this.filebrowserLinkBrowseUrl = filebrowserLinkBrowseUrl;
    }

    public String getFilebrowserImagerowseUrl() {
        return filebrowserImagerowseUrl;
    }

    public void setFilebrowserImagerowseUrl(String filebrowserImagerowseUrl) {
        this.filebrowserImagerowseUrl = filebrowserImagerowseUrl;
    }

    public String getFilebrowserLinkUploadUrl() {
        return filebrowserLinkUploadUrl;
    }

    public void setFilebrowserLinkUploadUrl(String filebrowserLinkUploadUrl) {
        this.filebrowserLinkUploadUrl = filebrowserLinkUploadUrl;
    }

    public String getFilebrowserImageUploadUrl() {
        return filebrowserImageUploadUrl;
    }

    public void setFilebrowserImageUploadUrl(String filebrowserImageUploadUrl) {
        this.filebrowserImageUploadUrl = filebrowserImageUploadUrl;
    }

    public String getFilebrowserFlashUploadUrl() {
        return filebrowserFlashUploadUrl;
    }

    public void setFilebrowserFlashUploadUrl(String filebrowserFlashUploadUrl) {
        this.filebrowserFlashUploadUrl = filebrowserFlashUploadUrl;
    }

    public boolean isAlignment() {
        return alignment;
    }

    public void setAlignment(boolean alignment) {
        this.alignment = alignment;
    }

    public boolean isLists() {
        return lists;
    }

    public void setLists(boolean lists) {
        this.lists = lists;
    }

    public boolean isTables() {
        return tables;
    }

    public void setTables(boolean tables) {
        this.tables = tables;
    }

    public boolean isImages() {
        return images;
    }

    public void setImages(boolean images) {
        this.images = images;
    }

    public boolean isSource() {
        return source;
    }

    public void setSource(boolean source) {
        this.source = source;
    }

    public boolean isSpellChecker() {
        return spellChecker;
    }

    public void setSpellChecker(boolean spellChecker) {
        this.spellChecker = spellChecker;
    }

    /* TODO to be ported
    if (MgnlFCKConfig.styles != '' || MgnlFCKConfig.templates != '' || MgnlFCKConfig.fonts != '' || MgnlFCKConfig.fontSizes != '' || MgnlFCKConfig.colors != '' || MgnlFCKConfig.bgColors) {
    toolbar[1] = '/';
    toolbar[2] = new Array();

    if (MgnlFCKConfig.templates != '') {
        toolbar[2].push('Templates');
        //TODO not ported to CKEditor. See http://docs.cksource.com/CKEditor_3.x/Developers_Guide/Templates
        FCKConfig.TemplatesXmlPath = CKEDITOR.basePath + MgnlFCKConfig.templates;
    }

    if (MgnlFCKConfig.styles != '') {
        toolbar[2].push('Style');
        //TODO see doc at http://docs.cksource.com/ckeditor_api/symbols/CKEDITOR.config.html#.stylesSet
        CKEDITOR.config.stylesSet  = {};
        //TODO not ported to CKEditor
        FCKConfig.StylesXmlPath = CKEDITOR.basePath + MgnlFCKConfig.styles;
    }

    if (MgnlFCKConfig.fonts != '') {
        toolbar[2].push('FontName');
        CKEDITOR.config.font_names = MgnlFCKConfig.fonts;
    }

    if (MgnlFCKConfig.fontSizes != '') {
        toolbar[2].push('FontSize');
        CKEDITOR.config.fontSize_sizes = MgnlFCKConfig.fontSizes;
    }

    if (MgnlFCKConfig.colors != '') {
        toolbar[2].push('TextColor');
        CKEDITOR.config.colorButton_colors = MgnlFCKConfig.colors;
    }
}*/

}
