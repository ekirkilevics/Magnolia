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
import info.magnolia.cms.beans.config.ItemType;
import info.magnolia.cms.beans.config.Template;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.core.MetaData;
import info.magnolia.cms.core.NodeData;
import info.magnolia.cms.exchange.simple.Syndicator;
import info.magnolia.cms.gui.dialog.DialogSpacer;
import info.magnolia.cms.security.Authenticator;
import info.magnolia.cms.security.SessionAccessControl;
import info.magnolia.cms.util.MetaDataUtil;
import info.magnolia.cms.util.NodeDataUtil;
import info.magnolia.cms.util.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.Value;
import javax.servlet.http.HttpServletRequest;
import org.apache.log4j.Logger;


/**
 * @author Vinzenz Wyser
 * @version 2.0
 */
public class Tree extends ControlSuper {

    private String repository;

    private String pathOpen;

    private String pathCurrent;

    private String pathSelected;

    private int indentionWidth = 15;

    private ArrayList itemTypes = new ArrayList();

    public static final String DOCROOT = "/admindocroot/controls/tree/";

    private int height = 400;

    // todo: global; where?
    public static final String ICONDOCROOT = "/admindocroot/icons/16/";

    private String iconPage = ICONDOCROOT + "document_plain_earth.gif";

    private String iconContentNode = ICONDOCROOT + "cubes.gif";

    private String iconNodeData = ICONDOCROOT + "cube_green.gif";

    private String iconOndblclick = null;

    private String shifterExpand = DOCROOT + "shifter_EXPAND.gif";

    private String shifterCollaspe = DOCROOT + "shifter_COLLAPSE.gif";

    private String shifterEmpty = DOCROOT + "shifter_EMPTY.gif";

    private static final String LINE_INTERNODE_MASK = DOCROOT + "line_internode_mask.gif";

    public static final int ACTION_MOVE = 0;

    public static final int ACTION_COPY = 1;

    public static final int ACTION_ACTIVATE = 2;

    public static final int ACTION_DEACTIVATE = 3;

    public static final int PASTETYPE_ABOVE = 0;

    public static final int PASTETYPE_BELOW = 1;

    public static final int PASTETYPE_SUB = 2;

    public static final int PASTETYPE_LAST = 3;

    private boolean drawShifter = true;

    private String javascriptTree = "mgnlTreeControl";

    private ArrayList columns = new ArrayList();

    private ArrayList menuItems = new ArrayList();

    boolean snippetMode = true;

    private String columnResizer = DOCROOT + "columnResizer.gif";

    private static Logger log = Logger.getLogger(Tree.class);

    /**
     * Constructor.
     * @param repository name of the repository (i.e. "website", "users")
     * @param request
     */
    public Tree(String repository, HttpServletRequest request) {
        this.setRepository(repository);
        this.setRequest(request);
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
        if (s != null && !s.equals("")) {
            // this.setPathOpen(s);
            this.setPathOpen(s.substring(0, s.lastIndexOf("/")));
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
        else {
            return ("/");
        }
    }

    private void setPathCurrent(String s) {
        this.pathCurrent = s;
    }

    private String getPathCurrent() {
        return this.pathCurrent;
    }

    public void setIndentionWidth(int i) {
        this.indentionWidth = i;
    }

    public int getIndentionWidth() {
        return this.indentionWidth;
    }

    public void setItemTypes(ArrayList al) {
        this.itemTypes = al;
    }

    public ArrayList getItemTypes() {
        return this.itemTypes;
    }

    /**
     * Add a itemType to the itemTypes that will be shown in this branch.
     * @param s itemType (one of: ItemType.NT_CONTENT, ItemType.NT_CONTENTNODE, ItemType.NT_NODEDATA)
     */
    public void addItemType(String s) {
        this.getItemTypes().add(s);
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
    public void setColums(ArrayList al) {
        this.columns = al;
    }

    public ArrayList getColumns() {
        return this.columns;
    }

    public TreeColumn getColumns(int col) {
        return (TreeColumn) this.getColumns().get(col);
    }

    public void addColumn(TreeColumn tc) {
        this.getColumns().add(tc);
    }

    /**
     * Set the context menu.
     * @param al list of menu items
     */
    public void setMenuItems(ArrayList al) {
        this.menuItems = al;
    }

    public ArrayList getMenuItems() {
        return this.menuItems;
    }

    public TreeMenuItem getMenuItems(int col) {
        return (TreeMenuItem) this.getMenuItems().get(col);
    }

    public void addMenuItem(TreeMenuItem tmi) {
        this.getMenuItems().add(tmi);
    }

    /**
     * Set the name of the javascript tree object.
     * @param variableName
     */
    public void setJavascriptTree(String variableName) {
        this.javascriptTree = variableName;
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
            if (!parentPath.equals("/")) {
                path = parentPath + "/" + label;
            }
            else {
                path = "/" + label;
            }
            this.deActivateNode(path);
            parentNode.deleteContent(label);
            parentNode.save();
        }
        catch (Exception e) {
        }
    }

    public void deleteNode(String path) {
        try {
            String parentPath = path.substring(0, path.lastIndexOf("/"));
            String label = path.substring(path.lastIndexOf("/") + 1);
            deleteNode(parentPath, label);
        }
        catch (Exception e) {
        }
    }

    public void createNode(String itemType) {
        this.createNode("untitled", itemType);
    }

    public void createNode(String label, String itemType) {
        try {
            HierarchyManager hm = SessionAccessControl.getHierarchyManager(this.getRequest(), this.getRepository());
            Content parentNode = hm.getContent(this.getPath());
            String slash = "/";
            boolean isRoot = false;
            if (this.getPath().equals("/")) {
                isRoot = true;
                slash = "";
            }
            if (hm.isExist(this.getPath() + slash + label)) {
                // todo: bugfix getUniqueLabel???
                if (!isRoot) {
                    label = Path.getUniqueLabel(hm, this.getPath(), label);
                }
                else {
                    label = Path.getUniqueLabel(hm, "", label);
                }
            }
            if (itemType.equals(ItemType.NT_NODEDATA)) {
                NodeData d = parentNode.createNodeData(label);
            }
            else {
                Content newNode;
                if (itemType.equals(ItemType.NT_CONTENT)) {
                    newNode = parentNode.createContent(label);
                }
                else {
                    newNode = parentNode.createContentNode(label);
                }
                newNode.getMetaData().setAuthorId(Authenticator.getUserId(this.getRequest()));
                newNode.getMetaData().setCreationDate();
                newNode.getMetaData().setModificationDate();
                newNode.getMetaData().setSequencePosition();
                // todo: default template
                // now tmp: first template of list is taken...
                if (this.getRepository().equals(ContentRepository.WEBSITE) && itemType.equals(ItemType.NT_CONTENT)) {
                    Iterator templates = Template.getAvailableTemplates();
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
            e.printStackTrace();
        }
    }

    public String saveNodeData(String nodeDataName, String value, boolean isMeta) {
        String returnValue = "";
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
                        if (value.equals("true")) {
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
                        if (value != null && value.equals("true")) {
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
        }
        return "";
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
        String label = pathOrigin.substring(pathOrigin.lastIndexOf("/") + 1);
        String slash = "/";
        if (pathSelected.equals("/")) {
            slash = "";
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
                return "";
            }
            else {
                return touchedContent.getHandle();
            }
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
                return "";
            }
        }
        else {
            try {
                HierarchyManager hm = SessionAccessControl.getHierarchyManager(this.getRequest(), this.getRepository());
                // PASTETYPE_ABOVE | PASTETYPE_BELOW
                String pathSelectedParent = pathSelected.substring(0, pathSelected.lastIndexOf("/"));
                String pathOriginParent = pathOrigin.substring(0, pathOrigin.lastIndexOf("/"));
                if (pathSelectedParent.equals("")) {
                    slash = "";
                    pathSelectedParent = "/";
                }
                if (pathOriginParent.equals("")) {
                    pathOriginParent = "/";
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
                Iterator it1 = parentContent.getChildren(ItemType.NT_CONTENT).iterator();
                while (it1.hasNext()) {
                    Content c = (Content) it1.next();
                    if (c.getHandle().equals(selectedContent.getHandle())) {
                        selectedType = ItemType.NT_CONTENT;
                    }
                    if (c.getHandle().equals(touchedContent.getHandle())) {
                        touchedType = ItemType.NT_CONTENT;
                    }
                }
                Iterator it2 = parentContent.getChildren(ItemType.NT_CONTENTNODE).iterator();
                while (it2.hasNext()) {
                    Content c = (Content) it2.next();
                    if (c.getHandle().equals(selectedContent.getHandle())) {
                        selectedType = ItemType.NT_CONTENTNODE;
                    }
                    if (c.getHandle().equals(touchedContent.getHandle())) {
                        touchedType = ItemType.NT_CONTENTNODE;
                    }
                }
                if (touchedType.equals(ItemType.NT_NODEDATA)) {
                    return ""; // sorting not possible
                }
                long posSelected = selectedContent.getMetaData().getSequencePosition();
                long posAbove = 0;
                long posBelow = 0;
                long posFirst = 0;
                Iterator it = parentContent.getChildren(touchedType).iterator();
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
                    else {
                        posAbove = c.getMetaData().getSequencePosition();
                    }
                }
                if (touchedType != selectedType) {
                    if (touchedType.equals(ItemType.NT_CONTENTNODE) && selectedType.equals(ItemType.NT_CONTENT)) {
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
                return "";
            }
        }
    }

    public Content copyMoveNode(String source, String destination, boolean move) {
        // todo: ??? generic -> RequestInterceptor.java
        try {
            HierarchyManager hm = SessionAccessControl.getHierarchyManager(this.getRequest(), this.getRepository());
            if (hm.isExist(destination)) {
                String parentPath = destination.substring(0, destination.lastIndexOf("/"));
                String label = destination.substring(destination.lastIndexOf("/") + 1);
                label = Path.getUniqueLabel(hm, parentPath, label);
                destination = parentPath + "/" + label;
            }
            if (move) {
                if (destination.indexOf(source + "/") == 0) {
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
            }
            newContent.save();
            return newContent;
        }
        catch (Exception e) {
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
        String returnValue = "";
        try {
            HierarchyManager hm = SessionAccessControl.getHierarchyManager(this.getRequest(), this.getRepository());
            String parentPath = this.getPath().substring(0, this.getPath().lastIndexOf("/"));
            newLabel = Path.getValidatedLabel(newLabel);
            String dest = parentPath + "/" + newLabel;
            if (hm.isExist(dest)) {
                newLabel = Path.getUniqueLabel(hm, parentPath, newLabel);
                dest = parentPath + "/" + newLabel;
            }
            this.deActivateNode(this.getPath());
            hm.moveTo(this.getPath(), dest);
            SessionAccessControl.invalidateUser(this.getRequest());
            Content newPage = hm.getContent(dest);
            returnValue = newLabel;
            newPage.updateMetaData(this.getRequest());
            newPage.save();
        }
        catch (Exception e) {
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
                c = hm.getContentNode(path);
            }
            Syndicator syndicator = new Syndicator(this.getRequest());
            if (recursive) {
                deepActivate(syndicator, c, hm);
            }
            else {
                syndicator.activate(this.getRepository(), "", path, recursive);
            }
        }
        catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    private void deepActivate(Syndicator syndicator, Content content, HierarchyManager hm) {
        try {
            syndicator.activate(this.getRepository(), "", content.getHandle(), false);
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
            HierarchyManager hm = SessionAccessControl.getHierarchyManager(this.getRequest(), this.getRepository());
            Syndicator syndicator = new Syndicator(this.getRequest());
            syndicator.deActivate(this.getRepository(), path);
        }
        catch (Exception e) {
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
        // resizer
        html.append("<div id=\""
            + this.getJavascriptTree()
            + "_ColumnResizerDiv\" style=\"position:absolute;top:-50px;z-index:500;\">");
        for (int i = 1; i < this.getColumns().size(); i++) {
            // div around image: ie and safari do not allow drag of images
            // todo: fix bad behaviour in mozilla: resizer "turns blue" when selecting
            html.append("<div onmousedown=\""
                + this.getJavascriptTree()
                + ".dragColumnStart(this,"
                + i
                + ");\" id=\""
                + this.getJavascriptTree()
                + "ColumnResizer"
                + i
                + "\" style=\"position:relative;left:-1000px;background-image:url("
                + this.getColumnResizer()
                + ");display:inline;\">");
            // use resizer gif to get exact size
            html.append("<img src=\"" + this.getColumnResizer() + "\" style=\"visibility:hidden;\">");
            html.append("</div>");
        }
        html.append("</div>");
        // column header
        html.append("<div id=\""
            + this.getJavascriptTree()
            + "_ColumnHeader\" style=\"position:absolute;top:-50px;z-index:480;\">");
        for (int i = 0; i < this.getColumns().size(); i++) {
            TreeColumn tc = this.getColumns(i);
            html.append("<span class=\""
                + this.getJavascriptTree()
                + "CssClassColumn"
                + i
                + "\"><span class=\"mgnlTreeHeader\"><nobr>"
                + tc.getTitle()
                + "</nobr></span></span>");
        }
        html.append("</div>");
        // write css definitions
        html.append("<style type=text/css>");
        int numberOfColumns = this.getColumns().size();
        if (numberOfColumns == 0) {
            numberOfColumns = 1;
        }
        for (int i = 0; i < numberOfColumns; i++) {
            html.append("." + this.getJavascriptTree() + "CssClassColumn" + i);
            html.append("{position:absolute;left:0px;clip:rect(0 0 100 0);cursor:default;}");
        }
        html.append("</style>");
        html.append("<div id=\""
            + this.getJavascriptTree()
            + "_ColumnResizerLine\" style=\"position:absolute;top:0px;left:-100px;visibility:hidden;width:1px;height:"
            + this.getHeight()
            + "px;background-color:#333333;z-index:490;\"></div>");
        html.append("<div id=\""
            + this.getJavascriptTree()
            + "_"
            + this.getPath()
            + "_DivMain\" onclick=\""
            + this.getJavascriptTree()
            + ".mainDivReset();\" oncontextmenu=\""
            + this.getJavascriptTree()
            + ".menuShow(event);return false;\" class=\"mgnlTreeDiv\" style=\"height:"
            + this.getHeight()
            + "px;\">");
        html.append(new DialogSpacer().getHtml(8));
        // html.append("<div id=\""+this.getJavascriptTree()+"_"+this.getPath()+"_DivSub\" style=\"display:none;\">";
        html.append("<div id=\"" + this.getJavascriptTree() + "_" + this.getPath() + "_DivSub\">");
        return html.toString();
    }

    public String getHtmlFooter() {
        StringBuffer html = new StringBuffer();
        html.append("</div>");
        boolean permissionWrite = true;
        try {
            HierarchyManager hm = SessionAccessControl.getHierarchyManager(this.getRequest(), this.getRepository());
            Content root = hm.getContent(this.getPath());
            permissionWrite = root.isGranted(info.magnolia.cms.security.Permission.WRITE);
        }
        catch (RepositoryException re) {
        }
        // line to place a very last position
        String lineId = this.getJavascriptTree() + "_" + this.getPath() + "_LineInter";
        html.append("<div id=\""
            + lineId
            + "\" class=\"mgnlTreeLineInter\"><img src=\""
            + Tree.LINE_INTERNODE_MASK
            + "\" width=\"1\" height=\"5\">");
        html.append("<img src=\"/admindocroot/0.gif\" width=\"150\" height=\"5\" onmouseover=\""
            + this.getJavascriptTree()
            + ".moveNodeHighlightLine('"
            + lineId
            + "');\" onmouseout=\""
            + this.getJavascriptTree()
            + ".moveNodeResetLine('"
            + lineId
            + "');\" onmousedown=\""
            + this.getJavascriptTree()
            + ".pasteNode('"
            + this.getPath()
            + "',"
            + Tree.PASTETYPE_SUB
            + ","
            + Boolean.toString(permissionWrite)
            + ",'"
            + lineId
            + "');\">");
        html.append("</div>");
        html.append(new Hidden(this.getJavascriptTree() + "_" + this.getPath() + "_PermissionWrite", Boolean
            .toString(permissionWrite), false).getHtml());
        html.append("</div>");
        // address bar
        String pathOpen = this.getPathOpen();
        if (pathOpen == null) {
            pathOpen = "";
        }
        html.append(new DialogSpacer().getHtml(3));
        html.append("<input id=\""
            + this.getJavascriptTree()
            + "_AddressBar\" type=\"text\" onkeydown=\"if (mgnlIsKeyEnter(event)) "
            + this.getJavascriptTree()
            + ".expandNode(this.value);\" class=\"mgnlDialogControlEdit\" style=\"width:100%;\" value=\""
            + pathOpen
            + "\">");
        // menu
        StringBuffer menuJavascript = new StringBuffer();
        if (this.getMenuItems().size() != 0) {
            html.append("<div id=\"" + this.getJavascriptTree() + "_DivMenu\" class=\"mgnlTreeMenu\">");
            int counter = 0;
            for (int i = 0; i < this.getMenuItems().size(); i++) {
                TreeMenuItem item = (TreeMenuItem) this.getMenuItems(i);
                if (item == null) {
                    html.append("<div class=\"mgnlTreeMenuLine\">");
                    html.append("<img src=\"/admindocroot/0.gif\" width=\"1\" height=\"1\"></div>");
                }
                else {
                    item.setJavascriptTree(this.getJavascriptTree());
                    String id = this.getJavascriptTree() + "_MenuItem" + i;
                    item.setId(id);
                    menuJavascript.append(this.getJavascriptTree()
                        + ".menuItems["
                        + counter
                        + "]=new mgnlTreeMenuItem('"
                        + id
                        + "');\n");
                    menuJavascript.append(this.getJavascriptTree()
                        + ".menuItems["
                        + counter
                        + "].conditions=new Object();");
                    for (int cond = 0; cond < item.getJavascriptConditions().size(); cond++) {
                        menuJavascript.append(this.getJavascriptTree()
                            + ".menuItems["
                            + counter
                            + "].conditions["
                            + cond
                            + "]="
                            + item.getJavascriptCondition(cond)
                            + ";");
                    }
                    html.append(item.getHtml());
                    counter++;
                }
            }
            html.append("</div>");
        }
        // shadow for moving pages
        html.append("<div id=\"");
        html.append(this.getJavascriptTree());
        html.append("_MoveShadow\" ");
        html.append("style=\"position:absolute;top:0px;left:0px;visibility:hidden;background-color:#fff;\"></div>");

        // "move denied"
        html.append("<img src=\""
            + Tree.DOCROOT
            + "move_denied.gif\" id=\""
            + this.getJavascriptTree()
            + "_MoveDenied\" style=\"position:absolute;top:0px;left:0px;visibility:hidden;\">");
        // initialize js tree object
        html.append("<script>");
        html.append("var "
            + this.getJavascriptTree()
            + "=new mgnlTree('"
            + this.getRepository()
            + "','"
            + this.getPath()
            + "','"
            + this.getJavascriptTree()
            + "',"
            + this.getHeight()
            + ");");
        // html.append(this.getJavascriptTree()+".columns=new Array();"); //->in tree.js
        // add menu to tree object
        html.append(menuJavascript);
        // add columns to tree object
        for (int i = 0; i < this.getColumns().size(); i++) {
            TreeColumn tc = (TreeColumn) this.getColumns(i);
            html.append(this.getJavascriptTree()
                + ".columns["
                + i
                + "]=new mgnlTreeColumn("
                + tc.getWidth()
                + ",'"
                + tc.getHtmlEdit()
                + "','"
                + tc.getName()
                + "',"
                + tc.getIsMeta()
                + ","
                + tc.getIsLabel()
                + ","
                + tc.getIsNodeDataValue()
                + ","
                + tc.getIsNodeDataType()
                + ");");
        }
        html.append("mgnlTreeControls['" + this.getJavascriptTree() + "']=" + this.getJavascriptTree() + ";");
        // js is not run on remote request
        html.append(this.getJavascriptTree() + ".selectNode('" + this.getPathSelected() + "');");
        html.append("</script>");
        return html.toString();
    }

    public String getHtmlBranch() {
        String html = "";
        return html;
    }

    public String getHtmlChildren() {
        StringBuffer html = new StringBuffer();
        HierarchyManager hm = SessionAccessControl.getHierarchyManager(this.getRequest(), this.getRepository());
        Content parentNode = null;
        try {
            parentNode = hm.getPage(this.getPathCurrent());
            // loop the children of the different item types
            for (int i = 0; i < this.getItemTypes().size(); i++) {
                String type = (String) this.getItemTypes().get(i);
                html.append(this.getHtmlChildrenOfOneType(parentNode, type));
            }
        }
        catch (RepositoryException re) {
        }
        return html.toString();
    }

    public String getHtmlChildrenOfOneType(Content parentNode, String itemType) {
        StringBuffer html = new StringBuffer();
        String icon;
        if (itemType.equals(ItemType.NT_CONTENT)) {
            icon = this.getIconPage();
        }
        else if (itemType.equals(ItemType.NT_CONTENTNODE)) {
            icon = this.getIconContentNode();
        }
        else if (itemType.equals(ItemType.NT_NODEDATA)) {
            icon = this.getIconNodeData();
        }
        else {
            icon = "";
        }
        try {
            // todo: parentNode - level of this.getPath
            int left = (parentNode.getLevel()) * this.getIndentionWidth();
            Iterator it = parentNode.getChildren(itemType).iterator();
            while (it.hasNext()) {
                Object o = it.next();
                Content c = null;
                NodeData d = null;
                String handle = "";
                String name = "";
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
                        if (c.getChildren(type).size() > 0) {
                            hasSub = true;
                            if (this.getPathOpen() != null
                                && (this.getPathOpen().indexOf(handle + "/") == 0 || this.getPathOpen().equals(handle))) {
                                showSub = true;
                            }
                            break;
                        }
                    }
                }
                String idPre = this.getJavascriptTree() + "_" + handle;
                String jsHighlightNode = this.getJavascriptTree()
                    + ".nodeHighlight(this,'"
                    + handle
                    + "',"
                    + Boolean.toString(permissionWrite)
                    + ");";
                String jsResetNode = this.getJavascriptTree() + ".nodeReset(this,'" + handle + "');";
                String jsSelectNode = this.getJavascriptTree()
                    + ".selectNode('"
                    + handle
                    + "',"
                    + Boolean.toString(permissionWrite)
                    + ",'"
                    + itemType
                    + "');";
                String jsExpandNode;
                if (this.getDrawShifter()) {
                    jsExpandNode = this.getJavascriptTree() + ".expandNode('" + handle + "');";
                }
                else {
                    jsExpandNode = jsSelectNode;
                }
                String jsHighlightLine = this.getJavascriptTree()
                    + ".moveNodeHighlightLine('"
                    + idPre
                    + "_LineInter');";
                String jsResetLine = this.getJavascriptTree() + ".moveNodeResetLine('" + idPre + "_LineInter');";
                // lineInter: line between nodes, to allow set cursor between nodes
                html.append("<div id=\"" + idPre + "_LineInter\" class=\"mgnlTreeLineInter\">");
                int maskWidth = left;
                if (maskWidth < 1) {
                    maskWidth = 1;
                }
                html.append("<img src=\"" + Tree.LINE_INTERNODE_MASK + "\" width=\"" + maskWidth + "\" height=\"5\">");
                if (permissionWriteParent) {
                    html.append("<img src=\"/admindocroot/0.gif\" height=\"5\" width=\"150\" onmouseover=\""
                        + jsHighlightLine
                        + "\" onmouseout=\""
                        + jsResetLine
                        + "\" onmousedown=\""
                        + this.getJavascriptTree()
                        + ".pasteNode('"
                        + handle
                        + "',"
                        + Tree.PASTETYPE_ABOVE
                        + ",true);\">");
                }
                html.append("</div>");
                html.append("<div id=\""
                    + idPre
                    + "_DivMain\" style=\"position:relative;top:0;left:0;width:100%;height:18px;\">");
                html.append("&nbsp;"); // do not remove!
                int paddingLeft = left;
                if (paddingLeft < 0) {
                    paddingLeft = 0;
                }
                html.append("<nobr><span id=\""
                    + idPre
                    + "_Column0Outer\" class="
                    + this.getJavascriptTree()
                    + "CssClassColumn0 style=\"padding-left:"
                    + paddingLeft
                    + "px;\">");
                if (this.getDrawShifter()) {
                    String shifter = "";
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
                    if (!shifter.equals("")) {
                        html.append("<img id=\""
                            + idPre
                            + "_Shifter\" onmousedown=\""
                            + this.getJavascriptTree()
                            + ".shifterDown('"
                            + handle
                            + "');\" onmouseout=\""
                            + this.getJavascriptTree()
                            + ".shifterOut();\" class=mgnlTreeShifter src=\""
                            + shifter
                            + "\">");
                    }
                }
                html.append("<span id="
                    + idPre
                    + "_Name onmouseover=\""
                    + jsHighlightNode
                    + "\" onmouseout=\""
                    + jsResetNode
                    + "\" onmousedown=\""
                    + jsSelectNode
                    + this.getJavascriptTree()
                    + ".pasteNode('"
                    + handle
                    + "',"
                    + Tree.PASTETYPE_SUB
                    + ","
                    + permissionWrite
                    + ");\">");
                if (!icon.equals("")) {
                    html.append("<img id=\""
                        + idPre
                        + "_Icon\" class=\"mgnlTreeIcon\" src=\""
                        + icon
                        + "\" onmousedown=\""
                        + jsExpandNode
                        + "\"");
                    if (this.getIconOndblclick() != null) {
                        html.append(" ondblclick=\"" + this.getIconOndblclick() + "\"");
                    }
                    html.append(">");
                }
                String dblclick = "";
                if (permissionWrite && !this.getColumns(0).getHtmlEdit().equals("")) {
                    dblclick = " ondblclick=\""
                        + this.getJavascriptTree()
                        + ".editNodeData(this,'"
                        + handle
                        + "',0);\"";
                }
                html.append("<span class=\"mgnlTreeText\" id=\"" + idPre + "_Column0Main\"" + dblclick + ">");
                html.append(name);
                html.append("</span></span></span></nobr>");
                html.append(new Hidden(idPre + "_PermissionWrite", Boolean.toString(permissionWrite), false).getHtml());
                html.append(new Hidden(idPre + "_ItemType", itemType, false).getHtml());
                html.append(new Hidden(idPre + "_IsActivated", Boolean.toString(isActivated), false).getHtml());
                for (int i = 1; i < this.getColumns().size(); i++) {
                    String str = "";
                    TreeColumn tc = (TreeColumn) this.getColumns(i);
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
                        if (str.equals("")) {
                            str = TreeColumn.EMPTY;
                        }
                        tc.setName(name); // workaround, will be passed to js TreeColumn object
                    }
                    tc.setEvent("onmouseover", jsHighlightNode, true);
                    tc.setEvent("onmouseout", jsResetNode, true);
                    tc.setEvent("onmousedown", jsSelectNode, true);
                    html.append("<nobr><span class=\""
                        + this.getJavascriptTree()
                        + "CssClassColumn"
                        + i
                        + "\"><span id=\""
                        + idPre
                        + "_Column"
                        + i
                        + "Main\""
                        + tc.getHtmlCssClass()
                        + tc.getHtmlEvents());
                    if (permissionWrite && !tc.getHtmlEdit().equals("")) {
                        html.append(" ondblclick=\""
                            + this.getJavascriptTree()
                            + ".editNodeData(this,'"
                            + handle
                            + "',"
                            + i
                            + ");\"");
                    }
                    html.append(">" + str + "</span></span></nobr>");
                }
                html.append("</div>");
                String display = "none";
                if (showSub) {
                    display = "block";
                }
                html.append("<div id=\"" + idPre + "_DivSub\" style=\"display:" + display + ";\">");
                if (hasSub) {
                    if (showSub) {
                        String pathRemaining = this.getPathOpen().substring(this.getPathCurrent().length());
                        if (pathRemaining.length() > 0) {
                            // get rid of first slash (/people/franz -> people/franz)
                            String slash = "/";
                            if (this.getPathCurrent().equals("/")) {
                                // first slash already removed
                                slash = ""; // no slash needed between pathCurrent and nextChunk
                            }
                            else {
                                pathRemaining = pathRemaining.substring(1);
                            }
                            String nextChunk;
                            if (pathRemaining.indexOf("/") != -1) {
                                nextChunk = pathRemaining.substring(0, pathRemaining.indexOf("/"));
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
                html.append("</div>\n");
            }
        }
        catch (RepositoryException re) {
        }
        return html.toString();
    }
}
