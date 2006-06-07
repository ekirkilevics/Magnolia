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

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.core.ItemType;
import info.magnolia.cms.exchange.Syndicator;
import info.magnolia.cms.util.FactoryUtil;
import info.magnolia.cms.util.Rule;
import info.magnolia.commands.ContextAttributes;
import info.magnolia.context.MgnlContext;

import org.apache.commons.chain.Command;
import org.apache.commons.chain.Context;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * the activation command which do real activation
 * @author jackie
 */
public class ActivationCommand implements Command {
    
    private static Logger log = LoggerFactory.getLogger(ActivationCommand.class);

    public boolean execute(Context ctx) {
        boolean recursive;
        String path = (String) ctx.get(ContextAttributes.P_PATH);
        String repository = (String) ctx.get(ContextAttributes.P_REPOSITORY);
        recursive = Boolean.valueOf((String) ctx.get(ContextAttributes.P_RECURSIVE)).booleanValue();

        if (log.isDebugEnabled()) {
            log.debug("recursive = " + recursive);
            log.debug("user = " + ((info.magnolia.context.Context) ctx).getUser().getName());
        }
        
        try {
            doActivate(repository, path, recursive);
        }
        catch (Exception e) {
            log.error("cannot do activate:"+ e.getMessage());
            return true;
        }
        log.info("exec successfully.");
        return false;
    }

    /**
     * do real activation
     * @param path node path
     * @param recursive activet recursively or no
     * @throws Exception
     */
    private void doActivate(String repository, String path, boolean recursive) throws Exception {
        Rule rule = new Rule();
        rule.addAllowType(ItemType.CONTENTNODE.getSystemName());
        rule.addAllowType(ItemType.NT_METADATA);
        rule.addAllowType(ItemType.NT_RESOURCE);
        if (recursive) {
            rule.addAllowType(ItemType.CONTENT.getSystemName());
        }

        Syndicator syndicator = (Syndicator) FactoryUtil.getInstance(Syndicator.class);
        syndicator.init(MgnlContext.getUser(), repository, ContentRepository
            .getDefaultWorkspace(repository), rule);

        String parentPath = StringUtils.substringBeforeLast(path, "/");
        if (StringUtils.isEmpty(parentPath)) {
            parentPath = "/";
        }
        syndicator.activate(parentPath, path);

    }

}
