/**
 *
 * Magnolia and its source-code is licensed under the LGPL.
 * You may copy, adapt, and redistribute this file for commercial or non-commercial use.
 * When copying, adapting, or redistributing this document in keeping with the guidelines above,
 * you are required to provide proper attribution to obinary.
 * If you reproduce or distribute the document without making any substantive modifications to its content,
 * please use the following attribution line:
 *
 * Copyright 1993-2005 obinary Ltd. (http://www.obinary.com) All rights reserved.
 *
 */
package info.magnolia.cms.i18n;

import javax.servlet.http.HttpServletRequest;


/**
 * This class helps to get the messages used in templates (paragraphs, dialogs, ..). First it make a lookup in
 * messages_templating_custom and then in messages_templating..<br>
 * If a string is not found it returns directly the key without ?
 * @author philipp
 */
public final class TemplateMessagesUtil {

    /**
     * Use this basename if the string is not found in the custom basename
     */
    public static final String DEFAULT_BASENAME = "info.magnolia.module.admininterface.messages_templating";

    /**
     * Name of the custom basename
     */
    public static final String CUSTOM_BASENAME = "info.magnolia.module.admininterface.messages_templating_custom";

    /**
     * Util has no public constructor
     */
    private TemplateMessagesUtil() {
    }
    
    /**
     * Get the message.
     * @param request request
     * @param key key
     * @return message
     */
    public static String get(HttpServletRequest request, String key) {
        String msg = MessagesManager.getMessages(request, DEFAULT_BASENAME).getWithDefault(key, key);
        if (!msg.equals(key)) {
            return msg;
        }
        return MessagesManager.getMessages(request, CUSTOM_BASENAME).getWithDefault(key, key);

    }

    /**
     * Get the message with replacement strings. Use the {nr} syntax
     * @param request request
     * @param key key
     * @param args replacement strings
     * @return message
     */
    public static String get(HttpServletRequest request, String key, Object[] args) {
        String msg = MessagesManager.getMessages(request, DEFAULT_BASENAME).getWithDefault(key, args, key);
        if (!msg.equals(key)) {
            return msg;
        }
        return MessagesManager.getMessages(request, CUSTOM_BASENAME).getWithDefault(key, args, key);
    }

}