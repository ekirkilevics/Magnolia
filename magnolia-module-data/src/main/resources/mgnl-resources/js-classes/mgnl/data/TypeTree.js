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
 

classDef("mgnl.data.TypeTree", {

    edit : function(tree){
    	mgnlTreeMenuOpenDialog(tree,'.magnolia/dialogs/moduleDataType.html');
    },

    create: function(path){
        
        // a not existing node is created
        if(path != "/"){
            path +=  "/";
        }
    
        // this indicates to create this node
        path +=  "mgnlNew";
    
        mgnlOpenWindow(".magnolia/dialogs/moduleDataType.html?mgnlPath=" + path  + "&mgnlRepository=config");
    },
    
    importData : function(tree){
        var rootPath=tree.path;
        var nodeToReload=tree.getNode(rootPath);
	    var params=new Object();
        params.typeAction='importData';
        params.forceReload=true;
        nodeToReload.expand(params);
    }
    
});