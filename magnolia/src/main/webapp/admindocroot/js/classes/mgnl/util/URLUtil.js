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
 