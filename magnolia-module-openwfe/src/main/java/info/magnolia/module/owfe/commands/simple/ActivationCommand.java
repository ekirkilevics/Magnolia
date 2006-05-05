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
import info.magnolia.cms.beans.runtime.MgnlContext;
import info.magnolia.cms.core.ItemType;
import info.magnolia.cms.exchange.Syndicator;
import info.magnolia.cms.security.User;
import info.magnolia.cms.util.FactoryUtil;
import info.magnolia.cms.util.Rule;
import info.magnolia.module.owfe.MgnlConstants;

import java.util.HashMap;

import org.apache.commons.chain.Context;
import org.apache.commons.lang.StringUtils;


/**
 * the activation command which do real activation
 * @author jackie
 */
public class ActivationCommand extends MgnlCommand {

    static final String[] parameters = {MgnlConstants.P_RECURSIVE, MgnlConstants.P_PATH};

    /**
     * List of the parameters that this command needs to run
     * @return a list of string describing the parameters needed. The parameters should have a mapping in this class.
     */
    public String[] getExpectedParameters() {
        return parameters;
    }

    public boolean exec(HashMap params, Context ctx) {

        String path;
        boolean recursive;
        path = (String) params.get(MgnlConstants.P_PATH);

        // recursive = (params.get(MgnlConstants.P_RECURSIVE).toString()).equalsIgnoreCase("true");

        recursive = Boolean.valueOf((String) params.get(MgnlConstants.P_RECURSIVE)).booleanValue();
        log.info("recursive = " + recursive);
        // for tesing
        log.info("user = " + ((info.magnolia.cms.beans.runtime.Context) ctx).getUser());

        try {
            doActivate(((info.magnolia.cms.beans.runtime.Context) ctx).getUser(), path, recursive);
        }
        catch (Exception e) {
            log.error("cannot do activate", e);
            return false;
        }
        log.info("exec successfully.");
        return true;
    }

    /**
     * do real activation
     * @param path node path
     * @param recursive activet recursively or no
     * @throws Exception
     */
    private void doActivate(User user, String path, boolean recursive) throws Exception {
        Rule rule = new Rule();
        rule.addAllowType(ItemType.CONTENTNODE.getSystemName());
        rule.addAllowType(ItemType.NT_METADATA);
        if (recursive) {
            rule.addAllowType(ItemType.CONTENT.getSystemName());
        }

        Syndicator syndicator = (Syndicator) FactoryUtil.getInstance(Syndicator.class);
        syndicator.init(MgnlContext.getUser(), MgnlConstants.WEBSITE_REPOSITORY, ContentRepository
            .getDefaultWorkspace(MgnlConstants.WEBSITE_REPOSITORY), rule);

        String parentPath = StringUtils.substringBeforeLast(path, "/");
        if (StringUtils.isEmpty(parentPath)) {
            parentPath = "/";
        }
        syndicator.activate(parentPath, path);

    }

}
