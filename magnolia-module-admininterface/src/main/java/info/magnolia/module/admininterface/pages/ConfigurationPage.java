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
package info.magnolia.module.admininterface.pages;

import info.magnolia.cms.beans.config.Server;
import info.magnolia.cms.beans.config.ShutdownManager;
import info.magnolia.cms.beans.config.VirtualURIManager;
import info.magnolia.cms.core.SystemProperty;
import info.magnolia.cms.security.SecureURI;
import info.magnolia.module.admininterface.TemplatedMVCHandler;

import java.util.List;
import java.util.Set;
import java.util.Map;
import java.util.Collection;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * @author Fabrizio Giustina
 * @version $Revision$ ($Author$)
 */
public class ConfigurationPage extends TemplatedMVCHandler {

    /**
     * Required constructor.
     * @param name page name
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     */
    public ConfigurationPage(String name, HttpServletRequest request, HttpServletResponse response) {
        super(name, request, response);
    }

    public Server getServer() {
        return Server.getInstance();
    }

    public String getDefaultMailServer() {
        return Server.getDefaultMailServer();
    }

    public String getServerId() {
        return Server.getServerId();
    }

    public Collection getVirtualUriMappings() {
        return VirtualURIManager.getInstance().getURIMappings().values();
    }

    public List getShutdownTasks() {
        return ShutdownManager.listShutdownTasks();
    }

    public Map getSecureURIs() {
        return SecureURI.listSecureURIs();
    }

    public Map getUnsecureURIs() {
        return SecureURI.listUnsecureURIs();
    }

    public Map getSystemProperties() {
        return SystemProperty.getPropertyList();
    }

}
