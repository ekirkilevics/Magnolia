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
 
classDef("mgnl.owfe.Inbox", {
    /**
     * Some default show functions
     */
	showFunctions:{
		website: function(){
			var url = this.current.path;

		    if(contextPath.length != 0){
    			url = contextPath + url;
    		}
			url += ".html";
			if(this.current.version != null && this.current.version.length >0){
				url += "?mgnlVersion=" + this.current.version;
			}
			var w = window.open(url);
			w.focus();
		},
		
		dms: function(){
			if(this.current.version != null && this.current.version.length >0){
				mgnl.dms.DMS.showVersion(this.current.path, this.current.version);
			}
			else{
				mgnl.dms.DMS.show(this.current.path);
			}
		}
	},

    /**
     * The currently selected objects.
     */
    current: {
    	id:null, 
    	path:null, 
    	repository: null,
    	workItemPath: null,
    	editDialog: 'inboxComment'
    },
    
    /**
     * The inbox will override this function depending on what you select
     */
	show: function(){},
	
    edit: function(){
    	mgnlOpenDialog(this.current.workItemPath + '/value/attributes','','',this.current.editDialog, 'Store');
    },
    
    proceed: function(){
        $('flowItemId').value = this.current.id;
        $('command').value = "proceed";
        document.mgnlForm.submit();
    },
    
    reject: function(id){
        id = id==null ? this.currentId : id;
        $('flowItemId').value = id;
        $('command').value = "reject";
        document.mgnlForm.submit();
    },

    cancel: function(id){
        id = id==null ? this.currentId : id;
        $('flowItemId').value = id;
        $('command').value = "cancel";
        document.mgnlForm.submit();
    }
    
});
 
 