/**
 * Magnolia and its source-code is licensed under the LGPL.
 * You may copy, adapt, and redistribute this file for commercial or non-commercial use.
 * When copying, adapting, or redistributing this document in keeping with the guidelines above,
 * you are required to provide proper attribution to obinary.
 * If you reproduce or distribute the document without making any substantive modifications to its content,
 * please use the following attribution line:
 *
 * Copyright 1993-2006 obinary Ltd. (http://www.obinary.com) All rights reserved.
 */
package info.magnolia.module.delta;

import info.magnolia.cms.module.ModuleUtil;
import info.magnolia.cms.module.ServletDefinition;
import info.magnolia.module.InstallContext;
import org.jdom.JDOMException;

import java.io.IOException;


/**
 * @author philipp
 * @version $Id$
 */
public class RegisterServletTask extends AbstractTask {
    private final ServletDefinition servletDefinition;

    public RegisterServletTask(ServletDefinition servletDefinition) {
        super("Servlet " + servletDefinition.getName(), "Registers servlet" + servletDefinition.getName() + " (" + servletDefinition.getComment() + ")");
        this.servletDefinition = servletDefinition;
    }

    public void execute(InstallContext installContext) throws TaskExecutionException {
        installContext.warn("We can't register a servlet in web.xml because this would trigger a restart of the container. We will implement a filter, which is able to wrap servlets. Please register the servlet manually!");
        /*
        try {
            boolean webXmlModified = ModuleUtil.registerServlet(getServletDefinition());
            if (webXmlModified) {
                installContext.restartNeeded("Servlet " + getServletDefinition().getName() + " registered in web.xml");
            }
        }
        catch (JDOMException e) {
            throw new TaskExecutionException("Can't register servlet [" + getServletDefinition().getName() + "]", e);
        }
        catch (IOException e) {
            throw new TaskExecutionException("Can't register servlet [" + getServletDefinition().getName() + "]", e);
        }
        */
    }

    public ServletDefinition getServletDefinition() {
        return this.servletDefinition;
    }

}
