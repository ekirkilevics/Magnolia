/**
 * This file Copyright (c) 2003-2009 Magnolia International
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

import info.magnolia.cms.beans.config.MIMEMapping;
import info.magnolia.context.MgnlContext;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;


/**
 * @author fgiust
 * @version $Revision$ ($Author$)
 */
public class ExtensionVoter extends AbstractBoolVoter {

    private String[] allow;

    private String[] deny;

    public void setAllow(String allow) {
        this.allow = StringUtils.split(allow, ',');
    }

    public void setDeny(String deny) {
        this.deny = StringUtils.split(deny, ',');
    }

    protected boolean boolVote(Object value) {
        String extension;
        if(value instanceof String){
            extension = StringUtils.substringAfterLast((String)value, ".");
        }
        else{
            extension = MgnlContext.getAggregationState().getExtension();
        }

        if (StringUtils.isEmpty(MIMEMapping.getMIMEType(extension))) {
            return false; // check for MIMEMapping, extension must exist
        }

        if (allow != null && allow.length > 0 && !ArrayUtils.contains(allow, extension)) {
            return false;
        }

        if (deny != null && deny.length > 0 && ArrayUtils.contains(deny, extension)) {
            return false;
        }

        return true;
    }

}
