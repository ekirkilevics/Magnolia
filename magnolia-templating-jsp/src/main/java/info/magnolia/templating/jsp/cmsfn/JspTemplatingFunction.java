/**
 * This file Copyright (c) 2010-2011 Magnolia International
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
package info.magnolia.templating.jsp.cmsfn;

import info.magnolia.cms.util.SiblingsHelper;
import info.magnolia.jcr.util.ContentMap;
import info.magnolia.objectfactory.Components;
import info.magnolia.templating.functions.TemplatingFunctions;

import java.util.Collection;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;

import org.tldgen.annotations.Function;


/**
 * Static wrapper of the Templating function class.
 * This wrapper gives us the ability to expose these functions as Tags in the JSP's.
 * @version $Id$
 *
 */

public class JspTemplatingFunction {


    private static TemplatingFunctions templatingFunctions ;

    /**
     * Convenience method.
     * Currently, TemplatingFunction (singleton) is accessed once, but in case of we need to change
     * this and to access it each time, this could be done here (discussed with Greg and Philipp).
     * Initial Impl:
     *  private static TemplatingFunctions templatingFunctions = Components.getComponent(TemplatingFunctions.class);
     */
    private static TemplatingFunctions getTemplatingFunctions() {
       if( templatingFunctions==null ) {
           templatingFunctions = Components.getComponent(TemplatingFunctions.class);
       }
        return templatingFunctions;
    }

    @Function
    public static Node asJCRNode(ContentMap contentMap) {
        return getTemplatingFunctions().asJCRNode(contentMap);
    }

    @Function
    public static ContentMap asContentMap(Node content) {
        return getTemplatingFunctions().asContentMap(content);
    }

    @Function
    public static  List<ContentMap> children(ContentMap content, String nodeTypeName) throws RepositoryException {
        if(!nodeTypeName.isEmpty()){
            return getTemplatingFunctions().children(content, nodeTypeName);
        }
        return getTemplatingFunctions().children(content);
    }

    @Function()
    public static  ContentMap root(ContentMap contentMap, String nodeTypeName) throws RepositoryException {
        if(!nodeTypeName.isEmpty()) {
            ContentMap root = getTemplatingFunctions().root(contentMap, nodeTypeName);
            return root;
        }
        return getTemplatingFunctions().root(contentMap);
    }

    @Function
    public static  ContentMap parent(ContentMap contentMap, String nodeTypeName) throws RepositoryException {
        if(!nodeTypeName.isEmpty()) {
            return getTemplatingFunctions().parent(contentMap, nodeTypeName);
        }
        return getTemplatingFunctions().parent(contentMap);
    }

    /**
     * Returns the page's {@link ContentMap} of the passed {@link ContentMap}. If the passed {@link ContentMap} represents a page, the passed {@link ContentMap} will be returned.
     * If the passed {@link ContentMap} has no parent page at all, null is returned.
     *
     * @param content the {@link ContentMap} to get the page's {@link ContentMap} from.
     * @return returns the page {@link ContentMap} of the passed content {@link ContentMap}.
     * @throws RepositoryException
     */
    @Function
    public static  ContentMap page(ContentMap content) throws RepositoryException {
        return getTemplatingFunctions().page(content);
    }


    @Function
    public static  List<ContentMap> ancestors(ContentMap contentMap, String nodeTypeName) throws RepositoryException {
        if(nodeTypeName.isEmpty()) {
            return getTemplatingFunctions().ancestors(contentMap);
        }
        return getTemplatingFunctions().ancestors(contentMap, nodeTypeName);
    }

    @Function
    public static  ContentMap inherit(ContentMap content, String relPath) throws RepositoryException {
        if(!relPath.isEmpty()) {
            return getTemplatingFunctions().inherit(content, relPath);
        }
        return getTemplatingFunctions().inherit(content);
    }

    @Function
    public static  Property inheritProperty(ContentMap content, String relPath) throws RepositoryException {
        return getTemplatingFunctions().inheritProperty(content, relPath);
    }

    @Function
    public static  List<ContentMap> inheritList(ContentMap content, String relPath) throws RepositoryException {
        return getTemplatingFunctions().inheritList(content, relPath);
    }

    @Function
    public static  boolean isInherited(ContentMap content) {
        return getTemplatingFunctions().isInherited(content);
    }

    @Function
    public static  boolean isFromCurrentPage(ContentMap content) {
        return getTemplatingFunctions().isFromCurrentPage(content);
    }

    /**
     * Create link for the Node identified by nodeIdentifier in the specified workspace.
     */
    @Function
    public static  String linkForWorkspace(String workspace, String nodeIdentifier) {
        return getTemplatingFunctions().link(workspace, nodeIdentifier);
    }

    /**
     * FIXME Add a LinkUtil.createLink(Property property).... Dirty Hack.
     * FIXME: Should be changed when a decision is made on SCRUM-525.
     */
    @Function
    public static  String linkForProperty(Property property) {
        return getTemplatingFunctions().link(property);
    }


    @Function
    public static  String link(ContentMap contentMap) throws RepositoryException {
        return getTemplatingFunctions().link(contentMap);
    }

    /**
     * Get the language used currently.
     * @return The language as a String.
     */
    @Function
    public static  String language(){
        return getTemplatingFunctions().language();
    }

    /**
     * Returns an external link prepended with <code>http://</code> in case the protocol is missing or an empty String
     * if the link does not exist.
     *
     * @param content The node's map representation where the link property is stored on.
     * @param linkPropertyName The property where the link value is stored in.
     * @return The link prepended with <code>http://</code>
     */
    @Function
    public static  String externalLink(ContentMap content, String linkPropertyName) {
        return getTemplatingFunctions().externalLink(content, linkPropertyName);
    }


    /**
     * Return a link title based on the @param linkTitlePropertyName. When property @param linkTitlePropertyName is
     * empty or null, the link itself is provided as the linkTitle (prepended with <code>http://</code>).
     *
     * @param content The node where the link property is stored on.
     * @param linkPropertyName The property where the link value is stored in.
     * @param linkTitlePropertyName The property where the link title value is stored
     * @return the resolved link title value
     */
    @Function
    public static  String externalLinkTitle(ContentMap content, String linkPropertyName, String linkTitlePropertyName) {
        return getTemplatingFunctions().externalLinkTitle(content, linkPropertyName, linkTitlePropertyName);
    }

    @Function
    public static  boolean isEditMode() {
        return getTemplatingFunctions().isEditMode();
    }

    @Function
    public static  boolean isPreviewMode() {
        return getTemplatingFunctions().isPreviewMode();
    }

    @Function
    public static  boolean isAuthorInstance() {
        return getTemplatingFunctions().isAuthorInstance();
    }

    @Function
    public static  boolean isPublicInstance() {
        return getTemplatingFunctions().isPublicInstance();
    }

    /**
     * Util method to create html attributes <code>name="value"</code>. If the value is empty an empty string will be returned.
     * This is mainly helpful to avoid empty attributes.
     */
    @Function
    public static  String createHtmlAttribute(String name, String value) {
        return getTemplatingFunctions().createHtmlAttribute(name, value);
    }

    /**
     * Returns an instance of SiblingsHelper for the given contentMap.
     */
    @Function
    public static  SiblingsHelper siblings(ContentMap node) throws RepositoryException {
        return getTemplatingFunctions().siblings(node);
    }


    /**
     * Return the Node for the Given Path
     * from the given repository.
     * If the repository is empty, take the default (website).
     */
    @Function
    public static  Node content(String path,String repository){
        if( repository.isEmpty()) {
            return getTemplatingFunctions().content(path);
        }
        return getTemplatingFunctions().content(repository, path);
    }

    @Function
    public static  List<ContentMap> asContentMapList(Collection<Node> nodeList) {
        return getTemplatingFunctions().asContentMapList(nodeList);
    }

    @Function
    public static  List<Node> asNodeList(Collection<ContentMap> contentMapList) {
        return getTemplatingFunctions().asNodeList(contentMapList);
    }

    /**
     * Removes escaping of HTML on properties.
     */
    @Function
    public static  ContentMap decode(ContentMap content){
        return getTemplatingFunctions().decode(content);
    }

    /**
     * Returns the string representation of a property from the metaData of the node or <code>null</code> if the node has no Magnolia metaData or if no matching property is found.
     */
    @Function
    public static String metaDataProperty(ContentMap content, String property){
        return getTemplatingFunctions().metaDataProperty(content, property);
    }

}
