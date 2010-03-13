/**
 * This file Copyright (c) 2009-2010 Magnolia International
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
package info.magnolia.module.cache;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Simple policy that delegates all the requests to any policies added in its policies list.
 * The policies in the list are invoked in the order in which they were added.
 * @author had
 * @version $Id:$
 */
public class DelegateFlushPolicy implements FlushPolicy {

    Logger log = LoggerFactory.getLogger(DelegateFlushPolicy.class);

    List<FlushPolicy> policies = new ArrayList<FlushPolicy>();

    public List<FlushPolicy> getPolicies() {
        return policies;
    }

    public void setPolicies(List<FlushPolicy> policies) {
        this.policies.clear();
        this.policies.addAll(policies);
    }

    public void addPolicy(FlushPolicy policy) {
        this.policies.add(policy);
    }

    public void start(Cache cache) {
        for (FlushPolicy policy : policies) {
            policy.start(cache);
        }
    }

    public void stop(Cache cache) {
        for (FlushPolicy policy : policies) {
            policy.stop(cache);
        }
    }
}
