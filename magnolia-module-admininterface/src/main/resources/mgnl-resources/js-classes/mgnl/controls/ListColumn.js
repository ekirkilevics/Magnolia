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
 
classDef("mgnl.controls.ListColumn", function (list, width, index){

    this.list = list;

    this.index = index;
    
    this.fixed;
    
    this.width;
    
    this.cssClass = mgnl.util.DHTMLUtil.getCSSClass(list.name + "CssClassColumn" + index);
    
    this.line = $(list.name + "ColumnLine" + index);
    
    this.resizer = $(list.name + "ColumnResizer" + index);

    this.list.columns[index] = this;
    
    if(width.indexOf("px")!=-1){
        this.width = parseInt(width.substr(0, width.length-2));
        this.fixed=true;
    }
    else{
        this.width = parseInt(width);
        this.fixed=false;
    }
    
    this.resize = function(left, width){
        this.cssClass.style.left = left + "px";
        this.cssClass.style.width = width;
        this.cssClass.style.clip="rect(0 " + width + " 20 0)";
        
        if(this.resizer){
            this.resizer.style.left = left + "px";
        }
        if(this.line){
            this.line.style.left = (left + 5) + "px";
            this.line.style.height = (this.list.height - 20) + "px";
        }
        this.width = width;
        this.left = left;
    }
 });