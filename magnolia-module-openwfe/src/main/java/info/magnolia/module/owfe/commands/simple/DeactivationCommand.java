/**
 *
 * Magnolia and its source-code is licensed under the LGPL.
 * You may copy, adapt, and redistribute this file for commercial or non-commercial use.
 * When copying, adapting, or redistributing this document in keeping with the guidelines above,
 * you are required to provide proper attribution to obinary.
 * If you reproduce or distribute the document without making any substantive modifications to its content,
 * please use the following attribution line:
 *
 * Copyright 2006 obinary Ltd. (http://www.obinary.com) All rights reserved.
 *
 */
package info.magnolia.module.owfe.commands.simple;

import info.magnolia.cms.beans.commands.MgnlCommand;
import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.core.ItemType;
import info.magnolia.cms.exchange.Syndicator;
import info.magnolia.cms.security.User;
import info.magnolia.cms.util.FactoryUtil;
import info.magnolia.cms.util.Rule;
import info.magnolia.module.owfe.MgnlConstants;

import java.util.HashMap;

import org.apache.commons.chain.Context;


/**
 * the deactivation command which do real deactivation
 * @author jackie
 */
public class DeactivationCommand extends MgnlCommand {

    final static String[] expected = {MgnlConstants.P_PATH};

    /**
     * List of the parameters that this command needs to run
     * @return a list of string describing the parameters needed. The parameters should have a mapping in this class.
     */
    public String[] getExpectedParameters() {
        return expected;
    }

    public boolean exec(HashMap params, Context Ctx) {
        String path;
        path = (String) params.get(MgnlConstants.P_PATH);
        try {
            doDeactivate(((info.magnolia.cms.beans.runtime.Context) Ctx).getUser(), path);
        }
        catch (Exception e) {
            log.error("cannot do deactivate", e);
            return false;
        }
        return true;
    }

    private void doDeactivate(User user, String path) throws Exception {
        Rule rule = new Rule();
        rule.addAllowType(ItemType.CONTENTNODE.getSystemName());
        rule.addAllowType(ItemType.NT_FILE);
        Syndicator syndicator = (Syndicator) FactoryUtil.getInstance(Syndicator.class);
        syndicator.init(user, MgnlConstants.WEBSITE_REPOSITORY, ContentRepository
            .getDefaultWorkspace(MgnlConstants.WEBSITE_REPOSITORY), rule);
        syndicator.deActivate(path);
    }

}
