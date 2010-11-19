/**
 * This file Copyright (c) 2010 Magnolia International
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
package info.magnolia.cms.filters;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.ArrayUtils;

import info.magnolia.voting.Voter;
import info.magnolia.voting.Voting;
import info.magnolia.voting.voters.DontDispatchOnForwardAttributeVoter;

/**
 * Used to configure if a filter is executed for a specific dispatcher type (REQUEST, FORWARD, INCLUDE, ERROR).
 *
 * @author tmattsson
 * @see info.magnolia.cms.filters.AbstractMgnlFilter
 */
public class DispatchRule {

    private boolean enabled = true;
    private Voter[] bypasses = new Voter[0];
    private boolean dispatchOnForwardAttribute = false;
    private Voting voting = Voting.Factory.getDefaultVoting();

    public DispatchRule() {
    }

    public DispatchRule(boolean enabled) {
        this.enabled = enabled;
    }

    public void addBypass(Voter voter) {
        this.bypasses = (Voter[]) ArrayUtils.add(this.bypasses, voter);
    }

    public void setDispatchOnForwardAttribute(boolean dispatchOnForwardAttribute) {
        this.dispatchOnForwardAttribute = dispatchOnForwardAttribute;
    }

    public boolean isDispatchOnForwardAttribute() {
        return dispatchOnForwardAttribute;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean bypasses(HttpServletRequest request) {
        if (!isEnabled())
            return true;
        if (!dispatchOnForwardAttribute && request.getAttribute(DontDispatchOnForwardAttributeVoter.DONT_DISPATCH_ON_FORWARD_ATTRIBUTE) != null)
            return true;
        return voting.vote(bypasses, request) > 0;
    }
}
