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
