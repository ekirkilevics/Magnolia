/**
 *
 * Magnolia and its source-code is licensed under the LGPL.
 * You may copy, adapt, and redistribute this file for commercial or non-commercial use.
 * When copying, adapting, or redistributing this document in keeping with the guidelines above,
 * you are required to provide proper attribution to obinary.
 * If you reproduce or distribute the document without making any substantive modifications to its content,
 * please use the following attribution line:
 *
 * Copyright 1993-2005 obinary Ltd. (http://www.obinary.com) All rights reserved.
 *
 */
package info.magnolia.cms.gui.controlx.impl;

import info.magnolia.cms.gui.controlx.RenderKit;
import info.magnolia.cms.gui.controlx.Renderer;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.collections.Factory;
import org.apache.commons.collections.MapUtils;

/**
 * If a not registered render type is passed a TemplatedRenderer instance is returned.
 * @author Philipp Bracher
 * @version $Revision$ ($Author$)
 *
 */
public class RenderKitImpl implements RenderKit {
    
    /**
     * The renderers registered.
     */
    Map renderers;
    
    /**
     * Init the layzy map. 
     */
    public RenderKitImpl() {
        renderers = MapUtils.lazyMap(new HashMap(), new Factory(){
            public Object create() {
                return new TemplatedRenderer();
            } 
        });
    }

    /**
     * @see info.magnolia.cms.gui.controlx.RenderKit#register(java.lang.String, info.magnolia.cms.gui.controlx.Renderer)
     */
    public void register(String type, Renderer renderer) {
        this.renderers.put(type, renderer);
    }

    /**
     * @see info.magnolia.cms.gui.controlx.RenderKit#getRenderer(java.lang.String)
     */
    public Renderer getRenderer(String type) {
        // using the lazy map
        return (Renderer) this.renderers.get(type);
    }

}
