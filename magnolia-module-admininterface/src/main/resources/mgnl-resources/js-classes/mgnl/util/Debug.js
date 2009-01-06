/**
 * This file Copyright (c) 1993-2009 Magnolia International
 * Ltd.  (http://www.magnolia.info). All rights reserved.
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
 * is available at http://www.magnolia.info/mna.html
 *
 * Any modifications to this file must keep this entire header
 * intact.
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
            doc.write('<html><body><input type="button" value="Clear" onclick="document.getElementById(\'consoleDiv\').innerHTML=\'\';" > <p>');
            doc.write('<div id="consoleDiv" style="font-family: sans-serif; font-size: 10pt">');
            doc.write('</div></body></html>');
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
            
        div.appendChild(doc.createTextNode(msg));
        div.appendChild(doc.createElement("br"));
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