/**
 *
 * Magnolia and its source-code is licensed under the LGPL.
 * You may copy, adapt, and redistribute this file for commercial or non-commercial use.
 * When copying, adapting, or redistributing this document in keeping with the guidelines above,
 * you are required to provide proper attribution to obinary.
 * If you reproduce or distribute the document without making any substantive modifications to its content,
 * please use the following attribution line:
 *
 * Copyright 1993-2007 magnolia Ltd. (http://www.magnolia.info) All rights reserved.
 *
 */
package info.magnolia.cms.mail;

import info.magnolia.cms.mail.templates.MgnlEmail;

/**
 * Register this per type factories by MgnlMailFactory.registerMailType().
 * @author philipp
 */
public interface MgnlMailTypeFactory {
	
	/**
     * Create a new mail instance for this type
     */
    public MgnlEmail createEmail() throws Exception;

}
