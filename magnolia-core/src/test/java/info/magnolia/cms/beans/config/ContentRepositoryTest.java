/**
 * This file Copyright (c) 2009 Magnolia International
 * Ltd.  (http://www.magnolia-cms.com). All rights reserved.
 *
 *
 * This program and the accompanying materials are made
 * available under the terms of the Magnolia Network Agreement
 * which accompanies this distribution, and is available at
 * http://www.magnolia-cms.com/mna.html
 *
 * Any modifications to this file must keep this entire header
 * intact.
 *
 */
package info.magnolia.cms.beans.config;

import junit.framework.TestCase;

/**
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class ContentRepositoryTest extends TestCase {
    public void testUnknownRepositoryShouldYieldMeaningfulExceptionMessage() {
        try {
            ContentRepository.getRepository("dummy");
            fail("should have failed, since we haven't set any repository at all");
        } catch (Throwable t) {
            assertEquals("Failed to retrieve repository 'dummy' (mapped as 'dummy'). Your Magnolia instance might not have been initialized properly.", t.getMessage());
        }
    }
    public void testUnknownRepositoryShouldAlsoYieldMeaningfulExceptionMessageForRepositoryProviders() {
        try {
            ContentRepository.getRepositoryProvider("dummy");
            fail("should have failed, since we haven't set any repository at all");
        } catch (Throwable t) {
            assertEquals("Failed to retrieve repository provider 'dummy' (mapped as 'dummy'). Your Magnolia instance might not have been initialized properly.", t.getMessage());
        }
    }
}
