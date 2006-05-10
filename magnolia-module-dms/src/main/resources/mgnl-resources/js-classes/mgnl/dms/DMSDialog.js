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

classDef("mgnl.dms.DMSDialog", {

    init: function(){
        // change the default function
        mgnlControlFileSetFileName = mgnl.dms.DMSDialog.setFileName;
    },
    
    setFileName: function(id,clear){
        var nameElem=document.getElementById(id+"_fileName");
        var extElem=document.getElementById(id+"_extension");
    
        var ext="";
        var name="";
    
        if (!clear){
            var fileElem=document.getElementById(id);
            var value=fileElem.value;
    
            var slash="\\";
            if (value.indexOf("/")!=-1){
                slash="/";
            }
    
            if (value.indexOf(".")!=-1){
                name=value.substring(value.lastIndexOf(slash)+1,value.lastIndexOf("."));
                ext=value.substring(value.lastIndexOf(".") + 1);
            }
            else{ 
                name=value.substring(value.lastIndexOf(slash)+1);
            }
        }
        else{
            ext="";
        }
        
        if (nameElem){ 
            nameElem.value=name;
        }
        
        if (extElem){
            extElem.value=ext;
        }
    },

    hideSaveButton: function(){
        divs = window.document.getElementsByTagName("div");
        for(i=0; i<divs.length; i++){
            if(divs[i].className=="mgnlDialogTabsetSaveBar"){
                var div = divs[i];
                div.firstChild.style.visibility = "hidden";
            }
        }
    }
});