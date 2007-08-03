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
package info.magnolia.setup.for3_1;

import javax.jcr.RepositoryException;

import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.core.NodeData;
import info.magnolia.cms.util.ContentUtil;
import info.magnolia.cms.util.NodeDataUtil;
import info.magnolia.commands.DelegateCommand;
import info.magnolia.module.InstallContext;
import info.magnolia.module.delta.AllModulesNodeOperation;
import info.magnolia.module.delta.TaskExecutionException;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author philipp
 * @version $Id$
 *
 */
public class ReconfigureCommands extends AllModulesNodeOperation {

    /**
     * Logger.
     */
    private static Logger log = LoggerFactory.getLogger(ReconfigureCommands.class);

    public ReconfigureCommands() {
        super("reconfigure commands", "rename impl to class or commandName");
    }

    protected void operateOnModuleNode(Content node, HierarchyManager hm, InstallContext ctx) throws RepositoryException, TaskExecutionException  {
        try {
            if(node.hasContent("commands")){
                ContentUtil.visit(node.getContent("commands"), new ContentUtil.Visitor(){
                   public void visit(Content node) throws Exception {
                       if(node.hasNodeData("impl")){
                           String value = node.getNodeData("impl").getString();
                           node.deleteNodeData("impl");
                           NodeData classNodeData = NodeDataUtil.getOrCreate(node, "class");
                           if(StringUtils.contains(value, ".")){
                               classNodeData.setValue(value);
                           }
                           else if(StringUtils.isNotEmpty(value)){
                               classNodeData.setValue(DelegateCommand.class.getName());
                               NodeDataUtil.getOrCreateAndSet(node, "commandName", value);
                           }
                           else{
                               // it is a chain --> nothing to do
                           }
                       }
                   }
                });
            }
        }
        catch(RepositoryException e){
            throw e;
        }
        catch (Exception e) {
            throw new TaskExecutionException("can't reconfigure tasks", e);
        }
    }
}
