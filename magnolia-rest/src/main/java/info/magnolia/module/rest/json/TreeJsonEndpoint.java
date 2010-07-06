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
package info.magnolia.module.rest.json;

import info.magnolia.module.rest.tree.JsonTreeHandlerManager;
import info.magnolia.module.rest.tree.TreeHandler;
import info.magnolia.module.rest.tree.TreeNodeList;
import info.magnolia.module.rest.tree.config.JsonTreeConfiguration;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;

@Path("/tree")
public class TreeJsonEndpoint {

    @GET
    @Path("/{treeName}")
    public TreeNodeList getNode(@PathParam("treeName") String treeName) throws Exception {
        return getNode(treeName, "");
    }

    @GET
    @Path("/{treeName}/{path:(.)*}")
    public TreeNodeList getNode(@PathParam("treeName") String treeName, @PathParam("path") String path) throws Exception {

        TreeHandler treeHandler = JsonTreeHandlerManager.getInstance().getTreeHandler(treeName);

        if (treeHandler == null)
            return null;

        return treeHandler.getChildren("/" + path);
    }

    @POST
    @Path("/{treeName}/config")
    public JsonTreeConfiguration getConfiguration(@PathParam("treeName") String treeName) throws Exception {

        TreeHandler treeHandler = JsonTreeHandlerManager.getInstance().getTreeHandler(treeName);

        if (treeHandler == null)
            return null;

        return treeHandler.getConfiguration();
    }

    @POST
    @Path("/{treeName}/{path:(.)*}/command")
    public Object executeCommand(
            @PathParam("treeName") String treeName,
            @PathParam("path") String path,
            @Context HttpServletRequest request) throws Exception {

        TreeHandler treeHandler = JsonTreeHandlerManager.getInstance().getTreeHandler(treeName);

        if (treeHandler == null)
            return null;

        return treeHandler.executeCommand(path, request.getParameter("command"), request.getParameterMap());
    }
}
