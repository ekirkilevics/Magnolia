// For general configuration options provided by CKEditor, please see http://docs.cksource.com/ckeditor_api/symbols/CKEDITOR.config.html.
// For a mapping between old options offered by fckeditor and the new ones, please see
// http://docs.cksource.com/CKEditor_3.x/Developers_Guide/FCKeditor_CKEditor_Configuration_Mapping

// set the css to use in the editor
CKEDITOR.config.contentCss = CKEDITOR.basePath + 'custom/css/magnoliaStandard.css';

// TODO skin mechanism, files and naming seem to have changed so we probably need to re-write our skin from scratch.
//However, CKEditor provided no docs about that so far.
// Magnolia skin. It may be the name of the skin folder inside the editor installation path, or the name and the path separated by a comma.
//CKEDITOR.config.skin = 'magnolia,'+ CKEDITOR.basePath + 'custom/skin/magnolia/';

// i18n
// Always auto detecting unless the language configuration is specified. Falls back to defaultLanguage.
//CKEDITOR.config.language = MgnlFCKConfig.language;

// line breaks
//CKEDITOR.config.enterMode = MgnlFCKConfig.enterMode;			// p | div | br
//CKEDITOR.config.shiftEnterMode = MgnlFCKConfig.shiftEnterMode;

// create toolbar
var toolbar = new Array();
toolbar[0] = new Array();

toolbar[0].push('Bold');
toolbar[0].push('Italic');
toolbar[0].push('SpecialChar');
toolbar[0].push('-');
toolbar[0].push('Link');
toolbar[0].push('Unlink');
toolbar[0].push('-');
toolbar[0].push('Copy');
toolbar[0].push('Paste');
toolbar[0].push('PasteText');
toolbar[0].push('PasteWord');

/*if (MgnlFCKConfig.alignment) {
    toolbar[0].push('-');
    toolbar[0].push('JustifyLeft');
    toolbar[0].push('JustifyCenter');
    toolbar[0].push('JustifyRight');
    toolbar[0].push('JustifyFull');
}

if (MgnlFCKConfig.lists) {
    toolbar[0].push('-');
    toolbar[0].push('OrderedList');
    toolbar[0].push('UnorderedList');
}

if (MgnlFCKConfig.tables) {
    toolbar[0].push('-');
    toolbar[0].push('Table');
}

if (MgnlFCKConfig.images) {
    toolbar[0].push('-');
    toolbar[0].push('Image');
    toolbar[0].push('Flash');
}
*/
toolbar[0].push('-');
toolbar[0].push('Undo');
toolbar[0].push('Redo');

/*if (MgnlFCKConfig.source) {
    toolbar[0].push('-');
    toolbar[0].push('Source');
}
if (MgnlFCKConfig.showSpellChecker) {
    toolbar[0].push('-');
    toolbar[0].push('SpellCheck');
    if (MgnlFCKConfig.spellChecker) {
       //TODO CKEditor doc here says "Depends on which plugin(s) is/are loaded."
       FCKConfig.SpellChecker = MgnlFCKConfig.spellChecker;
       //if it's SCAYT start it
       FCKConfig.ScaytAutoStartup = true;
    }
}

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
}
*/

CKEDITOR.config.toolbar_Magnolia = toolbar
// make the configured toolbar available
CKEDITOR.config.toolbar = 'Magnolia';

CKEDITOR.config.removeDialogTabs = 'link:advanced,image:advanced'

//CKEDITOR.config.filebrowserLinkBrowseUrl = CKEDITOR.basePath + "/.magnolia/pages/repositoryBrowser.html?contextPath=" + CKEDITOR.basePath

//CKEDITOR.config.filebrowserImageBrowseUrl = CKEDITOR.basePath + "/.magnolia/pages/repositoryBrowser.html?contextPath=" + CKEDITOR.basePath;

// set new urls
//CKEDITOR.config.filebrowserLinkUploadUrl = CKEDITOR.basePath + "/.magnolia/fckeditor/upload?" + params + "&type=file";

//CKEDITOR.config.filebrowserImageUploadUrl = CKEDITOR.basePath + "/.magnolia/fckeditor/upload?" + params + "&type=image";

//CKEDITOR.config.filebrowserFlashUploadUrl = CKEDITOR.basePath + "/.magnolia/fckeditor/upload?" + params + "&type=flash";
