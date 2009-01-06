/**
 * This file Copyright (c) 1993-2009 Magnolia International
 * Ltd.  (http://www.magnolia-cms.com). All rights reserved.
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
 * is available at http://www.magnolia-cms.com/mna.html
 *
 * Any modifications to this file must keep this entire header
 * intact.
 *
 */

 /**
  * This is a Util to create proper urls
  */
classDef( "mgnl.util.URLUtil", {
    
    /**
     * Used to construct everytime a new url so that it is impossilbe to cache the site
     */
    getCacheKiller: function() {
            var now = new Date();
            return now.getTime();
    },
    
    /**
     * Add a parameter to the url so that it is impossilbe to cache the site
     */
    addCacheKiller: function(url) {
        return this.addParameter(url, "mgnlCK", this.getCacheKiller());
    },

    /**
     * Add a parameter to the url
     */
    addParameter: function(href, name, value) {
        var delimiter;
        if (href.indexOf("?")==-1)
             delimiter="?";
        else 
            delimiter="&";
        return href+delimiter+name+"="+value;
    },

    /** 
     * remove the parameter from an url
     */
    removeParameter: function(href, name){
        //works only for a single paramter

        var tmp = href.split("?");
    
        var newHref=tmp[0];
        var query= new Array();
        if (tmp[1]) {
            var paramObj=tmp[1].split("&");
    
            for (var i=0;i<paramObj.length;i++) {
                if (paramObj[i].indexOf(name+"=")!=0) {
                        query.push(paramObj[i]);
                }
            }
        }
            
        if(query.length > 0) {
            newHref += "?";
                
            for(var i=0; i < query.length; i++) {
                newHref += query[i];
                    
                if(i + 1 < query.length) {
                    newHref += "&";
                }
            }
        }
        
        return newHref;
    },
    
    /**
     * Is this an external URL containing ://
     */
     isExternal: function(url){
        return url.indexOf("://") != -1;
     }
 });
 