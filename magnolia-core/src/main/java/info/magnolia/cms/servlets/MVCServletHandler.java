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
package info.magnolia.cms.servlets;

import java.io.IOException;


/**
 * This Handler is used in the MVCServlet
 * @author Philipp Bracher
 * @version $Id$
 */
public interface MVCServletHandler {

    String VIEW_NOTHING = "nothing"; //$NON-NLS-1$

    /**
     * Depending on the request it is generating a logical command name
     * @return name of the command
     */
    String getCommand();

    /**
     * Call the method through reflection
     * @param command
     * @return the name of the view to show (used in renderHtml)
     */
    String execute(String command);

    /**
     * Render the tree depending on the view name.
     * @param view
     * @return
     * @throws IOException
     */
    void renderHtml(String view) throws IOException;

    /**
     * The name of the handler
     */
    String getName();

    /**
     * Called after instantiating
     */
    void init() throws Exception;
}