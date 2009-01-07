/**
 * This file Copyright (c) 2003-2009 Magnolia International
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
package info.magnolia.cms.gui.dialog;

import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.DefaultNodeData;
import info.magnolia.cms.core.ItemType;
import info.magnolia.cms.core.NodeData;
import info.magnolia.cms.i18n.Messages;
import info.magnolia.cms.i18n.MessagesUtil;
import info.magnolia.cms.i18n.MessagesManager;
import info.magnolia.cms.util.AlertUtil;
import info.magnolia.cms.util.ContentUtil;
import info.magnolia.cms.util.NodeDataUtil;
import info.magnolia.cms.util.RequestFormUtil;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Vinzenz Wyser
 * @version 2.0
 */
public abstract class DialogControlImpl implements DialogControl {

    private static final String REQUIRED_PROPERTY = "required";

    public static final String VALIDATION_PATTERN_PROPERTY = "validationPattern";

    public static final String DEFAULT_VALUE_PROPERTY = "defaultValue";

    private static final String I18N_BASENAME_PROPERTY = "i18nBasename";

    public static final String SESSION_ATTRIBUTENAME_DIALOGOBJECT = "mgnlSessionAttribute"; //$NON-NLS-1$

    public static final String SESSION_ATTRIBUTENAME_DIALOGOBJECT_REMOVE = "mgnlSessionAttributeRemove"; //$NON-NLS-1$

    /**
     * Logger.
     */
    private static Logger log = LoggerFactory.getLogger(DialogControlImpl.class);

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
    private Content storageNode;

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

    protected String value;

    /**
     * multiple values, e.g. checkbox.
     */
    private List values;

    private DialogControlImpl parent;

    private DialogControlImpl topParent;

    /**
     * Used if this control has its own message bundle defined or if this is the dialog object itself. Use getMessages
     * method to get the object for a control.
     */
    private Messages messages;

    /**
     */
    public void init(HttpServletRequest request, HttpServletResponse response, Content storageNode, Content configNode)
        throws RepositoryException {

        if (log.isDebugEnabled()) {
            log.debug("Init " + getClass().getName()); //$NON-NLS-1$
        }

        this.storageNode = storageNode;
        this.request = request;
        this.response = response;

        this.initializeConfig(configNode);
    }

    /**
     * @see info.magnolia.cms.gui.dialog.DialogControl#drawHtml(Writer)
     */
    public void drawHtml(Writer out) throws IOException {
        this.drawHtmlPreSubs(out);
        this.drawSubs(out);
        this.drawHtmlPostSubs(out);
    }

    public void addSub(Object o) {
        this.getSubs().add(o);
        if(o instanceof DialogControlImpl){
            ((DialogControlImpl)o).setParent(this);
        }
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
        if (this.value == null) {
            if (this.getStorageNode() != null) {
                this.value = readValue();
                if(this instanceof UUIDDialogControl){
                    String repository = ((UUIDDialogControl)this).getRepository();
                    this.value = ContentUtil.uuid2path(repository, this.value);
                }
            }
            RequestFormUtil params = new RequestFormUtil(request);
            if (params.getParameter(this.getName()) != null) {
                this.value = params.getParameter(this.getName());
            }

            if (this.value == null && StringUtils.isNotEmpty(getConfigValue(DEFAULT_VALUE_PROPERTY))) {
                return this.getMessage(this.getConfigValue(DEFAULT_VALUE_PROPERTY));
            }

            if (this.value == null) {
                this.value = StringUtils.EMPTY;
            }
        }
        return this.value;
    }

    protected String readValue() {
        try {
            if(!this.getStorageNode().hasNodeData(this.getName())){
                return null;
            }
        }
        catch (RepositoryException e) {
            log.error("can't read nodedata [" + this.getName() + "]", e);
            return null;
        }
        return this.getStorageNode().getNodeData(this.getName()).getString();
    }

    public void setSaveInfo(boolean b) {
        this.setConfig("saveInfo", b); //$NON-NLS-1$
    }

    /**
     * Set the name of this control. This is not the same value as the id setted by the parent. In common this value is
     * setted in the dialog configuration.
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

    public Content getStorageNode() {
        return this.storageNode;
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
            if (log.isDebugEnabled()) {
                log.debug("removeSessionAttribute() for " + name + " failed because this.request is null"); //$NON-NLS-1$ //$NON-NLS-2$
            }
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
            DialogControlImpl ds = (DialogControlImpl) it.next();
            // set the parent in case not yet set. this is the case when custom code manipulates the subs list manually
            ds.setParent(this);
            ds.setId(dsId);
            ds.drawHtml(out);
            i++;
        }
    }

    protected void drawHtmlPostSubs(Writer out) throws IOException {
        // do nothing
    }

    public DialogControlImpl getParent() {
        return this.parent;
    }

    protected void setTopParent(DialogControlImpl top) {
        this.topParent = top;
    }

    public DialogControlImpl getTopParent() {
        DialogControlImpl topParent = this;
        if(this.topParent ==  null){
            while(topParent.getParent() != null){
                topParent = topParent.getParent();
            }
            this.topParent = topParent;
        }
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
    public DialogControlImpl getSub(String name) {
        DialogControlImpl found;
        for (Iterator iter = subs.iterator(); iter.hasNext();) {
            Object control = iter.next();

            // could be an implementation of DialogControl only
            if (control instanceof DialogControlImpl) {
                if (StringUtils.equals(((DialogControlImpl) control).getName(), name)) {
                    return (DialogControlImpl) control;
                }
                found = ((DialogControlImpl) control).getSub(name);
                if (found != null) {
                    return found;
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
        this.storageNode = null;
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
        if (this.values == null) {
            this.values = readValues();

            if(this instanceof UUIDDialogControl){
                String repository = ((UUIDDialogControl)this).getRepository();
                List pathes = new ArrayList();
                for (Iterator iter = this.values.iterator(); iter.hasNext();) {
                    String uuid = (String) iter.next();
                    String path = ContentUtil.uuid2path(repository, uuid);
                    pathes.add(path);
                }
                this.values = pathes;
            }

            if (request != null) {
                RequestFormUtil params = new RequestFormUtil(request);
                String[] values = params.getParameterValues(this.getName());
                if (values != null && values.length > 0) {
                    this.values.clear();
                    for (int i = 0; i < values.length; i++) {
                        String value = values[i];
                        this.values.add(value);
                    }
                }
            }
        }

        return this.values;
    }

    protected List readValues() {
        List values = new ArrayList();
        if (this.getStorageNode() != null) {
            try {
                NodeData node = this.getStorageNode().getNodeData(this.getName());
                if(node.isMultiValue() == DefaultNodeData.MULTIVALUE_TRUE) {
                    values = NodeDataUtil.getValuesStringList(node.getValues());
                } else {
                    Iterator it = this.getStorageNode().getContent(this.getName()).getNodeDataCollection().iterator();
                    while (it.hasNext()) {
                        NodeData data = (NodeData) it.next();
                        values.add(data.getString());
                    }
                }
            }
            catch (PathNotFoundException e) {
                // not yet existing: OK
            }
            catch (RepositoryException re) {
                log.error("can't set values", re);
            }
        }
        return values;
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

        config.put("handle", configNodeParent.getHandle());

        // name is usually mandatory, use node name if a name property is not set
        if (!config.containsKey("name")) {
            config.put("name", configNodeParent.getName());
        }

        this.config = config;

        Iterator it = configNodeParent.getChildren(ItemType.CONTENTNODE).iterator();
        while (it.hasNext()) {
            Content configNode = (Content) it.next();

            // allow references
            while(configNode.hasNodeData("reference")){
                configNode = configNode.getNodeData("reference").getReferencedContent();
            }

            String controlType = configNode.getNodeData("controlType").getString(); //$NON-NLS-1$

            if (StringUtils.isEmpty(controlType)) {
                String name = configNode.getName();
                if (!name.startsWith("options")) { //$NON-NLS-1$
                    log.debug("Missing control type for configNode " + name); //$NON-NLS-1$
                }
                return;
            }

            if (log.isDebugEnabled()) {
                log.debug("Loading control \"" + controlType + "\" for " + configNode.getHandle()); //$NON-NLS-1$ //$NON-NLS-2$
            }
            DialogControl dialogControl = DialogFactory
                .loadDialog(request, response, this.getStorageNode(), configNode);
            this.addSub(dialogControl);
        }
    }

    private void setParent(DialogControlImpl parent) {
        this.parent = parent;
    }

    /**
     * Get the AbstractMessagesImpl object for this dialog/control. It checks first if there was a bundle defined
     * <code>i18nBasename</code>, then it tries to find the parent with the first definition.
     * @return
     */
    protected Messages getMessages() {
        if (messages == null) {
            // if this is the root
            if (this.getParent() == null) {
                messages = MessagesManager.getMessages();
            }
            else {
                // try to get it from the control nearest to the root
                messages = this.getParent().getMessages();
            }
            // if this control defines a bundle (basename in the terms of jstl)
            String basename = this.getConfigValue(I18N_BASENAME_PROPERTY);
            if (StringUtils.isNotEmpty(basename)) {
                // extend the chain with this bundle
                messages = MessagesUtil.chain(basename, messages);
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

    /**
     * If the validation fails the code will set a message in the context using the AlertUtil.
     * @return true if valid
     */
    public boolean validate() {

        if (this.isRequired()) {
            boolean valueFound = false;
            for (Iterator iter = this.getValues().iterator(); iter.hasNext();) {
                String value = (String) iter.next();
                if(!StringUtils.isEmpty(value)){
                    valueFound = true;
                    break;
                }
            }
            if (!valueFound && StringUtils.isEmpty(this.getValue())) {
                setValidationMessage("dialogs.validation.required");
                return false;
            }
        }
        if(StringUtils.isNotEmpty(getValidationPattern()) && StringUtils.isNotEmpty(this.getValue())){
            if(!Pattern.matches(getValidationPattern(), this.getValue())){
                setValidationMessage("dialogs.validation.invalid");
                return false;
            }
        }
        for (Iterator iter = this.getSubs().iterator(); iter.hasNext();) {
            DialogControl sub = (DialogControl) iter.next();
            if (sub instanceof DialogControlImpl) {
                DialogControlImpl subImpl = (DialogControlImpl) sub;
                subImpl.setParent(this);
                if (!subImpl.validate()) {
                    return false;
                }
            }

        }
        return true;
    }

    protected void setValidationMessage(String msg) {
        String name = this.getMessage(this.getLabel());
        String tabName = "";
        if (this.getParent() instanceof DialogTab) {
            DialogTab tab = (DialogTab) this.getParent();
            tabName = tab.getMessage(tab.getLabel());
        }
        AlertUtil.setMessage(this.getMessage(msg, new Object[]{name, tabName, this.getValue()}));
    }

    public String getValidationPattern() {
        return this.getConfigValue(VALIDATION_PATTERN_PROPERTY);
    }

    /**
     * True if a value is required. Set it in the configuration
     * @return
     */
    public boolean isRequired() {
        if (BooleanUtils.toBoolean(this.getConfigValue(REQUIRED_PROPERTY))) {
            return true;
        }
        return false;
    }

    public void setRequired(boolean required) {
        this.setConfig(REQUIRED_PROPERTY, BooleanUtils.toStringTrueFalse(required));
    }

    /**
     * @deprecated use getStorageNode()
     */
    public Content getWebsiteNode() {
        return getStorageNode();
    }

}
