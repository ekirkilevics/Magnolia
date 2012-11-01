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
package info.magnolia.module.admininterface;

import info.magnolia.test.mock.MockContent;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;

import javax.jcr.LoginException;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;

import info.magnolia.cms.beans.config.URI2RepositoryManager;
import info.magnolia.cms.beans.config.URI2RepositoryMapping;
import info.magnolia.cms.beans.runtime.Document;
import info.magnolia.cms.beans.runtime.FileProperties;
import info.magnolia.cms.beans.runtime.MultipartForm;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.ItemType;
import info.magnolia.cms.core.NodeData;
import info.magnolia.cms.gui.fckeditor.FCKEditorTmpFiles;
import info.magnolia.cms.i18n.DefaultI18nContentSupport;
import info.magnolia.cms.i18n.I18nContentSupport;
import info.magnolia.cms.security.AccessDeniedException;
import info.magnolia.context.MgnlContext;
import info.magnolia.test.ComponentsTestUtil;
import info.magnolia.test.mock.MockUtil;
import info.magnolia.test.mock.MockWebContext;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
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
        //THEN any exception occurs
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
        //THEN any exception occurs
    }
}
