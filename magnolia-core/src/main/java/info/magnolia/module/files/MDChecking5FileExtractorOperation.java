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
package info.magnolia.module.files;

import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.core.NodeData;
import info.magnolia.cms.util.ContentUtil;
import info.magnolia.cms.util.NodeDataUtil;
import org.apache.commons.codec.binary.Hex;

import javax.jcr.RepositoryException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.DigestInputStream;
import java.security.DigestOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * A FileExtractorOperation which checks extracted files against an MD5 checksum
 * stored in the repository. If the checksum does not exist or is identical to the
 * file being extracted, the file is extracted and potentially overwrites the
 * existing one. If not, which means the file was modified by the user,
 * the operation is cancelled.
 *
 * TODO : what to do with removed files ? should probably be the responsibility of a different Task.
 *
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
class MDChecking5FileExtractorOperation extends BasicFileExtractorOperation {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(MDChecking5FileExtractorOperation.class);

    private final HierarchyManager hm;

    MDChecking5FileExtractorOperation(HierarchyManager hm, String resourcePath, String absoluteTargetPath) {
        super(resourcePath, absoluteTargetPath);
        this.hm = hm;
    }

    protected InputStream checkInput() throws IOException {
        return wrap(super.checkInput());
    }

    // TODO : behaviour: overwrite, log, backup, .. ?
    protected File checkOutput() throws IOException {
        final File out = super.checkOutput();

        if (out.exists()) {
            // check md5 of the existing file
            final InputStream stream = new FileInputStream(out);
            final String existingFileMD5 = calculateMD5(stream);

            // check repository md5
            final NodeData repoMD5prop = getRepositoryNode().getNodeData("md5");
            if (repoMD5prop.isExist()) {
                final String repoMD5 = repoMD5prop.getString();
                if (existingFileMD5.equals(repoMD5)) {
                    // resourcePath was found, with correct md5 in repo : repoMD5
                    return out;
                } else {
                    log.warn("Can't extract " + resourcePath + " as this file was probably modified locally: expected MD5 [" + repoMD5 + "] but current MD5 is [" + existingFileMD5 + "].");
                    return null;
                }
            }
            // resourcePath was found, but no md5 found in repo ...
        } else {
            // resourcePath does not exist yet, extracting ...
        }
        return out;
    }

    protected OutputStream openOutput(File outFile) throws IOException {
        final OutputStream outputStream = super.openOutput(outFile);
        final MessageDigest md5 = getMessageDigest();
        return new DigestOutputStream(outputStream, md5);
    }

    protected void copyAndClose(InputStream in, OutputStream out) throws IOException {
        super.copyAndClose(in, out);

        final DigestInputStream md5Stream = (DigestInputStream) in;
        final String newMD5 = retrieveMD5(md5Stream);

        try {
            NodeDataUtil.getOrCreate(getRepositoryNode(), "md5").setValue(newMD5);
        } catch (RepositoryException e) {
            throw new RuntimeException(e); // TODO
        }
        //TODO save .. ?
    }

    protected Content getRepositoryNode() {
        try {
            final String repoPath = getRepositoryPath(resourcePath);
            final Content node;
            if (!hm.isExist(repoPath)) {
                node = ContentUtil.createPath(hm, repoPath);
            } else {
                node = hm.getContent(repoPath);
            }
            return node;
        } catch (RepositoryException e) {
            throw new RuntimeException(e); // TODO
        }
    }

    /**
     * Returns the path to the node that has the property with this resource's md5
     * TODO : implement properly + test
     */
    protected String getRepositoryPath(String resourcePath) {
        return "/server/install" + resourcePath;
    }

    /**
     * Completely reads an InputStream and returns its MD5
     * TODO : should we close it?
     */
    protected String calculateMD5(InputStream stream) throws IOException {
        final DigestInputStream md5Stream = wrap(stream);
        byte[] buffer = new byte[1024];
        while (md5Stream.read(buffer) != -1) {
        }

        return retrieveMD5(md5Stream);
    }

    protected DigestInputStream wrap(InputStream stream) {
        final MessageDigest md5 = getMessageDigest();
        return new DigestInputStream(stream, md5);
    }

    protected String retrieveMD5(DigestInputStream md5Stream) {
        final byte[] digInBytes = md5Stream.getMessageDigest().digest();
        return String.valueOf(Hex.encodeHex(digInBytes));
    }

    protected MessageDigest getMessageDigest() {
        try {
            return MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Can't check files with md5: " + e.getMessage(), e);
        }
    }
}
