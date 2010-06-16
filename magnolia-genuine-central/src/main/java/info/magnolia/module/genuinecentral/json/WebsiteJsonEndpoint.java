/**
 * This file Copyright (c) 2003-2010 Magnolia International
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
package info.magnolia.module.genuinecentral.json;

import info.magnolia.module.genuinecentral.tree.WebsitePage;
import info.magnolia.module.genuinecentral.tree.WebsitePageList;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Path("/website")
public class WebsiteJsonEndpoint {

    @GET
    public WebsitePageList getRootNode() {
        WebsitePageList pages = new WebsitePageList();
        pages.add(createMockPage("news", "News Desk", true));
        pages.add(createMockPage("about", "About", false));
        return pages;
    }

    @GET
    @Path("{path:(.)*}")
    public WebsitePageList getNode(@PathParam("path") String path) {

        WebsitePageList pages = new WebsitePageList();

        if (path.equals("news")) {
            pages.add(createMockPage("merger", "QWE merges with RTY", false));
            pages.add(createMockPage("hiring", "New position available", false));
        } else if (path.equals("news/merger")) {
        } else if (path.equals("news/hiring")) {
        } else {
            // Sends a 404 to the client
            return null;
        }

        return pages;
    }

    private WebsitePage createMockPage(String name, String title, boolean hasChildren) {

        List<String> templates = new ArrayList<String>();
        templates.add("main");
        templates.add("section");

        WebsitePage page = new WebsitePage();
        page.setName(name);
        page.setTitle(title);
        page.setLastModified(new Date());
        page.setStatus("active");
        page.setTemplate("main");
        page.setHasChildren(hasChildren);
        page.setAvailableTemplates(templates);
        return page;
    }
}
