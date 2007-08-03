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
package info.magnolia.cms.exchange;

import info.magnolia.voting.Voter;

/**
 * @author Sameer Charles
 * $Id$
 */
public interface Subscription extends Voter {

    public String getName();

    public void setName(String name);

    public String getFromURI();

    public void setFromURI(String fromURI);

    public String getToURI();

    public void setToURI(String toURI);

    public String getRepository();

    public void setRepository(String repository);
}
