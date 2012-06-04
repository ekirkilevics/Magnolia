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
package info.magnolia.cms.security;

import java.security.NoSuchAlgorithmException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Encryption helper.
 *
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
 *
 * @author Sameer Charles
 * @version 2.0
 * @deprecated since 4.5.3 - use SecurityUtil instead.
 */
@Deprecated
public final class Digester {

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

    public static String getDigest(String data, String algorithm) throws NoSuchAlgorithmException {
        return SecurityUtil.getDigest(data, algorithm);
    }

    public static byte[] getDigest(byte[] data, String algorithm) throws NoSuchAlgorithmException {
        return SecurityUtil.getDigest(data, algorithm);
    }

    /**
     * Gets SHA-1 encoded -> hex string.
     */
    public static String getSHA1Hex(String data) {
        return SecurityUtil.getSHA1Hex(data);
    }

    /**
     * Gets MD5 encoded -> hex string.
     */
    public static String getMD5Hex(String data) {
        return SecurityUtil.getMD5Hex(data);
    }

    /**
     * Converts a byte array to a string Hex.
     * @param data to be converted
     * @return string representing hex values of the byte array
     */
    public static String toHEX(String data) {
        return toHEX(data.getBytes());
    }

    /**
     * Converts a byte array to a string Hex.
     * @param data to be converted
     * @return string representing hex values of the byte array
     */
    public static String toHEX(byte[] data) {
        return SecurityUtil.byteArrayToHex(data);
    }

    public static String getBCrypt(String text) {
        return SecurityUtil.getBCrypt(text);
    }

    public static boolean matchBCrypted(String candidate, String hash) {
        return SecurityUtil.matchBCrypted(candidate, hash);
    }
}
