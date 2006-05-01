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
    
    proceed: function(id){
        $('flowItemId').value = id;
        $('command').value = "proceed";
        this.openCommentDialog();
    },
    
    reject: function(id){
        $('flowItemId').value = id;
        $('command').value = "reject";
        this.openCommentDialog();
    },

    cancel: function(id){
        $('flowItemId').value = id;
        $('command').value = "cancel";
        document.mgnlForm.submit();
    },
    
    afterSaveComment: function(comment){
        $('comment').value = comment;
        document.mgnlForm.submit();
    },

    openCommentDialog: function(){
        mgnlOpenDialog('','','','inboxComment','');
    },
    
});
 
 