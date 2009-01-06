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
    this.divMessageBox = $("mgnlAdminCentralMessageBoxDiv");
    this.divFooter =$('mgnlAdminCentralFooterDiv');
    this.iframeContent = $("mgnlAdminCentralContentIFrame");
    this.iframeScrolledContent = $("mgnlAdminCentralScrolledContentIFrame");

    /**
     * Used if this instance is called from an other window
     */
    this.window = window;

    /**
     * Set message displayed below the menu
     */
    this.setMessage = function(msg, className){
        this.divMessageBox.innerHTML = msg;
        this.divMessageBox.className = className;
        MgnlDHTMLUtil.show(this.divMessageBox);
    }

    this.resetMessage = function(){
        this.divMessageBox.innerHTML = "";
        MgnlDHTMLUtil.hide(this.divMessageBox);
    }

    /**
     * Resize
     */
    this.resize = function(){
        if (this.divContent && this.divMenu){
            var sizeObj=mgnlGetWindowSize();

            var h=sizeObj.h-60-25;
            var w=sizeObj.w-195-20;

            this.divMenu.style.height=h + "px";

            this.divContent.style.width=w + "px";
            this.divContent.style.height=h + "px";
            this.divScrolledContent.style.width=w + "px";
            this.divScrolledContent.style.height=h + "px";

            this.iframeContent.style.height=(h+2) + "px";
            this.iframeScrolledContent.style.height=(h+2) + "px";

            // set the position of the footer
            this.divFooter.style.top = (sizeObj.h - 17) + "px";
            this.divFooter.style.width = w + "px";
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

        MgnlDHTMLUtil.hide(this.divContent);
        MgnlDHTMLUtil.hide(this.divScrolledContent);

        // if loading of the new page takes a while the user should not see the old page in the meantime
        $(name).src = "about:blank";

        url = MgnlURLUtil.addCacheKiller(url);
        if(!MgnlURLUtil.isExternal(url)){
            url = contextPath + url;
        }

        $(name).src = url;

        var div = scrolled? this.divScrolledContent : this.divContent;
        // border?
        if(border){
            div.style.border= 'solid 1px';
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
    this.showTree = function(name, path, usePathAsRoot){
        usePathAsRoot = usePathAsRoot!=null?true:false;

        var url = "/.magnolia/trees/" + name + ".html";
        if(path && path.length >0) {
            if (!usePathAsRoot) {
                url += "?pathSelected="+path +"&pathOpen="+path;
            } else {
                url += "?pathSelected="+path +"&pathOpen="+path+"&path="+path;
            }
        }
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
MgnlAdminCentral.showTree = function(name, path, usePathAsRoot){
    mgnlAdminCentral = MgnlDHTMLUtil.findVariable("mgnlAdminCentral")
    if(mgnlAdminCentral){
        mgnlAdminCentral.showTree(name, path, usePathAsRoot);
        mgnlAdminCentral.window.focus();
    }
    else{
        this.openInNewWindow(function(mgnlAdminCentral){
            mgnlAdminCentral.showTree(name, path, usePathAsRoot);
            // remove the handler again
            MgnlAdminCentral.onOpenedInNewWindow = null;
        });
    }
}

MgnlAdminCentral.showContent = function(url, border, scrolled){
    MgnlDHTMLUtil.findVariable("mgnlAdminCentral").showContent(url, border, scrolled);
}
