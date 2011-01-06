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
package info.magnolia.utf8;

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.core.ItemType;
import info.magnolia.cms.core.NodeData;
import info.magnolia.cms.core.Path;
import info.magnolia.cms.security.AccessDeniedException;
import info.magnolia.cms.util.ContentUtil;
import info.magnolia.cms.util.NodeDataUtil;
import info.magnolia.context.MgnlContext;
import info.magnolia.importexport.DataTransporter;
import info.magnolia.importexport.PropertiesImportExport;
import info.magnolia.test.RepositoryTestCase;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.jcr.ImportUUIDBehavior;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.UnsupportedRepositoryOperationException;
import javax.jcr.Value;
import javax.jcr.ValueFactory;

import junit.framework.Assert;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;


/**
 * @author luca boati
 */
public class Utf8ContentTest extends RepositoryTestCase
{

    public void testReadingUtf8AccentedChars() throws Exception
    {
        HierarchyManager hm = MgnlContext.getInstance().getHierarchyManager(ContentRepository.WEBSITE);
        bootstrapTextXml(hm);

        String text = "citt\u00E0\u00E8\u00EC\u00F2\u00F9\u00E4\u00F6\u00EB\u00E9\u00E8\u00E0\u00EC";
        Content content = hm.getContent("/utf8test/" + text);
        assertNotNull(content);
        assertEquals("/utf8test/" + text, content.getHandle());
    }

    public void testReadingUtf8Greek() throws Exception
    {
        HierarchyManager hm = MgnlContext.getInstance().getHierarchyManager(ContentRepository.WEBSITE);
        bootstrapTextXml(hm);

        String text = "\u03BA\u1F79\u03C3\u03BC\u03B5";
        Content content = hm.getContent("/utf8test/" + text);
        assertNotNull(content);
        assertEquals("/utf8test/" + text, content.getHandle());
    }

    public void testReadingUtf8Russian() throws Exception
    {
        HierarchyManager hm = MgnlContext.getInstance().getHierarchyManager(ContentRepository.WEBSITE);
        bootstrapTextXml(hm);

        String text = "\u041D\u0430 \u0431\u0435\u0440\u0435\u0433\u0443 \u043F\u0443\u0441\u0442\u044B\u043D\u043D\u044B\u0445 \u0432\u043E\u043B\u043D";
        Content content = hm.getContent("/utf8test/" + text);
        assertNotNull(content);
        assertEquals("/utf8test/" + text, content.getHandle());
    }

    public void testReadingUtf8SpecialChars() throws Exception
    {
        HierarchyManager hm = MgnlContext.getInstance().getHierarchyManager(ContentRepository.WEBSITE);
        bootstrapTextXml(hm);

        String text = "utf8!?#{}$!\u00A3%()=@";

        // String textEncoded = ISO9075.encode("utf8!?#"); // utf8_x0021__x003f__x0023_
        // String textDecoded = ISO9075.decode(textEncoded);

        Content content = hm.getContent("/utf8test/" + text);
        assertNotNull(content);
        assertEquals("/utf8test/" + text, content.getHandle());
    }

    public void testReadingUtf8JapaneseChars() throws Exception
    {
        HierarchyManager hm = MgnlContext.getInstance().getHierarchyManager(ContentRepository.WEBSITE);
        bootstrapTextXml(hm);

        String text = "\u30E2\u30AF\u30EC\u30F3";
        Content content = hm.getContent("/utf8test/" + text);
        assertNotNull(content);
        assertEquals("/utf8test/" + text, content.getHandle());
    }

    public void testCreateExportImportContentUtf8SpecialChars()
    {
        HierarchyManager hm = MgnlContext.getInstance().getHierarchyManager(ContentRepository.WEBSITE);
        String basePath = "utf8test";
        String baseGreek = "\u03BA\u1F79\u03C3\u03BC\u03B5";
        String text = "utf8!?#{}$!\u00A3%()=@";

        try
        {
            Content root = hm.getRoot();
            Session session = root.getWorkspace().getSession();
            Content base = root.createContent(basePath, ItemType.CONTENT);
            base = hm.getContent(basePath);
            Content special = base.createContent(text, ItemType.CONTENT);

            File xmlFile = exportNode(ContentRepository.WEBSITE, base.getWorkspace().getSession(), special);

            Content base2 = root.createContent(baseGreek, ItemType.CONTENT);

            FileInputStream inStream = new FileInputStream(xmlFile);
            session.importXML(base2.getHandle(), inStream, ImportUUIDBehavior.IMPORT_UUID_CREATE_NEW);

            IOUtils.closeQuietly(inStream);
            xmlFile.delete();

            assertEquals(2, root.getChildren().size());
            assertEquals(1, base2.getChildren().size());
            Content special2 = base2.getContent(text);
            assertEquals("/" + baseGreek + "/" + text, special2.getHandle());
        }
        catch (AccessDeniedException e)
        {
            Assert.fail();
        }
        catch (PathNotFoundException e)
        {
            Assert.fail();
        }
        catch (RepositoryException e)
        {
            Assert.fail();
        }
        catch (FileNotFoundException e)
        {
            Assert.fail();
        }
        catch (IOException e)
        {
            Assert.fail();
        }
    }

    public void testSettingAnUtf8NodeData() throws IOException, RepositoryException
    {
        Content content = getTestContent();
        String text = "citt\u00E0";
        Value value = createValue(text);
        NodeData nodeData = content.setNodeData("nd1", value);
        assertEquals(text, nodeData.getString());
    }

    public void testSettingNewUtf8ContentNode() throws IOException, RepositoryException
    {
        Content content = getTestContent();
        String text = "citt\u00E0";
        ContentUtil.getOrCreateContent(content, text, ItemType.CONTENT);
        Content newContent = content.getContent(text);
        String name = newContent.getName();
        assertEquals(text, name);
    }

    /**
     * @param session
     * @param exported
     * @throws FileNotFoundException
     * @throws IOException
     */
    private File exportNode(String repository, Session session, Content exported) throws FileNotFoundException,
        IOException
    {
        String handle = exported.getHandle();
        String xmlName = repository + StringUtils.replace(handle, "/", ".") + ".xml";
        xmlName = DataTransporter.encodePath(xmlName,".", DataTransporter.UTF8);
        File xmlFile = File.createTempFile(xmlName, null, Path.getTempDirectory());
        FileOutputStream fos = new FileOutputStream(xmlFile);
        try
        {
            DataTransporter.executeExport(fos, false, true, session, handle, repository, DataTransporter.XML);
        }
        finally
        {
            IOUtils.closeQuietly(fos);
        }
        return xmlFile;
    }

    private void bootstrapTextXml(HierarchyManager hm) throws Exception
    {
        File f1 = new File(getClass().getResource("/info/magnolia/utf8/website.utf8test.xml").getFile());
        bootstrapSingleResource("/info/magnolia/utf8/" + f1.getName());
        hm.save();
    }

    private Content getTestContent() throws IOException, RepositoryException
    {
        String contentProperties = "/myutf8content.@type=mgnl:content\n" + "/myutf8content.nd1=hello";

        HierarchyManager hm = MgnlContext.getHierarchyManager(ContentRepository.WEBSITE);
        new PropertiesImportExport().createContent(hm.getRoot(), IOUtils.toInputStream(contentProperties));
        hm.save();
        Content content = hm.getContent("/myutf8content");
        return content;
    }

    private Value createValue(Object valueObj) throws RepositoryException, UnsupportedRepositoryOperationException
    {
        ValueFactory valueFactory = MgnlContext
            .getHierarchyManager(ContentRepository.WEBSITE)
            .getWorkspace()
            .getSession()
            .getValueFactory();
        return NodeDataUtil.createValue(valueObj, valueFactory);
    }

}
