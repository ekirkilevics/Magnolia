/**
 * This file Copyright (c) 2003-2012 Magnolia International
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
package info.magnolia.module.delta;

import info.magnolia.cms.core.Content;
import info.magnolia.cms.util.ClasspathResourcesUtil;
import info.magnolia.importexport.PropertiesImportExport;
import info.magnolia.module.InstallContext;

import javax.jcr.RepositoryException;
import java.io.IOException;
import java.io.InputStream;

/**
 * A Task which will import nodes and properties using a .properties file.
 * TODO : conflict behaviour
 *
 * @see PropertiesImportExport
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class PropertiesImportTask extends AbstractRepositoryTask {
    private final String resource;
    private final String workspace;

    public PropertiesImportTask(String name, String description, String workspace, String resource) {
        super(name, description);
        this.resource = resource;
        this.workspace = workspace;
    }

    @Override
    protected void doExecute(InstallContext installContext) throws RepositoryException, TaskExecutionException {
        try {
            final InputStream propsStream = ClasspathResourcesUtil.getStream(resource);
            final Content root = installContext.getHierarchyManager(workspace).getRoot();
            new PropertiesImportExport().createContent(root, propsStream);
        } catch (IOException e) {
            throw new TaskExecutionException("Could not load properties: " + e.getMessage(), e);
        }
    }
}
