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
package info.magnolia.module.admininterface.trees;

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.core.ItemType;
import info.magnolia.cms.exchange.ExchangeException;
import info.magnolia.cms.exchange.Syndicator;
import info.magnolia.cms.util.FactoryUtil;
import info.magnolia.cms.util.Rule;
import info.magnolia.context.MgnlContext;
import info.magnolia.module.admininterface.AdminTreeMVCHandler;

import javax.jcr.RepositoryException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Handles the tree rendering for the "config" repository.
 * @author Fabrizio Giustina
 * @version $Id$
 */
public class ConfigTreeHandler extends AdminTreeMVCHandler {

    /**
     * Logger
     */
    private static Logger log = LoggerFactory.getLogger(ConfigTreeHandler.class);

    /**
     * @param name
     * @param request
     * @param response
     */
    public ConfigTreeHandler(String name, HttpServletRequest request, HttpServletResponse response) {
        super(name, request, response);
        this.setConfiguration(new ConfigTreeConfiguration());
    }

    /**
     * Create the <code>Syndicator</code> to activate the specified path.
     * @param path node path to be activated
     * @return the <code>Syndicator</code> used to activate
     * @see info.magnolia.module.admininterface.AdminTreeMVCHandler#getActivationSyndicator(String)
     */
    public Syndicator getActivationSyndicator(String path) {
        boolean recursive = (this.getRequest().getParameter("recursive") != null); //$NON-NLS-1$
        Rule rule = new Rule();
        if (recursive) {
            rule.addAllowType(ItemType.CONTENT.getSystemName());
            rule.addAllowType(ItemType.CONTENTNODE.getSystemName());
            rule.addAllowType(ItemType.NT_METADATA);
            rule.addAllowType(ItemType.NT_RESOURCE);
        }

        Syndicator syndicator = (Syndicator) FactoryUtil.getInstance(Syndicator.class);
        syndicator.init(MgnlContext.getUser(), this.getRepository(), ContentRepository.getDefaultWorkspace(this
            .getRepository()), rule);

        return syndicator;
    }

    /**
     * since Rule take care of what sub nodes to include we can simply activate this path
     * @param syndicator
     * @param parentPath
     * @param path
     */
    public void activateNodeRecursive(Syndicator syndicator, String parentPath, String path) throws ExchangeException,
        RepositoryException {
        syndicator.activate(parentPath, path);
    }
}