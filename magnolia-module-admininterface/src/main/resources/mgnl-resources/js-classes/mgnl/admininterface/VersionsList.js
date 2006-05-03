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
  * 
  */
 classDef("mgnl.admininterface.VersionsList", function(onShowItem){
 
    /**
     * The current selected items version label
     */
    this.currentVersionLabel = null;

    /**
     * The function called to show the lists item
     */
    this.onShowItem = onShowItem;
    
    this.restore = function(versionLabel){
        versionLabel = versionLabel==null ? this.currentVersionLabel : versionLabel;
        document.mgnlForm.command.value="restore";
        document.mgnlForm.versionLabel.value=versionLabel;
        document.mgnlForm.submit();
    };

    this.showItem = function(versionLabel){
        versionLabel = versionLabel==null ? this.currentVersionLabel : versionLabel;
        // on show must be set by the user of this class
        onShowItem(versionLabel);
    };

 });
 
/**
 * Show versions of a page
 */
mgnl.admininterface.VersionsList.show = function(repository, path){
    url = "/.magnolia/pages/" + repository + "VersionsList.html";
    url = MgnlURLUtil.addParameter(url, "repository", repository);
    url = MgnlURLUtil.addParameter(url, "path", path);
    
    mgnlOpenWindow(url, 1000, 600);
};