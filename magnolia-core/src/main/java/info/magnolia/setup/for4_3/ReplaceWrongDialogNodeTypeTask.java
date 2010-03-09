/**
 * This file Copyright (c) 2010 Magnolia International
 * Ltd.  (http://www.magnolia-cms.com). All rights reserved.
 *
 *
 * This file is dual-licensed under both the Magnolia
 * Network Agreement and the GNU General Public License.
 * You may elect to use one or the other of these licenses.
 *
 * This file is distributed in the hope that it will be
 * useful, but AS-IS and WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE, TITLE, or NONINFRINGEMENT.
 * Redistribution, except as permitted by whichever of the GPL
 * or MNA you select, is prohibited.
 *
 * 1. For the GPL license (GPL), you can redistribute and/or
 * modify this file under the terms of the GNU General
 * Public License, Version 3, as published by the Free Software
 * Foundation.  You should have received a copy of the GNU
 * General Public License, Version 3 along with this program;
 * if not, write to the Free Software Foundation, Inc., 51
 * Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * 2. For the Magnolia Network Agreement (MNA), this file
 * and the accompanying materials are made available under the
 * terms of the MNA which accompanies this distribution, and
 * is available at http://www.magnolia-cms.com/mna.html
 *
 * Any modifications to this file must keep this entire header
 * intact.
 *
 */
package info.magnolia.setup.for4_3;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.core.ItemType;
import info.magnolia.cms.core.Path;
import info.magnolia.cms.util.ContentUtil;
import info.magnolia.module.InstallContext;
import info.magnolia.module.delta.AbstractRepositoryTask;
import info.magnolia.module.delta.TaskExecutionException;

import javax.jcr.ImportUUIDBehavior;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



/**
 * Check for each module in the config repository if dialogs are of the incorrect type <em>mgnl:content</em> 
 * and attempts to replace them with the correct one <em>mgnl:contentNode<em>.<br>
 * See also jira MAGNOLIA-2810
 * @author fgrilli
 * @version $Id$
 *
 */
public class ReplaceWrongDialogNodeTypeTask extends AbstractRepositoryTask {
    
    private final static Logger log = LoggerFactory.getLogger(ReplaceWrongDialogNodeTypeTask.class);
    
    private static final Pattern DIALOG_NODE_TYPE = Pattern.compile("(.+jcr:primaryType.+<sv:value>)(mgnl:content)(</sv:value>)");

    private static final String REPLACEMENT = "$1mgnl:contentNode$3";
    
    public ReplaceWrongDialogNodeTypeTask() {
        super("Replace incorrect dialog node types", "Checks for each module in the config repository if dialogs are of the incorrect type mgnl:content and replaces them with the correct one mgnl:contentNode");
    }

    protected void doExecute(InstallContext installContext) throws RepositoryException, TaskExecutionException {
        HierarchyManager hm = installContext.getHierarchyManager(ContentRepository.CONFIG);
        List<Content> dialogNodes = new ArrayList<Content>();
        Content srcRoot = hm.getContent("/modules");
        collectDialogNodes(srcRoot, dialogNodes);
        installContext.info("Found " +dialogNodes.size()+ " dialog(s)");
        for(Content srcNode: dialogNodes){
            String dest = srcNode.getHandle();
            try {
                installContext.info("Checking if "+ dest +" needs to be replaced due to incorrect dialog type...");
                replaceInSession(srcNode);
            }
            catch (RepositoryException e) {
                installContext.error("Can't replace " + dest, e );
            }
        }
    }
    
    private void replaceInSession(Content src) throws RepositoryException {
        final String destParentPath = StringUtils.defaultIfEmpty(StringUtils.substringBeforeLast(src.getHandle(), "/"), "/");
        final Session session = src.getWorkspace().getSession();
        FileOutputStream outStream = null;
        FileInputStream inStream = null;
        File file = null;
        try{
            file = File.createTempFile("mgnl", null, Path.getTempDirectory());
            outStream = new FileOutputStream(file);
            session.exportSystemView(src.getHandle(), outStream, false, false);
            outStream.flush();
            final String content = FileUtils.readFileToString(file);
            log.debug("content string is {}", content);
            final Matcher matcher = DIALOG_NODE_TYPE.matcher(content);
            String replaced = null;
            if(matcher.find()){
                replaced = matcher.replaceFirst(REPLACEMENT);
                log.info("{} will be replaced", src.getHandle());
                log.debug("replaced string is {}", replaced);
            } else {
                log.info("{} won't be replaced", src.getHandle());
                return;
            }
            FileUtils.writeStringToFile(file, replaced);
            inStream = new FileInputStream(file);
            session.importXML(
                destParentPath,
                inStream,
                ImportUUIDBehavior.IMPORT_UUID_COLLISION_REPLACE_EXISTING);
        }
        catch (IOException e) {
            throw new RepositoryException("Can't replace node " + src.getHandle(), e);
        }finally {
            IOUtils.closeQuietly(outStream);
            IOUtils.closeQuietly(inStream);
            FileUtils.deleteQuietly(file);
        }
    }
    private void collectDialogNodes(Content current, List<Content> dialogNodes) throws RepositoryException {
        if(isDialogNode(current)){
            dialogNodes.add(current);
            return;
        }
        for (Content child : ContentUtil.getAllChildren(current)) {
            collectDialogNodes(child, dialogNodes);
        }
    }

    private boolean isDialogNode(Content node) throws RepositoryException{
        if(isDialogControlNode(node)){
            return false;
        }

        // if leave
        if(ContentUtil.getAllChildren(node).isEmpty()){
            return true;
        }

        // if has node datas
        if(!node.getNodeDataCollection().isEmpty()){
            return true;
        }

        // if one subnode is a control
        for (Content child : node.getChildren(ItemType.CONTENTNODE)) {
            if (isDialogControlNode(child)) {
                return true;
            }
        }
        return false;
    }

    private boolean isDialogControlNode(Content node) throws RepositoryException{
        return node.hasNodeData("controlType") || node.hasNodeData("reference");
    }

}
