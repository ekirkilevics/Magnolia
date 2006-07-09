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
import info.magnolia.cms.exchange.Syndicator;
import info.magnolia.cms.util.FactoryUtil;
import info.magnolia.cms.util.Rule;
import info.magnolia.context.MgnlContext;
import info.magnolia.module.admininterface.AdminTreeMVCHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Handles the tree rendering for the "website" repository.
 * @author Fabrizio Giustina
 * @version $Id$
 */
public class WebsiteTreeHandler extends AdminTreeMVCHandler {

    /**
     * Logger
     */
    private static Logger log = LoggerFactory.getLogger(WebsiteTreeHandler.class);

    /**
     * @param name
     * @param request
     * @param response
     */
    public WebsiteTreeHandler(String name, HttpServletRequest request, HttpServletResponse response) {
        super(name, request, response);
        this.setConfiguration(new WebsiteTreeConfiguration());
    }

    /**
     * Create the <code>Syndicator</code> to activate the specified path.
     * @param path node path to be activated
     * @return the <code>Syndicator</code> used to activate
     */
    public Syndicator getActivationSyndicator(String path) {
        /*
         * Here rule defines which content types to collect, its a resposibility of the caller ro set this, it will be
         * different in every hierarchy, for instance - in website tree recursive activation : rule will allow
         * mgnl:contentNode, mgnl:content and nt:file - in website tree non-recursive activation : rule will allow
         * mgnl:contentNode and nt:file only
         */
        Rule rule = new Rule();
        rule.addAllowType(ItemType.CONTENTNODE.getSystemName());
        rule.addAllowType(ItemType.NT_METADATA);
        rule.addAllowType(ItemType.NT_RESOURCE);

        Syndicator syndicator = (Syndicator) FactoryUtil.getInstance(Syndicator.class);
        syndicator.init(MgnlContext.getUser(), this.getRepository(), ContentRepository.getDefaultWorkspace(this
            .getRepository()), rule);

        return syndicator;
    }

}