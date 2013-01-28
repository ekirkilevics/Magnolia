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

import info.magnolia.cms.core.Path;
import info.magnolia.cms.core.SystemProperty;
import info.magnolia.cms.exchange.ActivationManager;
import info.magnolia.context.MgnlContext;
import info.magnolia.objectfactory.Components;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Security;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.apache.commons.lang.StringUtils;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.mindrot.jbcrypt.BCrypt;

/**
 * Utility functions required in the context of Security.
 *
 * @version $Id$
 */
public class SecurityUtil {

    private static final String PRIVATE_KEY = "key.private";
    private static final String PUBLIC_KEY = "key.public";
    private static final String KEY_LOCATION_PROPERTY = "magnolia.author.key.location";


    public static final String SHA1 = "SHA-1"; //$NON-NLS-1$
    public static final String MD5 = "MD5"; //$NON-NLS-1$

    /**
     * There are five (5) FIPS-approved* algorithms for generating a condensed representation of a message (message
     * digest): SHA-1, SHA-224, SHA-256,SHA-384, and SHA-512. <strong>Not supported yet</strong>
     */
    public static final String SHA256 = "SHA-256"; //$NON-NLS-1$
    public static final String SHA384 = "SHA-384"; //$NON-NLS-1$
    public static final String SHA512 = "SHA-512"; //$NON-NLS-1$

    /**
     * Encryption algorithm used ... if you are ever changing this, keep in mind underlying impl relies on padding!
     */

    private static final String ALGORITHM = "RSA";

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    /**
     * Checks if the currently acting user is anonymous.
     */
    public static boolean isAnonymous() {
        User user = MgnlContext.getUser();
        return (user != null && UserManager.ANONYMOUS_USER.equals(user.getName()));
    }

    public static boolean isAuthenticated() {
        User user = MgnlContext.getUser();
        return (user != null && !UserManager.ANONYMOUS_USER.equals(user.getName()));
    }

    public static String decrypt(String pass) throws SecurityException {
        return decrypt(pass, getPublicKey());
    }

    public static String decrypt(String message, String encodedKey) throws SecurityException {
        try {
            if (StringUtils.isBlank(encodedKey)) {
                throw new SecurityException("Activation key was not found. Please make sure your instance is correctly configured.");
            }

            // decode key
            byte[] binaryKey = hexToByteArray(encodedKey);

            // create RSA public key cipher
            Cipher pkCipher = Cipher.getInstance(ALGORITHM, "BC");
            try {
                // create private key
                X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(binaryKey);
                KeyFactory kf = KeyFactory.getInstance(ALGORITHM, "BC");
                PublicKey pk = kf.generatePublic(publicKeySpec);
                pkCipher.init(Cipher.DECRYPT_MODE, pk);

            } catch (InvalidKeySpecException e) {
                // decrypting with private key?
                PKCS8EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(binaryKey);
                KeyFactory kf = KeyFactory.getInstance(ALGORITHM, "BC");
                PrivateKey pk = kf.generatePrivate(privateKeySpec);
                pkCipher.init(Cipher.DECRYPT_MODE, pk);
            }

            // decrypt
            String[] chunks = StringUtils.split(message, ";");
            if (chunks == null) {
                throw new SecurityException("The encrypted information is corrupted or incomplete. Please make sure someone is not trying to intercept or modify encrypted message.");
            }
            StringBuilder clearText = new StringBuilder();
            for (String chunk : chunks) {
                byte[] byteChunk = hexToByteArray(chunk);
                clearText.append(new String(pkCipher.doFinal(byteChunk), "UTF-8"));
            }
            return clearText.toString();
        } catch (NumberFormatException e) {
            throw new SecurityException("The encrypted information is corrupted or incomplete. Please make sure someone is not trying to intercept or modify encrypted message.", e);
        } catch (IOException e) {
            throw new SecurityException("Failed to read authentication string. Please use Java version with cryptography support.", e);
        } catch (NoSuchAlgorithmException e) {
            throw new SecurityException("Failed to read authentication string. Please use Java version with cryptography support.", e);
        } catch (NoSuchPaddingException e) {
            throw new SecurityException("Failed to read authentication string. Please use Java version with cryptography support.", e);
        } catch (InvalidKeySpecException e) {
            throw new SecurityException("Failed to read authentication string. Please use Java version with cryptography support.", e);
        } catch (InvalidKeyException e) {
            throw new SecurityException("Failed to read authentication string. Please use Java version with cryptography support.", e);
        } catch (NoSuchProviderException e) {
            throw new SecurityException("Failed to find encryption provider. Please use Java version with cryptography support.", e);
        } catch (IllegalBlockSizeException e) {
            throw new SecurityException("Failed to decrypt message. It might have been corrupted during transport.", e);
        } catch (BadPaddingException e) {
            throw new SecurityException("Failed to decrypt message. It might have been corrupted during transport.", e);
        }

    }

    public static String encrypt(String pass) throws SecurityException {
        String encodedKey = getPrivateKey();
        return encrypt(pass, encodedKey);
    }

    public static String encrypt(String message, String encodedKey) {
        try {

            // read private key
            if (StringUtils.isBlank(encodedKey)) {
                throw new SecurityException("Activation key was not found. Please make sure your instance is correctly configured.");
            }
            byte[] binaryKey = hexToByteArray(encodedKey);

            // create RSA public key cipher
            Cipher pkCipher = Cipher.getInstance(ALGORITHM, "BC");
            try {
                // create private key
                PKCS8EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(binaryKey);
                KeyFactory kf = KeyFactory.getInstance(ALGORITHM, "BC");
                PrivateKey pk = kf.generatePrivate(privateKeySpec);

                pkCipher.init(Cipher.ENCRYPT_MODE, pk);
            } catch (InvalidKeySpecException e) {
                // encrypting with public key?
                X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(binaryKey);
                KeyFactory kf = KeyFactory.getInstance(ALGORITHM, "BC");
                PublicKey pk = kf.generatePublic(publicKeySpec);

                pkCipher.init(Cipher.ENCRYPT_MODE, pk);
            }

            // encrypt
            byte[] bytes = message.getBytes("UTF-8");
            // split bit message in chunks
            int start = 0;
            StringBuilder chaos = new StringBuilder();
            while (start < bytes.length) {
                byte[] tmp = new byte[Math.min(bytes.length - start, binaryKey.length / 8)];
                System.arraycopy(bytes, start, tmp, 0, tmp.length);
                start += tmp.length;
                byte[] encrypted = pkCipher.doFinal(tmp);
                chaos.append(byteArrayToHex(encrypted));
                chaos.append(";");
            }
            chaos.setLength(chaos.length() - 1);

            return chaos.toString();

        } catch (IOException e) {
            throw new SecurityException("Failed to create authentication string. Please use Java version with cryptography support.", e);
        } catch (NoSuchAlgorithmException e) {
            throw new SecurityException("Failed to create authentication string. Please use Java version with cryptography support.", e);
        } catch (NoSuchPaddingException e) {
            throw new SecurityException("Failed to create authentication string. Please use Java version with cryptography support.", e);
        } catch (InvalidKeySpecException e) {
            throw new SecurityException("Failed to create authentication string. Please use Java version with cryptography support.", e);
        } catch (InvalidKeyException e) {
            throw new SecurityException("Failed to create authentication string. Please use Java version with cryptography support.", e);
        } catch (NoSuchProviderException e) {
            throw new SecurityException("Failed to find encryption provider. Please use Java version with cryptography support.", e);
        } catch (IllegalBlockSizeException e) {
            throw new SecurityException("Failed to encrypt string. Please use Java version with cryptography support.", e);
        } catch (BadPaddingException e) {
            throw new SecurityException("Failed to encrypt string. Please use Java version with cryptography support.", e);
        }
    }


    public static String getPrivateKey() {
        String path = SystemProperty.getProperty(KEY_LOCATION_PROPERTY);
        checkPrivateKeyStoreExistence(path);
        try {
            Properties defaultProps = new Properties();
            FileInputStream in = new FileInputStream(path);
            defaultProps.load(in);
            in.close();
            return defaultProps.getProperty(PRIVATE_KEY);
        } catch (FileNotFoundException e) {
            throw new SecurityException("Failed to retrieve private key. Please make sure the key is located in " + path, e);
        } catch (IOException e) {
            throw new SecurityException("Failed to retrieve private key. Please make sure the key is located in " + path, e);
        }
    }

    public static void updateKeys(MgnlKeyPair keys) {
        // update filestore only when private key is present
        if (keys.getPrivateKey() != null) {
            String path = SystemProperty.getProperty(KEY_LOCATION_PROPERTY);
            try {
                Properties defaultProps = new Properties();
                defaultProps.put(PRIVATE_KEY, keys.getPrivateKey());
                defaultProps.put(PUBLIC_KEY, keys.getPublicKey());
                File keystore = new File(path);
                File parentFile = keystore.getParentFile();
                if (parentFile != null) {
                    parentFile.mkdirs();
                }
                FileWriter writer = new FileWriter(keystore);
                String date = new SimpleDateFormat("dd.MMM.yyyy hh:mm").format(new Date());
                defaultProps.store(writer, "generated " + date + " by " + MgnlContext.getUser().getName());
                writer.close();
            } catch (FileNotFoundException e) {
                throw new SecurityException("Failed to store private key. Please make sure the key is located in " + path, e);
            } catch (IOException e) {
                throw new SecurityException("Failed to store private key. Please make sure the key is located in " + path, e);
            }
        }
        try {
            Session session = MgnlContext.getSystemContext().getJCRSession("config");
            session.getNode("/server/activation").setProperty("publicKey", keys.getPublicKey());
            session.save();
        } catch (RepositoryException e) {
            throw new SecurityException("Failed to store public key.", e);
        }
    }

    public static String getPublicKey() {
        ActivationManager aman = Components.getComponentProvider().getComponent(ActivationManager.class);
        return aman.getPublicKey();
    }

    private static final String HEX = "0123456789ABCDEF";

    public static String byteArrayToHex(byte[] raw) {
        if (raw == null) {
            return null;
        }
        final StringBuilder hex = new StringBuilder(2 * raw.length);
        for (final byte b : raw) {
            hex.append(HEX.charAt((b & 0xF0) >> 4))
            .append(HEX.charAt((b & 0x0F)));
        }
        return hex.toString();
    }

    public static byte[] hexToByteArray(String s) {
        byte[] b = new byte[s.length() / 2];
        for (int i = 0; i < b.length; i++) {
            int index = i * 2;
            int v = Integer.parseInt(s.substring(index, index + 2), 16);
            b[i] = (byte) v;
        }
        return b;
    }

    public static MgnlKeyPair generateKeyPair(int keyLength) throws NoSuchAlgorithmException {
        KeyPairGenerator kgen = KeyPairGenerator.getInstance(ALGORITHM);
        kgen.initialize(keyLength);
        KeyPair key = kgen.genKeyPair();
        return new MgnlKeyPair(byteArrayToHex(key.getPrivate().getEncoded()), byteArrayToHex(key.getPublic().getEncoded()));
    }

    /**
     * Used for removing password parameter from cache key.
     * @param cacheKey.toString()
     * @return
     */
    public static String stripPasswordFromCacheLog(String log){
        String value = stripParameterFromCacheLog(log, "mgnlUserPSWD");
        value = stripParameterFromCacheLog(value, "passwordConfirmation");
        value = stripParameterFromCacheLog(value, "password");
        return value;
    }

    public static String stripPasswordFromUrl(String url){
        if(StringUtils.isBlank(url)){
            return null;
        }
        String value = null;
        value = StringUtils.substringBefore(url, "mgnlUserPSWD");
        value = value + StringUtils.substringAfter(StringUtils.substringAfter(url, "mgnlUserPSWD"), "&");
        return StringUtils.removeEnd(value, "&");
    }

    public static String stripParameterFromCacheLog(String log, String parameter){
        if(StringUtils.isBlank(log)){
            return null;
        }else if(!StringUtils.contains(log, parameter)){
            return log;
        }
        String value = null;
        value = StringUtils.substringBefore(log, parameter);
        String afterString = StringUtils.substringAfter(log, parameter);
        if(StringUtils.indexOf(afterString, " ") < StringUtils.indexOf(afterString, "}")){
            value = value + StringUtils.substringAfter(afterString, " ");
        }else{
            value = value + "}" + StringUtils.substringAfter(afterString, "}");
        }
        return value;
    }


    private static void checkPrivateKeyStoreExistence(final String path) throws SecurityException {
        if(StringUtils.isBlank(path)) {
            throw new SecurityException("Private key store path is either null or empty. Please, check [" + KEY_LOCATION_PROPERTY + "] value in magnolia.properties");
        }
        String absPath = Path.getAbsoluteFileSystemPath(path);
        File keypair = new File(absPath);
        if(!keypair.exists()) {
            throw new SecurityException("Private key store doesn't exist at [" + keypair.getAbsolutePath() + "]. Please, ensure that [" + KEY_LOCATION_PROPERTY + "] actually points to the correct location");
        }
    }

    public static String getBCrypt(String text) {
        // gensalt's log_rounds parameter determines the complexity
        // the work factor is 2^log_rounds, and the default is 10
        String hashed = BCrypt.hashpw(text, BCrypt.gensalt(12));
        return hashed;
    }

    public static boolean matchBCrypted(String candidate, String hash) {
        // Check that an unencrypted password matches one that has
        // previously been hashed
        return BCrypt.checkpw(candidate, hash);
    }

    /**
     * Message Digesting function.
     *
     */
    public static String getDigest(String data, String algorithm) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance(algorithm);
        md.reset();
        return new String(md.digest(data.getBytes()));
    }

    /**
     * Message Digesting function.
     *
     */
    public static byte[] getDigest(byte[] data, String algorithm) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance(algorithm);
        md.reset();
        return md.digest(data);
    }

    /**
     * Gets SHA-1 encoded -> hex string.
     */
    public static String getSHA1Hex(byte[] data) {
        try {
            return byteArrayToHex(getDigest(data, SecurityUtil.SHA1));
        }
        catch (NoSuchAlgorithmException e) {
            throw new SecurityException("Couldn't digest with " + SecurityUtil.SHA1 + " algorithm!");
        }
    }

    public static String getSHA1Hex(String data) {
        return getSHA1Hex(data.getBytes());
    }

    /**
     * Gets MD5 encoded -> hex string.
     */
    public static String getMD5Hex(byte[] data) {
        try {
            return byteArrayToHex(getDigest(data, SecurityUtil.MD5));
        }
        catch (NoSuchAlgorithmException e) {
            throw new SecurityException("Couldn't digest with " + SecurityUtil.MD5 + " algorithm!");
        }
    }

    public static String getMD5Hex(String data) {
        return getMD5Hex(data.getBytes());
    }
}
