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
package info.magnolia.cms.gui.dialog;

import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.ItemType;
import info.magnolia.cms.core.NodeData;
import info.magnolia.cms.i18n.Messages;
import info.magnolia.cms.i18n.MessagesManager;
import info.magnolia.cms.i18n.TemplateMessagesUtil;
import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.RepositoryException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.Writer;
import java.util.*;


/**
 * @author Vinzenz Wyser
 * @version 2.0
 */
public abstract class DialogSuper implements DialogInterface {

    private static final String I18N_BASENAME_PROPERTY = "i18nBasename";

    public static final String SESSION_ATTRIBUTENAME_DIALOGOBJECT = "mgnlSessionAttribute"; //$NON-NLS-1$

    public static final String SESSION_ATTRIBUTENAME_DIALOGOBJECT_REMOVE = "mgnlSessionAttributeRemove"; //$NON-NLS-1$

    /**
     * Logger.
     */
    private static Logger log = LoggerFactory.getLogger(DialogSuper.class);

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

    /**
     * The id is used to make the controls unic. Used by the javascripts. This is not a configurable value. See method
     * set and getName().
     */
    private String id = "mgnlControl"; //$NON-NLS-1$

    private String value;

    /**
     * multiple values, e.g. checkbox.
     */
    private List values = new ArrayList();

    private DialogSuper parent;

    private DialogSuper topParent;

    /**
     * Used if this control has its own message bundle defined or if this is the dialog object itself. Use getMessages
     * method to get the object for a control.
     */
    private Messages messages;

    /**
     */
    public void init(HttpServletRequest request, HttpServletResponse response, Content websiteNode, Content configNode)
        throws RepositoryException {

        if (log.isDebugEnabled()) {
            log.debug("Init " + getClass().getName()); //$NON-NLS-1$
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
        this.setConfig("saveInfo", b); //$NON-NLS-1$
    }

    /**
     * Set the name of this control. This is not the same value as the id setted by the parent. In common this value is
     * setted in the dialog configuration.
     * @param the name
     */
    public void setName(String s) {
        this.setConfig("name", s); //$NON-NLS-1$
    }

    /**
     * Return the configured name of this control (not the id).
     * @return the name
     */
    public String getName() {
        return this.getConfigValue("name"); //$NON-NLS-1$
    }

    public void addOption(Object o) {
        this.getOptions().add(o);
    }

    public Content getWebsiteNode() {
        return this.websiteNode;
    }

    public void setLabel(String s) {
        this.config.put("label", s); //$NON-NLS-1$
    }

    public void setDescription(String s) {
        this.config.put("description", s); //$NON-NLS-1$
    }

    public void removeSessionAttribute() {
        String name = this.getConfigValue(SESSION_ATTRIBUTENAME_DIALOGOBJECT);
        HttpServletRequest request = this.getRequest();
        if (request == null) {
            request = this.getTopParent().getRequest();
        }
        try {
            HttpSession httpsession = request.getSession(false);
            if (httpsession != null) {
                httpsession.removeAttribute(name);
            }
        }
        catch (Exception e) {
            if (log.isDebugEnabled())
                log.debug("removeSessionAttribute() for " + name + " failed because this.request is null"); //$NON-NLS-1$ //$NON-NLS-2$
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
            // use underscore (not divis)! could be used as js variable names
            String dsId = this.getId() + "_" + i; //$NON-NLS-1$

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

    public DialogSuper getParent() {
        return this.parent;
    }

    protected void setTopParent(DialogSuper top) {
        this.topParent = top;
    }

    public DialogSuper getTopParent() {
        return this.topParent;
    }

    public List getSubs() {
        return this.subs;
    }

    /**
     * Find a control by its name
     * @param name the name of the control to find
     * @return the found control or null
     */
    public DialogSuper getSub(String name) {
        for (Iterator iter = subs.iterator(); iter.hasNext();) {
            Object control = iter.next();

            // could be an implementation of DialogInterface only
            if (control instanceof DialogSuper) {
                if (StringUtils.equals(((DialogSuper) control).getName(), name)) {
                    return (DialogSuper) control;
                }
            }
        }
        return null;
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

    public String getId() {
        return this.id;
    }

    public String getLabel() {
        return this.getConfigValue("label", StringUtils.EMPTY); //$NON-NLS-1$
    }

    public String getDescription() {
        return this.getConfigValue("description", StringUtils.EMPTY); //$NON-NLS-1$
    }

    public List getOptions() {
        return this.options;
    }

    public List getValues() {
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

    /**
     * This method sets a control into the session
     */
    public void setSessionAttribute() {
        String name = SESSION_ATTRIBUTENAME_DIALOGOBJECT + "_" + this.getName() + "_" + new Date().getTime(); //$NON-NLS-1$ //$NON-NLS-2$
        this.setConfig(SESSION_ATTRIBUTENAME_DIALOGOBJECT, name);
        HttpServletRequest request = this.getRequest();
        if (request == null) {
            request = this.getTopParent().getRequest();
        }
        try {

            // @todo IMPORTANT remove use of http session
            HttpSession httpsession = request.getSession(true);
            httpsession.setAttribute(name, this);
        }
        catch (Exception e) {
            log.error("setSessionAttribute() for " + name + " failed because this.request is null"); //$NON-NLS-1$ //$NON-NLS-2$
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

        // name is usually mandatory, use node name if a name property is not set
        if (!config.containsKey("name")) {
            config.put("name", configNodeParent.getName());
        }

        this.config = config;

        Iterator it = configNodeParent.getChildren(ItemType.CONTENTNODE).iterator();
        while (it.hasNext()) {
            Content configNode = (Content) it.next();
            String controlType = configNode.getNodeData("controlType").getString(); //$NON-NLS-1$

            if (StringUtils.isEmpty(controlType)) {
                String name = configNode.getName();
                if (!name.startsWith("options")) { //$NON-NLS-1$
                    log.warn("Missing control type for configNode " + name); //$NON-NLS-1$
                }
                return;
            }

            if (log.isDebugEnabled()) {
                log.debug("Loading control \"" + controlType + "\" for " + configNode.getHandle()); //$NON-NLS-1$ //$NON-NLS-2$
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

    /**
     * Get the Messages object for this dialog/control. It checks first if there was a bundle defined
     * <code>i18nBasename</code>, then it tries to find the parent with the first definition.
     * @return
     */
    protected Messages getMessages() {
        if (messages == null) {
            // if this is the root
            if (this.getParent() == null) {
                messages = TemplateMessagesUtil.getMessages();
            }
            else {
                // try to get it from the control nearest to the root
                messages = this.getParent().getMessages();
            }
            // if this control defines a bundle (basename in the terms of jstl)
            String basename = this.getConfigValue(I18N_BASENAME_PROPERTY);
            if (StringUtils.isNotEmpty(basename)) {
                // extend the chain with this bundle
                Messages myMessages = MessagesManager.getMessages(basename);
                myMessages.setFallBackMessages(messages);
                messages = myMessages;
            }
        }
        return messages;
    }

    /**
     * Get the message.
     * @param key key
     * @return message
     */
    public String getMessage(String key) {
        return this.getMessages().getWithDefault(key, key);
    }

    /**
     * Get the message with replacement strings. Use the {nr} syntax
     * @param key key
     * @param args replacement strings
     * @return message
     */
    public String getMessage(String key, Object[] args) {
        return this.getMessages().getWithDefault(key, args, key);
    }

}
