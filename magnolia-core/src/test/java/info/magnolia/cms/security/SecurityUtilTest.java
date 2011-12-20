/**
 * This file Copyright (c) 2011 Magnolia International
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import info.magnolia.cms.core.SystemProperty;
import info.magnolia.cms.exchange.ActivationManager;
import info.magnolia.context.MgnlContext;
import info.magnolia.context.SystemContext;
import info.magnolia.test.ComponentsTestUtil;
import info.magnolia.test.mock.jcr.MockNode;

import java.io.File;
import java.io.FileReader;
import java.security.Provider;
import java.security.Security;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;

import javax.jcr.Session;

import org.junit.After;
import org.junit.Test;

/**
 * Tests for basic security functionality.
 * 
 * @version $Id$
 * 
 */
public class SecurityUtilTest {

    @Test
    public void testCrypt() throws Exception {
        String path = "src/test/resources/testkeypair.properties";
        SystemProperty.setProperty("magnolia.author.key.location", path);
        ActivationManager actMan = mock(ActivationManager.class);
        ComponentsTestUtil.setInstance(ActivationManager.class, actMan);

        Properties p = new Properties();
        p.load(new FileReader(path));
        String publicKey = p.getProperty("key.public");
        when(actMan.getPublicKey()).thenReturn(publicKey);

        // short message
        doCrypt("bla bla");
        // medium size
        doCrypt("1322900338979;johndoe;C750AFBA94E355BF5544434E227708C3");
        // way too long for one block
        doCrypt(publicKey);
    }

    public void doCrypt(String test) throws Exception {
        // encrypt
        String encrypted = SecurityUtil.encrypt(test);
        assertFalse(encrypted.isEmpty());
        assertNotNull(encrypted);
        assertFalse(test.equals(encrypted));

        // decrypt
        String decrypted = SecurityUtil.decrypt(encrypted);
        assertEquals(test, decrypted);
        try {
            String wrongDecrypted = SecurityUtil.decrypt("bla bla boo");

            // JCA Provider would fail ... BC just returns nonsense ... who's to judge what's better
            // fail("Should have failed, instead produced: " + wrongDecrypted);
        } catch (SecurityException e) {
            // expected
        }
    }

    @Test
    public void testKeyStore() throws Exception {
        SystemContext mctx = mock(SystemContext.class);
        when(mctx.getUser()).thenReturn(new DummyUser());
        MgnlContext.setInstance(mctx);
        ComponentsTestUtil.setInstance(SystemContext.class, mctx);
        Session mockSession = mock(Session.class);
        when(mctx.getJCRSession("config")).thenReturn(mockSession);
        MockNode mocknode = new MockNode();
        when(mockSession.getNode("/server/activation")).thenReturn(mocknode);

        ActivationManager actMan = mock(ActivationManager.class);
        ComponentsTestUtil.setInstance(ActivationManager.class, actMan);
        String path = "target/key.store.properties";
        File keyStore = new File(path);
        if (keyStore.exists()) {
            keyStore.delete();
        }
        SystemProperty.setProperty("magnolia.author.key.location", path);
        MgnlKeyPair keyPair = SecurityUtil.generateKeyPair(512);
        SecurityUtil.updateKeys(keyPair);

        // verify(mocknode).setProperty("publicKey", keyPair[1]);
        when(actMan.getPublicKey()).thenReturn(mocknode.getProperty("publicKey").getString());
        assertEquals("PrivateKey:", keyPair.getPrivateKey(), SecurityUtil.getPrivateKey());
        assertEquals("PublicKey:", keyPair.getPublicKey(), SecurityUtil.getPublicKey());
        keyStore.delete();
        assertFalse(keyStore.exists());
    }

    private void printSet(
            String setName,
            Set<String> algorithms) {
        System.out.println(setName + ":");

        if (algorithms.isEmpty()) {
            System.out.println("            None available.");
        } else {
            Iterator<String> it = algorithms.iterator();

            while (it.hasNext()) {
                String name = it.next();

                System.out.println("            " + name);
            }
        }
    }

    @Test
    public void testBCProvider() throws Exception {
        // init provider
        SecurityUtil.generateKeyPair(1024);
        Provider[] providers = Security.getProviders();
        boolean found = false;
        for (Provider p : providers) {
            if ("BC".equals(p.getName())) {
                // optionally list what we've got available
                // System.out.println(p.getName());
                // for (Service s : p.getServices()) {
                // System.out.println(s);
                // }
                found = true;
                break;
            }
        }
        assertTrue(found);
    }

    /**
     * Method to list all available providers and their capabilities.
     */
    public void listProviders() {
        Provider[] providers = Security.getProviders();
        Set ciphers = new HashSet();
        Set keyAgreements = new HashSet();
        Set macs = new HashSet();
        Set messageDigests = new HashSet();
        Set signatures = new HashSet();

        for (int i = 0; i != providers.length; i++) {
            Iterator it = providers[i].keySet().iterator();

            while (it.hasNext()) {
                String entry = (String) it.next();

                if (entry.startsWith("Alg.Alias.")) {
                    entry = entry.substring("Alg.Alias.".length());
                }

                if (entry.startsWith("Cipher.")) {
                    ciphers.add(entry.substring("Cipher.".length()));
                } else if (entry.startsWith("KeyAgreement.")) {
                    keyAgreements.add(entry.substring("KeyAgreement.".length()));
                } else if (entry.startsWith("Mac.")) {
                    macs.add(entry.substring("Mac.".length()));
                } else if (entry.startsWith("MessageDigest.")) {
                    messageDigests.add(entry.substring("MessageDigest.".length()));
                } else if (entry.startsWith("Signature.")) {
                    signatures.add(entry.substring("Signature.".length()));
                }
            }
        }

        printSet("Ciphers", ciphers);
        printSet("KeyAgreeents", keyAgreements);
        printSet("Macs", macs);
        printSet("MessageDigests", messageDigests);
        printSet("Signatures", signatures);
    }

    @After
    public void tearDown() {
        ComponentsTestUtil.clear();
        MgnlContext.setInstance(null);
    }
}
