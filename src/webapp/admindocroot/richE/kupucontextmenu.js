/*****************************************************************************
 *
 * Copyright (c) 2003-2004 Kupu Contributors. All rights reserved.
 *
 * This software is distributed under the terms of the Kupu
 * License. See LICENSE.txt for license text. For a list of Kupu
 * Contributors see CREDITS.txt.
 *
 *****************************************************************************/

// $Id: kupucontextmenu.js,v 1.1 2004/04/27 15:32:27 enz Exp $


//----------------------------------------------------------------------------
// ContextMenu
//----------------------------------------------------------------------------

function ContextMenu() {
    /* the contextmenu */
    this.contextmenu = null;
    this.seperator = 1;

    this.initialize = function(editor) {
        /* set the event handlers and such */
        this.editor = editor;
        // needs some work since it won't work for more than one editor
        addEventHandler(editor.getInnerDocument(), "contextmenu", this.createContextMenu, this);
        addEventHandler(editor.getInnerDocument(), "focus", this.hideContextMenu, this);
        addEventHandler(document, "focus", this.hideContextMenu, this);
        addEventHandler(editor.getInnerDocument(), "click", this.hideContextMenu, this);
        addEventHandler(document, "click", this.hideContextMenu, this);
    };

    this.createContextMenu = function(event) {
        /* Create and show the context menu 
        
            The method will ask all tools for any (optional) elements they
            want to add the menu and when done render it
        */
        if (event.stopPropagation) {
            event.stopPropagation();
        };
        event.returnValue = false;
        if (this.editor.getBrowserName() == 'IE') {
            this.editor._saveSelection();
        };
        // somehow Mozilla on Windows seems to generate the oncontextmenu event
        // several times on each rightclick, here's a workaround
        if (this.editor.getBrowserName() == 'Mozilla' && this.contextmenu) {
            return false;
        };
        this.hideContextMenu();
        var selNode = this.editor.getSelectedNode();
        var elements = new Array();
        for (var id in this.editor.tools) {
            var tool = this.editor.tools[id];
            // alas, some people seem to want backward compatibility ;)
            if (tool.createContextMenuElements) {
                var els = tool.createContextMenuElements(selNode, event);
                elements = elements.concat(els);
            };
        };
        // remove the last seperator
        this._createNewContextMenu(elements, event);
        this.last_event = event;
        return false;
    };

    this.hideContextMenu = function(event) {
        /* remove the context menu from view */
        if (this.contextmenu) {
            try {
                this.editor.getInnerDocument().getElementsByTagName('body')[0].removeChild(this.contextmenu);
            } catch (e) {
                // after some commands, the contextmenu will be removed by
                // the browser, ignore those cases
            };
            this.contextmenu = null;
        };
    };

    this._createNewContextMenu = function(elements, event) {
        /* add the elements to the contextmenu and show it */
        var doc = this.editor.getInnerDocument();
        var menu = doc.createElement('div');
        menu.contentEditable = false;
        menu.designMode = 'Off';
        this._setMenuStyle(menu);
        for (var i=0; i < elements.length; i++) {
            var element = elements[i];
            if (element !== this.seperator) {
                var div = doc.createElement('div');
                div.style.width = '100%';
                var label = doc.createTextNode('\u00a0' + element.label);
                div.appendChild(label);
                menu.appendChild(div);
                // set a reference to the div on the element
                element.element = div;
                addEventHandler(div, "mousedown", element.action, element.context);
                addEventHandler(div, "mouseover", element.changeOverStyle, element);
                addEventHandler(div, "mouseout", element.changeNormalStyle, element);
                addEventHandler(div, "mouseup", this.hideContextMenu, this);
            } else {
                var hr = doc.createElement('hr');
                menu.appendChild(hr);
            };
        };
        // now move the menu to the right position
        if (this.editor.getBrowserName() == 'Mozilla') {
            var left = event.layerX;
            var top = event.layerY;
            var clienttop = event.clientY;
            if (clienttop > (parseInt(this.editor.getDocument().getWindow().innerHeight) - 
                        (parseInt(menu.style.lineHeight) * elements.length))) {
                top -= parseInt(menu.style.lineHeight) * elements.length;
            };
        } else {
            var left = event.clientX;
            var top = event.clientY;
        };
        menu.style.left = left + 'px';
        menu.style.top = top + 'px';
        menu.style.visibility = 'visible';
        addEventHandler(menu, 'focus', function() {this.blur}, menu)
        doc.getElementsByTagName('body')[0].appendChild(menu);
        this.contextmenu = menu;
    };
    
    this._setMenuStyle = function(menu) {
        /* set the styles for the menu

            to change the menu style, override this method
        */
        menu.style.position = 'absolute';
        menu.style.backgroundColor = 'white';
        menu.style.fontFamily = 'Verdana, Arial, Helvetica, sans-serif';
        menu.style.fontSize = '12px';
        menu.style.lineHeight = '16px';
        menu.style.borderWidth = '1px';
        menu.style.borderStyle = 'solid';
        menu.style.borderColor = 'black';
        menu.style.cursor = 'default';
        menu.style.width = "8em";
    };

    this._showOriginalMenu = function(event) {
        this.editor.getInnerDocument().dispatchEvent(this._last_event);
    };
};

function ContextMenuElement(label, action, context) {
    /* context menu element struct
    
        should be returned (optionally in a list) by the tools' 
        createContextMenuElements methods
    */
    this.label = label; // the text shown in the menu
    this.action = action; // a reference to the method that should be called
    this.context = context; // a reference to the object on which the method
                            //  is defined
    this.element = null; // the contextmenu machinery will add a reference 
                            // to the element here

    this.changeOverStyle = function(event) {
        /* set the background of the element to 'mouseover' style

            override only for the prototype, not for individual elements
            so every element looks the same
        */
        this.element.style.backgroundColor = 'blue';
    };

    this.changeNormalStyle = function(event) {
        /* set the background of the element back to 'normal' style

            override only for the prototype, not for individual elements
            so every element looks the same
        */
        this.element.style.backgroundColor = 'white';
    };
};

