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

import info.magnolia.module.rest.tree.TreeNode;

import javax.ws.rs.*;

@Path("/repositories")
public class RepositoryJsonEndpoint {

    @GET
    @Path("/{repositoryName}/{path:(.)*}")
    public TreeNode getNode(
            @PathParam("repositoryName") String repositoryName,
            @PathParam("path") String path) {

        return new TreeNode();
    }

    @POST
    @Path("/{repositoryName}/{path:(.)*}/create")
    public TreeNode createNode(
            @PathParam("repositoryName") String repositoryName,
            @PathParam("path") String path,
            @QueryParam("itemType") String itemType) {

        // depending on the itemType we will need to find a suitable command

        // itemType (content, contentnode, user, role, group... but likely not metaData and nodeData)
        // optional name? defaults to?
        //

        return new TreeNode();
    }

    @POST
    @Path("/{repositoryName}/{path:(.)*}/setNodeData")
    public TreeNode setNodeData(
            @PathParam("repositoryName") String repositoryName,
            @PathParam("path") String path,
            @QueryParam("name") String name,
            @QueryParam("value") String value,
            @QueryParam("type") String type) {

        return new TreeNode();
    }

    @POST
    @Path("/{repositoryName}/{path:(.)*}/removeNodeData")
    public TreeNode removeNodeData(
            @PathParam("repositoryName") String repositoryName,
            @PathParam("path") String path,
            @QueryParam("name") String name) {

        return new TreeNode();
    }

    @POST
    @Path("/{repositoryName}/{path:(.)*}/delete")
    public TreeNode deleteNode(
            @PathParam("repositoryName") String repositoryName,
            @PathParam("path") String path) {

        // depending on the itemType might need to do different things

        return new TreeNode();
    }

    @POST
    @Path("/{repositoryName}/{path:(.)*}/rename")
    public TreeNode renameNode(
            @PathParam("repositoryName") String repositoryName,
            @PathParam("path") String path) {

        // depending on the itemType might need to do different things

        return new TreeNode();
    }

    @POST
    @Path("/{repositoryName}/{path:(.)*}/move")
    public TreeNode moveNode(
            @PathParam("repositoryName") String repositoryName,
            @PathParam("path") String path,
            @PathParam("newPath") String newPath) {

        // some itemTypes cannot be moved, i.e. users

        return new TreeNode();
    }

    @POST
    @Path("/{repositoryName}/{path:(.)*}/copy")
    public TreeNode copyNode(
            @PathParam("repositoryName") String repositoryName,
            @PathParam("path") String path,
            @PathParam("newPath") String newPath) {

        return new TreeNode();
    }

    @POST
    @Path("/{repositoryName}/{path:(.)*}/activate")
    public TreeNode activateNode(
            @PathParam("repositoryName") String repositoryName,
            @PathParam("path") String path) {

        return new TreeNode();
    }

    @POST
    @Path("/{repositoryName}/{path:(.)*}/deactivate")
    public TreeNode deactivateNode(
            @PathParam("repositoryName") String repositoryName,
            @PathParam("path") String path) {

        return new TreeNode();
    }
}
