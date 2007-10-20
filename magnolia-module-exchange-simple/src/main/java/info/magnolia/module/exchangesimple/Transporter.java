// to be included: outStream.writeBytes("\r\n\r\n"); // test for commons-fileupload incompatibity
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

import info.magnolia.cms.exchange.ExchangeException;

import java.io.BufferedInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URLConnection;
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
     * http form multipart form post
     * @param connection
     * @param activationContent
     * @throws ExchangeException
     */
    public static void transport(URLConnection connection, ActivationContent activationContent)
        throws ExchangeException {
        InputStream is = null;
        DataOutputStream outStream = null;
        try {
            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setUseCaches(false);
            connection.setRequestProperty("Content-type", "multipart/form-data; boundary=" + BOUNDARY);
            connection.setRequestProperty("Cache-Control", "no-cache");

            outStream = new DataOutputStream(connection.getOutputStream());

            // set all resources from activationContent
            Iterator fileNameIterator = activationContent.getFiles().keySet().iterator();
            while (fileNameIterator.hasNext()) {
                outStream.writeBytes("\r\n--" + BOUNDARY + "\r\n");
                String fileName = (String) fileNameIterator.next();
                is = new BufferedInputStream(new FileInputStream(activationContent.getFile(fileName)));
                outStream.writeBytes("content-disposition: form-data; name=\""
                    + fileName
                    + "\"; filename=\""
                    + fileName
                    + "\"\r\n");
                outStream.writeBytes("content-type: application/octet-stream" + "\r\n\r\n");

                IOUtils.copy(is, outStream);
                IOUtils.closeQuietly(is);
            }

            outStream.writeBytes("\r\n--" + BOUNDARY + "--\r\n");
            outStream.flush();

            log.debug("Activation content sent as multipart/form-data");
        }
        catch (Exception e) {
            throw new ExchangeException("Simple exchange transport failed: "
                + ClassUtils.getShortClassName(e.getClass()), e);
        }
        finally {
            IOUtils.closeQuietly(is);
            IOUtils.closeQuietly(outStream);
        }

    }

}
