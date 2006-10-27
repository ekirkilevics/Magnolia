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
package info.magnolia.module.workflow.commands.flow;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @deprecated please us info.magnolia.module.workflow.commands.ActivationFlowCommand instead.
 */
public class ActivationFlowCommand extends info.magnolia.module.workflow.commands.ActivationFlowCommand  {

    private static final Logger log = LoggerFactory.getLogger(ActivationFlowCommand.class);

    /**
     * Warn about the deprecation
     */
    public ActivationFlowCommand() {
        log.warn("you are using the deprecated class {} where you should use {}", ActivationFlowCommand.class.getName(), info.magnolia.module.workflow.commands.ActivationFlowCommand.class.getName());
    }
}
