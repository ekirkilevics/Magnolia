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
package info.magnolia.module.exchangesimple;

import info.magnolia.cms.exchange.ExchangeException;

import java.io.BufferedInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.util.Iterator;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.ClassUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Class responsible to transport activation content
 * @author Sameer Charles $Id$
 */
public class Transporter {

    /**
     * Logger
     */
    private static Logger log = LoggerFactory.getLogger(Transporter.class);

    /**
     * content boundary
     */
    private static final String BOUNDARY = "mgnlExchange-cfc93688d385";

    /**
     * Aggregates all transfer data including boundaries into single temporary file.
     * @param ac Activatied content.
     * @return File with all transport data.
     * @throws IOException when some activation content can't be read or writing into temp file fails.
     */
    private static File prepareTempFile (ActivationContent ac) 
        throws IOException {

        File f = File.createTempFile(""+System.currentTimeMillis(), ".mgnl_activation");

        log.debug("prepareTempFile() " + f.getPath());

        f.deleteOnExit(); // just to be sure

        DataOutputStream dos = new DataOutputStream(new FileOutputStream(f));

        dos.writeBytes("--" + BOUNDARY + "\r\n");

        Iterator it = ac.getFiles().keySet().iterator();
        while (it.hasNext()) {
            String fileName = (String) it.next();

            dos.writeBytes("content-disposition: form-data; name=\"" + fileName + "\"; filename=\"" + fileName + "\"\r\n");
            dos.writeBytes("content-type: application/octet-stream\r\n\r\n");

            BufferedInputStream bis = new BufferedInputStream(new FileInputStream(ac.getFile(fileName)));

            IOUtils.copy(bis, dos);
            IOUtils.closeQuietly(bis);

            dos.writeBytes("\r\n" + "--" + BOUNDARY + "\r\n");
        }
        return f;
    }

    /**
     * http form multipart form post
     * @param connection
     * @param activationContent
     * @throws ExchangeException
     */
    public static void transport(HttpURLConnection connection, ActivationContent activationContent) throws ExchangeException {
        File tempFile = null;
        FileInputStream is = null;
        OutputStream os = null;

        try {
            tempFile = prepareTempFile(activationContent);
            
            connection.setFixedLengthStreamingMode((int) tempFile.length());
            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setUseCaches(false);
            connection.setRequestProperty("Content-type", "multipart/form-data; boundary=" + BOUNDARY);
            connection.setRequestProperty("Cache-Control", "no-cache");

            is = new FileInputStream(tempFile);
            os = connection.getOutputStream();

            IOUtils.copy(is, os);
        }
        catch (Exception e) {
            String msg = "Simple exchange transport failed: " + e.getMessage();
            log.error(msg, e);
            throw new ExchangeException(msg, e);
        }
        finally {

            IOUtils.closeQuietly(is);
            IOUtils.closeQuietly(os);

            if (tempFile != null) {
                tempFile.delete();
            }
        }
    }
}
