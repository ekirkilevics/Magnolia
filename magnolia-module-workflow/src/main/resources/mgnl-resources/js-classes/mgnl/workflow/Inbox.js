/**
 * This file Copyright (c) 1993-2009 Magnolia International
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

classDef("mgnl.workflow.Inbox", {
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
        id = id==null ? this.current.id : id;
        $('flowItemId').value = id;
        $('command').value = "reject";
        document.mgnlForm.submit();
    },

    cancel: function(id){
        id = id==null ? this.current.id : id;
        $('flowItemId').value = id;
        $('command').value = "cancel";
        document.mgnlForm.submit();
    }

});

