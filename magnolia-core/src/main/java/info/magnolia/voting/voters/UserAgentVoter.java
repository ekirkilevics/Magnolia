/**
 * This file Copyright (c) 2008-2009 Magnolia International
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
package info.magnolia.voting.voters;

import info.magnolia.context.Context;
import info.magnolia.context.MgnlContext;
import info.magnolia.context.WebContext;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

/**
 * A voter which checks the user agent header in <code>request</code>
 * object against a list of allowed and/or rejected user agents.
 *
 * @author had
 * @version $Revision: $ ($Author: $)
 */
public class UserAgentVoter extends AbstractBoolVoter {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(UserAgentVoter.class);

    private final List allowed = new ArrayList();
    private final List rejected = new ArrayList();

    public List getAllowed() {
        return allowed;
    }

    public void addAllowed(String contentType) {
        allowed.add(contentType);
    }

    public List getRejected() {
        return rejected;
    }

    public void addRejected(String contentType) {
        rejected.add(contentType);
    }

    protected boolean boolVote(Object value) {
        final HttpServletRequest request;
        if (value instanceof HttpServletRequest) {
            request = (HttpServletRequest) value;
        } else {
            final Context ctx = MgnlContext.getInstance();
            if (ctx instanceof WebContext) {
                request = ((WebContext) ctx).getRequest();
            } else {
                return false;
            }
        }

        final String userAgent = request.getHeader("User-Agent");
        // values look like:
        // Mozilla/5.0 (X11; U; Linux i686; en-US; rv:1.9.0.4) Gecko/2008111317 Ubuntu/8.04 (hardy) Firefox/3.0.4
        if (userAgent == null) {
            log.warn("No user agent on the request, can't vote.");
            return false;
        }
        if (allowed.size() > 0 && !matches(allowed, userAgent)) {
            return false;
        }
        if (rejected.size() > 0 && matches(rejected, userAgent)) {
            return false;
        }
        return true;
    }

    /**
     * Matches obtained user agent against patterns in the list.
     * @param patterns List of patterns to check.
     * @param userAgent User Agent value from the request.
     * @return true if at least one pattern matches the <code>userAgent</code>, false otherwise.
     */
    private boolean matches(List patterns, String userAgent) {
        for (int i = 0; i < patterns.size(); i++) {
            if (userAgent.matches((String) patterns.get(i))) {
                return true;
            }
        }
        return false;
    }
}
