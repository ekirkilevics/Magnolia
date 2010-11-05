/**
 * This file Copyright (c) 2008-2009 Magnolia International
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
classDef("mgnl.workflow.WorkflowWebsiteTree",

    // static methods only
    {
        enterComment: function(tree, action, recursive){
            this.tree = tree;
            this.recursive = recursive;
            this.action = action;

            if (action == 2) {
	            // manipulate tree object
	            tree.activateNode =

	            // will call the same method but with the comment as a parameter
	            mgnl.admininterface.Dialog.open('startActivationWorkflow');
            } else if (action == 3) {
	            // manipulate tree object
	            tree.deactivateNode =

	            // will call the same method but with the comment as a parameter
	            mgnl.admininterface.Dialog.open('startDeactivationWorkflow');
            }

        },

        submitActivation: function(form){
            var nodeToReload=this.tree.selectedNode.getParent();

            var params=new Object();
            params.forceReload=true;
            params.treeAction=this.action;
            params.pathSelected=this.tree.selectedNode.id;
            if (this.recursive) params.recursive=this.recursive;

            // iterate over fields of the form
            for (var i = 0; i < form.elements.length; i++) {
                var element = form.elements[i];
                // ignores mgnlSaveInfo and _saveHandler and _configNode elements
                // unfortunately also ignores checkbox fields ...
                if (element.type != "hidden") {
                    params[element.name] = element.value;
                }
            }

            nodeToReload.expand(params);
        }


    }
);