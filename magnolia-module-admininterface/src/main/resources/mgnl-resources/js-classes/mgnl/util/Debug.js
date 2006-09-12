/**
 *
 * Magnolia and its source-code is licensed under the LGPL.
 * You may copy, adapt, and redistribute this file for commercial or non-commercial use.
 * When copying, adapting, or redistributing this document in keeping with the guidelines above,
 * you are required to provide proper attribution to obinary.
 * If you reproduce or distribute the document without making any substantive modifications to its content,
 * please use the following attribution line:
 *
 * Copyright 1993-2006 obinary Ltd. (http://www.obinary.com) All rights reserved.
 *
 */

classDef("mgnl.util.Debug", {

    /**
     * True if the debuggin is working
     */
    on: false,

    /**
     * the context which can be switched on and of
     */
    contextes : {
        "mgnl.Runtime": true,
        "mgnl.controls.List": true,
        tree: true,
        dialog: true,
        acl: true,
        debug: true
    },

    debug: function(msg, context, o, level){
        if(!this.on)
            return;
        if(!level)
            level = 1;
        
        if(context && context._class)
            context=context._class;
        
        // is the context in debug mode?
        if(context != null && (this.contextes[context] == null || !this.contextes[context]))
            return;
            
        var console = this.getRootWindow().mgnlDebugConsole;
        var doc = null;
        // create new window if not allready done
        if(console == null){
            console = window.open('','mgnlDebugConsole');
            this.getRootWindow().mgnlDebugConsole = console;
            doc = console.document;
            doc.write('<input type="button" value="Clear" onclick="document.getElementById(\'consoleDiv\').innerHTML=\'\';" > <p>');
            doc.write('<div id="consoleDiv" style="font-family: sans-serif; font-size: 10pt">');
            doc.write('</div>');
            doc.close();
            
            // reset the focus
            window.focus();
        }
        else{
            doc = console.document;
        }
        
        if(doc == null)
            return;
        
        // get the div to write in
        var div = doc.getElementById('consoleDiv');
        
        if(o){
            msg += ":" + this.debugObject(o, level, "");
        }
        
        if(context != null)
            msg = context + ": " + msg;
        div.appendChild(document.createTextNode(msg));
        div.appendChild(document.createElement("br"));
    },
    
    /**
     * Write out the objects values
     * @obj the object to debug
     * @param level level of recursion (default is 1)
     * @spaces used for the recursion to shift the content
     */
    debugObject: function(o, level, spaces){
        var res = "";
        switch(typeof o){
            case "object":
                if(level<=0)
                    return "object";
                res = "<br>" + spaces + "{<br>";
                for(var key in o){
                    if(!res.match(/\{<br>$/))
                        res += ",<br>";
                    res += spaces + "&nbsp;&nbsp;&nbsp;" + key + ":";
                    res += this.debugObject(o[key], level-1, spaces + "&nbsp;&nbsp;&nbsp;"); 
                }
                res += "<br>" + spaces + "}";
                break;
            case "function":
                return "function"
                break;
            
            default:
                return o;
        }
        return res;
    },
    
    /**
     * Copied from the dhtml util to avoid dependencies
     */
    getRootWindow: function(current){
        current = current?current:window;
        
        if(current.top != current)
            return this.getRootWindow(current.top);
        if(current.opener != null)
            return this.getRootWindow(current.opener);
        return current;
    }
});