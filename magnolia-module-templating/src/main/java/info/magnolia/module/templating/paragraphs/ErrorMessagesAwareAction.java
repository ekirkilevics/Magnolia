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
package info.magnolia.module.templating.paragraphs;

import info.magnolia.context.MgnlContext;
import info.magnolia.cms.util.LinkUtil;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;

import java.util.Collections;
import java.util.Map;

/**
 * An abstract action which exposes the errorMessages, retrieved as an MgnlContext attribute, decoded from a json
 * string into a Map. It also provides a utility method which encodes and add the errorMessages to a url.
 *
 * TODO : once MAGNOLIA-1698 is fixed, we'll probably be able to do an include or forward instead of a redirect, thus just storing errorMessages in the context instead of encoding it and passing it in the url
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public abstract class ErrorMessagesAwareAction {
    private Map errorMessages;

    public String execute() {
        final String errorMsgJsonStr = (String) MgnlContext.getAttribute("errorMessages");
        if (StringUtils.isEmpty(errorMsgJsonStr)) {
            errorMessages = Collections.EMPTY_MAP;
        } else {
            final JSONObject json = new JSONObject(errorMsgJsonStr);
            errorMessages = (Map) JSONObject.toBean(json, Map.class);
        }

        return doExecute();
    }

    protected abstract String doExecute();

    public Map getErrorMessages() {
        return errorMessages;
    }

    public static String addErrorMessagesToUrl(String uri, Map errorMessages) {
        final StringBuffer sb = new StringBuffer(uri);
        if (errorMessages !=null && !errorMessages.isEmpty()) {
            final JSONObject json = new JSONObject(errorMessages);
            final String jsonStr = json.toString();

            LinkUtil.addParameter(sb, "errorMessages", jsonStr);
        }
        return sb.toString();
    }

}
