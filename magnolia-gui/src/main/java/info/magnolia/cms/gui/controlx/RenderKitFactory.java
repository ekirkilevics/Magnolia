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

import info.magnolia.cms.gui.controlx.impl.TestRenderKit;

import java.util.HashMap;
import java.util.Map;


/**
 * Factory to get the used RenderKit
 * @author Philipp Bracher
 * @version $Revision$ ($Author$)
 */
public class RenderKitFactory {

    /**
     * RenderKit used in the admin interface
     */
    public static String ADMIN_INTERFACE_RENDER_KIT = "adminInterfaceRenderKit";

    /**
     * RenderKit used in the public website.
     */
    public static String WEB_RENDER_KIT = "webRenderKit";

    /**
     * Minimal output for testing reasons
     */
    public static String TEST_RENDER_KIT = "testRenderKit";

    /**
     * The registered RenderKits
     */
    private static Map renderKits = new HashMap();

    /**
     * Register the test render kit as default.
     */
    static {
        registerRenderKit(TEST_RENDER_KIT, new TestRenderKit());
        registerRenderKit(ADMIN_INTERFACE_RENDER_KIT, new TestRenderKit());
        registerRenderKit(WEB_RENDER_KIT, new TestRenderKit());
    }

    /**
     * Register a RenderKit
     * @param name
     * @param renderKit
     */
    public static void registerRenderKit(String name, RenderKit renderKit) {
        renderKits.put(name, renderKit);
    }

    /**
     * Get a named RenderKit
     * @param name
     * @return
     */
    public static RenderKit getRenderKit(String name) {
        return (RenderKit) renderKits.get(name);
    }
}
