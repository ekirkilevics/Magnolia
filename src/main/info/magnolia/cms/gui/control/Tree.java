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
import info.magnolia.cms.beans.config.Template;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.ContentHandler;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.core.ItemType;
import info.magnolia.cms.core.MetaData;
import info.magnolia.cms.core.NodeData;
import info.magnolia.cms.core.Path;
import info.magnolia.cms.exchange.simple.Syndicator;
import info.magnolia.cms.gui.misc.Spacer;
import info.magnolia.cms.security.Authenticator;
import info.magnolia.cms.security.SessionAccessControl;
import info.magnolia.cms.util.MetaDataUtil;
import info.magnolia.cms.util.NodeDataUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.Value;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;


/**
 * @author Vinzenz Wyser
 * @version 2.0
 */
public class Tree extends ControlSuper {

    public static final String DOCROOT = "/admindocroot/controls/tree/"; //$NON-NLS-1$

    // todo: global; where?
    public static final String ICONDOCROOT = "/admindocroot/icons/16/"; //$NON-NLS-1$

    public static final int ACTION_MOVE = 0;

    public static final int ACTION_COPY = 1;

    public static final int ACTION_ACTIVATE = 2;

    public static final int ACTION_DEACTIVATE = 3;

    public static final int PASTETYPE_ABOVE = 0;

    public static final int PASTETYPE_BELOW = 1;

    public static final int PASTETYPE_SUB = 2;

    public static final int PASTETYPE_LAST = 3;

    /**
     * Logger.
     */
    private static Logger log = Logger.getLogger(Tree.class);

    private String repository;

    private String pathOpen;

    private String pathCurrent;

    private String pathSelected;

    private int indentionWidth = 15;

    private List itemTypes = new ArrayList();

    private int height = 400;

    private String iconPage = ICONDOCROOT + "document_plain_earth.gif"; //$NON-NLS-1$

    private String iconContentNode = ICONDOCROOT + "cubes.gif"; //$NON-NLS-1$

    private String iconNodeData = ICONDOCROOT + "cube_green.gif"; //$NON-NLS-1$

    private String iconOndblclick;

    private String shifterExpand = DOCROOT + "shifter_EXPAND.gif"; //$NON-NLS-1$

    private String shifterCollaspe = DOCROOT + "shifter_COLLAPSE.gif"; //$NON-NLS-1$

    private String shifterEmpty = DOCROOT + "shifter_EMPTY.gif"; //$NON-NLS-1$

    private boolean drawShifter = true;

    private String javascriptTree = "mgnlTreeControl"; //$NON-NLS-1$

    private List columns = new ArrayList();

    // private List menuItems = new ArrayList();
    private ContextMenu menu;

    private boolean snippetMode = true;

    private String columnResizer = DOCROOT + "columnResizer.gif"; //$NON-NLS-1$

    /**
     * Constructor.
     * @param repository name of the repository (i.e. "website", "users")
     * @param request
     */
    public Tree(String repository, HttpServletRequest request) {
        this.setRepository(repository);
        this.setRequest(request);
        this.setMenu(new ContextMenu(this.getJavascriptTree()));
    }

    public void setRepository(String s) {
        this.repository = s;
    }

    public String getRepository() {
        return this.repository;
    }

    public void setPathOpen(String s) {
        this.pathOpen = s;
    }

    public String getPathOpen() {
        return this.pathOpen;
    }

    /**
     * Sets which path will be selected (and opened - overwrites pathOpen).
     * @param s
     */
    public void setPathSelected(String s) {
        if (StringUtils.isNotEmpty(s)) {
            // this.setPathOpen(s);
            this.setPathOpen(s.substring(0, s.lastIndexOf("/"))); //$NON-NLS-1$
        }
        this.pathSelected = s;
    }

    public String getPathSelected() {
        return this.pathSelected;
    }

    public String getPath() {
        if (super.getPath() != null) {
            return super.getPath();
        }

        return ("/"); //$NON-NLS-1$
    }

    protected void setPathCurrent(String s) {
        this.pathCurrent = s;
    }

    protected String getPathCurrent() {
        return this.pathCurrent;
    }

    public void setIndentionWidth(int i) {
        this.indentionWidth = i;
    }

    public int getIndentionWidth() {
        return this.indentionWidth;
    }

    public List getItemTypes() {
        return this.itemTypes;
    }

    /**
     * Add a itemType to the itemTypes that will be shown in this branch.
     * @param s itemType (one of: ItemType.NT_CONTENT, ItemType.NT_CONTENTNODE, ItemType.NT_NODEDATA)
     */
    public void addItemType(String s) {
        this.itemTypes.add(s);
    }

    /**
     * Add a itemType to the itemTypes that will be shown in this branch.
     * @param s itemType (one of: ItemType.CONTENT, ItemType.CONTENTNODE)
     */
    public void addItemType(ItemType s) {
        this.itemTypes.add(s.getSystemName());
    }

    /**
     * Set the icon of pages.
     * @param src source of the image
     */
    public void setIconPage(String src) {
        this.iconPage = src;
    }

    public String getIconPage() {
        return this.iconPage;
    }

    /**
     * Set the icon of content nodes.
     * @param src source of the image
     */
    public void setIconContentNode(String src) {
        this.iconContentNode = src;
    }

    public String getIconContentNode() {
        return this.iconContentNode;
    }

    /**
     * Set the icon of node data.
     * @param src source of the image
     */
    public void setIconNodeData(String src) {
        this.iconNodeData = src;
    }

    public String getIconNodeData() {
        return this.iconNodeData;
    }

    /**
     * Set the double click event of the icon.
     * @param s javascriopt method
     */
    public void setIconOndblclick(String s) {
        this.iconOndblclick = s;
    }

    public String getIconOndblclick() {
        return this.iconOndblclick;
    }

    /**
     * Set the shifter image (expand branch). "_EXPAND" in file name will be replaced by "_COLLAPSE" after expanding
     * e.g. myShifterIcon_EXPAND.gif
     * @param src source of the image
     */
    public void setShifterExpand(String src) {
        this.shifterExpand = src;
    }

    public String getShifterExpand() {
        return this.shifterExpand;
    }

    /**
     * Set the shifter image (collapse branch). "_COLLAPSE" in file name will be replaced by "_EXPAND" after collapsing
     * e.g. myShifterIcon_COLLAPSE.gif
     * @param src source of the image
     */
    public void setShifterCollapse(String src) {
        this.shifterCollaspe = src;
    }

    public String getShifterCollapse() {
        return this.shifterCollaspe;
    }

    /**
     * Set the shifter image when no children are available (not expandable). "_EMPTY" in the file name will be replaced
     * when children are available e.g. myShifterIcon_EMPTY.gif
     * @param src source of the image
     */
    public void setShifterEmpty(String src) {
        this.shifterEmpty = src;
    }

    public String getShifterEmpty() {
        return this.shifterEmpty;
    }

    public void setDrawShifter(boolean b) {
        this.drawShifter = b;
    }

    public boolean getDrawShifter() {
        return this.drawShifter;
    }

    public void setHeight(int i) {
        this.height = i;
    }

    public int getHeight() {
        return this.height;
    }

    /**
     * Set the columns (for pages and content nodes only).
     * @param al list of TreeColumns
     */
    public void setColums(List al) {
        this.columns = al;
    }

    public List getColumns() {
        return this.columns;
    }

    public TreeColumn getColumns(int col) {
        return (TreeColumn) this.getColumns().get(col);
    }

    public void addColumn(TreeColumn tc) {
        this.getColumns().add(tc);
    }

    /**
     * Set the name of the javascript tree object.
     * @param variableName
     */
    public void setJavascriptTree(String variableName) {
        this.javascriptTree = variableName;
        this.menu.setName(variableName + "Menu"); //$NON-NLS-1$
    }

    public String getJavascriptTree() {
        return this.javascriptTree;
    }

    /**
     * Sets if only a snippet (requested branch) shall be returnde or including the surounding html (tree header, js/css
     * links etc).
     * @param b true: snippet only
     */
    public void setSnippetMode(boolean b) {
        this.snippetMode = b;
    }

    public boolean getSnippetMode() {
        return this.snippetMode;
    }

    // @todo: set size of column resizer gif and pass it to js object
    public void setColumnResizer(String src) {
        this.columnResizer = src;
    }

    public String getColumnResizer() {
        return this.columnResizer;
    }

    public void deleteNode(String parentPath, String label) {
        try {
            HierarchyManager hm = SessionAccessControl.getHierarchyManager(this.getRequest(), this.getRepository());
            Content parentNode = hm.getContent(parentPath);
            String path;
            if (!parentPath.equals("/")) { //$NON-NLS-1$
                path = parentPath + "/" + label; //$NON-NLS-1$
            }
            else {
                path = "/" + label; //$NON-NLS-1$
            }
            this.deActivateNode(path);
            parentNode.delete(label);
            parentNode.save();
        }
        catch (Exception e) {
            log.debug("Exception caught: " + e.getMessage(), e); //$NON-NLS-1$
        }
    }

    public void deleteNode(String path) {
        try {
            String parentPath = path.substring(0, path.lastIndexOf("/")); //$NON-NLS-1$
            String label = path.substring(path.lastIndexOf("/") + 1); //$NON-NLS-1$
            deleteNode(parentPath, label);
        }
        catch (Exception e) {
            log.debug("Exception caught: " + e.getMessage(), e); //$NON-NLS-1$
        }
    }

    public void createNode(String itemType) {
        this.createNode("untitled", itemType); //$NON-NLS-1$
    }

    public void createNode(String label, String itemType) {
        try {
            HierarchyManager hm = SessionAccessControl.getHierarchyManager(this.getRequest(), this.getRepository());
            Content parentNode = hm.getContent(this.getPath());
            String slash = "/"; //$NON-NLS-1$
            boolean isRoot = false;
            if (this.getPath().equals("/")) { //$NON-NLS-1$
                isRoot = true;
                slash = StringUtils.EMPTY;
            }
            if (hm.isExist(this.getPath() + slash + label)) {
                // todo: bugfix getUniqueLabel???
                if (!isRoot) {
                    label = Path.getUniqueLabel(hm, this.getPath(), label);
                }
                else {
                    label = Path.getUniqueLabel(hm, StringUtils.EMPTY, label);
                }
            }
            if (itemType.equals(ItemType.NT_NODEDATA)) {
                parentNode.createNodeData(label);
            }
            else {
                Content newNode;
                if (itemType.equals(ItemType.CONTENT.getSystemName())) {
                    newNode = parentNode.createContent(label);
                }
                else {
                    newNode = parentNode.createContent(label, ItemType.CONTENTNODE);
                }
                newNode.getMetaData().setAuthorId(Authenticator.getUserId(this.getRequest()));
                newNode.getMetaData().setCreationDate();
                newNode.getMetaData().setModificationDate();
                newNode.getMetaData().setSequencePosition();
                // todo: default template
                // now tmp: first template of list is taken...
                if (this.getRepository().equals(ContentRepository.WEBSITE)
                    && itemType.equals(ItemType.CONTENT.getSystemName())) {
                    Iterator templates = Template.getAvailableTemplates(SessionAccessControl.getAccessManager(this
                        .getRequest(), ContentRepository.CONFIG));
                    while (templates.hasNext()) {
                        Template template = (Template) templates.next();
                        newNode.getMetaData().setTemplate(template.getName());
                        break;
                    }
                }
            }
            parentNode.save();
        }
        catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    public String saveNodeData(String nodeDataName, String value, boolean isMeta) {
        String returnValue = StringUtils.EMPTY;
        try {
            HierarchyManager hm = SessionAccessControl.getHierarchyManager(this.getRequest(), this.getRepository());
            Content page = hm.getContent(this.getPath());
            if (!isMeta) {
                NodeData node;
                int type = PropertyType.STRING;
                if (!page.getNodeData(nodeDataName).isExist()) {
                    node = page.createNodeData(nodeDataName);
                }
                else {
                    node = page.getNodeData(nodeDataName);
                    type = node.getType();
                }
                // todo: share with Contorol.Save
                switch (type) {
                    case PropertyType.STRING:
                        node.setValue(value);
                        break;
                    case PropertyType.BOOLEAN:
                        if (value.equals("true")) { //$NON-NLS-1$
                            node.setValue(true);
                        }
                        else {
                            node.setValue(false);
                        }
                        break;
                    case PropertyType.DOUBLE:
                        try {
                            node.setValue(Double.valueOf(value).doubleValue());
                        }
                        catch (Exception e) {
                            node.setValue(0);
                        }
                        break;
                    case PropertyType.LONG:
                        try {
                            node.setValue(Long.valueOf(value).longValue());
                        }
                        catch (Exception e) {
                            node.setValue(0);
                        }
                        break;
                    case PropertyType.DATE:
                        // todo
                        break;
                }
                page.updateMetaData(this.getRequest());
                page.save();
                returnValue = new NodeDataUtil(node).getValueString();
            }
            else {
                page.getMetaData().setProperty(nodeDataName, value);
                page.updateMetaData(this.getRequest());
                page.save();
                returnValue = new MetaDataUtil(page).getPropertyValueString(nodeDataName);
            }
        }
        catch (Exception e) {
            log.debug("Exception caught: " + e.getMessage(), e); //$NON-NLS-1$
        }
        return returnValue;
    }

    public String saveNodeDataType(String nodeDataName, int type) {
        try {
            HierarchyManager hm = SessionAccessControl.getHierarchyManager(this.getRequest(), this.getRepository());
            Content page = hm.getContent(this.getPath());
            Value value = null;
            if (page.getNodeData(nodeDataName).isExist()) {
                value = page.getNodeData(nodeDataName).getValue();
                page.deleteNodeData(nodeDataName);
            }
            NodeData node = page.createNodeData(nodeDataName);
            if (value != null) {
                switch (type) {
                    case PropertyType.STRING:
                        node.setValue(value.getString());
                        break;
                    case PropertyType.BOOLEAN:
                        if (value != null && value.getBoolean()) {
                            node.setValue(true);
                        }
                        else {
                            node.setValue(false);
                        }
                        break;
                    case PropertyType.DOUBLE:
                        try {
                            node.setValue(value.getDouble());
                        }
                        catch (Exception e) {
                            node.setValue(0);
                        }
                        break;
                    case PropertyType.LONG:
                        try {
                            node.setValue(value.getLong());
                        }
                        catch (Exception e) {
                            node.setValue(0);
                        }
                        break;
                    case PropertyType.DATE:
                        // todo
                        break;
                }
            }
            page.updateMetaData(this.getRequest());
            page.save();
            return PropertyType.nameFromValue(page.getNodeData(nodeDataName).getType());
            // return PropertyType.nameFromValue(node.getType());
        }
        catch (Exception e) {
            log.debug("Exception caught: " + e.getMessage(), e); //$NON-NLS-1$
        }
        return StringUtils.EMPTY;
    }

    public String pasteNode(String pathOrigin, String pathSelected, int pasteType, int action) {
        // todo: proper doc
        // todo: ??? generic -> RequestInterceptor.java
        // move and copy of nodes works as copy/cut - paste (remaining of the very first prototype)
        // "Copy node" copies a node to the "clipboard" (clipboard object of the js tree object)
        // "Move node" copies a node to the "clipboard", setting clipboardMethod "cut"
        // select a node after copy or cut triggers Tree.pasteNode
        // action: clipboardMethod (copy or move)
        // pasteType: above, below, last position, as sub node
        boolean move = false;
        if (action == ACTION_MOVE) {
            move = true;
        }
        String label = pathOrigin.substring(pathOrigin.lastIndexOf("/") + 1); //$NON-NLS-1$
        String slash = "/"; //$NON-NLS-1$
        if (pathSelected.equals("/")) { //$NON-NLS-1$
            slash = StringUtils.EMPTY;
        }
        String destination = pathSelected + slash + label;
        if (pasteType == PASTETYPE_SUB && action != ACTION_COPY && destination.equals(pathOrigin)) {
            // drag node to parent node: move to last position
            pasteType = PASTETYPE_LAST;
        }
        if (pasteType == PASTETYPE_SUB) {
            destination = pathSelected + slash + label;
            Content touchedContent = this.copyMoveNode(pathOrigin, destination, move);
            if (touchedContent == null) {
                return StringUtils.EMPTY;
            }
            return touchedContent.getHandle();

        }
        else if (pasteType == PASTETYPE_LAST) {
            // LAST only available for sorting inside the same directory
            try {
                HierarchyManager hm = SessionAccessControl.getHierarchyManager(this.getRequest(), this.getRepository());
                Content touchedContent = hm.getContent(pathOrigin);
                touchedContent.getMetaData().setSequencePosition();
                return touchedContent.getHandle();
            }
            catch (RepositoryException re) {
                return StringUtils.EMPTY;
            }
        }
        else {
            try {
                HierarchyManager hm = SessionAccessControl.getHierarchyManager(this.getRequest(), this.getRepository());
                // PASTETYPE_ABOVE | PASTETYPE_BELOW
                String pathSelectedParent = pathSelected.substring(0, pathSelected.lastIndexOf("/")); //$NON-NLS-1$
                String pathOriginParent = pathOrigin.substring(0, pathOrigin.lastIndexOf("/")); //$NON-NLS-1$
                if (StringUtils.isEmpty(pathSelectedParent)) {
                    slash = StringUtils.EMPTY;
                    pathSelectedParent = "/"; //$NON-NLS-1$
                }
                if (StringUtils.isEmpty(pathOriginParent)) {
                    pathOriginParent = "/"; //$NON-NLS-1$
                }
                Content touchedContent = null;
                // *
                // (copy node) or (move node if source and destination differ in parent directory)
                if (action == ACTION_COPY || !pathSelectedParent.equals(pathOriginParent)) {
                    destination = pathSelectedParent + slash + label;
                    touchedContent = this.copyMoveNode(pathOrigin, destination, move);
                }
                else {
                    // sort only (move inside the same directory)
                    touchedContent = hm.getContent(pathOrigin);
                }
                Content parentContent = hm.getContent(pathSelectedParent);
                Content selectedContent = hm.getContent(pathSelected);

                // *
                // set sequence position (average of selected and above resp. below)
                // todo: !!!!!!!!
                // how to find out type of node?

                String selectedType = ItemType.NT_NODEDATA;
                String touchedType = ItemType.NT_NODEDATA;
                Iterator it1 = parentContent.getChildren(ItemType.CONTENT, Content.SORT_BY_SEQUENCE).iterator();
                while (it1.hasNext()) {
                    Content c = (Content) it1.next();
                    if (c.getHandle().equals(selectedContent.getHandle())) {
                        selectedType = ItemType.CONTENT.getSystemName();
                    }
                    if (c.getHandle().equals(touchedContent.getHandle())) {
                        touchedType = ItemType.CONTENT.getSystemName();
                    }
                }
                Iterator it2 = parentContent.getChildren(ItemType.CONTENTNODE, Content.SORT_BY_SEQUENCE).iterator();
                while (it2.hasNext()) {
                    Content c = (Content) it2.next();
                    if (c.getHandle().equals(selectedContent.getHandle())) {
                        selectedType = ItemType.CONTENTNODE.getSystemName();
                    }
                    if (c.getHandle().equals(touchedContent.getHandle())) {
                        touchedType = ItemType.CONTENTNODE.getSystemName();
                    }
                }
                if (touchedType.equals(ItemType.NT_NODEDATA)) {
                    return StringUtils.EMPTY; // sorting not possible
                }
                long posSelected = selectedContent.getMetaData().getSequencePosition();
                long posAbove = 0;
                long posBelow = 0;
                long posFirst = 0;
                Iterator it = parentContent.getChildren(touchedType, Content.SORT_BY_SEQUENCE).iterator();
                boolean first = true;
                while (it.hasNext()) {
                    Content c = (Content) it.next();
                    if (first) {
                        posFirst = c.getMetaData().getSequencePosition();
                        first = false;
                    }
                    if (c.getHandle().equals(selectedContent.getHandle())) {
                        if (it.hasNext()) {
                            Content nextC = (Content) it.next();
                            posBelow = nextC.getMetaData().getSequencePosition();
                        }
                        break;
                    }

                    posAbove = c.getMetaData().getSequencePosition();

                }
                if (touchedType != selectedType) {
                    if (touchedType.equals(ItemType.CONTENTNODE.getSystemName())
                        && selectedType.equals(ItemType.CONTENT.getSystemName())) {
                        // move at first position
                        // (tried to move a content node around a page)
                        pasteType = PASTETYPE_ABOVE;
                        posAbove = posFirst;
                    }
                    else {
                        // move to last position
                        // (tried to move a page around a content node or node data
                        // tried to move a content around a node data
                        pasteType = PASTETYPE_BELOW;
                        posBelow = 0;
                    }
                }
                long posTouched;
                if (pasteType == PASTETYPE_ABOVE) {
                    if (posAbove == 0) {
                        posTouched = posSelected - (MetaData.SEQUENCE_POS_COEFFICIENT * 1000); // first position in
                    }
                    // directory ->
                    // 1000*coefficient
                    // above first
                    else {
                        posTouched = (posAbove + posSelected) / 2;
                    }
                }
                else {
                    if (posBelow == 0) {
                        posTouched = 0; // last position in directory -> timestamp (passing 0)
                    }
                    else {
                        posTouched = (posBelow + posSelected) / 2;
                    }
                }
                touchedContent.getMetaData().setSequencePosition(posTouched);
                touchedContent.updateMetaData(this.getRequest());
                touchedContent.save();
                return touchedContent.getHandle();
            }
            catch (RepositoryException re) {
                return StringUtils.EMPTY;
            }
        }
    }

    public Content copyMoveNode(String source, String destination, boolean move) {
        // todo: ??? generic -> RequestInterceptor.java
        try {
            HierarchyManager hm = SessionAccessControl.getHierarchyManager(this.getRequest(), this.getRepository());
            if (hm.isExist(destination)) {
                String parentPath = destination.substring(0, destination.lastIndexOf("/")); //$NON-NLS-1$
                String label = destination.substring(destination.lastIndexOf("/") + 1); //$NON-NLS-1$
                label = Path.getUniqueLabel(hm, parentPath, label);
                destination = parentPath + "/" + label; //$NON-NLS-1$
            }
            if (move) {
                if (destination.indexOf(source + "/") == 0) { //$NON-NLS-1$
                    // todo: disable this possibility in javascript
                    // move source into destinatin not possible
                    return null;
                }
                this.deActivateNode(source);
                try {
                    hm.moveTo(source, destination);
                }
                catch (Exception e) {
                    // try to move below node data
                    return null;
                }
            }
            else {
                // copy
                hm.copyTo(source, destination);
            }
            SessionAccessControl.invalidateUser(this.getRequest());
            Content newContent = hm.getContent(destination);
            try {
                newContent.updateMetaData(this.getRequest());
                newContent.getMetaData().setSequencePosition();
                newContent.getMetaData(MetaData.ACTIVATION_INFO).setUnActivated();
            }
            catch (Exception e) {
                log.debug("Exception caught: " + e.getMessage(), e); //$NON-NLS-1$
            }
            newContent.save();
            return newContent;
        }
        catch (Exception e) {
            log.debug("Exception caught: " + e.getMessage(), e); //$NON-NLS-1$
        }
        return null;
    }

    public void moveNode(String source, String destination) {
        this.copyMoveNode(source, destination, true);
    }

    public void copyNode(String source, String destination) {
        this.copyMoveNode(source, destination, false);
    }

    public String renameNode(String newLabel) {
        String returnValue = StringUtils.EMPTY;
        try {
            HierarchyManager hm = SessionAccessControl.getHierarchyManager(this.getRequest(), this.getRepository());
            String parentPath = this.getPath().substring(0, this.getPath().lastIndexOf("/")); //$NON-NLS-1$
            newLabel = Path.getValidatedLabel(newLabel);
            String dest = parentPath + "/" + newLabel; //$NON-NLS-1$
            if (hm.isExist(dest)) {
                newLabel = Path.getUniqueLabel(hm, parentPath, newLabel);
                dest = parentPath + "/" + newLabel; //$NON-NLS-1$
            }

            this.deActivateNode(this.getPath());

            if (log.isInfoEnabled()) {
                log.info("Moving node from " + this.getPath() + " to " + dest); //$NON-NLS-1$ //$NON-NLS-2$
            }
            if (hm.isNodeData(this.getPath())) {
                Content parentPage = hm.getContent(parentPath);
                NodeData newNodeData = parentPage.createNodeData(newLabel);
                NodeData existingNodeData = hm.getNodeData(this.getPath());
                newNodeData.setValue(existingNodeData.getString());
                existingNodeData.delete();
                dest = parentPath;
            }
            else {
                // we can't rename a node. we must move
                // we must place the node at the same position
                Content current = hm.getContent(this.getPath());
                Content parent = current.getParent();
                String placedBefore = null;
                for (Iterator iter = parent.getChildren(current.getNodeType().getName()).iterator(); iter.hasNext();) {
                    Content child = (Content) iter.next();
                    if (child.getHandle().equals(this.getPath())) {
                        if (iter.hasNext()) {
                            child = (Content) iter.next();
                            placedBefore = child.getName();
                        }
                    }
                }

                hm.moveTo(this.getPath(), dest);

                // now set at the same place as before
                if (placedBefore != null) {
                    parent.orderBefore(newLabel, placedBefore);
                }
            }
            SessionAccessControl.invalidateUser(this.getRequest());
            Content newPage = hm.getContent(dest);
            returnValue = newLabel;
            newPage.updateMetaData(this.getRequest());
            newPage.save();
        }
        catch (Exception e) {
            log.debug("Exception caught: " + e.getMessage(), e); //$NON-NLS-1$
        }
        return returnValue;
    }

    public void activateNode(String path, boolean recursive) {
        // todo: ??? generic -> RequestInterceptor.java
        try {
            HierarchyManager hm = SessionAccessControl.getHierarchyManager(this.getRequest(), this.getRepository());
            Content c = null;
            if (hm.isPage(path)) {
                c = hm.getContent(path);
            }
            else {
                c = hm.getContent(path);
            }
            Syndicator syndicator = new Syndicator(this.getRequest());
            if (recursive) {
                deepActivate(syndicator, c, hm);
            }
            else {
                syndicator.activate(this.getRepository(), StringUtils.EMPTY, path, recursive);
            }
        }
        catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    private void deepActivate(Syndicator syndicator, Content content, HierarchyManager hm) {
        try {
            syndicator.activate(this.getRepository(), StringUtils.EMPTY, content.getHandle(), false);
            Collection children = content.getChildren();
            if (children != null) {
                Iterator it = children.iterator();
                while (it.hasNext()) {
                    deepActivate(syndicator, (Content) it.next(), hm);
                }
            }
        }
        catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    public void deActivateNode(String path) {
        try {
            Syndicator syndicator = new Syndicator(this.getRequest());
            syndicator.deActivate(this.getRepository(), path);
        }
        catch (Exception e) {
            log.debug("Exception caught: " + e.getMessage(), e); //$NON-NLS-1$
        }
    }

    public String getHtml() {
        StringBuffer html = new StringBuffer();
        if (!this.getSnippetMode()) {
            html.append(this.getHtmlHeader());
        }
        this.setPathCurrent(this.getPath());
        html.append(this.getHtmlChildren());
        if (!this.getSnippetMode()) {
            html.append(this.getHtmlFooter());
        }
        return html.toString();
    }

    public String getHtmlHeader() {

        StringBuffer html = new StringBuffer();

        // write css definitions
        // @todo style is not valid in body!
        html.append("<style type=\"text/css\">"); //$NON-NLS-1$
        int numberOfColumns = this.getColumns().size();
        if (numberOfColumns == 0) {
            numberOfColumns = 1;
        }
        for (int i = 0; i < numberOfColumns; i++) {
            html.append("." + this.getJavascriptTree() + "CssClassColumn" + i); //$NON-NLS-1$ //$NON-NLS-2$
            html.append("\n{position:absolute;left:0px;clip:rect(0 0 100 0);cursor:default;}\n"); //$NON-NLS-1$
        }
        html.append("</style>\n\n"); //$NON-NLS-1$

        // resizer
        html.append("<div id=\"" //$NON-NLS-1$
            + this.getJavascriptTree() + "_ColumnResizerDiv\" style=\"position:absolute;top:-50px;z-index:500;\">"); //$NON-NLS-1$
        for (int i = 1; i < this.getColumns().size(); i++) {
            // div around image: ie and safari do not allow drag of images
            // todo: fix bad behaviour in mozilla: resizer "turns blue" when selecting
            html.append("<div onmousedown=\"" //$NON-NLS-1$
                + this.getJavascriptTree() + ".dragColumnStart(this," //$NON-NLS-1$
                + i + ");\" id=\"" //$NON-NLS-1$
                + this.getJavascriptTree() + "ColumnResizer" //$NON-NLS-1$
                + i + "\" style=\"position:relative;left:-1000px;background-image:url(" //$NON-NLS-1$
                + this.getRequest().getContextPath() + this.getColumnResizer() + ");display:inline;\">"); //$NON-NLS-1$
            // use resizer gif to get exact size
            html.append("<img src=\"" //$NON-NLS-1$
                + this.getRequest().getContextPath()
                + this.getColumnResizer()
                + "\" alt=\"\" style=\"visibility:hidden;\" />"); //$NON-NLS-1$
            html.append("</div>"); //$NON-NLS-1$
        }
        html.append("</div>"); //$NON-NLS-1$
        // column header
        html.append("<div id=\"" //$NON-NLS-1$
            + this.getJavascriptTree() + "_ColumnHeader\" style=\"position:absolute;top:-50px;z-index:480;\">"); //$NON-NLS-1$
        for (int i = 0; i < this.getColumns().size(); i++) {
            TreeColumn tc = this.getColumns(i);
            html.append("<span class=\"mgnlTreeColumn " //$NON-NLS-1$
                + this.getJavascriptTree() + "CssClassColumn" //$NON-NLS-1$
                + i + "\"><span class=\"mgnlTreeHeader\">" //$NON-NLS-1$
                + tc.getTitle() + "<!-- ie --></span></span>"); //$NON-NLS-1$
        }
        html.append("</div>"); //$NON-NLS-1$

        html.append("<div id=\"" //$NON-NLS-1$
            + this.getJavascriptTree()
            + "_ColumnResizerLine\" style=\"position:absolute;top:0px;left:-100px;visibility:hidden;width:1px;height:" //$NON-NLS-1$
            + this.getHeight()
            + "px;background-color:#333333;z-index:490;\"></div>"); //$NON-NLS-1$
        html.append("<div id=\"" //$NON-NLS-1$
            + this.getJavascriptTree() + "_" //$NON-NLS-1$
            + this.getPath() + "_DivMain\" onclick=\"" //$NON-NLS-1$
            + this.getJavascriptTree() + ".mainDivReset();\" oncontextmenu=\"" //$NON-NLS-1$
            + this.getJavascriptTree() + ".menuShow(event);return false;\" class=\"mgnlTreeDiv\" style=\"height:" //$NON-NLS-1$
            + this.getHeight() + "px;\">"); //$NON-NLS-1$
        html.append(Spacer.getHtml(8, 8));
        // html.append("<div id=\""+this.getJavascriptTree()+"_"+this.getPath()+"_DivSub\" style=\"display:none;\">";
        html.append("<div id=\"" + this.getJavascriptTree() + "_" + this.getPath() + "_DivSub\">"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        return html.toString();
    }

    public String getHtmlFooter() {
        StringBuffer html = new StringBuffer();
        html.append("</div>"); //$NON-NLS-1$
        boolean permissionWrite = true;
        try {
            HierarchyManager hm = SessionAccessControl.getHierarchyManager(this.getRequest(), this.getRepository());
            Content root = hm.getContent(this.getPath());
            permissionWrite = root.isGranted(info.magnolia.cms.security.Permission.WRITE);
        }
        catch (RepositoryException e) {
            log.debug("Exception caught: " + e.getMessage(), e); //$NON-NLS-1$
        }

        // lineInter: line between nodes, to allow set cursor between nodes
        // line to place a very last position
        String lineId = this.getJavascriptTree() + "_" + this.getPath() + "_LineInter"; //$NON-NLS-1$ //$NON-NLS-2$

        html.append("<div id=\"" //$NON-NLS-1$
            + lineId + "\" class=\"mgnlTreeLineInter mgnlLineEnabled\" onmouseover=\"" //$NON-NLS-1$
            + this.javascriptTree + ".moveNodeHighlightLine('" //$NON-NLS-1$
            + lineId + "');\" onmouseout=\"" //$NON-NLS-1$
            + this.javascriptTree + ".moveNodeResetLine('" //$NON-NLS-1$
            + lineId + "');\" onmousedown=\"" //$NON-NLS-1$
            + this.javascriptTree + ".pasteNode('" //$NON-NLS-1$
            + this.getPath() + "'," //$NON-NLS-1$
            + Tree.PASTETYPE_SUB + "," //$NON-NLS-1$
            + Boolean.toString(permissionWrite) + ",'" //$NON-NLS-1$
            + lineId + "');\" ></div>"); //$NON-NLS-1$

        html.append(new Hidden(this.getJavascriptTree() + "_" + this.getPath() + "_PermissionWrite", Boolean //$NON-NLS-1$ //$NON-NLS-2$
            .toString(permissionWrite), false).getHtml());
        html.append("</div>"); //$NON-NLS-1$
        // address bar
        String pathOpen = this.getPathOpen();
        if (pathOpen == null) {
            pathOpen = StringUtils.EMPTY;
        }

        html.append(Spacer.getHtml(3, 3));
        html.append("\n\n\n\n\n\n\n\n<input id=\"" //$NON-NLS-1$
            + this.getJavascriptTree()
            + "AddressBar\" type=\"text\" onkeydown=\"if (mgnlIsKeyEnter(event)) " //$NON-NLS-1$
            + this.getJavascriptTree()
            + ".expandNode(this.value);\" class=\"mgnlDialogControlEdit\" style=\"width:100%;\" value=\"" //$NON-NLS-1$
            + pathOpen
            + "\" />\n\n\n\n"); //$NON-NLS-1$

        // shadow for moving pages
        html.append("<div id=\""); //$NON-NLS-1$
        html.append(this.getJavascriptTree());
        html.append("_MoveShadow\" "); //$NON-NLS-1$
        html.append("style=\"position:absolute;top:0px;left:0px;visibility:hidden;background-color:#fff;\"></div>"); //$NON-NLS-1$

        // "move denied"
        html.append("<img src=\"" //$NON-NLS-1$
            + this.getRequest().getContextPath()
            + Tree.DOCROOT
            + "move_denied.gif\" id=\"" //$NON-NLS-1$
            + this.getJavascriptTree()
            + "_MoveDenied\" style=\"position:absolute;top:0px;left:0px;visibility:hidden;\" />"); //$NON-NLS-1$
        // initialize js tree object
        html.append("<script type=\"text/javascript\">"); //$NON-NLS-1$
        html.append("var " //$NON-NLS-1$
            + this.getJavascriptTree() + "=new mgnlTree('" //$NON-NLS-1$
            + this.getRepository() + "','" //$NON-NLS-1$
            + this.getPath() + "','" //$NON-NLS-1$
            + this.getJavascriptTree() + "'," //$NON-NLS-1$
            + this.getHeight() + ");"); //$NON-NLS-1$

        // add columns to tree object
        for (int i = 0; i < this.getColumns().size(); i++) {
            TreeColumn tc = this.getColumns(i);
            html.append(this.getJavascriptTree() + ".columns[" //$NON-NLS-1$
                + i + "]=new mgnlTreeColumn(" //$NON-NLS-1$
                + tc.getWidth() + ",'" //$NON-NLS-1$
                + tc.getHtmlEdit() + "','" //$NON-NLS-1$
                + tc.getName() + "'," //$NON-NLS-1$
                + tc.getIsMeta() + "," //$NON-NLS-1$
                + tc.getIsLabel() + "," //$NON-NLS-1$
                + tc.getIsNodeDataValue() + "," //$NON-NLS-1$
                + tc.getIsNodeDataType() + ");"); //$NON-NLS-1$
        }
        html.append("mgnlTreeControls['" + this.getJavascriptTree() + "']=" + this.getJavascriptTree() + ";\n"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        // js is not run on remote request
        html.append(this.getJavascriptTree() + ".selectNode('" + this.getPathSelected() + "');"); //$NON-NLS-1$ //$NON-NLS-2$
        html.append("</script>"); //$NON-NLS-1$

        // contextmenu
        if (menu.getMenuItems().size() != 0) {
            StringBuffer menuJavascript = new StringBuffer();
            html.append(menu.getHtml());
        }

        // register menu
        html.append("<script>" + this.getJavascriptTree() + ".menu = " + menu.getName() + "</script>"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

        return html.toString();
    }

    public String getHtmlBranch() {
        return StringUtils.EMPTY;
    }

    public String getHtmlChildren() {
        StringBuffer html = new StringBuffer();
        HierarchyManager hm = SessionAccessControl.getHierarchyManager(this.getRequest(), this.getRepository());
        Content parentNode = null;
        try {
            parentNode = hm.getContent(this.getPathCurrent());
            // loop the children of the different item types
            for (int i = 0; i < this.getItemTypes().size(); i++) {
                String type = (String) this.getItemTypes().get(i);
                html.append(this.getHtmlChildrenOfOneType(parentNode, type));
            }
        }
        catch (RepositoryException e) {
            log.debug("Exception caught: " + e.getMessage(), e); //$NON-NLS-1$
        }
        return html.toString();
    }

    public String getHtmlChildrenOfOneType(Content parentNode, String itemType) {
        StringBuffer html = new StringBuffer();
        String icon = null;
        if (itemType.equals(ItemType.CONTENT.getSystemName())) {
            icon = this.getIconPage();
        }
        else if (itemType.equals(ItemType.CONTENTNODE.getSystemName())) {
            icon = this.getIconContentNode();
        }
        else if (itemType.equals(ItemType.NT_NODEDATA)) {
            icon = this.getIconNodeData();
        }

        try {
            // todo: parentNode - level of this.getPath
            int left = (parentNode.getLevel()) * this.getIndentionWidth();
            Iterator it;
            if (itemType.equalsIgnoreCase(ItemType.NT_NODEDATA)) {
                it = parentNode.getNodeDataCollection().iterator();
            }
            else {
                it = parentNode.getChildren(itemType, ContentHandler.SORT_BY_SEQUENCE).iterator();
            }
            while (it.hasNext()) {
                Object o = it.next();
                Content c = null;
                NodeData d = null;
                String handle = StringUtils.EMPTY;
                String name = StringUtils.EMPTY;
                boolean hasSub = false;
                boolean showSub = false;
                boolean isActivated = false;
                boolean permissionWrite = false;
                boolean permissionWriteParent = false;
                if (itemType.equals(ItemType.NT_NODEDATA)) {
                    d = (NodeData) o;
                    handle = d.getHandle();
                    name = d.getName();

                    if (d.isGranted(info.magnolia.cms.security.Permission.WRITE)) {
                        permissionWrite = true;
                    }
                }
                else {
                    c = (Content) o;
                    handle = c.getHandle();
                    if (this.getColumns().size() == 0) {
                        name = c.getName();
                    }
                    else {
                        this.getColumns(0).setWebsiteNode(c);
                        name = this.getColumns(0).getHtml();
                    }
                    if (c.isGranted(info.magnolia.cms.security.Permission.WRITE)) {
                        permissionWrite = true;
                    }
                    if (c.getAncestor(c.getLevel() - 1).isGranted(info.magnolia.cms.security.Permission.WRITE)) {
                        permissionWriteParent = true;
                    }
                    isActivated = c.getMetaData(MetaData.ACTIVATION_INFO).getIsActivated();
                    for (int i = 0; i < this.getItemTypes().size(); i++) {
                        String type = (String) this.getItemTypes().get(i);
                        int size = 0;
                        if (type.equalsIgnoreCase(ItemType.NT_NODEDATA)) {
                            size = c.getNodeDataCollection().size();
                        }
                        else {
                            size = c.getChildren(type).size();
                        }
                        if (size > 0) {
                            hasSub = true;
                            if (this.getPathOpen() != null
                                && (this.getPathOpen().indexOf(handle + "/") == 0 || this.getPathOpen().equals(handle))) { //$NON-NLS-1$
                                showSub = true;
                            }
                            break;
                        }
                    }
                }
                String idPre = this.javascriptTree + "_" + handle; //$NON-NLS-1$
                String jsHighlightNode = this.javascriptTree + ".nodeHighlight(this,'" //$NON-NLS-1$
                    + handle + "'," //$NON-NLS-1$
                    + Boolean.toString(permissionWrite) + ");"; //$NON-NLS-1$
                String jsResetNode = this.javascriptTree + ".nodeReset(this,'" + handle + "');"; //$NON-NLS-1$ //$NON-NLS-2$
                String jsSelectNode = this.javascriptTree + ".selectNode('" //$NON-NLS-1$
                    + handle + "'," //$NON-NLS-1$
                    + Boolean.toString(permissionWrite) + ",'" //$NON-NLS-1$
                    + itemType + "');"; //$NON-NLS-1$
                String jsExpandNode;
                if (this.getDrawShifter()) {
                    jsExpandNode = this.javascriptTree + ".expandNode('" + handle + "');"; //$NON-NLS-1$ //$NON-NLS-2$
                }
                else {
                    jsExpandNode = jsSelectNode;
                }
                String jsHighlightLine = this.javascriptTree + ".moveNodeHighlightLine('" + idPre + "_LineInter');"; //$NON-NLS-1$ //$NON-NLS-2$
                String jsResetLine = this.javascriptTree + ".moveNodeResetLine('" + idPre + "_LineInter');"; //$NON-NLS-1$ //$NON-NLS-2$

                int maskWidth = left;
                if (maskWidth < 1) {
                    maskWidth = 1;
                }

                // lineInter: line between nodes, to allow set cursor between nodes
                // try to avoid blank images, setting js actions on divs should be ok
                if (permissionWriteParent) {
                    html.append("<div id=\"" //$NON-NLS-1$
                        + idPre + "_LineInter\" class=\"mgnlTreeLineInter mgnlLineEnabled\" onmouseover=\"" //$NON-NLS-1$
                        + jsHighlightLine + "\" onmouseout=\"" //$NON-NLS-1$
                        + jsResetLine + "\" onmousedown=\"" //$NON-NLS-1$
                        + this.javascriptTree + ".pasteNode('" //$NON-NLS-1$
                        + handle + "'," //$NON-NLS-1$
                        + Tree.PASTETYPE_ABOVE + ",true);\" ></div>"); //$NON-NLS-1$
                }
                else {
                    html.append("<div id=\"" //$NON-NLS-1$
                        + idPre + "_LineInter\" class=\"mgnlTreeLineInter mgnlLineDisabled\"></div>"); //$NON-NLS-1$
                }

                html.append("<div id=\"" //$NON-NLS-1$
                    + idPre + "_DivMain\" style=\"position:relative;top:0;left:0;width:100%;height:18px;\">"); //$NON-NLS-1$
                html.append("&nbsp;"); // do not remove! //$NON-NLS-1$
                int paddingLeft = left;
                if (paddingLeft < 0) {
                    paddingLeft = 0;
                }
                html.append("<span id=\"" //$NON-NLS-1$
                    + idPre + "_Column0Outer\" class=\"mgnlTreeColumn " //$NON-NLS-1$
                    + this.javascriptTree + "CssClassColumn0\" style=\"padding-left:" //$NON-NLS-1$
                    + paddingLeft + "px;\">"); //$NON-NLS-1$
                if (this.getDrawShifter()) {
                    String shifter = StringUtils.EMPTY;
                    if (hasSub) {
                        if (showSub) {
                            if (this.getShifterCollapse() != null) {
                                shifter = this.getShifterCollapse();
                            }
                        }
                        else {
                            if (this.getShifterExpand() != null) {
                                shifter = this.getShifterExpand();
                            }
                        }
                    }
                    else {
                        if (this.getShifterEmpty() != null) {
                            shifter = this.getShifterEmpty();
                        }
                    }
                    if (StringUtils.isNotEmpty(shifter)) {
                        html.append("<img id=\"" //$NON-NLS-1$
                            + idPre + "_Shifter\" onmousedown=\"" //$NON-NLS-1$
                            + this.javascriptTree + ".shifterDown('" //$NON-NLS-1$
                            + handle + "');\" onmouseout=\"" //$NON-NLS-1$
                            + this.javascriptTree + ".shifterOut();\" class=\"mgnlTreeShifter\" src=\"" //$NON-NLS-1$
                            + this.getRequest().getContextPath() + shifter + "\" />"); //$NON-NLS-1$
                    }
                }
                html.append("<span id=" //$NON-NLS-1$
                    + idPre + "_Name onmouseover=\"" //$NON-NLS-1$
                    + jsHighlightNode + "\" onmouseout=\"" //$NON-NLS-1$
                    + jsResetNode + "\" onmousedown=\"" //$NON-NLS-1$
                    + jsSelectNode + this.javascriptTree + ".pasteNode('" //$NON-NLS-1$
                    + handle + "'," //$NON-NLS-1$
                    + Tree.PASTETYPE_SUB + "," //$NON-NLS-1$
                    + permissionWrite + ");\">"); //$NON-NLS-1$
                if (StringUtils.isNotEmpty(icon)) {
                    html.append("<img id=\"" //$NON-NLS-1$
                        + idPre + "_Icon\" class=\"mgnlTreeIcon\" src=\"" //$NON-NLS-1$
                        + this.getRequest().getContextPath() + icon + "\" onmousedown=\"" //$NON-NLS-1$
                        + jsExpandNode + "\""); //$NON-NLS-1$
                    if (this.getIconOndblclick() != null) {
                        html.append(" ondblclick=\"" + this.getIconOndblclick() + "\""); //$NON-NLS-1$ //$NON-NLS-2$
                    }
                    html.append(" />"); //$NON-NLS-1$
                }
                String dblclick = StringUtils.EMPTY;
                if (permissionWrite && StringUtils.isNotEmpty(this.getColumns(0).getHtmlEdit())) {
                    dblclick = " ondblclick=\"" + this.javascriptTree + ".editNodeData(this,'" + handle + "',0);\""; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                }
                html.append("<span class=\"mgnlTreeText\" id=\"" + idPre + "_Column0Main\"" + dblclick + ">"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                html.append(name);
                html.append("</span></span></span>"); //$NON-NLS-1$
                html.append(new Hidden(idPre + "_PermissionWrite", Boolean.toString(permissionWrite), false).getHtml()); //$NON-NLS-1$
                html.append(new Hidden(idPre + "_ItemType", itemType, false).getHtml()); //$NON-NLS-1$
                html.append(new Hidden(idPre + "_IsActivated", Boolean.toString(isActivated), false).getHtml()); //$NON-NLS-1$
                for (int i = 1; i < this.getColumns().size(); i++) {
                    String str = StringUtils.EMPTY;
                    TreeColumn tc = this.getColumns(i);
                    if (!itemType.equals(ItemType.NT_NODEDATA)) {
                        // content node ItemType.NT_CONTENTNODE and ItemType.NT_CONTENT
                        if (!tc.getIsNodeDataType() && !tc.getIsNodeDataValue()) {
                            tc.setWebsiteNode(c);
                            tc.setId(handle);
                            str = tc.getHtml();
                        }
                    }
                    else {
                        NodeDataUtil util = new NodeDataUtil(d);
                        if (tc.getIsNodeDataType()) {
                            str = util.getTypeName(d.getType());
                        }
                        else if (tc.getIsNodeDataValue()) {
                            str = util.getValueString();
                        }
                        if (StringUtils.isEmpty(str)) {
                            str = TreeColumn.EMPTY;
                        }
                        tc.setName(name); // workaround, will be passed to js TreeColumn object
                    }
                    tc.setEvent("onmouseover", jsHighlightNode, true); //$NON-NLS-1$
                    tc.setEvent("onmouseout", jsResetNode, true); //$NON-NLS-1$
                    tc.setEvent("onmousedown", jsSelectNode, true); //$NON-NLS-1$
                    html.append("<span class=\"mgnlTreeColumn " //$NON-NLS-1$
                        + this.javascriptTree + "CssClassColumn" //$NON-NLS-1$
                        + i + "\"><span id=\"" //$NON-NLS-1$
                        + idPre + "_Column" //$NON-NLS-1$
                        + i + "Main\"" //$NON-NLS-1$
                        + tc.getHtmlCssClass() + tc.getHtmlEvents());
                    if (permissionWrite && StringUtils.isNotEmpty(tc.getHtmlEdit())) {
                        html.append(" ondblclick=\"" //$NON-NLS-1$
                            + this.javascriptTree + ".editNodeData(this,'" //$NON-NLS-1$
                            + handle + "'," //$NON-NLS-1$
                            + i + ");\""); //$NON-NLS-1$
                    }
                    html.append(">" + str + "</span></span>"); //$NON-NLS-1$ //$NON-NLS-2$
                }
                html.append("</div>"); //$NON-NLS-1$
                String display = "none"; //$NON-NLS-1$
                if (showSub) {
                    display = "block"; //$NON-NLS-1$
                }
                html.append("<div id=\"" + idPre + "_DivSub\" style=\"display:" + display + ";\">"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                if (hasSub) {
                    if (showSub) {
                        String pathRemaining = this.getPathOpen().substring(this.getPathCurrent().length());
                        if (pathRemaining.length() > 0) {
                            // get rid of first slash (/people/franz -> people/franz)
                            String slash = "/"; //$NON-NLS-1$
                            if (this.getPathCurrent().equals("/")) { //$NON-NLS-1$
                                // first slash already removed
                                slash = StringUtils.EMPTY; // no slash needed between pathCurrent and nextChunk
                            }
                            else {
                                pathRemaining = pathRemaining.substring(1);
                            }
                            String nextChunk;
                            if (pathRemaining.indexOf("/") != -1) { //$NON-NLS-1$
                                nextChunk = pathRemaining.substring(0, pathRemaining.indexOf("/")); //$NON-NLS-1$
                            }
                            else {
                                nextChunk = pathRemaining; // last chunk
                            }
                            String pathNext = this.getPathCurrent() + slash + nextChunk;
                            this.setPathCurrent(pathNext);
                            html.append(this.getHtmlChildren());
                        }
                    }
                }
                html.append("</div>\n"); //$NON-NLS-1$
            }
        }
        catch (RepositoryException e) {
            log.debug("Exception caught: " + e.getMessage(), e); //$NON-NLS-1$
        }
        return html.toString();
    }

    /**
     * @param menuNewFolder
     */
    public void addMenuItem(ContextMenuItem item) {
        menu.addMenuItem(item);
    }

    /**
     * Add a separator line between menu items.
     */
    public void addSeparator() {
        menu.addMenuItem(null);
    }

    protected ContextMenu getMenu() {
        return this.menu;
    }

    protected void setMenu(ContextMenu menu) {
        this.menu = menu;
    }
}
