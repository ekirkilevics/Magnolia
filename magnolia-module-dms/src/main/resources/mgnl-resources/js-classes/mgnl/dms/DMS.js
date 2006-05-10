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
 
classDef("mgnl.dms.DMS", {

    searchView: false,
    
    // this variable is used by the context menu
    selectedPath: "",
    selectedIsFolder: false,
    
    // this is the object used in the context menu to check the flag
    selectedIsNotFolderCondition:{test: function() {return !mgnl.dms.DMS.selectedIsFolder}},
    
    show : function(path){
        mgnlOpenWindow(".magnolia/dialogs/documentedit.html?mgnlPath=" + path + "&mgnlRepository=dms"); 
    },

    createNew: function(path){
        // a not existing node is created
        if(path != "/"){
            path +=  "/";
        }
    
        // this indicates to create this node
        path +=  "mgnlNew";
    
        mgnlOpenWindow(".magnolia/dialogs/documentedit.html?mgnlPath=" + path  + "&mgnlRepository=dms"); 
    },

    // check if it is a folder
    showDialogInTree: function(tree){
       if(tree.selectedNode.itemType=="mgnl:contentNode"){
            mgnlTreeMenuOpenDialog(tree,'.magnolia/dialogs/documentedit.html');
       }
    },

    showVersions: function(path){
        mgnl.admininterface.VersionsList.show('dms', path);
    },

    restoreVersion: function(path, name){
        window.location = contextPath +"/.magnolia/dms/versions.html?mgnlCommand=restore&mgnlPath=" + path + "&mgnlVersion=" + name; 
    },

    showVersion: function(path, name){
        mgnlOpenWindow(".magnolia/dialogs/documentedit.html?mgnlPath=" + path + "&mgnlRepository=dms&mgnlVersion=" + name);
    },

    downloadFile: function(path){
        document.location = contextPath + "/dms" + path;
    },

    reloadAfterEdit: function(path){
        if(!mgnl.dms.DMS.searchView){
            document.location = contextPath + "/.magnolia/trees/dms.html?mgnlCK=" + mgnlGetCacheKiller() + "&pathSelected=" + path;
        }
        else{
            document.location.reload();
        }
    },

    showInTree: function(path){
        MgnlAdminCentral.showTree('dms', path);
    },
    
    showTree: function(){
        top.mgnlAdminCentralSwitchExtractTree('dms');
    },
    
    simpleSearch: function(val){
        if(document.mgnlForm &&  document.mgnlForm.searchStr){
            document.mgnlForm.searchStr.value = val;
            document.mgnlForm.submit();
        }
        else{
            document.location = contextPath + '/.magnolia/pages/dmsSearchList.html?searchStr=' +val;
        }
    },
     
    uploadZip: function(path){
        mgnlOpenWindow(".magnolia/dms/uploadzip.html?path=" + path);
    }
});