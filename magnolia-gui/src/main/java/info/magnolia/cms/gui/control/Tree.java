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
package info.magnolia.cms.gui.control;

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.beans.config.Template;
import info.magnolia.cms.beans.config.TemplateManager;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.core.ItemType;
import info.magnolia.cms.core.NodeData;
import info.magnolia.cms.core.Path;
import info.magnolia.cms.security.Authenticator;
import info.magnolia.cms.util.FreeMarkerUtil;
import info.magnolia.cms.util.MetaDataUtil;
import info.magnolia.cms.util.NodeDataUtil;
import info.magnolia.context.MgnlContext;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.Value;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author Vinzenz Wyser
 * @version 2.0
 */
public class Tree extends ControlImpl {

    public static final String DOCROOT = "/.resources/controls/tree/"; //$NON-NLS-1$

    public static final String ICONDOCROOT = "/.resources/icons/16/"; //$NON-NLS-1$
    
    public static final String DEFAULT_ICON = ICONDOCROOT + "cubes.gif";

    public static final String DEFAULT_ICON_CONTENT = ICONDOCROOT + "document_plain_earth.gif";

    public static final String DEFAULT_ICON_CONTENTNODE = DEFAULT_ICON;

    public static final String DEFAULT_ICON_NODEDATA = ICONDOCROOT + "cube_green.gif";

    public static final String ITEM_TYPE_NODEDATA = "mgnl:nodeData";

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
    private static Logger log = LoggerFactory.getLogger(Tree.class);

    private String repository;

    private String pathOpen;

    private String pathCurrent;

    private String pathSelected;

    private int indentionWidth = 15;

    private List itemTypes = new ArrayList();

    private int height = 400;

    private Map icons = new HashMap();

    private String iconOndblclick;

    private String shifterExpand = DOCROOT + "shifter_EXPAND.gif"; //$NON-NLS-1$

    private String shifterCollaspe = DOCROOT + "shifter_COLLAPSE.gif"; //$NON-NLS-1$

    private String shifterEmpty = DOCROOT + "shifter_EMPTY.gif"; //$NON-NLS-1$

    private boolean drawShifter = true;

    private String javascriptTree = "mgnlTreeControl"; //$NON-NLS-1$

    private List columns = new ArrayList();

    private ContextMenu menu;

    private Comparator sortComparator;

    /**
     * the bar at the bottom of the page holding some function buttons
     */
    private FunctionBar functionBar;

    private boolean snippetMode = true;

    private String columnResizer = DOCROOT + "columnResizer.gif"; //$NON-NLS-1$

    private boolean browseMode;

    /**
     * Use the getter method to access this HierarchyManager.
     */
    private HierarchyManager hm;

    /**
     * Constructor.
     * @param name name of the tree (name of the treehandler)
     * @param repository name of the repository (i.e. "website", "users")
     * @param request
     */
    public Tree(String name, String repository, HttpServletRequest request) {
        this.setName(name);
        this.setRepository(repository);
        this.setRequest(request);
        this.setMenu(new ContextMenu(this.getJavascriptTree()));
        this.setFunctionBar(new FunctionBar(this.getJavascriptTree()));

        this.setHierarchyManager(MgnlContext.getHierarchyManager(this.getRepository()));

        // set default icons
        this.setIcon(ItemType.CONTENT.getSystemName(), DEFAULT_ICON_CONTENT);
        this.setIcon(ItemType.CONTENTNODE.getSystemName(), DEFAULT_ICON_CONTENTNODE);
        this.setIcon(ITEM_TYPE_NODEDATA, DEFAULT_ICON_NODEDATA);
    }

    /**
     * Constructor: the name of the tree is the same as the name of the repository
     * @param repository
     * @param request
     * @deprecated use Tree(name, repository, request) instead
     */
    public Tree(String repository, HttpServletRequest request) {
        this(repository, repository, request);
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
            if (StringUtils.isEmpty(this.getPathOpen())) {
                this.setPathOpen(StringUtils.substringBeforeLast(s, "/")); //$NON-NLS-1$
            }
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
        this.setIcon(ItemType.CONTENT.getSystemName(), src);
    }

    public String getIconPage() {
        return this.getIcon(ItemType.CONTENT.getSystemName());
    }

    /**
     * Set the icon of content nodes.
     * @param src source of the image
     */
    public void setIconContentNode(String src) {
        this.setIcon(ItemType.CONTENTNODE.getSystemName(), src);
    }

    public String getIconContentNode() {
        return this.getIcon(ItemType.CONTENTNODE.getSystemName());
    }

    /**
     * Set the icon of node data.
     * @param src source of the image
     */
    public void setIconNodeData(String src) {
        this.setIcon(ITEM_TYPE_NODEDATA, src);
    }

    public String getIconNodeData() {
        return this.getIcon(ITEM_TYPE_NODEDATA);
    }

    protected String getIcon(String itemType) {
        return (String) icons.get(itemType);
    }

    protected String getIcon(Content node, NodeData nodedata, String itemType) {
        if(icons.containsKey(itemType)){
            return (String) icons.get(itemType);
        }
        return DEFAULT_ICON;
    }

    public void setIcon(String typeName, String icon) {
        icons.put(typeName, icon);
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

    public String createNode(String itemType) {
        return this.createNode("untitled", itemType); //$NON-NLS-1$
    }

    /**
     * Creates a new node (either <code>NodeData</code> or <code>Content</code>) with the specified name (<tt>label</tt>)
     * and type.
     * @param label new node name
     * @param itemType new node type
     */
    public String createNode(String label, String itemType) {
        String name = label;
        try {
            Content parentNode = getHierarchyManager().getContent(this.getPath());
            String slash = "/"; //$NON-NLS-1$
            boolean isRoot = false;
            if (this.getPath().equals("/")) { //$NON-NLS-1$
                isRoot = true;
                slash = StringUtils.EMPTY;
            }
            if (getHierarchyManager().isExist(this.getPath() + slash + label)) {
                // todo: bugfix getUniqueLabel???
                if (!isRoot) {
                    name = Path.getUniqueLabel(getHierarchyManager(), this.getPath(), label);
                }
                else {
                    name = Path.getUniqueLabel(getHierarchyManager(), StringUtils.EMPTY, label);
                }
            }
            if (itemType.equals(ITEM_TYPE_NODEDATA)) {
                parentNode.createNodeData(name);
            }
            else {
                Content newNode;
                newNode = parentNode.createContent(name, itemType);
                newNode.getMetaData().setAuthorId(Authenticator.getUserId(this.getRequest()));
                newNode.getMetaData().setCreationDate();
                newNode.getMetaData().setModificationDate();
                // todo: default template
                if (this.getRepository().equals(ContentRepository.WEBSITE)
                    && itemType.equals(ItemType.CONTENT.getSystemName())) {

                	// default to the template used by the parent node
                	String newTemplate = parentNode.getTemplate();
                	if (StringUtils.isEmpty(newTemplate)) {
	                    // if that fails then first template of list is taken...
	                    Iterator templates = TemplateManager.getInstance().getAvailableTemplates(
                                    MgnlContext.getAccessManager(ContentRepository.CONFIG));
	                    if (templates.hasNext()) {
	                        Template template = (Template) templates.next();
	                        newTemplate = template.getName();
	                    }
                    }
                    newNode.getMetaData().setTemplate(newTemplate);
                }
            }
            parentNode.save();
        }
        catch (Exception e) {
            log.error(e.getMessage(), e);
            name = ""; // reset the name, so that you can check if the node was created
        }

        return name;
    }

    public String saveNodeData(String nodeDataName, String value, boolean isMeta) {
        String returnValue = StringUtils.EMPTY;
        try {
            Content page = getHierarchyManager().getContent(this.getPath());
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
                page.updateMetaData();
                page.save();
                returnValue = new NodeDataUtil(node).getValueString();
            }
            else {
                page.getMetaData().setProperty(nodeDataName, value);
                page.updateMetaData();
                page.save();
                returnValue = new MetaDataUtil(page).getPropertyValueString(nodeDataName);
            }
        }
        catch (Exception e) {
            if (log.isDebugEnabled()) {
                log.debug("Exception caught: " + e.getMessage(), e); //$NON-NLS-1$
            }
        }
        return returnValue;
    }

    public String saveNodeDataType(String nodeDataName, int type) {
        try {
            Content page = getHierarchyManager().getContent(this.getPath());
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
            page.updateMetaData();
            page.save();
            return PropertyType.nameFromValue(page.getNodeData(nodeDataName).getType());
            // return PropertyType.nameFromValue(node.getType());
        }
        catch (Exception e) {
            if (log.isDebugEnabled()) {
                log.debug("Exception caught: " + e.getMessage(), e); //$NON-NLS-1$
            }
        }
        return StringUtils.EMPTY;
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

        StringBuffer str = new StringBuffer();
        try {
            Map params = populateTemplateParameters();
            str.append(FreeMarkerUtil.process("info/magnolia/cms/gui/control/TreeHeader.ftl", params));
        }
        catch (Exception e) {
            log.error("can't render tree header", e);
        }

        return str.toString();
    }

    public String getHtmlFooter() {
        StringBuffer html = new StringBuffer();
        html.append("</div>"); //$NON-NLS-1$

        Map params = populateTemplateParameters();

        // include the tree footer / menu divs
        html.append(FreeMarkerUtil.process("info/magnolia/cms/gui/control/TreeFooter.ftl", params));

        return html.toString();
    }

    protected Map populateTemplateParameters() {
        boolean permissionWrite = true;
        try {
            Content root = getHierarchyManager().getContent(this.getPath());
            permissionWrite = root.isGranted(info.magnolia.cms.security.Permission.WRITE);
        }
        catch (RepositoryException e) {
            if (log.isDebugEnabled()) {
                log.debug("Exception caught: " + e.getMessage(), e); //$NON-NLS-1$
            }
        }

        // lineInter: line between nodes, to allow set cursor between nodes
        // line to place a very last position
        String lineId = this.getJavascriptTree() + "_" + this.getPath() + "_LineInter"; //$NON-NLS-1$ //$NON-NLS-2$

        // prepare the data for the templates
        Map params = new HashMap();
        params.put("tree", this);
        params.put("PASTETYPE_SUB", new Integer(Tree.PASTETYPE_SUB));
        params.put("DOCROOT", Tree.DOCROOT);
        params.put("lineId", lineId);
        params.put("permissionWrite", Boolean.valueOf(permissionWrite));
        params.put("columns", this.getColumns());
        params.put("menu", this.getMenu());
        params.put("treeCssClass", "mgnlTreeDiv");
        if (this.isBrowseMode()) {
            params.put("treeCssClass", "mgnlTreeBrowseModeDiv mgnlTreeDiv");
        }
        return params;
    }

    public String getHtmlBranch() {
        return StringUtils.EMPTY;
    }

    public String getHtmlChildren() {
        StringBuffer html = new StringBuffer();
        Content parentNode;
        try {
            parentNode = getHierarchyManager().getContent(this.getPathCurrent());
            // loop the children of the different item types
            for (int i = 0; i < this.getItemTypes().size(); i++) {
                String type = (String) this.getItemTypes().get(i);
                if (hasSub(parentNode, type)) {
                    html.append(this.getHtmlChildrenOfOneType(parentNode, type));
                }
            }
        }
        catch (RepositoryException e) {
            if (log.isDebugEnabled()) {
                log.debug("Exception caught: " + e.getMessage(), e); //$NON-NLS-1$
            }
        }
        return html.toString();
    }

    public String getHtmlChildrenOfOneType(Content parentNode, String itemType) {
        StringBuffer html = new StringBuffer();
        try {
            Iterator it;
            if (itemType.equalsIgnoreCase(ITEM_TYPE_NODEDATA)) {
                List nodeDatas = new ArrayList(parentNode.getNodeDataCollection());
                // order them alphabetically
                Collections.sort(nodeDatas, new Comparator() {

                    public int compare(Object arg0, Object arg1) {
                        return ((NodeData) arg0).getName().compareTo(((NodeData) arg1).getName());
                    }
                });
                it = nodeDatas.iterator();
            }
            else {
                Collection nodes = parentNode.getChildren(itemType);
                Comparator comp = this.getSortComparator();
                if(comp != null){
                    List sortedNodes = new ArrayList(nodes);
                    Collections.sort(sortedNodes, comp);
                    nodes = sortedNodes;
                }
                it = nodes.iterator();
            }
            while (it.hasNext()) {
                Object o = it.next();
                Content c = null;
                NodeData d = null;
                String handle;
                String name;
                boolean hasSub = false;
                boolean showSub = false;
                boolean isActivated = false;
                boolean permissionWrite = false;
                boolean permissionWriteParent = false;
                if (itemType.equals(ITEM_TYPE_NODEDATA)) {
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
                    isActivated = c.getMetaData().getIsActivated();
                    for (int i = 0; i < this.getItemTypes().size(); i++) {
                        String type = (String) this.getItemTypes().get(i);

                        hasSub = hasSub(c, type);

                        if (hasSub) {
                            if (this.getPathOpen() != null
                                && (this.getPathOpen().indexOf(handle + "/") == 0 || this.getPathOpen().equals(handle))) { //$NON-NLS-1$
                                showSub = true;
                            }
                            break;
                        }
                    }
                }

                // get next if this node is not shown
                if (!showNode(c, d, itemType)) {
                    continue;
                }

                String icon = getIcon(c, d, itemType);

                String idPre = this.javascriptTree + "_" + handle; //$NON-NLS-1$
                String jsHighlightNode = this.javascriptTree + ".nodeHighlight(this,'" //$NON-NLS-1$
                    + handle
                    + "'," //$NON-NLS-1$
                    + Boolean.toString(permissionWrite)
                    + ");"; //$NON-NLS-1$
                String jsResetNode = this.javascriptTree + ".nodeReset(this,'" + handle + "');"; //$NON-NLS-1$ //$NON-NLS-2$
                String jsSelectNode = this.javascriptTree + ".selectNode('" //$NON-NLS-1$
                    + handle
                    + "'," //$NON-NLS-1$
                    + Boolean.toString(permissionWrite)
                    + ",'" //$NON-NLS-1$
                    + itemType
                    + "');"; //$NON-NLS-1$
                String jsExpandNode;
                if (this.getDrawShifter()) {
                    jsExpandNode = this.javascriptTree + ".expandNode('" + handle + "');"; //$NON-NLS-1$ //$NON-NLS-2$
                }
                else {
                    jsExpandNode = jsSelectNode;
                }
                String jsHighlightLine = this.javascriptTree + ".moveNodeHighlightLine('" + idPre + "_LineInter');"; //$NON-NLS-1$ //$NON-NLS-2$
                String jsResetLine = this.javascriptTree + ".moveNodeResetLine('" + idPre + "_LineInter');"; //$NON-NLS-1$ //$NON-NLS-2$

                // lineInter: line between nodes, to allow set cursor between nodes
                // try to avoid blank images, setting js actions on divs should be ok
                if (permissionWriteParent) {
                    html.append("<div id=\"");
                    html.append(idPre);
                    html.append("_LineInter\" class=\"mgnlTreeLineInter mgnlLineEnabled\" onmouseover=\"");
                    html.append(jsHighlightLine);
                    html.append("\" onmouseout=\"");
                    html.append(jsResetLine);
                    html.append("\" onmousedown=\"");
                    html.append(this.javascriptTree);
                    html.append(".pasteNode('");
                    html.append(handle);
                    html.append("'," + Tree.PASTETYPE_ABOVE + ",true);\" ></div>");
                }
                else {
                    html.append("<div id=\"");
                    html.append(idPre);
                    html.append("_LineInter\" class=\"mgnlTreeLineInter mgnlLineDisabled\"></div>");
                }

                html.append("<div id=\"");
                html.append(idPre);
                html.append("_DivMain\" style=\"position:relative;top:0;left:0;width:100%;height:18px;\">");
                html.append("&nbsp;"); // do not remove! //$NON-NLS-1$

                html.append("<span id=\"");
                html.append(idPre);
                html.append("_Column0Outer\" class=\"mgnlTreeColumn ");
                html.append(this.javascriptTree);
                html.append("CssClassColumn0\" style=\"padding-left:");

                html.append(getPaddingLeft(parentNode));
                html.append("px;\">");
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
                        html.append("<img id=\"");
                        html.append(idPre);
                        html.append("_Shifter\" onmousedown=\"");
                        html.append(this.javascriptTree);
                        html.append(".shifterDown('");
                        html.append(handle);
                        html.append("');\" onmouseout=\"");
                        html.append(this.javascriptTree);
                        html.append(".shifterOut();\" class=\"mgnlTreeShifter\" src=\"");
                        html.append(this.getRequest().getContextPath());
                        html.append(shifter);
                        html.append("\" />");
                    }
                }
                html.append("<span id=");
                html.append(idPre);
                html.append("_Name onmouseover=\"");
                html.append(jsHighlightNode);
                html.append("\" onmouseout=\"");
                html.append(jsResetNode);
                html.append("\" onmousedown=\"");
                html.append(jsSelectNode);
                html.append(this.javascriptTree);
                html.append(".pasteNode('");
                html.append(handle);
                html.append("'," + Tree.PASTETYPE_SUB + ",");
                html.append(permissionWrite);
                html.append(");\">");
                if (StringUtils.isNotEmpty(icon)) {
                    html.append("<img id=\"");
                    html.append(idPre);
                    html.append("_Icon\" class=\"mgnlTreeIcon\" src=\"");
                    html.append(this.getRequest().getContextPath());
                    html.append(icon);
                    html.append("\" onmousedown=\"");
                    html.append(jsExpandNode);
                    html.append("\"");
                    if (this.getIconOndblclick() != null) {
                        html.append(" ondblclick=\"");
                        html.append(this.getIconOndblclick());
                        html.append("\"");
                    }
                    html.append(" />"); //$NON-NLS-1$
                }
                String dblclick = StringUtils.EMPTY;
                if (permissionWrite && StringUtils.isNotEmpty(this.getColumns(0).getHtmlEdit())) {
                    dblclick = " ondblclick=\"" + this.javascriptTree + ".editNodeData(this,'" + handle + "',0);\""; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                }
                html.append("<span class=\"mgnlTreeText\" id=\"");
                html.append(idPre);
                html.append("_Column0Main\"");
                html.append(dblclick);
                html.append(">");
                html.append(name);
                html.append("</span></span></span>"); //$NON-NLS-1$
                html.append(new Hidden(idPre + "_PermissionWrite", Boolean.toString(permissionWrite), false).getHtml()); //$NON-NLS-1$
                html.append(new Hidden(idPre + "_ItemType", itemType, false).getHtml()); //$NON-NLS-1$
                html.append(new Hidden(idPre + "_IsActivated", Boolean.toString(isActivated), false).getHtml()); //$NON-NLS-1$
                for (int i = 1; i < this.getColumns().size(); i++) {
                    String str = StringUtils.EMPTY;
                    TreeColumn tc = this.getColumns(i);
                    if (!itemType.equals(ITEM_TYPE_NODEDATA)) {
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
                            str = StringEscapeUtils.escapeXml(util.getValueString());
                        }
                        if (StringUtils.isEmpty(str)) {
                            str = TreeColumn.EMPTY;
                        }
                        tc.setName(name); // workaround, will be passed to js TreeColumn object
                    }
                    tc.setEvent("onmouseover", jsHighlightNode, true); //$NON-NLS-1$
                    tc.setEvent("onmouseout", jsResetNode, true); //$NON-NLS-1$
                    tc.setEvent("onmousedown", jsSelectNode, true); //$NON-NLS-1$
                    html.append("<span class=\"mgnlTreeColumn ");
                    html.append(this.javascriptTree);
                    html.append("CssClassColumn");
                    html.append(i);
                    html.append("\"><span id=\"");
                    html.append(idPre);
                    html.append("_Column");
                    html.append(i);
                    html.append("Main\"");
                    html.append(tc.getHtmlCssClass());
                    html.append(tc.getHtmlEvents());
                    if (permissionWrite && StringUtils.isNotEmpty(tc.getHtmlEdit())) {
                        html.append(" ondblclick=\"");
                        html.append(this.javascriptTree);
                        html.append(".editNodeData(this,'");
                        html.append(handle);
                        html.append("',");
                        html.append(i);
                        html.append(");\"");
                    }
                    html.append(">");
                    html.append(str);
                    html.append("</span></span>");
                }
                html.append("</div>"); //$NON-NLS-1$
                String display = "none"; //$NON-NLS-1$
                if (showSub) {
                    display = "block"; //$NON-NLS-1$
                }
                html.append("<div id=\"");
                html.append(idPre);
                html.append("_DivSub\" style=\"display:");
                html.append(display);
                html.append(";\">");
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
                            String nextChunk = StringUtils.substringBefore(pathRemaining, "/"); //$NON-NLS-1$

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
            if (log.isDebugEnabled()) {
                log.debug("Exception caught: " + e.getMessage(), e); //$NON-NLS-1$
            }
        }
        return html.toString();
    }

    protected int getPaddingLeft(Content parentNode) throws RepositoryException {
        int left = parentNode.getLevel() * this.getIndentionWidth();
        int paddingLeft = left + 8;
        if (paddingLeft < 8) {
            paddingLeft = 8;
        }
        return paddingLeft;
    }

    protected boolean hasSub(Content c, String type) {
        int size;
        if (type.equalsIgnoreCase(ITEM_TYPE_NODEDATA)) {
            size = c.getNodeDataCollection().size();
        }
        else {
            size = c.getChildren(type).size();
        }
        return size > 0;
    }

    /**
     * Override to make special exclusions. The current nodedata or node is passed.
     * @param node
     * @param nodedata
     * @param itemType
     * @return true if the node is shown
     */
    private boolean showNode(Content node, NodeData nodedata, String itemType) {
        return true;
    }

    /**
     * @param item ContextMenuItem
     */
    public void addMenuItem(ContextMenuItem item) {
        menu.addMenuItem(item);
    }

    /**
     * @param item FunctionBarItem
     */
    public void addFunctionBarItem(FunctionBarItem item) {
        if (item != null) {
            item.setJavascriptMenuName(functionBar.getJavascriptName());
        }
        functionBar.addMenuItem(item);
    }

    /**
     * Add a separator line between context menu items.
     */
    public void addSeparator() {
        menu.addMenuItem(null);
    }

    public ContextMenu getMenu() {
        return this.menu;
    }

    /**
     * @return the function bar object
     */
    public FunctionBar getFunctionBar() {
        return this.functionBar;
    }

    public void setMenu(ContextMenu menu) {
        this.menu = menu;
    }

    /**
     * @param functionBar the function bar object
     */
    public void setFunctionBar(FunctionBar functionBar) {
        this.functionBar = functionBar;
    }

    /**
     * @return Returns the browseMode.
     */
    public boolean isBrowseMode() {
        return browseMode;
    }

    /**
     * @param browseMode The browseMode to set.
     */
    public void setBrowseMode(boolean browseMode) {
        this.browseMode = browseMode;
    }

    /**
     * @return the current HierarchyManager
     */
    public HierarchyManager getHierarchyManager() {
        return hm;
    }

    /**
     * @param hm The HierarchyManager to set.
     */
    private void setHierarchyManager(HierarchyManager hm) {
        this.hm = hm;
    }


    public Comparator getSortComparator() {
        return sortComparator;
    }


    public void setSortComparator(Comparator sortComperator) {
        this.sortComparator = sortComperator;
    }
}
