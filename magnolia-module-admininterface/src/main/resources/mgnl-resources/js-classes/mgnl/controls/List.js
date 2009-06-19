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

classDef("mgnl.controls.List",

    // contstuctor
    function(name, form){
        this.name = name;
        this.form = form;

        // the row of the list
        this.selected = -1;

        this.mainDiv = $(name + "Div");
        this.sortGroupDiv = $(name + "SortGroupDiv");
        this.contentDiv = $(name + "ContentDiv");
        this.innerContentDiv = $(name + "InnerContentDiv");
        this.resizerLine = $(name + "ColumnResizerLine");

        this.columns = new Array();

        this.items = new Object();
    },

    // members
    {

        // css classes used
        css: {
            row:{
                selected: 'mgnlListRowSelected',
                hover: 'mgnlListRowHover',
                normal: 'mgnlListRow'
            }
        },

        addColumn: function(column){
            this.columns.push(column);
        },

        select: function(index, id){
            this.unselect();
            this.selected = index;

            var row = $(this.name + 'Row' + this.selected);
            row.className = this.css.row.selected;

            // refresh function bar if any
            if(window["mgnlFunctionBar"]){
                window["mgnlFunctionBar"].refresh();
            }
        },

        unselect: function(){
            if(this.selected == -1)
                return;
            var row = $(this.name + 'Row' + this.selected);
            row.className= this.css.row.normal;
            this.selected = -1;
        },

        show: function(){
            alert(this.getSelectedItem().id);
        },

        getSelectedItem: function(){
            return this.items[this.selected];
        },

        startResizeColumn: function(index){
            this.resizerLine.style.visibility="visible";
            this.resizerLine.style.left = (this.columns[index].left + 5) + "px";
            this.resizerLine.style.height = this.height + "px";
            var list = this;

            this.contentDiv.onmousemove = function(event){
                    list.onResizeColumn(event, index);
            };

            this.contentDiv.onmouseup = function(event){
                    list.stopResizeColumn(event, index);
            };

            mgnl.util.Debug.debug("start column resizing");
        },

        onResizeColumn: function(event, index){
            var pos = mgnl.util.DHTMLUtil.getMousePos(event);
            this.resizerLine.style.left = (pos.x-1) + "px";
        },

        stopResizeColumn: function(event, index){
            var newLeft = mgnl.util.DHTMLUtil.getMousePos(event).x -6;
            var column = this.columns[index];

            column.resize(newLeft, column.width + (column.left - newLeft));
            // resize also column to the left
            if(index >= 1){
                column = this.columns[index-1];
                column.resize(column.left, newLeft -column.left);
            }

            this.resizerLine.style.visibility="hidden";
            this.contentDiv.onmousemove = null;
            this.contentDiv.onmouseup = null;

            mgnl.util.Debug.debug("stop column resizing");
        },

        mouseover: function(id){
            if(id != this.selected){
                var row = $(this.name + 'Row' + id);
                row.className= this.css.row.hover;
            }
        },

        mouseout: function(id){
            if(id != this.selected){
                var row = $(this.name + 'Row' + id);
                row.className= this.css.row.normal;
            }
        },

        resize: function(){
            this.height = MgnlDHTMLUtil.getHeight(this.mainDiv);
            this.width = MgnlDHTMLUtil.getWidth(this.mainDiv);

            if(this.sortGroupDiv){
                var sortWidth = MgnlDHTMLUtil.getWidth(this.sortGroupDiv);
                this.sortGroupDiv.style.left = (this.width - sortWidth) + "px";
                this.sortGroupDiv.style.height = (this.height -1) + "px";
                this.sortGroupDiv.style.visibility = "visible";
                this.width -= sortWidth;
            }

            MgnlDebug.debug("new total widht:" + this.width, this);

            MgnlDHTMLUtil.setWidth(this.contentDiv, this.width);
            MgnlDHTMLUtil.setHeight(this.contentDiv, this.height);
            this.innerContentDiv.style.height = (this.height - 20) + "px";

            // columns
            var factor = this.getWidthFactor(this.width);
            MgnlDebug.debug("factor is:" +factor, this);
            left=0;
            for(i=0; i< this.columns.length;i++){
                var column = this.columns[i];
                var newColumnWidth;
                if(column.fixed){
                    newColumnWidth  = column.width;
                }
                else{
                    newColumnWidth  = factor * column.width;
                }
                column.resize(left, newColumnWidth)
                left += newColumnWidth; // next start
            }
        },

        /**
        * Calculate the factor to use for resizing the dynamic columns.
        */
        getWidthFactor: function(){
            var sum = 0;
            var fixSum = 0;
            for(i=0; i<this.columns.length; i++){
                if(!this.columns[i].fixed){
                    sum += this.columns[i].width;
                }
                else{
                    fixSum += this.columns[i].width;
                }
            }
            // this subtraction is found due experiments
            return (this.width - fixSum - 2*this.columns.length - 4) / sum;
        },

        sort: function(name, direction){
            this.form.sortBy.value = name;
            this.form.sortByOrder.value = direction;
            this.form.submit();
        },

        group: function(name, direction){
            this.form.groupBy.value = name;
            this.form.groupByOrder.value = direction;
            this.form.submit();
        },

        search: function(value){
            this.form.searchStr.value = value;
            this.form.submit();
        },

        isSelected: function(){
            return this.selected != -1;
        }
    }
);