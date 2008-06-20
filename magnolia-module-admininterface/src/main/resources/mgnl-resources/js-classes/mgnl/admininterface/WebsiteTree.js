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
importClass("mgnl.admininterface.VersionsList");

classDef("mgnl.admininterface.WebsiteTree", {

    showVersions: function(tree){
        mgnl.admininterface.VersionsList.show('website', tree.selectedNode.path);
    },
    
    search: function(value){
        document.location = contextPath +"/.magnolia/pages/websiteSearchList?searchStr=" + value;
    }
            
});

classDef("mgnl.admininterface.Tree", {

    showVersions: function(repo, tree){
        mgnl.admininterface.VersionsList.show(repo, tree.selectedNode.path);
    },
    
    search: function(value){
        document.location = contextPath +"/.magnolia/pages/websiteSearchList?searchStr=" + value;
    }
            
});