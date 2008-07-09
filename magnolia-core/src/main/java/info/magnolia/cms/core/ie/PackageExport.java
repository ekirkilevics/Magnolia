/**
 * This file Copyright (c) 2003-2008 Magnolia International
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
package info.magnolia.cms.core.ie;

import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.ItemType;
import info.magnolia.cms.core.NodeData;
import info.magnolia.cms.core.Path;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;

import org.apache.commons.io.IOUtils;
import org.safehaus.uuid.UUIDGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Date: May 24, 2005 Time: 10:34:36 AM
 * @author Sameer Charles $Id :$
 *
 * @deprecated deprecated since 3.6 but wasn't used before - MAGNOLIA-405
 */
public class PackageExport implements ExportHandler {

    /**
     * Logger.
     */
    protected static Logger log = LoggerFactory.getLogger(PackageExport.class);

    private static final String START_DIRECTORY = "data"; //$NON-NLS-1$

    public static final String DATA_FILE_NAME = "data.xml"; //$NON-NLS-1$

    /**
     * fields
     */
    private boolean binaryAsLink = true;

    private Map params = new Hashtable();

    /**
     * this class specific parameters
     */

    public void setBinaryAsLink(boolean binaryAsLink) {
        this.binaryAsLink = binaryAsLink;
    }

    public boolean getBinaryAsLink() {
        return this.binaryAsLink;
    }

    public Object exportContent(Content content) throws RepositoryException {
        String message = "export to object not supported by PackageExport"; //$NON-NLS-1$
        log.error(message);
        throw new UnsupportedOperationException(message);
    }

    public void exportContent(Content content, OutputStream outStream) throws RepositoryException, IOException {
        ContentZipper zipper = new ContentZipper(content);
        zipper.run();
        // write temporary file to the stream and delete after
        FileInputStream is = new FileInputStream(zipper.getZipFile());
        byte[] buffer = new byte[8192];
        int read = 0;
        while ((read = is.read(buffer)) > 0) {
            outStream.write(buffer, 0, read);
        }
        outStream.flush();
        IOUtils.closeQuietly(outStream);
        IOUtils.closeQuietly(is);
        zipper.getZipFile().delete();
    }

    public void setParameter(String key, Object value) {
        this.params.put(key, value);
    }

    public Object getParameter(String key) {
        return this.params.get(key);
    }

    /**
     * This class will be instantiated on each call to PackageExport.export method
     */
    class ContentZipper {

        private String zipFileName;

        private File zipFile;

        private ZipOutputStream outputStream;

        private Content content;

        ContentZipper(Content content) {
            this.content = content;
        }

        public void run() throws IOException, RepositoryException {
            try {
                this.createTargetFile();
                this.outputStream = new ZipOutputStream(new FileOutputStream(this.zipFile));
                // set compression method and level
                this.outputStream.setMethod(ZipOutputStream.DEFLATED);
                this.outputStream.setLevel(9);
                this.addTextContent();
                this.addBinaryContent(this.content);
                IOUtils.closeQuietly(this.outputStream);
            }
            catch (IOException e) {
                log.error(e.getMessage());
                log.error("failed to pack content, deleteting temp file " + this.getZipFileName()); //$NON-NLS-1$
                if (this.zipFile.exists()) {
                    this.zipFile.delete();
                }
                throw e;
            }
        }

        public String getZipFileName() {
            return this.zipFileName;
        }

        public void setZipFileName(String zipFileName) {
            this.zipFileName = zipFileName;
        }

        public File getZipFile() {
            return this.zipFile;
        }

        public void setZipFile(File zipFile) {
            this.zipFile = zipFile;
        }

        public ZipOutputStream getOutputStream() {
            return this.outputStream;
        }

        public void setOutputStream(ZipOutputStream outputStream) {
            this.outputStream = outputStream;
        }

        private void createTargetFile() throws IOException {
            this.zipFileName = UUIDGenerator.getInstance().generateTimeBasedUUID().toString() + ".zip"; //$NON-NLS-1$
            if (log.isDebugEnabled()) {
                log.debug("Generating zip file " + this.zipFileName); //$NON-NLS-1$
            }
            this.zipFile = new File(Path.getTempDirectoryPath() + "/" + this.zipFileName); //$NON-NLS-1$
            this.zipFile.createNewFile();
        }

        private void addTextContent() throws IOException, RepositoryException {
            XmlExport xmlExport = new XmlExport();
            xmlExport.setBinaryAsLink(true);
            if (log.isDebugEnabled()) {
                log.debug("adding a new zip file entry " + START_DIRECTORY + "/" + DATA_FILE_NAME); //$NON-NLS-1$ //$NON-NLS-2$
            }
            this.outputStream.putNextEntry(new ZipEntry(START_DIRECTORY + "/" + DATA_FILE_NAME)); //$NON-NLS-1$
            xmlExport.exportContent(this.content, this.outputStream);
            this.outputStream.closeEntry();
        }

        private void addBinaryContent(Content content) throws IOException, RepositoryException {
            Iterator dataNodes = content.getNodeDataCollection().iterator();
            while (dataNodes.hasNext()) {
                NodeData nodeData = (NodeData) dataNodes.next();
                if (nodeData.getType() == PropertyType.BINARY) {
                    if (log.isDebugEnabled()) {
                        log.debug("adding a new zip file entry " + START_DIRECTORY + nodeData.getHandle()); //$NON-NLS-1$
                    }
                    this.outputStream.putNextEntry(new ZipEntry(START_DIRECTORY + nodeData.getHandle()));
                    InputStream is = nodeData.getStream();
                    byte[] buffer = new byte[8192];
                    int read = 0;
                    while ((read = is.read(buffer)) > 0) {
                        this.outputStream.write(buffer, 0, read);
                    }
                    this.outputStream.closeEntry();
                }
            }
            Iterator subNodes = content.getChildren(ItemType.NT_BASE).iterator();
            while (subNodes.hasNext()) {
                this.addBinaryContent((Content) subNodes.next());
            }
        }
    }

}
