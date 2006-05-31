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

importClass("mgnl.util.DHTMLUtil");
importClass("mgnl.util.URLUtil");
importClass("mgnl.util.Debug");

/**
 * This is the singleton object defining the admin central
 **/
classDef("mgnl.admininterface.AdminCentral", MgnlAdminCentral);

function MgnlAdminCentral(){
    
    // only one instance is allowed
    window.mgnlAdminCentral = this;
    
    this.divContent = $("mgnlAdminCentralContentDiv");
    this.divScrolledContent = $("mgnlAdminCentralScrolledContentDiv");
    this.divMenu = $("mgnlAdminCentralMenuDiv");
    
    /**
     * Used if this instance is called from an other window
     */
    this.window = window;
    
    /**
     * Resize
     */
    this.resize = function(){
        if (this.divContent && this.divMenu){
            var sizeObj=mgnlGetWindowSize();

            var h=sizeObj.h-60-20;
            var w=sizeObj.w-205-20;
    
            this.divMenu.style.height=h;
    
            this.divContent.style.width=w;
            this.divContent.style.height=h;
            if($("mgnlAdminCentralContentIFrame").border=="none"){
                $("mgnlAdminCentralContentIFrame").style.height=h+2;
            }
            else{
                $("mgnlAdminCentralContentIFrame").style.height=h+2;
            }

            this.divScrolledContent.style.width=w;
            this.divScrolledContent.style.height=h;
            if($("mgnlAdminCentralScrolledContentIFrame").border=="none"){
                $("mgnlAdminCentralScrolledContentIFrame").style.height=h+2;
            }
            else{
                $("mgnlAdminCentralScrolledContentIFrame").style.height=h+2;
            }
        }
    };

    /**
     * Show the url in the content div (iframe)
     * @param url the url to call
     * @param border set a border on the div
     * @param scrolled show scrollbars (a different div will be used)
     */
    this.showContent = function(url, border, scrolled){
            scrolled = scrolled!=null?scrolled: true;
            border = border!=null?border: true;

        var name = scrolled? "mgnlAdminCentralScrolledContentIFrame": "mgnlAdminCentralContentIFrame";
        
        var doc = MgnlDHTMLUtil.getIFrameDocument(name);
            
        MgnlDHTMLUtil.hide(this.divContent);
        MgnlDHTMLUtil.hide(this.divScrolledContent);
    
        // clean only if this is an internal page (else one get's an security exception
        try{
            doc.open("plain/text");
        }
        catch(e){
        }
    
        url = MgnlURLUtil.addCacheKiller(url);
        if(!MgnlURLUtil.isExternal(url)){
            url = contextPath + url;
        }
        
        $(name).src = url;
        
        var div = scrolled? this.divScrolledContent : this.divContent;
        // border?
        if(border){
            div.style.border= 'solid thin';
            div.style.borderTopColor = "#999";
            div.style.borderLeftColor = "#999";
            div.style.borderRightColor = "#ccc"; 
            div.style.borderBottomColor = "#ccc";
        }
        else{
            div.style.border= 'none';
        }
        
        MgnlDHTMLUtil.show(div);
    };
    
    /**
     * Show the tree
     */
    this.showTree = function(name, path){
        var url = "/.magnolia/trees/" + name + ".html"; 
        if(path && path.length >0)
            url += "?pathSelected="+path +"&pathOpen="+path;
        this.showContent(url, false, false);
    }

}

/**
 * Set this method to execute after the admin central is opened in a new window
 */
MgnlAdminCentral.onOpenedInNewWindow = null;

/**
 * Open the admin central in a new window (used for example for the admin central button in the templates)
 */
MgnlAdminCentral.openInNewWindow = function(onOpenedInNewWindow){
    src = contextPath + "/.magnolia/adminCentral.html";
    src = MgnlURLUtil.addCacheKiller(src);
    
    w = window.open(src,"mgnlAdminCentral","");
    MgnlAdminCentral.onOpenedInNewWindow = onOpenedInNewWindow;
    
    if (w) {
        w.focus();
    }
    return w;
}

/**
 * Some static methods useabe from everywhere.
 */
MgnlAdminCentral.showTree = function(name, path){
    mgnlAdminCentral = MgnlDHTMLUtil.findVariable("mgnlAdminCentral")
    if(mgnlAdminCentral){
        mgnlAdminCentral.showTree(name, path);
        mgnlAdminCentral.window.focus();
    }
    else{
        this.openInNewWindow(function(mgnlAdminCentral){
            mgnlAdminCentral.showTree(name, path);
            // remove the handler again
            MgnlAdminCentral.onOpenedInNewWindow = null;
        });
    }
}

MgnlAdminCentral.showContent = function(url, border, scrolled){
    MgnlDHTMLUtil.findVariable("mgnlAdminCentral").showContent(url, border, scrolled);
}
