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
importClass("mgnl.util.BrowserCheck");

/**
 * Util to get the window size, inner document of IFrames and similar things
 */
classDef("mgnl.util.DHTMLUtil", {

    /**
     * Returns the an object containing a .w and .h property
     */
    getWindowSize: function()   {
        var obj=new Object();
        if( typeof (window.innerWidth) == 'number' ){
            //Non-IE
            obj.w=window.innerWidth;
            obj.h=window.innerHeight;
        }
        else if (document.documentElement && (document.documentElement.clientWidth || document.documentElement.clientHeight ) ){
            //IE 6+ in 'standards compliant mode'
            obj.w=document.documentElement.clientWidth;
            obj.h=document.documentElement.clientHeight;
        }
        else if( document.body && ( document.body.clientWidth || document.body.clientHeight ) ){
            //IE 4 compatible
            obj.w=document.body.clientWidth;
            obj.h=document.body.clientHeight;
        }
        return obj;
    },

    /**
     * Returns the inner document of an iframe
     */
    getIFrameDocument: function(iFrameName){
        if ($(iFrameName))
            return $(iFrameName).contentDocument;
        else
            if (document.frames && document.frames[iFrameName])
            return document.frames[iFrameName].document;
        else
                return null;
    },

    /**
     * Get the x position of the element
     */
    getPosX: function(obj){
        if (!obj) return 0;

          var x=document.body.scrollLeft;
          while (obj.offsetParent){
              x+=obj.offsetLeft;
              obj=obj.offsetParent;
          }
        return x;
    },

    /**
     * Get the y position of the element
     */
    getPosY: function(obj){
        if (!obj)
            return 0;

        var y=document.body.scrollTop;
          while (obj.offsetParent){
              y+=obj.offsetLeft;
              obj=obj.offsetParent;
          }
        return y;
    },

    /**
     * Get .x and .y of the mouse event (if scrolled)
     */
    getMousePos: function(event){
        var pos=new Object();
        if (document.all){
            pos.x=window.event.clientX + document.body.scrollLeft;
            pos.y=window.event.clientY + document.body.scrollTop;
        }
        else{
            pos.x=event.pageX;
            pos.y=event.pageY;
        }
        return pos;
    },

    /**
     * Get the root window (including opener)
     * @param current current window (optional)
     */
    getRootWindow: function(current){
        current = current?current:window;

        if(current.top != current)
            return this.getRootWindow(current.top);
        if(current.opener != null)
            return this.getRootWindow(current.opener);
        return current;
    },

    findVariable:  function(name, current){
        current = current?current:window;
        if(current[name]!=null)
            return current[name];

        if(current.top != current)
            return this.findVariable(name, current.top);
        if(current.opener != null)
            return this.findVariable(name, current.opener);
        return null;
    },

    /**
     * Hide the element
     */
    hide: function(element){
        element.style.visibility='hidden';
        element.style.display='none';
    },

    /**
     * Show the element
     */
    show: function(element){
        element.style.visibility='visible';
        element.style.display='block';
    },

    addOnLoad: function(handler){
        var orgHandler = window.onload;
        window.onload = function(){
            if(orgHandler)
                orgHandler();
            handler();
        };
    },

    addOnResize: function(handler){
        var orgHandler = window.onresize;
        window.onresize = function(){
            if(orgHandler)
                orgHandler();
            handler();
        };
    },

    getHeight: function(element){
        return element.offsetHeight;
    },

    /**
     * Get the width including borders, margins, ...
     */
    getWidth: function(element){
        return element.offsetWidth;
    },

    /**
     * Set the width. In case of borders this width includes the borders.
     */
    setWidth: function(element, width){
        // a very simple first approach
        var border = 0;
        if(element.style.borderLeftStyle && element.style.borderLeftStyle != "hidden"){
            border +=1;
        }
        if(element.style.borderRightStyle && element.style.borderRightStyle != "hidden"){
            border +=1;
        }
        element.style.width = width - border;
    },

    setHeight: function(element, height){
        // a very simple first approach
        var border = 0;
        if(element.style.borderTopStyle && element.style.borderTopStyle != "hidden"){
            border +=1;
        }
        if(element.style.borderBottomStyle && element.style.borderBottomStyle != "hidden"){
            border +=1;
        }
        element.style.height = height - border;
    },


    getCSSClass: function(name){
        var rulesKey;
        if (document.all){
            rulesKey="rules";
        }
        else if (document.getElementById){
            rulesKey="cssRules";
        }
        for (var elem0 = document.styleSheets.length-1; elem0>=0; elem0--) {
            var rules=document.styleSheets[elem0][rulesKey];

            //for (var elem1 in rule) //does not work in firebird 0.8, safari 1.2
            for (var elem1=0; elem1<rules.length; elem1++){
                var cssClass=rules[elem1].selectorText;
                // in safar 1.3 the selectorText is in lower case
                if (cssClass && cssClass.toLowerCase().indexOf("." + name.toLowerCase())!=-1){
                    return rules[elem1];
                }
            }
        }
    }

});
