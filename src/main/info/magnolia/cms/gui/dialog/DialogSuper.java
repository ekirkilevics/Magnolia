/**
 *
 * Magnolia and its source-code is licensed under the LGPL.
 * You may copy, adapt, and redistribute this file for commercial or non-commercial use.
 * When copying, adapting, or redistributing this document in keeping with the guidelines above,
 * you are required to provide proper attribution to obinary.
 * If you reproduce or distribute the document without making any substantive modifications to its content,
 * please use the following attribution line:
 *
 * Copyright 1993-2005 obinary Ltd. (http://www.obinary.com) All rights reserved.
 *
 */
package info.magnolia.cms.gui.dialog;

import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.ContentHandler;
import info.magnolia.cms.core.ItemType;
import info.magnolia.cms.core.NodeData;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.jcr.RepositoryException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;


/**
 * @author Vinzenz Wyser
 * @version 2.0
 */
public abstract class DialogSuper implements DialogInterface {

    public static final String SESSION_ATTRIBUTENAME_DIALOGOBJECT = "mgnlSessionAttribute";

    public static final String SESSION_ATTRIBUTENAME_DIALOGOBJECT_REMOVE = "mgnlSessionAttributeRemove";

    /**
     * Logger.
     */
    private static Logger log = Logger.getLogger(DialogSuper.class);

    /**
     * Current request.
     */
    private HttpServletRequest request;

    /**
     * Current response.
     */
    private HttpServletResponse response;

    /**
     * content data.
     */
    private Content websiteNode;

    /**
     * config data.
     */
    private Map config = new Hashtable();

    /**
     * Sub controls.
     */
    private List subs = new ArrayList();

    /**
     * options (radio, checkbox...).
     */
    private List options = new ArrayList();

    private String id = "mgnlControl";

    private String value;

    /**
     * multiple values, e.g. checkbox.
     */
    private List values = new ArrayList();

    private DialogSuper parent;

    private DialogSuper topParent;

    /**
     */
    public void init(HttpServletRequest request, HttpServletResponse response, Content websiteNode, Content configNode)
        throws RepositoryException {

        if (log.isDebugEnabled()) {
            log.debug("Init " + getClass().getName());
        }

        this.websiteNode = websiteNode;
        this.request = request;
        this.response = response;

        this.initializeConfig(configNode);
    }

    /**
     * @see info.magnolia.cms.gui.dialog.DialogInterface#drawHtml(Writer)
     */
    public void drawHtml(Writer out) throws IOException {
        this.drawHtmlPreSubs(out);
        this.drawSubs(out);
        this.drawHtmlPostSubs(out);
    }

    public void addSub(Object o) {
        this.getSubs().add(o);
    }

    public void setConfig(String key, String value) {
        if (value != null) {
            this.config.put(key, value);
        }
    }

    public void setConfig(String key, boolean value) {
        this.config.put(key, BooleanUtils.toBooleanObject(value).toString());
    }

    public void setConfig(String key, int value) {
        this.config.put(key, Integer.toString(value));
    }

    public String getConfigValue(String key, String nullValue) {
        if (this.config.containsKey(key)) {
            return (String) this.config.get(key);
        }

        return nullValue;
    }

    public String getConfigValue(String key) {
        return this.getConfigValue(key, StringUtils.EMPTY);
    }

    public void setValue(String s) {
        this.value = s;
    }

    public String getValue() {
        if (this.value != null) {
            return this.value;
        }
        else if (this.getWebsiteNode() != null) {
            return this.getWebsiteNode().getNodeData(this.getName()).getString();
        }
        else {
            return StringUtils.EMPTY;
        }
    }

    public void setSaveInfo(boolean b) {
        this.setConfig("saveInfo", b);
    }

    public void setName(String s) {
        this.setConfig("name", s);
    }

    public String getName() {
        return this.getConfigValue("name");
    }

    public void addOption(Object o) {
        this.getOptions().add(o);
    }

    public Content getWebsiteNode() {
        return this.websiteNode;
    }

    public void setLabel(String s) {
        this.config.put("label", s);
    }

    public void setDescription(String s) {
        this.config.put("description", s);
    }

    public void removeSessionAttribute() {
        String name = this.getConfigValue(SESSION_ATTRIBUTENAME_DIALOGOBJECT);
        HttpServletRequest request = this.getRequest();
        if (request == null) {
            request = this.getTopParent().getRequest();
        }
        try {
            HttpSession session = request.getSession();
            session.removeAttribute(name);
        }
        catch (Exception e) {
            log.debug("removeSessionAttribute() for " + name + " failed because this.request is null");
        }
    }

    public HttpServletRequest getRequest() {
        return this.request;
    }

    public void setOptions(List options) {
        this.options = options;
    }

    protected void drawHtmlPreSubs(Writer out) throws IOException {
        // do nothing
    }

    protected void drawSubs(Writer out) throws IOException {
        Iterator it = this.getSubs().iterator();
        int i = 0;
        while (it.hasNext()) {
            String dsId = this.getId() + "_" + i; // use underscore (not divis)! could be used as js variable names
            DialogSuper ds = (DialogSuper) it.next();
            ds.setId(dsId);
            ds.setParent(this);
            if (this.getParent() == null) {
                this.setTopParent(this);
            }
            ds.setTopParent(this.getTopParent());
            ds.drawHtml(out);
            i++;
        }
    }

    protected void drawHtmlPostSubs(Writer out) throws IOException {
        // do nothing
    }

    protected DialogSuper getParent() {
        return this.parent;
    }

    protected void setTopParent(DialogSuper top) {
        this.topParent = top;
    }

    protected DialogSuper getTopParent() {
        return this.topParent;
    }

    protected List getSubs() {
        return this.subs;
    }

    protected HttpServletResponse getResponse() {
        return this.response;
    }

    /**
     * @deprecated websitenode should only be set in init(), this is a workaround used in DialogDate
     */
    protected void clearWebsiteNode() {
        this.websiteNode = null;
    }

    protected String getId() {
        return this.id;
    }

    protected String getLabel() {
        return this.getConfigValue("label", StringUtils.EMPTY);
    }

    protected String getDescription() {
        return this.getConfigValue("description", "");
    }

    protected List getOptions() {
        return this.options;
    }

    protected List getValues() {
        if (this.getWebsiteNode() == null) {
            return this.values;
        }

        try {
            Iterator it = this.getWebsiteNode().getContent(this.getName()).getNodeDataCollection().iterator();
            List l = new ArrayList();
            while (it.hasNext()) {
                NodeData data = (NodeData) it.next();
                l.add(data.getString());
            }
            return l;
        }
        catch (RepositoryException re) {
            return this.values;
        }

    }

    protected void setSessionAttribute() {
        String name = SESSION_ATTRIBUTENAME_DIALOGOBJECT + "_" + this.getName() + "_" + new Date().getTime();
        this.setConfig(SESSION_ATTRIBUTENAME_DIALOGOBJECT, name);
        HttpServletRequest request = this.getRequest();
        if (request == null) {
            request = this.getTopParent().getRequest();
        }
        try {
            HttpSession session = request.getSession();
            session.setAttribute(name, this);
        }
        catch (Exception e) {
            log.error("setSessionAttribute() for " + name + " failed because this.request is null");
        }
    }

    private void setId(String id) {
        this.id = id;
    }

    private void initializeConfig(Content configNodeParent) throws RepositoryException {
        // create config and subs out of dialog structure
        Map config = new Hashtable();

        if (configNodeParent == null) {
            // can happen only if Dialog is instantiated directly
            return;
        }

        // get properties -> to this.config
        Iterator itProps = configNodeParent.getNodeDataCollection().iterator();
        while (itProps.hasNext()) {
            NodeData data = (NodeData) itProps.next();
            String name = data.getName();
            String value = data.getString();
            config.put(name, value);
        }
        this.config = config;

        Iterator it = configNodeParent.getChildren(ItemType.CONTENTNODE, ContentHandler.SORT_BY_SEQUENCE).iterator();
        while (it.hasNext()) {
            Content configNode = (Content) it.next();
            String controlType = configNode.getNodeData("controlType").getString();

            if (StringUtils.isEmpty(controlType)) {
                String name = configNode.getName();
                if (!name.startsWith("options")) {
                    log.warn("Missing control type for configNode " + name);
                }
                return;
            }

            if (log.isDebugEnabled()) {
                log.debug("Loading control \"" + controlType + "\" for " + configNode.getHandle());
            }
            DialogInterface dialogControl = DialogFactory.loadDialog(
                request,
                response,
                this.getWebsiteNode(),
                configNode);
            this.addSub(dialogControl);
        }
    }

    private void setParent(DialogSuper parent) {
        this.parent = parent;
    }

}
