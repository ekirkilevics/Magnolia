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

import info.magnolia.commands.MgnlCommand;
import info.magnolia.context.Context;
import info.magnolia.context.MgnlContext;
import info.magnolia.context.WebContextImpl;
import info.magnolia.module.workflow.WorkflowConstants;
import info.magnolia.module.workflow.WorkflowModule;
import info.magnolia.module.workflow.WorkflowUtil;
import openwfe.org.engine.workitem.AttributeUtils;
import openwfe.org.engine.workitem.LaunchItem;
import openwfe.org.engine.workitem.StringMapAttribute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import java.io.InputStream;
import java.io.Serializable;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;


public class FlowCommand extends MgnlCommand {

    /**
     * The name of the workflow to start
     */
    private String workflowName;

    private static Logger log = LoggerFactory.getLogger(FlowCommand.class);

    public boolean execute(Context ctx) {
        try {
            // Get the references
            LaunchItem li = new LaunchItem();
            prepareLaunchItem(ctx, li);
            setFlowDefinitionURL(li);
            WorkflowUtil.launchFlow(li, getWorkflowName());
        }
        catch (Exception e) {
            log.error("Launching failed", e);
            return false;
        }
        return true;
    }

    private void setFlowDefinitionURL(LaunchItem li) {
        InputStream is = null;
        try {
            WebContextImpl impl = (WebContextImpl) MgnlContext.getInstance();
            HttpServletRequest request = impl.getRequest();
            String cacheURL = WorkflowModule.getCacheURL();
            StringBuffer baseurl = new StringBuffer();
            if(cacheURL!=null)
                baseurl.append(cacheURL);
            else {
                baseurl.append("http");
                if(request.isSecure())
                    baseurl.append("s");
                baseurl.append("://");
                baseurl.append(request.getServerName());
                baseurl.append(":");
                baseurl.append(request.getServerPort());
            }
            baseurl.append(impl.getContextPath());
            baseurl.append("/.magnolia/pages/flows.html");
            baseurl.append("?command=showFlow&flowName=");
            baseurl.append(getWorkflowName());
            String surl = baseurl.toString();
            URL url = new URL(surl);
            URLConnection connection = url.openConnection();
            connection.connect();
            is = connection.getInputStream();
            li.setWorkflowDefinitionUrl(surl);
        } catch(Exception e) {
            log.info("Could not set workflow definition url:"+e.getMessage(),e);
        } finally {
            try {if(is!=null) is.close();} catch(Exception e) {/*just try to close any open stream*/}
        }
    }

    /**
     * The default implementation puts all the contexts attributes which are in the request scope into the work item.
     * @param context
     * @param launchItem
     */
    public void prepareLaunchItem(Context context, LaunchItem launchItem) {
        Map map = context.getAttributes(Context.LOCAL_SCOPE);
        // create map for workflowItem with all serializable entries from the context
        Map serializableMap = new HashMap();
        for (Iterator iter = map.keySet().iterator(); iter.hasNext();) {
            Object key = iter.next();
            Object val = map.get(key);
            if (val instanceof Serializable) {
                serializableMap.put(key, val);
            }
        }
        serializableMap.put(WorkflowConstants.ATTRIBUTE_USERNAME, context.getUser().getName());
        StringMapAttribute attrs = AttributeUtils.java2attributes(serializableMap);
        launchItem.setAttributes(attrs);
    }

    public String getWorkflowName() {
        return workflowName;
    }

    public void setWorkflowName(String flowName) {
        this.workflowName = flowName;
    }
}
