// get the special configuration for this editor instance
// this is the only way to configure the toolbar set
// remember the editor is running in an iframe

var MgnlFCKConfig = window.parent.MgnlFCKConfigs[FCK.Name];

// set the css to use in the editor
FCKConfig.EditorAreaCSS = MgnlFCKConfig.contextPath + MgnlFCKConfig.css;

// our skin
FCKConfig.SkinPath = MgnlFCKConfig.contextPath + "/.resources/fckeditor/custom/skin/";

// i18n
FCKConfig.AutoDetectLanguage = false;
FCKConfig.DefaultLanguage = MgnlFCKConfig.language;

// line breaks
FCKConfig.EnterMode = MgnlFCKConfig.enterMode;			// p | div | br
FCKConfig.ShiftEnterMode = MgnlFCKConfig.shiftEnterMode;

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

if (MgnlFCKConfig.alignment) {
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

toolbar[0].push('-');
toolbar[0].push('Undo');
toolbar[0].push('Redo');

if (MgnlFCKConfig.source) {
    toolbar[0].push('-');
    toolbar[0].push('Source');
}

if (MgnlFCKConfig.styles != '' || MgnlFCKConfig.templates != '' || MgnlFCKConfig.fonts != '' || MgnlFCKConfig.fontSizes != '' || MgnlFCKConfig.colors != '' || MgnlFCKConfig.bgColors) {
    toolbar[1] = '/';
    toolbar[2] = new Array();

    if (MgnlFCKConfig.templates != '') {
        toolbar[2].push('Templates');
        FCKConfig.TemplatesXmlPath = MgnlFCKConfig.contextPath + MgnlFCKConfig.templates;
    }

    if (MgnlFCKConfig.styles != '') {
        toolbar[2].push('Style');
        FCKConfig.StylesXmlPath = MgnlFCKConfig.contextPath + MgnlFCKConfig.styles;
    }

    if (MgnlFCKConfig.fonts != '') {
        toolbar[2].push('FontName');
        FCKConfig.FontNames = MgnlFCKConfig.fonts;
    }

    if (MgnlFCKConfig.fontSizes != '') {
        toolbar[2].push('FontSize');
        FCKConfig.FontSizes = MgnlFCKConfig.fontSizes;
    }

    if (MgnlFCKConfig.colors != '') {
        toolbar[2].push('TextColor');
        FCKConfig.FontColors = MgnlFCKConfig.colors;
    }
}

// make the configured toolbar available
FCKConfig.ToolbarSets["MagnoliaStandard"] = toolbar;

FCKConfig.LinkDlgHideTarget = false;
FCKConfig.LinkDlgHideAdvanced = true;

FCKConfig.ImageDlgHideLink = false;
FCKConfig.ImageDlgHideAdvanced = true;

FCKConfig.FlashDlgHideAdvanced = false;

FCKConfig.LinkBrowser = true;
FCKConfig.LinkBrowserURL = MgnlFCKConfig.contextPath + "/.magnolia/pages/repositoryBrowser.html?contextPath=" + MgnlFCKConfig.contextPath;

FCKConfig.ImageBrowser = false;

FCKConfig.FlashBrowser = false;

// configure upload
// pass parameters
var params = "repository=" + MgnlFCKConfig.repository;
params += "&path=" + MgnlFCKConfig.path;
params += "&nodeCollection=" + MgnlFCKConfig.nodeCollection;
params += "&node=" + MgnlFCKConfig.node;
params += "&name=" + FCK.Name;

// set new urls
FCKConfig.LinkUpload = true;
FCKConfig.LinkUploadURL = MgnlFCKConfig.contextPath + "/.magnolia/fckeditor/upload?" + params + "&type=file";

FCKConfig.ImageUpload = true;

FCKConfig.ImageUploadURL = MgnlFCKConfig.contextPath + "/.magnolia/fckeditor/upload?" + params + "&type=image";

FCKConfig.FlashUpload = true;
FCKConfig.FlashUploadURL = MgnlFCKConfig.contextPath + "/.magnolia/fckeditor/upload?" + params + "&type=flash";