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
package info.magnolia.cms.gui.control;

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.beans.runtime.Document;
import info.magnolia.cms.beans.runtime.MultipartForm;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.ContentNode;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.core.NodeData;
import info.magnolia.cms.gui.dialog.DialogSuper;
import info.magnolia.cms.gui.misc.FileProperties;
import info.magnolia.cms.security.Digester;
import info.magnolia.cms.security.SessionAccessControl;
import info.magnolia.cms.util.Path;

import java.util.Calendar;
import java.util.GregorianCalendar;

import javax.jcr.BooleanValue;
import javax.jcr.DateValue;
import javax.jcr.DoubleValue;
import javax.jcr.LongValue;
import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.StringValue;
import javax.jcr.Value;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;


/**
 * @author Vinzenz Wyser
 * @version 2.0
 */
public class Save extends ControlSuper {

    private static Logger log = Logger.getLogger(Save.class);

    private MultipartForm form;

    private String repository = ContentRepository.WEBSITE;

    public Save() {
    }

    public Save(MultipartForm form, HttpServletRequest request) {
        this.setForm(form);
        this.setRequest(request);
        this.setPath(form.getParameter("mgnlPath"));
        this.setNodeCollectionName(form.getParameter("mgnlNodeCollection"));
        this.setNodeName(form.getParameter("mgnlNode"));
        this.setParagraph(form.getParameter("mgnlParagraph"));
        this.setRepository(form.getParameter("mgnlRepository"));
    }

    public void setForm(MultipartForm form) {
        this.form = form;
    }

    public MultipartForm getForm() {
        return this.form;
    }

    public void setRepository(String s) {
        this.repository = s;
    }

    public String getRepository() {
        return this.repository;
    }

    public void save() {
        MultipartForm form = this.getForm();
        String[] saveInfo = form.getParameterValues("mgnlSaveInfo"); // name,type,propertyOrNode
        String nodeCollectionName = this.getNodeCollectionName(null);
        String nodeName = this.getNodeName(null);
        String path = this.getPath();
        HttpServletRequest request = this.getRequest();

        HierarchyManager hm = new HierarchyManager(this.getRequest());
        try {
            Session t = SessionAccessControl.getSession(request, this.getRepository());
            Node rootNode = t.getRootNode();
            hm.init(rootNode);
        }
        catch (Exception e) {
        }
        try {
            Content page = hm.getPage(path);
            // get or create nodeCollection
            Content nodeCollection = null;
            if (nodeCollectionName != null) {
                try {
                    nodeCollection = page.getContentNode(nodeCollectionName);
                }
                catch (RepositoryException re) {
                    // nodeCollection does not exist -> create
                    nodeCollection = page.createContentNode(nodeCollectionName);
                    log.info("Create - " + nodeCollection.getHandle());
                }
            }
            else {
                nodeCollection = page;
            }
            // get or create node
            Content node = null;
            if (nodeName != null) {
                try {
                    node = nodeCollection.getContentNode(nodeName);
                }
                catch (RepositoryException re) {
                    // node does not exist -> create
                    if (nodeName.equals("mgnlNew")) {
                        nodeName = Path.getUniqueLabel(hm, nodeCollection.getHandle(), "0");
                    }
                    node = nodeCollection.createContentNode(nodeName);
                    node.createNodeData("paragraph").setValue(this.getParagraph());
                    node.getMetaData().setSequencePosition();
                }
            }
            else {
                node = nodeCollection;
            }
            // update meta data (e.g. last modified) of this paragraph and the page
            node.updateMetaData(request);
            page.updateMetaData(request);
            // loop all saveInfo controls; saveInfo format: name, type, valueType(single|multiple, )
            for (int i = 0; i < saveInfo.length; i++) {
                String name;
                int type = type = PropertyType.STRING;
                int valueType = ControlSuper.VALUETYPE_SINGLE;
                int isRichEditValue = 0;
                int encoding = ControlSuper.ENCODING_NO;
                String[] values = {""};
                if (saveInfo[i].indexOf(",") != -1) {
                    String[] info = saveInfo[i].split(",");
                    name = info[0];
                    if (info.length >= 2) {
                        type = PropertyType.valueFromName(info[1]);
                    }
                    if (info.length >= 3) {
                        valueType = Integer.valueOf(info[2]).intValue();
                    }
                    if (info.length >= 4) {
                        isRichEditValue = Integer.valueOf(info[3]).intValue();
                    }
                    if (info.length >= 5) {
                        encoding = Integer.valueOf(info[4]).intValue();
                    }
                }
                else {
                    name = saveInfo[i];
                }
                if (type == PropertyType.BINARY) {
                    Document doc = form.getDocument(name);
                    if (doc == null && form.getParameter(name + "_" + File.REMOVE) != null) {
                        try {
                            node.deleteContentNode(name + "_" + FileProperties.PROPERTIES_CONTENTNODE);
                        }
                        catch (RepositoryException re) {
                        }
                        try {
                            node.deleteNodeData(name);
                        }
                        catch (RepositoryException re) {
                        }

                    }
                    else {
                        ContentNode propNode = null;
                        try {
                            propNode = node.getContentNode(name + "_" + FileProperties.PROPERTIES_CONTENTNODE);
                        }
                        catch (RepositoryException re) {
                            try {
                                if (doc != null) {
                                    propNode = node.createContentNode(name
                                        + "_"
                                        + FileProperties.PROPERTIES_CONTENTNODE);
                                }
                            }
                            catch (RepositoryException re2) {
                            }
                        }
                        if (doc != null) {
                            NodeData data = node.getNodeData(name);
                            if (!data.isExist()) {
                                data = node.createNodeData(name);
                                log.info("creating under - " + node.getHandle());
                                log.debug("creating node data for binary store - " + name);
                            }
                            data.setValue(doc.getStream());
                            log.debug("Node data updated");
                        }
                        if (propNode != null) {
                            NodeData propData;
                            String fileName = form.getParameter(name + "_" + FileProperties.PROPERTY_FILENAME);
                            if (fileName == null || fileName.equals(StringUtils.EMPTY)) {
                                fileName = doc.getFileName();
                            }
                            propData = propNode.getNodeData(FileProperties.PROPERTY_FILENAME);
                            if (!propData.isExist()) {
                                propData = propNode.createNodeData(FileProperties.PROPERTY_FILENAME);
                            }
                            propData.setValue(fileName);
                            if (doc != null) {
                                propData = propNode.getNodeData(FileProperties.PROPERTY_CONTENTTYPE);
                                if (!propData.isExist()) {
                                    propData = propNode.createNodeData(FileProperties.PROPERTY_CONTENTTYPE);
                                }
                                propData.setValue(doc.getType());
                                propData = propNode.getNodeData(FileProperties.PROPERTY_SIZE);
                                if (!propData.isExist()) {
                                    propData = propNode.createNodeData(FileProperties.PROPERTY_SIZE);
                                }
                                propData.setValue(doc.getLength());
                                propData = propNode.getNodeData(FileProperties.PROPERTY_EXTENSION);
                                if (!propData.isExist()) {
                                    propData = propNode.createNodeData(FileProperties.PROPERTY_EXTENSION);
                                }
                                propData.setValue(doc.getExtension());
                                String template = form.getParameter(name + "_" + FileProperties.PROPERTY_TEMPLATE);
                                if (StringUtils.isNotEmpty(template)) {
                                    propData = propNode.getNodeData(FileProperties.PROPERTY_TEMPLATE);
                                    if (!propData.isExist()) {
                                        propData = propNode.createNodeData(FileProperties.PROPERTY_TEMPLATE);
                                    }
                                    propData.setValue(template);
                                }
                                else {
                                    try {
                                        propNode.deleteNodeData(FileProperties.PROPERTY_TEMPLATE);
                                    }
                                    catch (PathNotFoundException e) {
                                    }
                                }
                                doc.delete();
                            }
                        }
                    }
                }
                else {
                    values = form.getParameterValues(name);
                    if (valueType == ControlSuper.VALUETYPE_MULTIPLE) {
                        // remove entire content node and (re-)write each
                        try {
                            node.deleteContentNode(name);
                        }
                        catch (PathNotFoundException e) {
                        }
                        if (values != null && values.length != 0) {
                            ContentNode multiNode = node.createContentNode(name);
                            try {
                                // MetaData.CREATION_DATE has private access; no method to delete it so far...
                                multiNode.deleteNodeData("creationdate");
                            }
                            catch (RepositoryException re) {
                            }
                            for (int ii = 0; ii < values.length; ii++) {
                                String valueStr = values[ii];
                                Value value = this.getValue(valueStr, type);
                                multiNode.createNodeData("" + ii).setValue(value);
                            }
                        }
                    }
                    else {
                        String valueStr = "";
                        if (values != null) {
                            valueStr = values[0]; // values is null when the expected field would not exis, e.g no
                        }
                        // checkbox selected
                        NodeData data = node.getNodeData(name);
                        if (isRichEditValue == 1) {
                            valueStr = this.getRichEditValueStr(valueStr);
                        }
                        // actualy encoding does only work for control password
                        boolean remove = false;
                        boolean write = false;
                        if (encoding == ControlSuper.ENCODING_BASE64) {
                            if (!valueStr.replaceAll(" ", "").equals("")) {
                                valueStr = new String(Base64.encodeBase64(valueStr.getBytes()));
                                write = true;
                            }
                        }
                        else if (encoding == ControlSuper.ENCODING_UNIX) {
                            if (!valueStr.equals("")) {
                                valueStr = Digester.getSHA1Hex(valueStr);
                                write = true;
                            }
                        }
                        else {
                            // no encoding
                            if (values == null || valueStr.equals("")) {
                                remove = true;
                            }
                            else {
                                write = true;
                            }
                        }
                        if (remove) {
                            // remove node if already exists
                            if (data.isExist()) {
                                node.deleteNodeData(name);
                            }
                        }
                        else if (write) {
                            Value value = this.getValue(valueStr, type);
                            if (value != null) {
                                if (data.isExist()) {
                                    data.setValue(value);
                                }
                                else {
                                    node.createNodeData(name, value, type);
                                }
                            }
                        }
                    }
                }
            }
            log.info("Saving - " + path);
            hm.save();
        }
        catch (RepositoryException re) {
            log.error(re.getMessage(), re);
        }
        this.removeSessionAttributes();
    }

    public void removeSessionAttributes() {
        HttpSession session = this.getRequest().getSession();
        MultipartForm form = this.getForm();
        String[] toRemove = form.getParameterValues(DialogSuper.SESSION_ATTRIBUTENAME_DIALOGOBJECT_REMOVE);
        if (toRemove != null) {
            for (int i = 0; i < toRemove.length; i++) {
                session.removeAttribute(toRemove[i]);
                // log.debug("removed: "+toRemove[i]);
            }
        }
    }

    public Value getValue(String s) {
        return this.getValue(s, PropertyType.STRING);
    }

    public Value getValue(long l) {
        return new LongValue(l);
    }

    public Value getValue(String valueStr, int type) {
        Value value = null;
        if (type == PropertyType.STRING) {
            value = new StringValue(valueStr);
        }
        else if (type == PropertyType.BOOLEAN) {
            if (valueStr.equals("true")) {
                value = new BooleanValue(true);
            }
            else {
                value = new BooleanValue(false);
            }
        }
        else if (type == PropertyType.DOUBLE) {
            try {
                double d = (new Double(valueStr)).doubleValue();
                value = new DoubleValue(d);
            }
            catch (Exception e) {
                value = new DoubleValue(0);
            }
        }
        else if (type == PropertyType.LONG) {
            try {
                long l = (new Long(valueStr)).longValue();
                value = new LongValue(l);
            }
            catch (Exception e) {
                value = new LongValue(0);
            }
        }
        else if (type == PropertyType.DATE) {
            try {
                Calendar date = new GregorianCalendar();
                try {
                    String newDateAndTime = valueStr;
                    String[] dateAndTimeTokens = newDateAndTime.split("T");
                    String newDate = dateAndTimeTokens[0];
                    String[] dateTokens = newDate.split("-");
                    int hour = 0;
                    int minute = 0;
                    int second = 0;
                    int year = Integer.parseInt(dateTokens[0]);
                    int month = Integer.parseInt(dateTokens[1]) - 1;
                    int day = Integer.parseInt(dateTokens[2]);
                    if (dateAndTimeTokens.length > 1) {
                        String newTime = dateAndTimeTokens[1];
                        String[] timeTokens = newTime.split(":");
                        hour = Integer.parseInt(timeTokens[0]);
                        minute = Integer.parseInt(timeTokens[1]);
                        second = Integer.parseInt(timeTokens[2]);
                    }
                    date.set(year, month, day, hour, minute, second);
                }
                // todo time zone??
                catch (Exception e) {
                    // ignore, it sets the current date / time
                }
                value = new DateValue(date);
            }
            catch (Exception e) {
            }
        }
        return value;
    }

    public String getRichEditValueStr(String valueStr) {
        valueStr = valueStr.replaceAll("\r\n", " ");
        // valueStr=valueStr.replaceAll("\n","");
        valueStr = valueStr.replaceAll("<br>", "\n");
        valueStr = valueStr.replaceAll("<BR>", "\n");
        // replace <a class="...></a> by <span class=""></span>
        valueStr = this.replaceABySpan(valueStr, "a");
        // replace <P>
        valueStr = this.replacePByBr(valueStr, "p");
        // replace <STRONG>, <EM> (ie)
        valueStr = this.replaceTag(valueStr, "em", "i");
        valueStr = this.replaceTag(valueStr, "strong", "b");
        return valueStr;
    }

    private String replaceABySpan(String value, String tagName) {
        if (value == null || value.equals("")) {
            return value;
        }
        String valueStart = value.substring(0, 1);
        String[] strObj = value.split("<" + tagName);
        StringBuffer valueStr = new StringBuffer();
        int i = 0;
        while (i < strObj.length) {
            String str = strObj[i];
            String tagPre = "";
            if (i != 0 || valueStart == "<" + tagName) {
                String openTag = str.substring(0, str.indexOf(">"));
                if (openTag.indexOf(" href=") == -1) {
                    str = str.replaceAll("</" + tagName + ">", "</span>");
                    tagPre = "<span";
                }
                else {
                    tagPre = "<" + tagName;
                }
            }
            valueStr.append(tagPre + str);
            i++;
        }
        String valueStr2 = valueStr.toString();
        if (!tagName.equals(tagName.toUpperCase())) {
            valueStr2 = this.replaceABySpan(valueStr2, tagName.toUpperCase());
        }
        return valueStr2;
    }

    private String replacePByBr(String value, String tagName) {
        String pre = "<" + tagName + ">";
        String post = "</" + tagName + ">";
        // get rid of last </p>
        if (value.lastIndexOf(post) == value.length() - post.length()) {
            value = value.substring(0, value.lastIndexOf(post));
        }
        value = value.replaceAll(pre + "&nbsp;" + post, "\n");
        value = value.replaceAll(pre, "");
        value = value.replaceAll(post, "\n\n");
        if (!tagName.equals(tagName.toUpperCase())) {
            value = this.replacePByBr(value, tagName.toUpperCase());
        }
        return value;
    }

    private String replaceTag(String value, String before, String after) {
        value = value.replaceAll("<" + before + ">", "<" + after + ">");
        value = value.replaceAll("</" + before + ">", "</" + after + ">");
        if (!before.equals(before.toUpperCase())) {
            value = this.replaceTag(value, before.toUpperCase(), after);
        }
        return value;
    }
}
