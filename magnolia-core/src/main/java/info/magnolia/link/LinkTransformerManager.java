/**
 * This file Copyright (c) 2009-2011 Magnolia International
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
package info.magnolia.link;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import info.magnolia.cms.core.AggregationState;
import info.magnolia.cms.core.Content;
import info.magnolia.context.MgnlContext;
import info.magnolia.objectfactory.Components;

/**
 * Single point of access for all Link Transformers.
 * @author had
 *
 */
public class LinkTransformerManager {

    private static final Logger log = LoggerFactory.getLogger(LinkTransformerManager.class);

    private boolean makeBrowserLinksRelative = false;
    private boolean addContextPathToBrowserLinks = false;

    private Map<String, LinkTransformer> transformers;
    
    public LinkTransformerManager() {
        this.transformers = new HashMap<String, LinkTransformer>();
    }
    
    public Map<String, LinkTransformer> getTransformers(){
        return this.transformers;
    }

    public void setTransformers(Map<String, LinkTransformer> transformers){
        this.transformers = transformers;
    }
    
    public void addTransformer(String key, LinkTransformer transformer){
        this.transformers.put(key, transformer);
    }

    public boolean isAddContextPathToBrowserLinks() {
        return this.addContextPathToBrowserLinks;
    }

    public void setAddContextPathToBrowserLinks(boolean addContextPathToBrowserLinks) {
        this.addContextPathToBrowserLinks = addContextPathToBrowserLinks;
    }

    public boolean isMakeBrowserLinksRelative() {
        return this.makeBrowserLinksRelative;
    }

    public void setMakeBrowserLinksRelative(boolean makeBrowserLinksRelative) {
        this.makeBrowserLinksRelative = makeBrowserLinksRelative;
    }

    /**
     * Gets the current singleton instance.
     */
    public static LinkTransformerManager getInstance() {
        return Components.getComponent(LinkTransformerManager.class);
    }
    
    /**
     * Gets registered absolute path transformer. 
     */
    public LinkTransformer getAbsoluteTransformer(){
        return transformers.get("absolute");
    }

    /**
     * Gets registered editor link transformer. 
     */
    public LinkTransformer getEditorTransformer(){
        return transformers.get("editor");
    }
    
    /**
     * Gets registered relative path transformer. 
     */
    public LinkTransformer getRelativeTransformer(){
        return transformers.get("relative");
    }
    
    /**
     * Gets registered complete URL transformer. 
     */
    public LinkTransformer getCompleteURLTransformer(){
        return transformers.get("completeURL");
    }
    
    /**
     * Gets registered complete URL transformer. 
     */
    public LinkTransformer getI18nTransformer(){
        return transformers.get("i18n");
    }

    /**
     * Creates instance of absolute link transformer that will prepend the context path, will use URI2Repository mapping while constructing links and will localize the link if localization is set up.
     */
    public AbsolutePathTransformer getAbsolute() {
        return getAbsolute(true);
    }

    /**
     * Creates instance of absolute link transformer that will optionally prepend the context path, but will always use URI2Repository mapping while constructing links and will localize the link if localization is set up.
     */
    public AbsolutePathTransformer getAbsolute(boolean addContextPath) {
        if(getAbsoluteTransformer() == null){
            return new AbsolutePathTransformer(addContextPath, true, true);
        }
        AbsolutePathTransformer transformer = (AbsolutePathTransformer)transformers.get("absolute");
        transformer.setAddContextPath(addContextPath);
        transformer.setUseI18N(true);
        transformer.setUseURI2RepositoryMapping(true);
        return transformer;
    }

    /**
     * Creates instance of Relative link transformer that will translate path to the provided Link relative to the content provided here. During the translation all valid URI2repository mappings and i18n will be applied.
     */
    public RelativePathTransformer getRelative(Content page) {
        if(getRelativeTransformer() == null){
            return new RelativePathTransformer(page.getJCRNode(), true, true);
        }
        RelativePathTransformer transformer = (RelativePathTransformer)transformers.get("relative");
        transformer.setUseI18N(true);
        transformer.setUseURI2RepositoryMapping(true);
        transformer.setAbsolutSourcePath(page.getJCRNode());
        return transformer;
    }

    /**
     * Creates instance of Relative link transformer that will translate path to the provided Link relative to path provided here. During the translation all valid URI2repository mappings and i18n will be applied.
     */
    public RelativePathTransformer getRelative(String absolutePath) {
        if(getRelativeTransformer() == null){
            return new RelativePathTransformer(absolutePath, true, true);
        }
        RelativePathTransformer transformer = (RelativePathTransformer)transformers.get("relative");
        transformer.setUseI18N(true);
        transformer.setUseURI2RepositoryMapping(true);
        transformer.setAbsolutSourcePath(absolutePath);
        return transformer;
    }

    /**
     * Creates instance of Complete URL link transformer that will create fully qualified and localized link to content denoted by Link provided to its transform method.
     */
    public CompleteUrlPathTransformer getCompleteUrl() {
        if(getCompleteURLTransformer() == null){
            return new CompleteUrlPathTransformer(true, true);
        }
        CompleteUrlPathTransformer transformer = (CompleteUrlPathTransformer)transformers.get("completeURL");
        transformer.setUseURI2RepositoryMapping(true);
        transformer.setUseI18N(true);
        return transformer;
    }

    /**
     * @see EditorLinkTransformer
     */
    public EditorLinkTransformer getEditorLink() {
        if(getEditorTransformer() == null){
            return new EditorLinkTransformer();
        }
        return (EditorLinkTransformer)transformers.get("editor");
    }

    /**
     * Creates instance of link transformer that will transform any provided links to either absolute or relative path based on the current server configuration.
     * @param currentPath Path to make links relative to, if relative path translation is configured on the server.
     */
    public LinkTransformer getBrowserLink(String currentPath) {
        if (MgnlContext.isWebContext()) {
            if (isMakeBrowserLinksRelative() ) {
                final AggregationState state = MgnlContext.getAggregationState();
                if (currentPath == null && state != null) {
                    currentPath = state.getOriginalURI();
                }
                if (currentPath != null) {
                    return getRelative(currentPath);
                }
            }
            return getAbsolute(addContextPathToBrowserLinks);
        }
        return getCompleteUrl();
    }
}
