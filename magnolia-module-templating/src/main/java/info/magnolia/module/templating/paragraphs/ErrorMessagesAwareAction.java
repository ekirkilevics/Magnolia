/**
 *
 * Magnolia and its source-code is licensed under the LGPL.
 * You may copy, adapt, and redistribute this file for commercial or non-commercial use.
 * When copying, adapting, or redistributing this document in keeping with the guidelines above,
 * you are required to provide proper attribution to obinary.
 * If you reproduce or distribute the document without making any substantive modifications to its content,
 * please use the following attribution line:
 *
 * Copyright 1993-2006 obinary Ltd. (http://www.obinary.com) All rights reserved.
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
