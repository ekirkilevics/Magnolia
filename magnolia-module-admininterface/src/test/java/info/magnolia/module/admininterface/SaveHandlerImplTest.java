/**
 * This file Copyright (c) 2003-2011 Magnolia International
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;

import javax.jcr.LoginException;
import javax.jcr.RepositoryException;

import info.magnolia.cms.beans.config.URI2RepositoryManager;
import info.magnolia.cms.beans.config.URI2RepositoryMapping;
import info.magnolia.cms.beans.runtime.Document;
import info.magnolia.cms.beans.runtime.FileProperties;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.ItemType;
import info.magnolia.cms.gui.fckeditor.FCKEditorTmpFiles;
import info.magnolia.cms.i18n.DefaultI18nContentSupport;
import info.magnolia.cms.i18n.I18nContentSupport;
import info.magnolia.context.MgnlContext;
import info.magnolia.test.ComponentsTestUtil;
import info.magnolia.test.mock.MockUtil;
import info.magnolia.test.mock.MockWebContext;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Test;


/**
 * TODO : review, rewrite.
 *
 * @version $Id$
 */
public class SaveHandlerImplTest {

    @After
    public void tearDown() throws Exception {
        ComponentsTestUtil.clear();
        MgnlContext.setInstance(null);
    }

    /**
     * Test for update links returned by rich editor.
     */
    @Test
    public void testUpdateLink() throws LoginException, RepositoryException, IOException {
        ComponentsTestUtil.setInstance(I18nContentSupport.class, new DefaultI18nContentSupport());
        URI2RepositoryManager uri2RepositoryManager = new URI2RepositoryManager();
        uri2RepositoryManager.addMapping(new URI2RepositoryMapping("/data", "data", ""));
        uri2RepositoryManager.addMapping(new URI2RepositoryMapping("/dms", "dms", ""));
        ComponentsTestUtil.setInstance(URI2RepositoryManager.class, uri2RepositoryManager);

        MockUtil.initMockContext();
        MockUtil.createAndSetHierarchyManager("data", getClass().getResourceAsStream("sample-data-repo.properties"));
        MockUtil.createAndSetHierarchyManager("dms", getClass().getResourceAsStream("sample-dms-repo.properties"));
        MockWebContext webCtx = (MockWebContext) MockUtil.getMockContext();
        webCtx.setContextPath("/magnoliaAuthor");

        //create document tmp files to make test repeatable
        File originalFile = new File(getClass().getResource("savehandlertest-document.jpg").getFile());
        File tmpFile1 = new File(originalFile.getParent() + "/test1.jpg");
        File tmpFile2 = new File(originalFile.getParent() + "/test2.jpg");
        FileUtils.copyFile(originalFile, tmpFile1);
        FileUtils.copyFile(originalFile, tmpFile2);
        FCKEditorTmpFiles.addDocument(new Document(tmpFile1, "image/jpg"), "7a645ab5-8df0-11e1-ac08-6b09862c6153");
        FCKEditorTmpFiles.addDocument(new Document(tmpFile2, "image/jpg"), "a225f776-8df0-11e1-ac08-6b09862c6153");

        SaveHandlerImpl save = new SaveHandlerImpl();
        save.setRepository("data");

        final String value = "<p><img width=\"300\" height=\"100\" src=\"/magnoliaAuthor/tmp/fckeditor/7a645ab5-8df0-11e1-ac08-6b09862c6153/test1.jpg\" alt=\"\" /><img src=\"/magnoliaAuthor/dms/demo-project/img/logos/magnolia-logo.png\" alt=\"\" /><img width=\"280\" height=\"70\" src=\"/magnoliaAuthor/tmp/fckeditor/a225f776-8df0-11e1-ac08-6b09862c6153/test2.jpg\" alt=\"\" /></p>";
        Content node = MgnlContext.getHierarchyManager("data").getContentByUUID("73f8656e-ce5a-4e4c-a15d-3e205e9d706d");

        final String updatedValue = save.updateLinks(node, "richedit", value);

        node = MgnlContext.getHierarchyManager("data").getContent("/contactA/richedit_files", false, ItemType.CONTENTNODE);
        Iterator<Content> iterFilesNode = node.getChildren().iterator();
        Content fileTest1 = iterFilesNode.next();
        Content fileTest2 = iterFilesNode.next();

        assertFalse(iterFilesNode.hasNext());
        assertEquals("test1", fileTest1.getNodeData("document").getAttribute(FileProperties.PROPERTY_FILENAME));
        assertEquals("test2", fileTest2.getNodeData("document").getAttribute(FileProperties.PROPERTY_FILENAME));

        final String expectedValue = "<p><img width=\"300\" height=\"100\" src=\"${link:{uuid:{"+ fileTest1.getUUID() + "},repository:{data},handle:{/contactA/richedit_files/file},nodeData:{document},extension:{jpg}}}\" alt=\"\" /><img src=\"${link:{uuid:{a1918662-8cbe-4346-ac5a-1b24a9950e2b},repository:{dms},handle:{/demo-project/img/logos/magnolia-logo},nodeData:{},extension:{png}}}\" alt=\"\" /><img width=\"280\" height=\"70\" src=\"${link:{uuid:{" + fileTest2.getUUID() + "},repository:{data},handle:{/contactA/richedit_files/file0},nodeData:{document},extension:{jpg}}}\" alt=\"\" /></p>";

        assertEquals(expectedValue, updatedValue);
    }

    /**
     * Test for update links with special characters returned by rich editor.
     */
    @Test
    public void testUpdateLinkWithSpecialCharacters() throws LoginException, RepositoryException, IOException {
        // GIVEN
        ComponentsTestUtil.setInstance(I18nContentSupport.class, new DefaultI18nContentSupport());
        URI2RepositoryManager uri2RepositoryManager = new URI2RepositoryManager();
        uri2RepositoryManager.addMapping(new URI2RepositoryMapping("/dms", "dms", ""));
        ComponentsTestUtil.setInstance(URI2RepositoryManager.class, uri2RepositoryManager);

        MockUtil.initMockContext();
        MockUtil.createAndSetHierarchyManager("dms");
        MockUtil.createAndSetHierarchyManager("website");
        MockWebContext webCtx = (MockWebContext) MockUtil.getMockContext();
        webCtx.setContextPath("/magnoliaAuthor");

        SaveHandlerImpl save = new SaveHandlerImpl();

        Content node = MgnlContext.getHierarchyManager("website").getRoot().createContent("demo").createContent("content");
        final String uuid = MgnlContext.getJCRSession("dms").getRootNode().addNode("testöäüćř").addNode("magnolia-logo").getIdentifier();

        final String value = "<p><img src=\"/magnoliaAuthor/dms/test&ouml;&auml;&uuml;ćř/magnolia-logo.png\" alt=\"\" /></p>";

        // WHEN
        final String updatedValue = save.updateLinks(node, "richedit", value);

        //THEN
        final String expectedValue = "<p><img src=\"${link:{uuid:{"+ uuid +"},repository:{dms},handle:{/testöäüćř/magnolia-logo},nodeData:{},extension:{png}}}\" alt=\"\" /></p>";

        assertEquals(expectedValue, updatedValue);
    }

    /**
     * Test for rich editor cleanup. IE often insert a br at the beginning of a paragraph.
     */
    @Test
    public void testGetRichEditValueStrCleanExplorerPs() {
        SaveHandlerImpl save = new SaveHandlerImpl();
        assertEquals("aaa\n\n  bbb", save.cleanLineBreaks("<P>aaa</P>\r\n<P><BR>bbb</P>", -1));
    }
}
