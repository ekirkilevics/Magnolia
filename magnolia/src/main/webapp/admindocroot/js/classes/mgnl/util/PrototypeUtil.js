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
 
 /**
  * A util to manage the prototypes in javascripts. Used to extend allready existing prototype for example.
  */
  
 classDef("mgnl.util.PrototypeUtil",{
    /**
     * Add the properties from src to dest.<b> 
     * <code> extend(obj.prototype, myOtherParent) </code>
     * @parma dst the target
     * @parma src the object whichs properties will get copied to dst
     * @parma overwrite true if existing properties get overwritten (default is true)
     */
    extend: function(dst, src, overwrite){
        // default is false
        overwrite = overwrite?overwrite: false;
        
        for(name in src){
            if(overwrite || dst[name] == null){
                dst[name] = src[name];
            }
        }
        return dst;
    }
 });