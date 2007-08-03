/**
 *
 * Magnolia and its source-code is licensed under the LGPL.
 * You may copy, adapt, and redistribute this file for commercial or non-commercial use.
 * When copying, adapting, or redistributing this document in keeping with the guidelines above,
 * you are required to provide proper attribution to obinary.
 * If you reproduce or distribute the document without making any substantive modifications to its content,
 * please use the following attribution line:
 *
 * Copyright 1993-2006 obinary Ltd. (http://www.obinary.com) All rights reserved.
 *
 */
package info.magnolia.module.exchangesimple;

import info.magnolia.module.exchangesimple.ActivationContent;
import info.magnolia.cms.exchange.ExchangeException;

import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URLConnection;
import java.util.Iterator;

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
     * max buffer size
     */
    private static final int BUFFER_SIZE = 1024;

    /**
     * http form multipart form post
     * @param connection
     * @param activationContent
     * @throws ExchangeException
     */
    public static void transport(URLConnection connection, ActivationContent activationContent)
        throws ExchangeException {
        FileInputStream fis = null;
        DataOutputStream outStream = null;
        try {
            byte[] buffer = new byte[BUFFER_SIZE];
            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setUseCaches(false);
            connection.setRequestProperty("Content-type", "multipart/form-data; boundary=" + BOUNDARY);
            connection.setRequestProperty("Cache-Control", "no-cache");

            outStream = new DataOutputStream(connection.getOutputStream());
            outStream.writeBytes("--" + BOUNDARY + "\r\n");

            // set all resources from activationContent
            Iterator fileNameIterator = activationContent.getFiles().keySet().iterator();
            while (fileNameIterator.hasNext()) {
                String fileName = (String) fileNameIterator.next();
                fis = new FileInputStream(activationContent.getFile(fileName));
                outStream.writeBytes("content-disposition: form-data; name=\""
                    + fileName
                    + "\"; filename=\""
                    + fileName
                    + "\"\r\n");
                outStream.writeBytes("content-type: application/octet-stream" + "\r\n\r\n");
                while (true) {
                    synchronized (buffer) {
                        int amountRead = fis.read(buffer);
                        if (amountRead == -1) {
                            break;
                        }
                        outStream.write(buffer, 0, amountRead);
                    }
                }
                fis.close();
                outStream.writeBytes("\r\n" + "--" + BOUNDARY + "\r\n");
            }
            outStream.flush();
            outStream.close();

            log.debug("Activation content sent as multipart/form-data");
        }
        catch (Exception e) {
            throw new ExchangeException("Simple exchange transport failed: "
                + ClassUtils.getShortClassName(e.getClass()), e);
        }
        finally {
            if (fis != null) {
                try {
                    fis.close();
                }
                catch (IOException e) {
                    log.error("Exception caught", e);
                }
            }
            if (outStream != null) {
                try {
                    outStream.close();
                }
                catch (IOException e) {
                    log.error("Exception caught", e);
                }
            }
        }

    }

}
