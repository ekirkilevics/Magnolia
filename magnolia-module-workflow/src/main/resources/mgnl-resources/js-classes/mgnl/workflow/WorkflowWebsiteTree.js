/**
 *
 * Magnolia and its source-code is licensed under the LGPL.
 * You may copy, adapt, and redistribute this file for commercial or non-commercial use.
 * When copying, adapting, or redistributing this document in keeping with the guidelines above,
 * you are required to provide proper attribution to obinary.
 * If you reproduce or distribute the document without making any substantive modifications to its content,
 * please use the following attribution line:
 *
 * Copyright 2008 obinary Ltd. (http://www.obinary.com) All rights reserved.
 *
 */
classDef("mgnl.workflow.WorkflowWebsiteTree",

    // static methods only
    {
        enterComment: function(tree, action, recursive){
            this.tree = tree;
            this.recursive = recursive;
            this.action = action;

            // manipulate tree object
            tree.activateNode =

            // will call the same method but with the comment as a parameter
            mgnl.admininterface.Dialog.open('startActivationWorkflow');
        },

        submitActivation: function(comment){
            var nodeToReload=this.tree.selectedNode.getParent();

            var params=new Object();
            params.forceReload=true;
            params.treeAction=this.action;
            params.pathSelected=this.tree.selectedNode.id;
            if (this.recursive) params.recursive=this.recursive;
            params.comment = comment;
            nodeToReload.expand(params);
        }


    }
);