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
package info.magnolia.voting.voters;

import junit.framework.TestCase;
import info.magnolia.voting.Voter;

/**
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class IfVoterTest extends TestCase {
    public void testReturnsValueOfElseIfConditionIsTrue() {
        final IfVoter v = new IfVoter();
        // condition's return value is ignored.
        v.setCondition(fakeVoter(true, -7, 7));
        v.setThen(fakeVoter(true, -2, 2));
        v.setOtherwise(fakeVoter(true, -4, 4));
        assertEquals(2, v.vote(null));
    }

    public void testReturnsValueOfOtherwiseIfConditionIsFalse() {
        final IfVoter v = new IfVoter();
        // condition's return value is ignored.
        v.setCondition(fakeVoter(false, -7, 7));
        v.setThen(fakeVoter(true, -2, 2));
        v.setOtherwise(fakeVoter(true, -4, 4));
        assertEquals(4, v.vote(null));
    }

    public void testReturns0IfConditionIsFalseAndOtherwiseIsntSet() {
        final IfVoter v = new IfVoter();
        // condition's return value is ignored.
        v.setCondition(fakeVoter(false, -7, 7));
        v.setThen(fakeVoter(true, -2, 2));
        assertEquals(0, v.vote(null));
    }

    private Voter fakeVoter(boolean bool, int falseValue, int trueValue) {
        final AbstractBoolVoter voter = bool ? new TrueVoter() : new FalseVoter();
        voter.setFalseValue(falseValue);
        voter.setTrueValue(trueValue);
        return voter;
    }
}
