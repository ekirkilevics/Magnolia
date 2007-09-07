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

classDef("mgnl.controls.List",

    // contstuctor
    function(name, form){
        this.name = name;
        this.form = form;

        // the row of the list
        this.selected = -1;

        // the id used by the list model
        this.selectedId = null;

        this.mainDiv = $(name + "Div");
        this.sortGroupDiv = $(name + "SortGroupDiv");
        this.contentDiv = $(name + "ContentDiv");
        this.innerContentDiv = $(name + "InnerContentDiv");
        this.resizerLine = $(name + "ColumnResizerLine");

        this.columns = new Array();

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
            this.selectedId = id;

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
            this.selectedId = null;
        },

        show: function(){
            alert(this.selectedId);
        },

        startResizeColumn: function(index){
            this.resizerLine.style.visibility="visible";
            this.resizerLine.style.left = this.columns[index].left + 5;
            this.resizerLine.style.height = this.height;
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
            this.resizerLine.style.left = pos.x-1;
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
                this.sortGroupDiv.style.left = this.width - sortWidth + "px";
                this.sortGroupDiv.style.height = this.height -1 + "px";
                this.sortGroupDiv.style.visibility = "visible";
                this.width -= sortWidth;
            }

            MgnlDebug.debug("new total widht:" + this.width, this);

            MgnlDHTMLUtil.setWidth(this.contentDiv, this.width);
            MgnlDHTMLUtil.setHeight(this.contentDiv, this.height);
            this.innerContentDiv.style.height = this.height - 20;

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