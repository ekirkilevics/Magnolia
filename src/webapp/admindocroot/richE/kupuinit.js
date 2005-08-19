/*****************************************************************************
 *
 * Copyright (c) 2003-2004 Kupu Contributors. All rights reserved.
 *
 * This software is distributed under the terms of the Kupu
 * License. See LICENSE.txt for license text. For a list of Kupu
 * Contributors see CREDITS.txt.
 *
 *****************************************************************************/

// $Id$


//----------------------------------------------------------------------------
// Sample initialization function
//----------------------------------------------------------------------------

function initKupu(iframe,nodeDataName) {
    /* Although this is meant to be a sample implementation, it can
        be used out-of-the box to run the sample pagetemplate or for simple
        implementations that just don't use some elements. When you want
        to do some customization, this should probably be overridden. For 
        larger customization actions you will have to subclass or roll your 
        own UI object.
    */

    // first we create a logger
    //var l = new PlainLogger('kupu-toolbox-debuglog', 5);
	var l = null;


    // now some config values
    // XXX To mimic the 'old' behaviour, vars should be retrieved from the 
    // iframe (attributes)
    var src = iframe.getAttribute('src');
    var dst = iframe.getAttribute('dst');
    if (!dst) {
        dst = '..';
    }
    var use_css = (iframe.getAttribute('usecss') != "0");
    var reload_src = (iframe.getAttribute('reloadsrc') == "1");
    var strict_output = (iframe.getAttribute('strict_output') == "1");
    var content_type = 'application/xhtml+xml';
    if (iframe.getAttribute('content_type')) {
        content_type = iframe.getAttribute('content_type');
    };
    
    var conf = {'src': src,
                'dst': dst,
                'use_css': use_css,
                'reload_after_save': reload_src,
                'strict_output': strict_output,
                'content_type': content_type
                };
    
    // the we create the document, hand it over the id of the iframe
    var doc = new KupuDocument(iframe);
    
    // now we can create the controller
    var kupu = new KupuEditor(doc, conf, l);

    // add some tools
    // XXX would it be better to pass along elements instead of ids?
    var colorchoosertool;
    if (document.getElementById('kupu-forecolor')) {
		colorchoosertool = new ColorchooserTool('kupu-forecolor', 'kupu-hilitecolor', 'kupu-colorchooser');
		kupu.registerTool('colorchooser', colorchoosertool);
	}

	var listtool;
	if (document.getElementById('kupu-list-ul-addbutton')) {
		var listtool = new ListTool('kupu-list-ul-addbutton', 'kupu-list-ol-addbutton', 'kupu-ulstyles', 'kupu-olstyles');
		kupu.registerTool('listtool', listtool);
	}

    var linktool,linktoolbox;
	if (document.getElementById("kupu-link-input")) {
		linktool = new LinkTool();
		kupu.registerTool('linktool', linktool);
		linktoolbox = new LinkToolBox("kupu-link-input", "kupu-link-button");
		linktool.registerToolBox('linktoolbox', linktoolbox);
		}

    var csstool,csstoolbox;
	if (document.getElementById(nodeDataName+"-css-input-css")) {
		csstool = new LinkTool();
		kupu.registerTool('csstool', csstool);
        csstoolbox = new CssToolBox(nodeDataName+'-css-input', nodeDataName+'-css-button');
	    csstool.registerToolBox('csstoolbox', csstoolbox);
        }

    var showpathtool = new ShowPathTool();
    kupu.registerTool('showpathtool', showpathtool);
    
    // now we can create a UI object which we can use from the UI
    var ui = new KupuUI('kupu-tb-styles');

    // the ui must be registered to the editor as well so it can be notified
    // of state changes
    kupu.registerTool('ui', ui); // XXX Should this be a different method?

    // register some cleanup filter
    // remove tags that aren't in the XHTML DTD
    var nonxhtmltagfilter = new NonXHTMLTagFilter();
    kupu.registerFilter(nonxhtmltagfilter);

    return kupu;
}
