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

import info.magnolia.cms.gui.controlx.impl.AdminInterfaceRenderKit;
import info.magnolia.cms.gui.controlx.impl.WebRenderKit;

/**
 * Factory to get the used RenderKit
 * @author Philipp Bracher
 * @version $Revision$ ($Author$)
 *
 */
public class RenderKitFactory {
    
    /**
     * The kit used in the admin interface
     */
    static RenderKit adminInterfaceRenderKit = new AdminInterfaceRenderKit();
    
    /**
     * Simple rendering. Less javascript, standard html controls
     */
    static RenderKit webRenderKit = new WebRenderKit();
    
    /**
     * @return Returns the adminInterfaceRenderKit.
     */
    public static RenderKit getAdminInterfaceRenderKit() {
        return adminInterfaceRenderKit;
    }

    
    /**
     * @param adminInterfaceRenderKit The adminInterfaceRenderKit to set.
     */
    public static void setAdminInterfaceRenderKit(RenderKit adminInterfaceRenderKit) {
        RenderKitFactory.adminInterfaceRenderKit = adminInterfaceRenderKit;
    }

    
    /**
     * @return Returns the webRenderKit.
     */
    public static RenderKit getWebRenderKit() {
        return webRenderKit;
    }

    
    /**
     * @param webRenderKit The webRenderKit to set.
     */
    public static void setWebRenderKit(RenderKit webRenderKit) {
        RenderKitFactory.webRenderKit = webRenderKit;
    }
    

}
