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
// Helper classes and functions
//----------------------------------------------------------------------------

function addEventHandler(element, event, method, context) {
    /* method to add an event handler for both IE and Mozilla */
    var wrappedmethod = new ContextFixer(method, context);
    if (_SARISSA_IS_MOZ) {
        element.addEventListener(event, wrappedmethod.execute, false);
    } else if (_SARISSA_IS_IE) {
        element.attachEvent("on" + event, wrappedmethod.execute);
    } else {
        throw "Unsupported browser!";
    }
};

function removeEventHandler(element, event, method) {
    /* method to remove an event handler for both IE and Mozilla */
    if (_SARISSA_IS_MOZ) {
        window.removeEventListener('focus', method, false);
    } else if (_SARISSA_IS_IE) {
        element.detachEvent("on" + event, method);
    } else {
        throw "Unsupported browser!";
    };
};

function openPopup(url, width, height) {
    /* open and center a popup window */
    var sw = screen.width;
    var sh = screen.height;
    var left = sw / 2 - width / 2;
    var top = sh / 2 - height / 2;
    var win = window.open(url, 'someWindow', 
                'width=' + width + ',height=' + height + ',left=' + left + ',top=' + top);
    return win;
};

function selectSelectItem(select, item) {
    /* select a certain item from a select */
    for (var i=0; i < select.options.length; i++) {
        var option = select.options[i];
        if (option.value == item) {
            select.selectedIndex = i;
            return;
        }
    }
    select.selectedIndex = 0;
};

/* ContextFixer, fixes a problem with the prototype based model

    When a method is called in certain particular ways, for instance
    when it is used as an event handler, the context for the method
    is changed, so 'this' inside the method doesn't refer to the object
    on which the method is defined (or to which it is attached), but for
    instance to the element on which the method was bound to as an event
    handler. This class can be used to wrap such a method, the wrapper 
    has one method that can be used as the event handler instead. The
    constructor expects at least 2 arguments, first is a reference to the
    method, second the context (a reference to the object) and optionally
    it can cope with extra arguments, they will be passed to the method
    as arguments when it is called (which is a nice bonus of using 
    this wrapper).
*/

function ContextFixer(func, context) {
    /* Make sure 'this' inside a method points to its class */
    this.func = func;
    this.context = context;
    this.args = arguments;
    var self = this;
    
    this.execute = function() {
        /* execute the method */
        var args = new Array();
        // the first arguments will be the extra ones of the class
        for (var i=0; i < self.args.length - 2; i++) {
            args.push(self.args[i + 2]);
        };
        // the last are the ones passed on to the execute method
        for (var i=0; i < arguments.length; i++) {
            args.push(arguments[i]);
        };
        self.func.apply(self.context, args);
    };
};

/* Alternative implementation of window.setTimeout

    This is a singleton class, the name of the single instance of the
    object is 'timer_instance', which has one public method called
    registerFunction. This method takes at least 2 arguments: a
    reference to the function (or method) to be called and the timeout.
    Arguments to the function are optional arguments to the 
    registerFunction method. Example:

    timer_instance.registerMethod(foo, 100, 'bar', 'baz');

    will call the function 'foo' with the arguments 'bar' and 'baz' with
    a timeout of 100 milliseconds.

    Since the method doesn't expect a string but a reference to a function
    and since it can handle arguments that are resolved within the current
    namespace rather then in the global namespace, the method can be used
    to call methods on objects from within the object (so this.foo calls
    this.foo instead of failing to find this inside the global namespace)
    and since the arguments aren't strings which are resolved in the global
    namespace the arguments work as expected even inside objects.

*/

function Timer() {
    /* class that has a method to replace window.setTimeout */
    this.lastid = 0;
    this.functions = {};
    
    this.registerFunction = function(object, func, timeout) {
        /* register a function to be called with a timeout

            args: 
                func - the function
                timeout - timeout in millisecs
                
            all other args will be passed 1:1 to the function when called
        */
        var args = new Array();
        for (var i=0; i < arguments.length - 3; i++) {
            args.push(arguments[i + 3]);
        }
        var id = this._createUniqueId();
        this.functions[id] = new Array(object, func, args);
        setTimeout("timer_instance._handleFunction(" + id + ")", timeout);
    };

    this._handleFunction = function(id) {
        /* private method that does the actual function call */
        var obj = this.functions[id][0];
        var func = this.functions[id][1];
        var args = this.functions[id][2];
        this.functions[id] = null;
        func.apply(obj, args);
    };

    this._createUniqueId = function() {
        /* create a unique id to store the function by */
        while (this.lastid in this.functions && this.functions[this.lastid]) {
            this.lastid++;
            if (this.lastid > 100000) {
                this.lastid = 0;
            }
        }
        return this.lastid;
    };
};

// create a timer instance in the global namespace, obviously this does some
// polluting but I guess it's impossible to avoid...

// OBVIOUSLY THIS VARIABLE SHOULD NEVER BE OVERWRITTEN!!!
timer_instance = new Timer();

// helper function on the Array object to test for containment
Array.prototype.contains = function(element) {
    /* see if some value is in this */
    for (var i=0; i < this.length; i++) {
        if (element == this[i]) {
            return true;
        };
    };
    return false;
};

//----------------------------------------------------------------------------
// Exceptions
//----------------------------------------------------------------------------

// XXX don't know if this is the regular way to define exceptions in JavaScript?
function Exception() {
    return;
};

// throw this as an exception inside an updateState handler to restart the
// update, may be required in situations where updateState changes the structure
// of the document (e.g. does a cleanup or so)
UpdateStateCancelBubble = new Exception();



//----------------------------------------------------------------------------
// added by obinary
//----------------------------------------------------------------------------


// check if last option is selected (blank)
// if yes, get the default value
function getSelectionValue(listbox)
	{
	if (listbox)
		{
		if (listbox.selectedIndex==listbox.length-1)
			{
			//last option is selected: find default
			var i=0;
			while (i<listbox.length)
				{
				if (listbox.options[i].id=="default") return listbox.options[i].value;
				i++;
				}
			}
		// no default found OR not last option selected
		return listbox.value;
		}
	else
		{
		return "";
		}
	}