/**
 * This file Copyright (c) 2003-2009 Magnolia International
 * Ltd.  (http://www.magnolia.info). All rights reserved.
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
 * is available at http://www.magnolia.info/mna.html
 *
 * Any modifications to this file must keep this entire header
 * intact.
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
