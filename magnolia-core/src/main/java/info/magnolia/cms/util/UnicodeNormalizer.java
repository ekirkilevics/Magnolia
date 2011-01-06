/**
 * This file Copyright (c) 2009-2011 Magnolia International
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
package info.magnolia.cms.util;

import info.magnolia.cms.core.SystemProperty;
import info.magnolia.objectfactory.Components;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * A wrapper around java.text.Normalizer and com.ibm.icu.text.Normalizer; uses the former if present, or none
 * if none is present.
 *
 * <strong>note:</strong> if not running under Java >=6, and without ICU, this does nothing.
 * If needed, one can use their own implementation, by setting the info.magnolia.cms.util.UnicodeNormalizer$Normalizer
 * system property.
 *
 * @see java.text.Normalizer
 * @see com.ibm.icu.text.Normalizer
 * @see <a href="http://www.icu-project.org/">http://www.icu-project.org/</a> to get the ICU4J library.
 * @see <a href="http://en.wikipedia.org/wiki/Unicode_equivalence#Normal_forms">http://en.wikipedia.org/wiki/Unicode_equivalence#Normal_forms</a> for more information.
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class UnicodeNormalizer {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(UnicodeNormalizer.class);

    private static final String JAVA6_NORMALIZER_CLASS = "java.text.Normalizer";
    private static final String JAVA6_FORMPARAM_CLASS = "java.text.Normalizer$Form";
    private static final String ICU_NORMALIZER_CLASS = "com.ibm.icu.text.Normalizer";

    private static final Normalizer normalizer = Components.getSingleton(Normalizer.class);

    public static String[] normalizeNFC(String[] values) {
        if (values == null)
            return null;
        for (int i = 0; i < values.length; i++) {
            values[i] = normalizeNFC(values[i]);
        }
        return values;
    }

    /**
     * Normalizes the given String to the NFC form.
     */
    public static String normalizeNFC(String in) {
        if (in == null)
            return null;
        return normalizer.normalizeNFC(in);
        /* if you're in dire need to debug:
         try {
            log.debug("not normalized: " + Arrays.toString(in.getBytes("UTF-8")) + " (" + in + ")");
            String out = normalizer.normalizeNFC(in);
            log.debug("    normalized: " + Arrays.toString(out.getBytes("UTF-8")) + " (" + out + ")");
            return out;
        } catch (UnsupportedEncodingException e) {
            // do nothing
        }
        return in;
        */
    }

    /**
     * Used to normalize a String.
     */
    public interface Normalizer {
        String normalizeNFC(String in);
    }

    /**
     * This uses reflection, since we're still compiling with Java 5.
     * This implementation could be externalized to a "java 6 only" module if needed.
     */
    public static final class Java6ReflectionNormalizer implements Normalizer {
        private final Method normalize;
        private final Object nfc;

        public Java6ReflectionNormalizer() {
            try {
                final Class<?> normalizer = Class.forName(JAVA6_NORMALIZER_CLASS);
                final Class<?> form = Class.forName(JAVA6_FORMPARAM_CLASS);
                normalize = normalizer.getMethod("normalize", CharSequence.class, form);
                nfc = form.getField("NFC").get(null);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            } catch (NoSuchFieldException e) {
                throw new RuntimeException(e);
            } catch (NoSuchMethodException e) {
                throw new RuntimeException(e);
            }

        }

        public String normalizeNFC(String in) {
            try {
                return (String) normalize.invoke(null, in, nfc);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            } catch (InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * Uses {@link com.ibm.icu.text.Normalizer} to normalize the string.
     */
    public static final class ICUNormalizer implements UnicodeNormalizer.Normalizer {
        public String normalizeNFC(String in) {
            return com.ibm.icu.text.Normalizer.normalize(in, com.ibm.icu.text.Normalizer.NFC);
        }
    }

    /**
     * Returns the original value unchanged.
     */
    public static final class NonNormalizer implements UnicodeNormalizer.Normalizer {
        public String normalizeNFC(String in) {
            return in;
        }
    }

    /**
     * Tries to load the normalizer dynamically and respects the property {@link SystemProperty#MAGNOLIA_UTF8_ENABLED}.
     */
    public static final class AutoDetectNormalizer implements Normalizer {
        private final Normalizer delegate;

        public AutoDetectNormalizer() {
            Normalizer candidate;
            if (!SystemProperty.getBooleanProperty(SystemProperty.MAGNOLIA_UTF8_ENABLED)) {
                candidate = new NonNormalizer();
            } else {
                try {
                    Class.forName(JAVA6_NORMALIZER_CLASS);
                    candidate = new Java6ReflectionNormalizer();
                    log.info("Running on Java 6, using {} for unicode form normalization.", candidate.getClass());
                } catch (ClassNotFoundException e) {
                    log.warn("Not running on Java 6 ({} not found). Attempting to locate the ICU4J library.", JAVA6_NORMALIZER_CLASS);
                    try {
                        Class.forName(ICU_NORMALIZER_CLASS);
                        candidate = new ICUNormalizer();
                        log.info("ICU4J found, using {} for Unicode form normalization.", candidate.getClass());
                    } catch (ClassNotFoundException e2) {
                        log.warn("ICU4J not found ({} not found), Unicode will not be 100% supported; no Unicode form normalization available. If Java 6 is not an option, you can get the ICU4J library from http://www.icu-project.org/.", ICU_NORMALIZER_CLASS);
                        candidate = new NonNormalizer();
                    }
                }
            }
            this.delegate = candidate;
        }

        public String normalizeNFC(String in) {
            return delegate.normalizeNFC(in);
        }
    }

}
