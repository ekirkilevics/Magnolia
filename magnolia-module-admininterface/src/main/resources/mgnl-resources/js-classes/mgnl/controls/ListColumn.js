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
        this.cssClass.style.width = width + "px";
        this.cssClass.style.clip="rect(0, " + width + "px, 20px, 0)";
        
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