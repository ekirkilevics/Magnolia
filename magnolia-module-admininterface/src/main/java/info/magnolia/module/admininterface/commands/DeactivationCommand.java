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
package info.magnolia.module.admininterface.commands;

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.exchange.Syndicator;
import info.magnolia.cms.i18n.MessagesManager;
import info.magnolia.cms.util.AlertUtil;
import info.magnolia.cms.util.FactoryUtil;
import info.magnolia.cms.util.Rule;
import info.magnolia.context.Context;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * the deactivation command which do real deactivation
 * @author jackie
 */
public class DeactivationCommand extends BaseRepositoryCommand {

    private static Logger log = LoggerFactory.getLogger(DeactivationCommand.class);

    public boolean execute(Context ctx) throws Exception {
        try{
            Syndicator syndicator = (Syndicator) FactoryUtil.getInstance(Syndicator.class);
            syndicator.init(ctx.getUser(), this.getRepository(), ContentRepository.getDefaultWorkspace(this.getRepository()), new Rule());
            syndicator.deActivate(getPath());
        }
        catch(Exception e){
            AlertUtil.setException(MessagesManager.get("tree.error.deactivate"), e, ctx);
            return false;
        }
        return true;
    }

}
