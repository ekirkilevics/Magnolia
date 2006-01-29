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
package info.magnolia.cms.security;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Date: Jul 6, 2004 Time: 11:26:05 AM
 * @author Sameer Charles
 * @version 2.0
 */
public final class Digester {

    /**
     * supported algorithms
     */
    public static final String SHA1 = "SHA-1"; //$NON-NLS-1$

    public static final String MD5 = "MD5"; //$NON-NLS-1$

    /**
     * There are five (5) FIPS-approved* algorithms for generating a condensed representation of a message (message
     * digest): SHA-1, SHA-224, SHA-256,SHA-384, and SHA-512. <strong>Not supported yet </strong>
     */
    public static final String SHA256 = "SHA-256"; //$NON-NLS-1$

    public static final String SHA384 = "SHA-384"; //$NON-NLS-1$

    public static final String SHA512 = "SHA-512"; //$NON-NLS-1$

    private static Logger log = LoggerFactory.getLogger(Digester.class);

    /**
     * Utility class, don't instantiate.
     */
    private Digester() {
        // unused
    }

    /**
     * Supported algorithm:
     * <ul>
     * <li>SHA-1 </li>
     * <li>MD5 </li>
     * </ul>
     * Future:
     * <ul>
     * <li>SHA-256 </li>
     * <li>SHA-384 </li>
     * <li>SHA-512 </li>
     * </ul>
     */
    public static String getDigest(String data, String algorithm) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance(algorithm);
        md.reset();
        return new String(md.digest(data.getBytes()));
    }

    /**
     * Supported algorithm:
     * <ul>
     * <li>SHA-1 </li>
     * <li>MD5 </li>
     * </ul>
     * Future:
     * <ul>
     * <li>SHA-256 </li>
     * <li>SHA-384 </li>
     * <li>SHA-512 </li>
     * </ul>
     */
    public static byte[] getDigest(byte[] data, String algorithm) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance(algorithm);
        md.reset();
        return md.digest(data);
    }

    /**
     * <p>
     * gets SHA-1 encoded -> hex string
     * </p>
     */
    public static String getSHA1Hex(String data) {
        try {
            String result = Digester.getDigest(data, Digester.SHA1);
            return Digester.toHEX(result);
        }
        catch (NoSuchAlgorithmException e) {
            log.error(e.getMessage(), e);
        }
        return data;
    }

    /**
     * <p>
     * gets MD5 encoded -> hex string
     * </p>
     */
    public static String getMD5Hex(String data) {
        try {
            String result = Digester.getDigest(data, Digester.MD5);
            return Digester.toHEX(result);
        }
        catch (NoSuchAlgorithmException e) {
            log.error(e.getMessage(), e);
        }
        return data;
    }

    /**
     * <p>
     * converts a byte array to a string Hex
     * @param data to be converted
     * @return string representing hex values of the byte array
     * </p>
     */
    public static String toHEX(String data) {
        return Digester.toHEX(data.getBytes());
    }

    /**
     * <p>
     * converts a byte array to a string Hex
     * @param data to be converted
     * @return string representing hex values of the byte array
     * </p>
     */
    public static String toHEX(byte[] data) {
        StringBuffer hexValue = new StringBuffer();
        char[] digits = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
        for (int i = 0; i < data.length; i++) {
            byte byteValue = data[i];
            hexValue.append(digits[(byteValue & 0xf0) >> 4]);
            hexValue.append(digits[byteValue & 0x0f]);
        }
        return hexValue.toString();
    }
}
