/**
 * This file Copyright (c) 2012-2012 Magnolia International
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
package info.magnolia.module.admininterface;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import info.magnolia.cms.beans.runtime.Document;
import info.magnolia.cms.beans.runtime.FileProperties;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.NodeData;
import info.magnolia.cms.security.AccessDeniedException;

import java.io.IOException;

import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;

import org.junit.Test;


/**

 * @version $Id$
 */
public class SaveHandlerImplDocTitlesTest {

    @Test
    public void testSetEmptyTitleToDocument() throws AccessDeniedException, PathNotFoundException, RepositoryException, IOException {

        //GIVEN
        String name = "someName";
        String template = "someTemplate";
        String fileName = ""; //empty file name

        Content node = mock(Content.class);
        NodeData data = mock(NodeData.class);
        when(node.getNodeData(name)).thenReturn(data);
        Document doc = null;

        //WHEN
        SaveHandlerImpl.saveDocument(node, doc, name, fileName, template);
        //THEN
        verify(data).setAttribute(FileProperties.PROPERTY_FILENAME, "untitled");
    }

    @Test
    public void testSetNonEmptyTitleToDocument() throws AccessDeniedException, PathNotFoundException, RepositoryException, IOException {

        //GIVEN
        String name = "someName";
        String template = "someTemplate";
        String fileName = "someFileName";

        Content node = mock(Content.class);
        NodeData data = mock(NodeData.class);
        when(node.getNodeData(name)).thenReturn(data);
        Document doc = null;

        //WHEN
        SaveHandlerImpl.saveDocument(node, doc, name, fileName, template);
        //THEN
        verify(data).setAttribute(FileProperties.PROPERTY_FILENAME, fileName);
    }
}
