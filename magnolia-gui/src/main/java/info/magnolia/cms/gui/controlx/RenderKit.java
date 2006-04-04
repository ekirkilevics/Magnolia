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
package info.magnolia.cms.gui.controlx;

/**
 * Provides the renderer for the controls.
 * @author Philipp Bracher
 * @version $Revision$ ($Author$)
 */
public interface RenderKit {

    /**
     * Register a renderer
     * @param type
     * @param renderer
     */
    public void register(String type, Renderer renderer);

    /**
     * Get a renderer
     * @param type
     * @return
     */
    public Renderer getRenderer(String type);
}
