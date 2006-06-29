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

package info.magnolia.module.data;

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.core.ItemType;
import info.magnolia.cms.exchange.Syndicator;
import info.magnolia.cms.util.FactoryUtil;
import info.magnolia.cms.util.Rule;
import info.magnolia.context.MgnlContext;
import info.magnolia.module.admininterface.AdminTreeMVCHandler;
import info.magnolia.module.data.gui.DataTreeControl;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;


/**
 * Data tree
 * @author Christoph Hoffmann (BeagleSoft GmbH)
 * @version $Revision$ ($Author$)
 *
 */
public class DataAdminTree extends AdminTreeMVCHandler {

    private static Logger log = Logger.getLogger(DataAdminTree.class);

    /**
     * @param name
     * @param request
     * @param response
     */
    public DataAdminTree(String name, HttpServletRequest request, HttpServletResponse response) {
        super(name, request, response);
        setTree(new DataTreeControl(getRepository(), request, response));
        setConfiguration(new DataAdminTreeConfig());
    }

    //TODO: check if this really works [cho]
	public Syndicator getActivationSyndicator(String path) {
        /*
         * Here rule defines which content types to collect, its a resposibility of the caller ro set this, it will be
         * different in every hierarchy, for instance - in website tree recursive activation : rule will allow
         * mgnl:contentNode, mgnl:content and nt:file - in website tree non-recursive activation : rule will allow
         * mgnl:contentNode and nt:file only
         */
        Rule rule = new Rule();
        rule.addAllowType(ItemType.CONTENTNODE.getSystemName());
        rule.addAllowType(ItemType.CONTENT.getSystemName());
        rule.addAllowType(ItemType.NT_METADATA);
        rule.addAllowType(ItemType.NT_RESOURCE);

        Syndicator syndicator = (Syndicator) FactoryUtil.getInstance(Syndicator.class);
        syndicator.init(MgnlContext.getUser(), this.getRepository(), ContentRepository.getDefaultWorkspace(this
            .getRepository()), rule);

        return syndicator;
	}

}