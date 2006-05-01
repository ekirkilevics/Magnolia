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
  * Some static methods to call the versions dialog or the undo command
  */
 classDef("mgnl.admininterface.Versions", {

    showVersions: function(repository, path){
        url = "/.magnolia/pages/" + repository + "VersionsList.html";
        url = MgnlURLUtil.addParameter(url, "repository", repository);
        url = MgnlURLUtil.addParameter(url, "path", path);
        
        mgnlOpenWindow(url, 1000, 600);
    },
    
    restore: function(versionLabel){
        document.mgnlForm.command.value="restore";
        document.mgnlForm.versionLabel.value=versionLabel;
        document.mgnlForm.submit();
    }
    
 });