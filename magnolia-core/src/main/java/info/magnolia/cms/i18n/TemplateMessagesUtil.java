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
package info.magnolia.cms.i18n;

/**
 * This class helps to get the messages used in templates (paragraphs, dialogs, ..). First it make a lookup in
 * messages_templating_custom and then in messages_templating..<br>
 * If a string is not found it returns directly the key without ?
 * <p>
 * This class is depricated use the fallBackMessages property of the Message class to make chains.
 * @author philipp
 */
public final class TemplateMessagesUtil {

    /**
     * Use this basename if the string is not found in the custom basename
     */
    public static final String DEFAULT_BASENAME = "info.magnolia.module.admininterface.messages_templating"; //$NON-NLS-1$

    /**
     * Name of the custom basename
     */
    public static final String CUSTOM_BASENAME = "info.magnolia.module.admininterface.messages_templating_custom"; //$NON-NLS-1$

    /**
     * Util has no public constructor
     */
    private TemplateMessagesUtil() {
    }

    /**
     * Get the messages for the templates
     * @return
     */
    public static Messages getMessages() {
        return MessagesUtil.chain(new String[]{
            TemplateMessagesUtil.CUSTOM_BASENAME,
            TemplateMessagesUtil.DEFAULT_BASENAME});
    }
}