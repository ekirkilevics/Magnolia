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
