/**
 * This file Copyright (c) 2008-2011 Magnolia International
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

import info.magnolia.context.MgnlContext;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;

/**
 * A voter which checks the content type set on the <strong>response</strong>
 * object against a list of allowed and/or rejected content types.
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class ResponseContentTypeVoter extends AbstractBoolVoter {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ResponseContentTypeVoter.class);

    private final List<String> allowed = new ArrayList<String>();
    private final List<String> rejected = new ArrayList<String>();

    public List<String> getAllowed() {
        return allowed;
    }

    public void addAllowed(String contentType) {
        allowed.add(contentType);
    }

    public List<String> getRejected() {
        return rejected;
    }

    public void addRejected(String contentType) {
        rejected.add(contentType);
    }

    protected boolean boolVote(Object value) {
        final HttpServletResponse response;
        if (value instanceof HttpServletResponse) {
            response = (HttpServletResponse) value;
        } else {
            if (MgnlContext.isWebContext()) {
                response = MgnlContext.getWebContext().getResponse();
            } else {
                return false;
            }
        }

        // strip the encoding off of the content type:
        final String contentType = StringUtils.substringBefore(response.getContentType(), ";");
        if (contentType == null) {
            log.warn("No content type set on the response, can't vote.");
            return false;
        }
        if (allowed.size() > 0 && !allowed.contains(contentType)) {
            return false;
        }
        if (rejected.size() > 0 && rejected.contains(contentType)) {
            return false;
        }
        return true;
    }
}
