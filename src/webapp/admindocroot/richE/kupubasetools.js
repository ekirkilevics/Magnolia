/*****************************************************************************
 *
 * Copyright (c) 2003-2004 Kupu Contributors. All rights reserved.
 *
 * This software is distributed under the terms of the Kupu
 * License. See LICENSE.txt for license text. For a list of Kupu
 * Contributors see CREDITS.txt.
 *
 *****************************************************************************/

// adjusted to needs of cms magnolia by www.obinary.com

lockLinkToolBoxUpdateState=false;



//----------------------------------------------------------------------------
//
// Toolboxes
//
//  These are addons for Kupu, simple plugins that implement a certain 
//  interface to provide functionality and control view aspects.
//
//----------------------------------------------------------------------------

//----------------------------------------------------------------------------
// Superclasses
//----------------------------------------------------------------------------

function KupuTool() {
    /* Superclass (or actually more of an interface) for tools 
    
        Tools must implement at least an initialize method and an 
        updateState method, and can implement other methods to add 
        certain extra functionality (e.g. createContextMenuElements).
    */

    this.toolboxes = {};

    // methods
    this.initialize = function(editor) {
        /* Initialize the tool.

            Obviously this can be overriden but it will do
            for the most simple cases
        */
        this.editor = editor;
    };

    this.registerToolBox = function(id, toolbox) {
        /* register a ui box 
        
            Note that this needs to be called *after* the tool has been 
            registered to the KupuEditor
        */
        this.toolboxes[id] = toolbox;
        toolbox.initialize(this, this.editor);
    };
    
    this.updateState = function(selNode, event) {
        /* Is called when user moves cursor to other element 

            Calls the updateState for all toolboxes and may want perform
            some actions itself
        */
        for (id in this.toolboxes) {
        	this.toolboxes[id].updateState(selNode, event);
        };
    };

    // private methods
    addEventHandler = addEventHandler;
    
    this._selectSelectItem = function(select, item) {
        this.editor.logMessage('Deprecation warning: KupuTool._selectSelectItem');

    };
}

function KupuToolBox() {
    /* Superclass for a user-interface object that controls a tool */

    this.initialize = function(tool, editor) {
        /* store a reference to the tool and the editor */
        this.tool = tool;
        this.editor = editor;
    };

    this.updateState = function(selNode, event) {
        /* update the toolbox according to the current iframe's situation */
    };
    
    this._selectSelectItem = function(select, item) {
        this.editor.logMessage('Deprecation warning: KupuToolBox._selectSelectItem');
    };
};

//----------------------------------------------------------------------------
// Implementations
//----------------------------------------------------------------------------

function KupuBaseButton(buttonid, elementnames, command, classon, classoff) {
    this.button = window.document.getElementById(buttonid);
    this.elementnames = elementnames;
    this.command = command;
    this.classon = classon;
    this.classoff = classoff;

    this.initialize = function(editor) {
        this.editor = editor;
        addEventHandler(this.button, 'click', this.execCommand, this);
    };

    this.execCommand = function() {
        /* exec this button's command */
        this.editor.execCommand(this.command);
    };

    this.updateState = function(selNode, event) {
        /* check if we need to be clicked or unclicked, and update accordingly 
        
            if the state of the button should be changed, we set the class
        */
        this.editor.logMessage('foo');
        var insideel = false;
        for (var i=0; i < this.elementnames.length; i++) {
            var el = this.elementnames[i];
            insideel = this.editor.getNearestParentOfType(selNode, el);
            this.editor.logMessage('el ' + el);
            this.editor.logMessage('insideel ' + insideel);
            if (insideel) {
                break;
            };
        };
        if (insideel) {
            this.button.className = this.classon;
        } else {
            this.button.className = this.classoff;
        };
    };
};

function KupuUI(textstyleselectid) {
    /* View 
    
        This is the main view, which controls most of the toolbar buttons.
        Even though this is probably never going to be removed from the view,
        it was easier to implement this as a plain tool (plugin) as well.
    */
    
    // attributes
    this.tsselect = document.getElementById(textstyleselectid);

    this.initialize = function(editor) {
        /* initialize the ui like tools */
        this.editor = editor;
    };
    
    // event handlers
    this.basicButtonHandler = function(action) {
        /* event handler for basic actions (toolbar buttons) */
        this.editor.execCommand(action);
        this.editor.updateState();
    };

    this.saveButtonHandler = function() {
        /* handler for the save button */
        this.editor.saveDocument();
    };

    this.saveAndExitButtonHandler = function(redirect_url) {
        /* save the document and, if successful, redirect */
        this.editor.saveDocument(redirect_url);
    };

    this.cutButtonHandler = function() {
        try {
            this.editor.execCommand('Cut');
        } catch (e) {
            if (this.editor.getBrowserName() == 'Mozilla') {
                alert('Cutting from JavaScript is disabled on your Mozilla due to security settings. For more information, read http://www.mozilla.org/editor/midasdemo/securityprefs.html');
            } else {
                throw e;
            };
        };
        this.editor.updateState();
    };

    this.copyButtonHandler = function() {
        try {
            this.editor.execCommand('Copy');
        } catch (e) {
            if (this.editor.getBrowserName() == 'Mozilla') {
                alert('Copying from JavaScript is disabled on your Mozilla due to security settings. For more information, read http://www.mozilla.org/editor/midasdemo/securityprefs.html');
            } else {
                throw e;
            };
        };
        this.editor.updateState();
    };

    this.pasteButtonHandler = function(text) {
        try {
        	if (!text) this.editor.execCommand('Paste');
        	else this.editor.execCommand('Paste',text);
        } catch (e) {
            if (this.editor.getBrowserName() == 'Mozilla') {
                alert('Pasting from JavaScript is disabled on your Mozilla due to security settings. For more information, read http://www.mozilla.org/editor/midasdemo/securityprefs.html');
                return "failed";
            } else {
                throw e;
            };
        };
        this.editor.updateState();
    };

    this.setTextStyle = function(style) {
        /* method for the text style pulldown */
        // XXX Yuck!!
        if (this.editor.getBrowserName() == "IE") {
            style = '<' + style + '>';
        };
        this.editor.execCommand('formatblock', style);
    };

    this.updateState = function(selNode) {
        /* set the text-style pulldown */
    
        // first get the nearest style
        var styles = {}; // use an object here so we can use the 'in' operator later on
        for (var i=0; i < this.tsselect.options.length; i++) {
            // XXX we should cache this
            styles[this.tsselect.options[i].value.toUpperCase()] = i;
        }
        
        var currnode = selNode;
        var index = 0;
        while (currnode) {
            if (currnode.nodeName.toUpperCase() in styles) {
                index = styles[currnode.nodeName.toUpperCase()];
                break
            }
            currnode = currnode.parentNode;
        }

        this.tsselect.selectedIndex = index;
        //dev test
        //var linkel = this.editor.getNearestParentOfType(selNode, 'a');
        //alert(linkel.getAttribute("class"));
    };
  
    this.createContextMenuElements = function(selNode, event) {
        var ret = new Array();
        ret.push(new ContextMenuElement('Cut', this.cutButtonHandler, this));
        ret.push(new ContextMenuElement('Copy', this.copyButtonHandler, this));
        ret.push(new ContextMenuElement('Paste', this.pasteButtonHandler, this));
        return ret;
    };
}

KupuUI.prototype = new KupuTool;

function ColorchooserTool(fgcolorbuttonid, hlcolorbuttonid, colorchooserid) {
    /* the colorchooser */
    
    this.fgcolorbutton = document.getElementById(fgcolorbuttonid);
    this.hlcolorbutton = document.getElementById(hlcolorbuttonid);
    this.ccwindow = document.getElementById(colorchooserid);
    this.command = null;

    this.initialize = function(editor) {
        /* attach the event handlers */
        this.editor = editor;
        
        this.createColorchooser(this.ccwindow);

        addEventHandler(this.fgcolorbutton, "click", this.openFgColorChooser, this);
        addEventHandler(this.hlcolorbutton, "click", this.openHlColorChooser, this);
        addEventHandler(this.ccwindow, "click", this.chooseColor, this);

        this.hide();

        this.editor.logMessage('Colorchooser tool initialized');
    };

    this.updateState = function(selNode) {
        /* update state of the colorchooser */
        this.hide();
    };

    this.openFgColorChooser = function() {
        /* event handler for opening the colorchooser */
        this.command = "forecolor";
        this.show();
    };

    this.openHlColorChooser = function() {
        /* event handler for closing the colorchooser */
        if (this.editor.getBrowserName() == "IE") {
            this.command = "backcolor";
        } else {
            this.command = "hilitecolor";
        }
        this.show();
    };

    this.chooseColor = function(event) {
        /* event handler for choosing the color */
        var target = _SARISSA_IS_MOZ ? event.target : event.srcElement;
        var cell = this.editor.getNearestParentOfType(target, 'td');
        this.editor.execCommand(this.command, cell.getAttribute('bgColor'));
        this.hide();
    
        this.editor.logMessage('Color chosen');
    };

    this.show = function(command) {
        /* show the colorchooser */
        this.ccwindow.style.display = "block";
    };

    this.hide = function() {
        /* hide the colorchooser */
        this.command = null;
        this.ccwindow.style.display = "none";
    };

    this.createColorchooser = function(table) {
        /* create the colorchooser table */
        
        var chunks = new Array('00', '33', '66', '99', 'CC', 'FF');
        table.setAttribute('id', 'kupu-colorchooser-table');
        table.style.borderWidth = '2px';
        table.style.borderStyle = 'solid';
        table.style.position = 'absolute';
        table.style.cursor = 'default';
        table.style.display = 'none';

        var tbody = document.createElement('tbody');

        for (var i=0; i < 6; i++) {
            var tr = document.createElement('tr');
            var r = chunks[i];
            for (var j=0; j < 6; j++) {
                var g = chunks[j];
                for (var k=0; k < 6; k++) {
                    var b = chunks[k];
                    var color = '#' + r + g + b;
                    var td = document.createElement('td');
                    td.setAttribute('bgColor', color);
                    td.style.backgroundColor = color;
                    td.style.borderWidth = '1px';
                    td.style.borderStyle = 'solid';
                    td.style.fontSize = '1px';
                    td.style.width = '10px';
                    td.style.height = '10px';
                    var text = document.createTextNode('\u00a0');
                    td.appendChild(text);
                    tr.appendChild(td);
                }
            }
            tbody.appendChild(tr);
        }
        table.appendChild(tbody);

        return table;
    };
}

ColorchooserTool.prototype = new KupuTool;

function PropertyTool(titlefieldid, descfieldid) {
    /* The property tool */

    this.titlefield = document.getElementById(titlefieldid);
    this.descfield = document.getElementById(descfieldid);

    this.initialize = function(editor) {
        /* attach the event handlers and set the initial values */
        this.editor = editor;
        addEventHandler(this.titlefield, "change", this.updateProperties, this);
        addEventHandler(this.descfield, "change", this.updateProperties, this);
        
        // set the fields
        var heads = this.editor.getInnerDocument().getElementsByTagName('head');
        if (!heads[0]) {
            this.editor.logMessage('No head in document!', 1);
        } else {
            var head = heads[0];
            var titles = head.getElementsByTagName('title');
            if (titles.length) {
                this.titlefield.value = titles[0].text;
            }
            var metas = head.getElementsByTagName('meta');
            if (metas.length) {
                for (var i=0; i < metas.length; i++) {
                    var meta = metas[i];
                    if (meta.getAttribute('name') && 
                            meta.getAttribute('name').toLowerCase() == 
                            'description') {
                        this.descfield.value = meta.getAttribute('content');
                        break;
                    }
                }
            }
        }

        this.editor.logMessage('Property tool initialized');
    };

    this.updateProperties = function() {
        /* event handler for updating the properties form */
        var doc = this.editor.getInnerDocument();
        var heads = doc.getElementsByTagName('HEAD');
        if (!heads) {
            this.editor.logMessage('No head in document!', 1);
            return;
        }

        var head = heads[0];

        // set the title
        var titles = head.getElementsByTagName('title');
        if (!titles) {
            var title = doc.createElement('title');
            var text = doc.createTextNode(this.titlefield.value);
            title.appendChild(text);
            head.appendChild(title);
        } else {
            titles[0].childNodes[0].nodeValue = this.titlefield.value;
        }

        // let's just fulfill the usecase, not think about more properties
        // set the description
        var metas = doc.getElementsByTagName('meta');
        var descset = 0;
        for (var i=0; i < metas.length; i++) {
            var meta = metas[i];
            if (meta.getAttribute('name').toLowerCase() == 'description') {
                meta.setAttribute('content', this.descfield.value);
            }
        }

        if (!descset) {
            var meta = doc.createElement('meta');
            meta.setAttribute('name', 'description');
            meta.setAttribute('content', this.descfield.value);
            head.appendChild(meta);
        }

        this.editor.logMessage('Properties modified');
    };
}

PropertyTool.prototype = new KupuTool;

function LinkTool() {
    /* Add and update hyperlinks */


    this.initialize = function(editor) {
        this.editor = editor;
        this.editor.logMessage('Link tool initialized');

		if (this.editor.getBrowserName() == 'IE') this.cssAttribute="className";
		else this.cssAttribute="class";
    };
    
    this.createLinkHandler = function(event) {
        /* create a link according to a url entered in a popup */
        var linkWindow = openPopup('popups/link.html', 300, 200);
        linkWindow.linktool = this;
        linkWindow.focus();
    };
    
    // the order of the arguments is a bit odd here because of backward compatibility
    this.createLink = function(url, type, name, target, css) {
        var currnode = this.editor.getSelectedNode();
        var linkel = this.editor.getNearestParentOfType(currnode, 'A');
        if (!linkel) {
            this.editor.execCommand("CreateLink",url);
            var currnode = this.editor.getSelectedNode();
            if (this.editor.getBrowserName() == 'IE') {
                linkel = this.editor.getNearestParentOfType(currnode, 'A');
            } else {
                linkel = currnode.nextSibling;
				if (!linkel) {
					//alert("no text selected");
					//netscape: creation at start of line...
					linkel = this.editor.getNearestParentOfType(currnode, 'A');
				};

            };
        } else {
            linkel.setAttribute('href',url);
        }

		if (!linkel) {
			//alert("no text selected");
			//this.editor.logMessage('No text selected');
			return;
		}

		//alert(type);
        if (type && type == 'replaceBySpan') {
        //if (type && type == 'anchor') {
            //alert(name);
            //linkel.setAttribute('name', type);
			//alert("name set");
            linkel.removeAttribute('href');
            //alert("href removed");
        }
        if (css) {
        	linkel.setAttribute(this.cssAttribute,css);
		}
		else {
			linkel.removeAttribute(this.cssAttribute);
        }
        if (target) {
        	linkel.setAttribute('target', target);
        }
        else {
        	linkel.removeAttribute('target');
        };
        //linkel.style.color = this.linkcolor;
        lockLinkToolBoxUpdateState=false;
        this.editor.logMessage('Link added');
    };
    
    this.deleteLink = function() {
        /* delete the current link */
        var currnode = this.editor.getSelectedNode();
        var linkel = this.editor.getNearestParentOfType(currnode, 'a');
        if (!linkel) {
            this.editor.logMessage('Not inside link');
            return;
        };
        while (linkel.childNodes.length) {
            linkel.parentNode.insertBefore(linkel.childNodes[0], linkel);
        };
        linkel.parentNode.removeChild(linkel);
    };
    
    this.createContextMenuElements = function(selNode, event) {
        /* create the 'Create link' or 'Remove link' menu elements */
        var ret = new Array();
        var link = this.editor.getNearestParentOfType(selNode, 'a');
        if (link) {
            ret.push(new ContextMenuElement('Delete link', this.deleteLink, this));
        } else {
            ret.push(new ContextMenuElement('Create link', this.createLinkHandler, this));
        };
        return ret;
    };
}

LinkTool.prototype = new KupuTool;

function LinkToolBox(inputid, buttonid) {
    /* create and edit links */
    
    this.input = document.getElementById(inputid);
    this.inputTarget = document.getElementById(inputid+"-target");
    this.inputCss = document.getElementById(inputid+"-css");
	this.button = document.getElementById(buttonid);
    this.buttonRemove = document.getElementById(buttonid+"-remove");


    this.initialize = function(tool, editor) {
        /* attach the event handlers */
        this.tool = tool;
        this.editor = editor;

		if (this.editor.getBrowserName()=='IE') this.cssAttribute="className";
		else this.cssAttribute="class";

        /*
        // input events do not work usable on ns (no event if clicking directly into the iFrame)
        // update by clicking create link button
        // keyup is best solution for ns, but does not work @ paste
        // no event works @ link browser (could be solved with (not yet existing) callback function of link browser)
        // WARNING! to enable, adapt the updateLink method method!
        addEventHandler(this.input, "keyup", this.updateLink, this);
        addEventHandler(this.inputTarget, "change", this.updateLink, this);
        addEventHandler(this.inputCss, "change", this.updateLink, this);
		*/
        addEventHandler(this.button, "click", this.addLink, this);
        addEventHandler(this.buttonRemove, "click", this.deleteLink, this);
    };


	this.getHrefIE = function(linkel,addition) {
		//alert("doc.host: "+document.location.host+"\nhost: "+linkel.host+"\nnameProp: "+linkel.nameProp+"\nhash: "+linkel.hash+"\npathname: "+linkel.pathname+"\nsearch: "+linkel.search);
		// addition: #anchor or ?query=string

		//todo: keep up to date!
		//to test, insert in ie/win a #anchor link, save and reselect the link
		var iframeSrcPath=".CMSadmin/dialogs/";
		var iframeSrcName="richEIFrame.html";

		var href=linkel.getAttribute('href');
		// respect sequence of statements!
		if (href.toLowerCase().indexOf("javascript")==0 || href.toLowerCase().indexOf("mailto")==0 || (document.location.host!=linkel.host && document.location.host+":80"!=linkel.host)) {
			// e.g. javascript:popUp('/en.html');
			// e.g. mailto:anna@obinary.com
			// e.g. http://www.obinary.com (note: if running on standard port,":80" will be added to linkel.host)
			//href=href;
		}
		else if (linkel.pathname==iframeSrcPath+iframeSrcName) {
			// e.g. -addition (#angkor)
			href=linkel.hash;
		}
		else if (linkel.pathname.indexOf(iframeSrcPath)==0) {
			// en/clients.html-addition
			href=linkel.pathname.replace(iframeSrcPath,"")+addition;
		}
		else {
			// /en/clients.html-addition
			href="/"+linkel.pathname+addition;
		}
		return href;

	};

    this.updateState = function(selNode) {
        /* if we're inside a link, update the input, else empty it */
        var linkel = this.editor.getNearestParentOfType(selNode, 'a');
        if (linkel && !lockLinkToolBoxUpdateState) {
        	// lockLinkToolBoxUpdateState: after creation of a new link, linkel is existing, but no attributes (beside href) can be accessed...
			// lock @ LinkToolBox.addLink
			// unlock @ LinkTool.createLink (end of method)

			//alert(this.inputCss.id);
			if (!linkel.href) {
				if (this.input) this.input.value = '';
				if (this.inputTarget) this.inputTarget.selectedIndex=this.inputTarget.length-1;
				if (this.inputCss) this.inputCss.selectedIndex=this.inputCss.length-1;
			}
			//var x=true;
			//if (!x) {}
			else {
				var href=linkel.getAttribute('href');
				if (this.editor.getBrowserName()=='IE') {
					//in ie, domain and stuff is add to href attribute...
					var addition="";
					if (linkel.hash) addition=linkel.hash;
					else if (linkel.search) addition=linkel.search;
					href=this.getHrefIE(linkel,addition);
				}

				var target=linkel.getAttribute('target');
				if (!target) target=''; //no target set (null)

				var css=linkel.getAttribute(this.cssAttribute);
				if (!css) css='';

				// 'javascript target' (e.g. popUp window - javascript:popUp(LINK);
				if (href.toLowerCase().indexOf("javascript")==0 && this.inputTarget)	{
					var snippet=href;
					var pre=href.substring(0,href.indexOf("'"));
					snippet=href.substring(href.indexOf("'")+1);
					var link=snippet.substring(0,snippet.indexOf("'"));
					var post=snippet.substring(snippet.indexOf("'")+1);
					var tmpTarget=pre+"LINK"+post;
					var i=0;
					while (i<this.inputTarget.options.length) {
						//check if there is a suitable 'target'; otherwise the javascript link has been entered directly to the href field
						if (this.inputTarget.options[i].value==tmpTarget) {
							href=link;
							target=tmpTarget;
							break;
						}
						i++;
					}
				}
				else if (href.toLowerCase().indexOf("mailto:")==0 && this.inputTarget)	{
					var tmpTarget="mailto:LINK";
					var i=0;
					while (i<this.inputTarget.options.length) {
						//check if there is a suitable 'target'
						if (this.inputTarget.options[i].value==tmpTarget) {
							href=href.substring(href.indexOf(":")+1);
							target=tmpTarget;
							break;
						}
						i++;
					}
				}
				//if (this.input) this.input.value = "["+href+"]["+css+"]["+target+"]";
				if (this.input) this.input.value = href;
				if (this.inputTarget) this.inputTarget.value = target; //assumption: if two options have the same value, the first will be selected (so far successfully tested)
				if (this.inputCss) this.inputCss.value = css;
			}
		}
        else {
        	// no link selected, clear all inputs
            if (this.input) this.input.value = '';
			if (this.inputTarget) this.inputTarget.selectedIndex=this.inputTarget.length-1;
            if (this.inputCss) this.inputCss.selectedIndex=this.inputCss.length-1;
        }
    };
    
    this.addLink = function(event) {
        /* add a link */
		lockLinkToolBoxUpdateState=true;
		var url = this.input.value;
		var target = getSelectionValue(this.inputTarget);
		if (target.indexOf("LINK")!=-1) {
			//e.g. javascript:openPopUp(LINK);
			var tmp=target.split("LINK");
			var quote="'";
			if (target.indexOf("mailto:")==0) quote="";
			var url=tmp[0]+quote+url+quote+tmp[1];
			target="";
		}
		var css = getSelectionValue(this.inputCss);
		this.tool.createLink(url,"","",target,css);
    };

    this.deleteLink = function(event) {
    	/* remove a link */
    	if (this.input) this.input.value = '';
		if (this.inputTarget) this.inputTarget.selectedIndex=this.inputTarget.length-1;
		if (this.inputCss) this.inputCss.selectedIndex=this.inputCss.length-1;
    	this.tool.deleteLink();
    }

    this.updateLink = function() {
        /* update the current link */
        var currnode = this.editor.getSelectedNode();
        var linkel = this.editor.getNearestParentOfType(currnode, 'A');
        if (!linkel) {
            return;
        }
        var url = this.input.value;
        linkel.setAttribute('href', url);

		var target = getSelectionValue(this.inputTarget);
        if (target=="") linkel.removeAttribute('target');
        else linkel.setAttribute('target', target);

		var css = getSelectionValue(this.inputCss);
        if (css=="") linkel.removeAttribute('class');
        else linkel.setAttribute('class', cssClass);

        this.editor.logMessage('Link modified');
    };
};

LinkToolBox.prototype = new LinkToolBox;



function CssToolBox(inputid, buttonid) {
    /* create and edit span -> insert <a class=; replace by <span @ save */
    //this.input = document.getElementById(inputid);
    this.inputCss = document.getElementById(inputid+"-css");
	this.button = document.getElementById(buttonid);
    this.buttonRemove = document.getElementById(buttonid+"-remove");

    this.initialize = function(tool, editor) {
        /* attach the event handlers */
        this.tool = tool;
        this.editor = editor;

		if (this.editor.getBrowserName()=='IE') this.cssAttribute="className";
		else this.cssAttribute="class";

        /*
        // input events do not work usable on ns (no event if clicking directly into the iFrame)
        // update by clicking create link button
        // keyup is best solution for ns, but does not work @ paste
        // no event works @ link browser (could be solved with (not yet existing) callback function of link browser)
        // WARNING! to enable, adapt the updateLink method method!
        addEventHandler(this.input, "keyup", this.updateLink, this);
        addEventHandler(this.inputTarget, "change", this.updateLink, this);
        addEventHandler(this.inputCss, "change", this.updateLink, this);
		*/
        addEventHandler(this.button, "click", this.addLink, this);
        addEventHandler(this.buttonRemove, "click", this.deleteLink, this);
    };


    this.updateState = function(selNode) {
        /* if we're inside a link, update the input, else empty it */
        var linkel = this.editor.getNearestParentOfType(selNode, 'a');
        if (linkel && !lockLinkToolBoxUpdateState) {
			//if (linkel.name=="REPLACEBYSPAN") {
			if (!linkel.href) {
			//if (linkel.getAttribute("name")=="replaceBySpan") {
				//alert(3);
				var css=linkel.getAttribute(this.cssAttribute);
				//alert(css);
				if (this.inputCss) this.inputCss.value = css;
				//var linkbuttondiv=document.getElementById("kupu-link-button-div");
				//if (linkbuttondiv) linkbuttondiv.style.visibility="hidden";
			}
			else {
				if (this.input) this.input.value = '';
				if (this.inputTarget) this.inputTarget.selectedIndex=this.inputTarget.length-1;
            	if (this.inputCss) this.inputCss.selectedIndex=this.inputCss.length-1;
            }
		}
        else {
        	// no link selected, clear all inputs
        	if (this.input) this.input.value = '';
			if (this.inputTarget) this.inputTarget.selectedIndex=this.inputTarget.length-1;
            if (this.inputCss) this.inputCss.selectedIndex=this.inputCss.length-1;
			//var linkbuttondiv=document.getElementById("kupu-link-button-div");
			//if (linkbuttondiv) linkbuttondiv.style.visibility="visible";
        }

    };

    this.addLink = function(event) {
        /* add a link */
        lockLinkToolBoxUpdateState=true;
        var css = getSelectionValue(this.inputCss);
	    var url="replaceBySpan"; //empty url causes error at link insert
		var type=url;
		this.tool.createLink(url,type,"","",css);
    };

    this.deleteLink = function(event) {
    	/* remove a link */
    	if (this.inputCss) this.inputCss.selectedIndex=this.inputCss.length-1;
    	this.tool.deleteLink();
    }

    this.updateLink = function() {
        /* update the current link */
        var currnode = this.editor.getSelectedNode();
        var linkel = this.editor.getNearestParentOfType(currnode, 'A');
        if (!linkel) {
            return;
        }
        var url = this.input.value;
        linkel.setAttribute('href', url);

		var target = getSelectionValue(this.inputTarget);
        if (target=="") linkel.removeAttribute('target');
        else linkel.setAttribute('target', target);

		var css = getSelectionValue(this.inputCss);
        if (css=="") linkel.removeAttribute('class');
        else linkel.setAttribute('class', css);

        this.editor.logMessage('Link modified');
    };
};

CssToolBox.prototype = new CssToolBox;























function ImageTool() {
    /* Image tool to add images */
    
    this.initialize = function(editor) {
        /* attach the event handlers */
        this.editor = editor;
        this.editor.logMessage('Image tool initialized');
    };

    this.createImageHandler = function(event) {
        /* create an image according to a url entered in a popup */
        var imageWindow = openPopup('popups/image.html', 300, 200);
        imageWindow.imagetool = this;
        imageWindow.focus();
    };
    
    this.createImage = function(url) {
        this.editor.execCommand("InsertImage", url);
        this.editor.logMessage('Image inserted');
    };
    
    this.createContextMenuElements = function(selNode, event) {
        return new Array(new ContextMenuElement('Create image', this.createImageHandler, this));
    };
}

ImageTool.prototype = new KupuTool;

function ImageToolBox(inputfieldid, insertbuttonid) {
    /* toolbox for adding images */

    this.inputfield = document.getElementById(inputfieldid);
    this.insertbutton = document.getElementById(insertbuttonid);

    this.initialize = function(tool, editor) {
        this.tool = tool;
        this.editor = editor;
        addEventHandler(this.insertbutton, "click", this.addImage, this);
    };
    
    this.addImage = function() {
        /* add an image */
        var url = this.inputfield.value;
        this.tool.createImage(url);
    };
};

ImageToolBox.prototype = new KupuToolBox;

function TableTool() {
    /* The table tool */

    // XXX There are some awfully long methods in here!!
    this.createContextMenuElements = function(selNode, event) {
        var table =  this.editor.getNearestParentOfType(selNode, 'table');
        if (!table) {
            ret = new Array();
            var el = new ContextMenuElement('Add table', this.addPlainTable, this);
            ret.push(el);
            return ret;
        } else {
            var ret = new Array();
            ret.push(new ContextMenuElement('Add row', this.addTableRow, this));
            ret.push(new ContextMenuElement('Delete row', this.delTableRow, this));
            ret.push(new ContextMenuElement('Add column', this.addTableColumn, this));
            ret.push(new ContextMenuElement('Delete column', this.delTableColumn, this));
            ret.push(new ContextMenuElement('Delete Table', this.delTable, this));
            return ret;
        };
    };

    this.addPlainTable = function() {
        /* event handler for the context menu */
        this.createTable(2, 3, 1, 'plain');
    };

    this.createTable = function(rows, cols, makeHeader, tableclass) {
        /* add a table */
        var doc = this.editor.getInnerDocument();

        table = doc.createElement("table");
        table.setAttribute("border", "1");
        table.setAttribute("cellpadding", "8");
        table.setAttribute("cellspacing", "2");
        table.setAttribute("class", tableclass);

        // If the user wants a row of headings, make them
        if (makeHeader) {
            var tr = doc.createElement("tr");
            var thead = doc.createElement("thead");
            for (i=0; i < cols; i++) {
                var th = doc.createElement("th");
                th.appendChild(doc.createTextNode("Col " + i+1));
                tr.appendChild(th);
            }
            thead.appendChild(tr);
            table.appendChild(thead);
        }

        tbody = doc.createElement("tbody");
        for (var i=0; i < rows; i++) {
            var tr = doc.createElement("tr");
            for (var j=0; j < cols; j++) {
                var td = doc.createElement("td");
                var content = doc.createTextNode('\u00a0');
                td.appendChild(content);
                tr.appendChild(td);
            }
            tbody.appendChild(tr);
        }
        table.appendChild(tbody);
        this.editor.insertNodeAtSelection(table);

        this.editor.logMessage('Table added');
    };

    this.addTableRow = function() {
        /* Find the current row and add a row after it */
        var currnode = this.editor.getSelectedNode();
        var currtbody = this.editor.getNearestParentOfType(currnode, "TBODY");
        var bodytype = "tbody";
        if (!currtbody) {
            currtbody = this.editor.getNearestParentOfType(currnode, "THEAD");
            bodytype = "thead";
        }
        var parentrow = this.editor.getNearestParentOfType(currnode, "TR");
        var nextrow = parentrow.nextSibling;

        // get the number of cells we should place
        var colcount = 0;
        for (var i=0; i < currtbody.childNodes.length; i++) {
            var el = currtbody.childNodes[i];
            if (el.nodeType != 1) {
                continue;
            }
            if (el.nodeName.toLowerCase() == 'tr') {
                var cols = 0;
                for (var j=0; j < el.childNodes.length; j++) {
                    if (el.childNodes[j].nodeType == 1) {
                        cols++;
                    }
                }
                if (cols > colcount) {
                    colcount = cols;
                }
            }
        }

        var newrow = this.editor.getInnerDocument().createElement("TR");

        for (var i = 0; i < colcount; i++) {
            var newcell;
            if (bodytype == 'tbody') {
                newcell = this.editor.getInnerDocument().createElement("TD");
            } else {
                newcell = this.editor.getInnerDocument().createElement("TH");
            }
            var newcellvalue = this.editor.getInnerDocument().createTextNode("\u00a0");
            newcell.appendChild(newcellvalue);
            newrow.appendChild(newcell);
        }

        if (!nextrow) {
            currtbody.appendChild(newrow);
        } else {
            currtbody.insertBefore(newrow, nextrow);
        }
        
        this.editor.logMessage('Table row added');
    };

    this.delTableRow = function() {
        /* Find the current row and delete it */
        var currnode = this.editor.getSelectedNode();
        var parentrow = this.editor.getNearestParentOfType(currnode, "TR");
        if (!parentrow) {
            this.editor.logMessage('No row to delete', 1);
            return;
        }

        // remove the row
        parentrow.parentNode.removeChild(parentrow);

        this.editor.logMessage('Table row removed');
    };

    this.addTableColumn = function() {
        /* Add a new column after the current column */
        var currnode = this.editor.getSelectedNode();
        var currtd = this.editor.getNearestParentOfType(currnode, 'TD');
        if (!currtd) {
            currtd = this.editor.getNearestParentOfType(currnode, 'TH');
        }
        if (!currtd) {
            this.editor.logMessage('No parentcolumn found!', 1);
            return;
        }
        var currtr = this.editor.getNearestParentOfType(currnode, 'TR');
        var currtable = this.editor.getNearestParentOfType(currnode, 'TABLE');
        
        // get the current index
        var tdindex = this._getColIndex(currtd);
        this.editor.logMessage('tdindex: ' + tdindex);

        // now add a column to all rows
        // first the thead
        var theads = currtable.getElementsByTagName('THEAD');
        if (theads) {
            for (var i=0; i < theads.length; i++) {
                // let's assume table heads only have ths
                var currthead = theads[i];
                for (var j=0; j < currthead.childNodes.length; j++) {
                    var tr = currthead.childNodes[j];
                    if (tr.nodeType != 1) {
                        continue;
                    }
                    var currindex = 0;
                    for (var k=0; k < tr.childNodes.length; k++) {
                        var th = tr.childNodes[k];
                        if (th.nodeType != 1) {
                            continue;
                        }
                        if (currindex == tdindex) {
                            var doc = this.editor.getInnerDocument();
                            var newth = doc.createElement('th');
                            var text = doc.createTextNode('\u00a0');
                            newth.appendChild(text);
                            if (tr.childNodes.length == k+1) {
                                // the column will be on the end of the row
                                tr.appendChild(newth);
                            } else {
                                tr.insertBefore(newth, tr.childNodes[k + 1]);
                            }
                            break;
                        }
                        currindex++;
                    }
                }
            }
        }

        // then the tbody
        var tbodies = currtable.getElementsByTagName('TBODY');
        if (tbodies) {
            for (var i=0; i < tbodies.length; i++) {
                // let's assume table heads only have ths
                var currtbody = tbodies[i];
                for (var j=0; j < currtbody.childNodes.length; j++) {
                    var tr = currtbody.childNodes[j];
                    if (tr.nodeType != 1) {
                        continue;
                    }
                    var currindex = 0;
                    for (var k=0; k < tr.childNodes.length; k++) {
                        var td = tr.childNodes[k];
                        if (td.nodeType != 1) {
                            continue;
                        }
                        if (currindex == tdindex) {
                            var doc = this.editor.getInnerDocument();
                            var newtd = doc.createElement('td');
                            var text = doc.createTextNode('\u00a0');
                            newtd.appendChild(text);
                            if (tr.childNodes.length == k+1) {
                                // the column will be on the end of the row
                                tr.appendChild(newtd);
                            } else {
                                tr.insertBefore(newtd, tr.childNodes[k + 1]);
                            }
                            break;
                        }
                        currindex++;
                    }
                }
            }
        }
        this.editor.logMessage('Table column added');
    };

    this.delTableColumn = function() {
        /* remove a column */
        var currnode = this.editor.getSelectedNode();
        var currtd = this.editor.getNearestParentOfType(currnode, 'TD');
        if (!currtd) {
            currtd = this.editor.getNearestParentOfType(currnode, 'TH');
        }
        var currcolindex = this._getColIndex(currtd);
        var currtable = this.editor.getNearestParentOfType(currnode, 'TABLE');

        // remove the theaders
        var heads = currtable.getElementsByTagName('THEAD');
        if (heads.length) {
            for (var i=0; i < heads.length; i++) {
                var thead = heads[i];
                for (var j=0; j < thead.childNodes.length; j++) {
                    var tr = thead.childNodes[j];
                    if (tr.nodeType != 1) {
                        continue;
                    }
                    var currindex = 0;
                    for (var k=0; k < tr.childNodes.length; k++) {
                        var th = tr.childNodes[k];
                        if (th.nodeType != 1) {
                            continue;
                        }
                        if (currindex == currcolindex) {
                            tr.removeChild(th);
                            break;
                        }
                        currindex++;
                    }
                }
            }
        }

        // now we remove the column field, a bit harder since we need to take 
        // colspan and rowspan into account XXX Not right, fix theads as well
        var bodies = currtable.getElementsByTagName('TBODY');
        for (var i=0; i < bodies.length; i++) {
            var currtbody = bodies[i];
            var relevant_rowspan = 0;
            for (var j=0; j < currtbody.childNodes.length; j++) {
                var tr = currtbody.childNodes[j];
                if (tr.nodeType != 1) {
                    continue;
                }
                var currindex = 0
                for (var k=0; k < tr.childNodes.length; k++) {
                    var cell = tr.childNodes[k];
                    if (cell.nodeType != 1) {
                        continue;
                    }
                    var colspan = cell.getAttribute('colspan');
                    if (currindex == currcolindex) {
                        tr.removeChild(cell);
                        break;
                    }
                    currindex++;
                }
            }
        }
        this.editor.logMessage('Table column deleted');
    };

    this.delTable = function() {
        /* delete the current table */
        var currnode = this.editor.getSelectedNode();
        var table = this.editor.getNearestParentOfType(currnode, 'table');
        if (!table) {
            this.editor.logMessage('Not inside a table!');
            return;
        };
        table.parentNode.removeChild(table);
        this.editor.logMessage('Table removed');
    };

    this.setColumnAlign = function(newalign) {
        /* change the alignment of a full column */
        var currnode = this.editor.getSelectedNode();
        var currtd = this.editor.getNearestParentOfType(currnode, "TD");
        var bodytype = 'tbody';
        if (!currtd) {
            currtd = this.editor.getNearestParentOfType(currnode, "TH");
            bodytype = 'thead';
        }
        var currcolindex = this._getColIndex(currtd);
        var currtable = this.editor.getNearestParentOfType(currnode, "TABLE");

        // unfortunately this is not enough to make the browsers display
        // the align, we need to set it on individual cells as well and
        // mind the rowspan...
        for (var i=0; i < currtable.childNodes.length; i++) {
            var currtbody = currtable.childNodes[i];
            if (currtbody.nodeType != 1 || 
                    (currtbody.nodeName.toUpperCase() != "THEAD" &&
                        currtbody.nodeName.toUpperCase() != "TBODY")) {
                continue;
            }
            for (var j=0; j < currtbody.childNodes.length; j++) {
                var row = currtbody.childNodes[j];
                if (row.nodeType != 1) {
                    continue;
                }
                var index = 0;
                for (var k=0; k < row.childNodes.length; k++) {
                    var cell = row.childNodes[k];
                    if (cell.nodeType != 1) {
                        continue;
                    }
                    if (index == currcolindex) {
                        if (this.editor.config.use_css) {
                            cell.style.textAlign = newalign;
                        } else {
                            cell.setAttribute('align', newalign);
                        }
                    }
                    index++;
                }
            }
        }
    };

    this.setTableClass = function(sel_class) {
        /* set the class for the table */
        var currnode = this.editor.getSelectedNode();
        var currtable = this.editor.getNearestParentOfType(currnode, 'TABLE');

        if (currtable) {
            currtable.className = sel_class;
        }
    };

    this._getColIndex = function(currcell) {
        /* Given a node, return an integer for which column it is */
        var prevsib = currcell.previousSibling;
        var currcolindex = 0;
        while (prevsib) {
            if (prevsib.nodeType == 1 && 
                    (prevsib.tagName.toUpperCase() == "TD" || 
                        prevsib.tagName.toUpperCase() == "TH")) {
                var colspan = prevsib.getAttribute('colspan');
                if (colspan) {
                    currcolindex += parseInt(colspan);
                } else {
                    currcolindex++;
                }
            }
            prevsib = prevsib.previousSibling;
            if (currcolindex > 30) {
                alert("Recursion detected when counting column position");
                return;
            }
        }

        return currcolindex;
    };
};

TableTool.prototype = new KupuTool;

function TableToolBox(addtabledivid, edittabledivid, newrowsinputid, 
                    newcolsinputid, makeheaderinputid, classselectid, alignselectid, addtablebuttonid,
                    addrowbuttonid, delrowbuttonid, addcolbuttonid, delcolbuttonid) {
    /* The table tool */

    // XXX There are some awfully long methods in here!!
    

    // a lot of dependencies on html elements here, but most implementations
    // will use them all I guess
    this.addtablediv = document.getElementById(addtabledivid);
    this.edittablediv = document.getElementById(edittabledivid);
    this.newrowsinput = document.getElementById(newrowsinputid);
    this.newcolsinput = document.getElementById(newcolsinputid);
    this.makeheaderinput = document.getElementById(makeheaderinputid);
    this.classselect = document.getElementById(classselectid);
    this.alignselect = document.getElementById(alignselectid);
    this.addtablebutton = document.getElementById(addtablebuttonid);
    this.addrowbutton = document.getElementById(addrowbuttonid);
    this.delrowbutton = document.getElementById(delrowbuttonid);
    this.addcolbutton = document.getElementById(addcolbuttonid);
    this.delcolbutton = document.getElementById(delcolbuttonid);

    // register event handlers
    this.initialize = function(tool, editor) {
        /* attach the event handlers */
        this.tool = tool;
        this.editor = editor;
        addEventHandler(this.addtablebutton, "click", this.addTable, this);
        addEventHandler(this.addrowbutton, "click", this.tool.addTableRow, this.tool);
        addEventHandler(this.delrowbutton, "click", this.tool.delTableRow, this.tool);
        addEventHandler(this.addcolbutton, "click", this.tool.addTableColumn, this.tool);
        addEventHandler(this.delcolbutton, "click", this.tool.delTableColumn, this.tool);
        addEventHandler(this.alignselect, "change", this.setColumnAlign, this);
        addEventHandler(this.classselect, "change", this.setTableClass, this);
        this.addtablediv.style.display = "block";
        this.edittablediv.style.display = "none";
        this.editor.logMessage('Table tool initialized');
    };

    this.updateState = function(selNode) {
        /* update the state (add/edit) and update the pulldowns (if required) */
        var table = this.editor.getNearestParentOfType(selNode, 'table');
        if (table) {
            this.addtablediv.style.display = "none";
            this.edittablediv.style.display = "block";
            var td = this.editor.getNearestParentOfType(selNode, 'td');
            if (!td) {
                td = this.editor.getNearestParentOfType(selNode, 'th');
            };
            if (td) {
                var align = td.getAttribute('align');
                if (this.editor.config.use_css) {
                    align = td.style.textAlign;
                };
                selectSelectItem(this.alignselect, align);
            };
            selectSelectItem(this.classselect, table.className);
        } else {
            this.edittablediv.style.display = "none";
            this.addtablediv.style.display = "block";
            this.alignselect.selectedIndex = 0;
            this.classselect.selectedIndex = 0;
        };
    };

    this.addTable = function() {
        /* add a table */
        var rows = this.newrowsinput.value;
        var cols = this.newcolsinput.value;
        var makeHeader = this.makeheaderinput.checked;
        var classchooser = document.getElementById("kupu-table-classchooser-add");
        var tableclass = this.classselect.options[this.classselect.selectedIndex].value;
        
        this.tool.createTable(rows, cols, makeHeader, tableclass);
    };

    this.setColumnAlign = function() {
        /* set the alignment of the current column */
        var newalign = this.alignselect.options[this.alignselect.selectedIndex].value;
        this.tool.setColumnAlign(newalign);
    };

    this.setTableClass = function() {
        /* set the class for the current table */
        var sel_class = this.classselect.options[this.classselect.selectedIndex].value;
        if (sel_class) {
            this.tool.setTableClass(sel_class);
        };
    };
};

TableToolBox.prototype = new KupuToolBox;

function ListTool(addulbuttonid, addolbuttonid, ulstyleselectid, olstyleselectid) {
    /* tool to set list styles */

    this.addulbutton = document.getElementById(addulbuttonid);
    this.addolbutton = document.getElementById(addolbuttonid);
    this.ulselect = document.getElementById(ulstyleselectid);
    this.olselect = document.getElementById(olstyleselectid);

    this.style_to_type = {'decimal': '1',
                            'lower-alpha': 'a',
                            'upper-alpha': 'A',
                            'lower-roman': 'i',
                            'upper-roman': 'I',
                            'disc': 'disc',
                            'square': 'square',
                            'circle': 'circle',
                            'none': 'none'
                            };
    this.type_to_style = {'1': 'decimal',
                            'a': 'lower-alpha',
                            'A': 'upper-alpha',
                            'i': 'lower-roman',
                            'I': 'upper-roman',
                            'disc': 'disc',
                            'square': 'square',
                            'circle': 'circle',
                            'none': 'none'
                            };
    
    this.initialize = function(editor) {
        /* attach event handlers */
        this.editor = editor;

        addEventHandler(this.addulbutton, "click", this.addUnorderedList, this);
        addEventHandler(this.addolbutton, "click", this.addOrderedList, this);
        addEventHandler(this.ulselect, "change", this.setUnorderedListStyle, this);
        addEventHandler(this.olselect, "change", this.setOrderedListStyle, this);
        this.ulselect.style.display = "none";
        this.olselect.style.display = "none";

        this.editor.logMessage('List style tool initialized');
    };

    this.updateState = function(selNode) {
        /* update the visibility and selection of the list type pulldowns */
        // we're going to walk through the tree manually since we want to 
        // check on 2 items at the same time
        var currnode = selNode;
        while (currnode) {
            if (currnode.nodeName.toLowerCase() == 'ul') {
                if (this.editor.config.use_css) {
                    var currstyle = currnode.style.listStyleType;
                } else {
                    var currstyle = this.type_to_style[currnode.getAttribute('type')];
                }
                selectSelectItem(this.ulselect, currstyle);
                this.olselect.style.display = "none";
                this.ulselect.style.display = "inline";
                return;
            } else if (currnode.nodeName.toLowerCase() == 'ol') {
                if (this.editor.config.use_css) {
                    var currstyle = currnode.listStyleType;
                } else {
                    var currstyle = this.type_to_style[currnode.getAttribute('type')];
                }
                selectSelectItem(this.olselect, currstyle);
                this.ulselect.style.display = "none";
                this.olselect.style.display = "inline";
                return;
            }

            currnode = currnode.parentNode;
            this.ulselect.selectedIndex = 0;
            this.olselect.selectedIndex = 0;
        }

        this.ulselect.style.display = "none";
        this.olselect.style.display = "none";
    };

    this.addUnorderedList = function() {
        /* add an unordered list */
        this.ulselect.style.display = "inline";
        this.olselect.style.display = "none";
        this.editor.execCommand("insertunorderedlist");
    };

    this.addOrderedList = function() {
        /* add an ordered list */
        this.olselect.style.display = "inline";
        this.ulselect.style.display = "none";
        this.editor.execCommand("insertorderedlist");
    };

    this.setUnorderedListStyle = function() {
        /* set the type of an ul */
        var currnode = this.editor.getSelectedNode();
        var ul = this.editor.getNearestParentOfType(currnode, 'ul');
        var style = this.ulselect.options[this.ulselect.selectedIndex].value;
        if (this.editor.config.use_css) {
            ul.style.listStyleType = style;
        } else {
            ul.setAttribute('type', this.style_to_type[style]);
        }

        this.editor.logMessage('List style changed');
    };

    this.setOrderedListStyle = function() {
        /* set the type of an ol */
        var currnode = this.editor.getSelectedNode();
        var ol = this.editor.getNearestParentOfType(currnode, 'ol');
        var style = this.olselect.options[this.olselect.selectedIndex].value;
        if (this.editor.config.use_css) {
            ol.style.listStyleType = style;
        } else {
            ol.setAttribute('type', this.style_to_type[style]);
        }

        this.editor.logMessage('List style changed');
    };
}

ListTool.prototype = new KupuTool;

function ShowPathTool() {
    /* shows the path to the current element in the status bar */

    this.updateState = function(selNode) {
        /* calculate and display the path */
        var path = '';
        var currnode = selNode;
        while (currnode != null && currnode.nodeName != '#document') {
            path = '/' + currnode.nodeName.toLowerCase() + path;
            currnode = currnode.parentNode;
        }
        
        try {
            window.status = path;
        } catch (e) {
            this.editor.logMessage('Could not set status bar message, ' +
                                    'check your browser\'s security settings.', 
                                    1);
        };
    };
}

ShowPathTool.prototype = new KupuTool;

