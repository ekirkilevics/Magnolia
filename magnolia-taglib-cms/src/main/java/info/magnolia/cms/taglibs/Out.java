/**
 * This file Copyright (c) 2003-2007 Magnolia International
 * Ltd.  (http://www.magnolia.info). All rights reserved.
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
 * is available at http://www.magnolia.info/mna.html
 *
 * Any modifications to this file must keep this entire header
 * intact.
 *
 */
package info.magnolia.cms.taglibs;

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.beans.runtime.FileProperties;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.NodeData;
import info.magnolia.cms.i18n.I18nContentSupportFactory;
import info.magnolia.cms.link.LinkHelper;
import info.magnolia.cms.util.ContentUtil;
import info.magnolia.cms.util.DateUtil;
import info.magnolia.cms.util.LinkUtil;
import info.magnolia.cms.util.Resource;
import info.magnolia.context.MgnlContext;

import java.io.IOException;
import java.util.Date;
import java.util.Locale;

import javax.jcr.PropertyType;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.PageContext;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.NestableRuntimeException;


/**
 * @author Sameer Charles
 * @author Fabrizio Giustina
 * @version $Revision$ ($Author$)
 */
public class Out extends BaseContentTag {

    /**
     * Facke nodeDataName returning the node's name.
     */
    public static final String NODE_NAME_NODEDATANAME = "name";

    /**
     * Facke nodeDataName returning the node's handle.
     */
    private static final String PATH_NODEDATANAME = "path";

    /**
     * Facke nodeDataName returning the node's handle.
     */
    private static final String HANDLE_NODEDATANAME = "handle";

    /**
     * Facke nodeDataName returning the node's uuid.
     */
    private static final String UUID_NODEDATANAME = "uuid";

    /**
     * No uuid to link resolving.
     */
    public static final String LINK_RESOLVING_NONE = "none";

    /**
     * Resolve to a absolute link but do not use the repository to uri mapping.
     */
    private static final String LINK_RESOLVING_HANDLE = "handle";

    /**
     * Resolve to relative path. Path is relative to current page.
     */
    public static final String LINK_RESOLVING_RELATIVE = "relative";

    /**
     * Resolve to a absolute link using the repository to uri mapping.
     */
    public static final String LINK_RESOLVING_ABSOLUTE = "absolute";

    /**
     * Stable serialVersionUID.
     */
    private static final long serialVersionUID = 222L;

    private static final String DEFAULT_LINEBREAK = "<br />"; //$NON-NLS-1$

    private static final String DEFAULT_DATEPATTERN = DateUtil.FORMAT_DATE_MEDIUM; //$NON-NLS-1$

    private String defaultValue = StringUtils.EMPTY;

    private String fileProperty = StringUtils.EMPTY;

    private String datePattern = DEFAULT_DATEPATTERN; // according to ISO 8601

    private String dateLanguage;

    private String lineBreak = DEFAULT_LINEBREAK;

    private String uuidToLink = LINK_RESOLVING_NONE;

    private String uuidToLinkRepository = ContentRepository.WEBSITE;


    /**
     * If set, the result of the evaluation will be set to a variable named from this attribute (and in the scope
     * defined using the "scope" attribute, defaulting to PAGE) instead of being written directly to the page.
     */
    private String var;

    /**
     * Scope for the attribute named from the "var" parameter. Setting this attribute doesn't have any effect if "var"
     * is not set.
     */
    private int scope = PageContext.PAGE_SCOPE;

    /**
     * Setter for <code>var</code>.
     * @param var The var to set.
     */
    public void setVar(String var) {
        this.var = var;
    }

    /**
     * Setter for <code>scope</code>.
     * @param scope The scope to set.
     */
    public void setScope(String scope) {
        if ("request".equalsIgnoreCase(scope)) { //$NON-NLS-1$
            this.scope = PageContext.REQUEST_SCOPE;
        }
        else if ("session".equalsIgnoreCase(scope)) { //$NON-NLS-1$
            this.scope = PageContext.SESSION_SCOPE;
        }
        else if ("application".equalsIgnoreCase(scope)) { //$NON-NLS-1$
            this.scope = PageContext.APPLICATION_SCOPE;
        }
        else {
            // default
            this.scope = PageContext.PAGE_SCOPE;
        }
    }

    /**
     * <p>
     * set which information of a file to retrieve
     * </p>
     * <p>
     * does only apply for nodeDatas of type=Binary
     * </p>
     * <p>
     * supported values (sample value):
     * </p>
     * <ul>
     * <li><b>path (default): </b> path inlcuding the filename (/dev/mainColumnParagraphs/0/image/Alien.png)</li>
     * <li><b>name </b>: name and extension (Alien.png)</li>
     * <li><b>extension: </b> extension as is (Png)</li>
     * <li><b>extensionLowerCase: </b> extension lower case (png)</li>
     * <li><b>extensionUpperCase: </b> extension upper case (PNG)</li>
     * <li><b>nameWithoutExtension: </b> (Alien)</li>
     * <li><b>handle: </b> /dev/mainColumnParagraphs/0/image</li>
     * <li><b>icon: </b>the default icon for the type of document</li>
     * <li><b>pathWithoutName: </b> (/dev/mainColumnParagraphs/0/image.png)</li>
     * <li><b>size: </b> size in bytes (2827)</li>
     * <li><b>sizeString: </b> size in bytes, KB or MB - max. 3 digits before comma - with unit (2.7 KB)</li>
     * <li><b>contentType: </b> (image/png)</li>
     * <li><b>width: </b>image width in pixels (images only)</li>
     * <li><b>height: </b>image height in pixels (images only)</li>
     * </ul>
     * </p>
     * @param property
     */
    public void setFileProperty(String property) {
        this.fileProperty = property;
    }

    /**
     * <p/> set which date format shall be delivered
     * </p>
     * <p/> does only apply for nodeDatas of type=Date
     * </p>
     * <p/> language according to java.text.SimpleDateFormat:
     * <ul>
     * <li><b>G </b> Era designator Text AD
     * <li><b>y </b> Year Year 1996; 96
     * <li><b>M </b> Month in year Month July; Jul; 07
     * <li><b>w </b> Week in year Number 27
     * <li><b>W </b> Week in month Number 2
     * <li><b>D </b> Day in year Number 189
     * <li><b>d </b> Day in month Number 10
     * <li><b>F </b> Day of week in month Number 2
     * <li><b>E </b> Day in week Text Tuesday; Tue
     * <li><b>a </b> Am/pm marker Text PM
     * <li><b>H </b> Hour in day (0-23) Number 0
     * <li><b>k </b> Hour in day (1-24) Number 24
     * <li><b>K </b> Hour in am/pm (0-11) Number 0
     * <li><b>h </b> Hour in am/pm (1-12) Number 12
     * <li><b>m </b> Minute in hour Number 30
     * <li><b>s </b> Second in minute Number 55
     * <li><b>S </b> Millisecond Number 978
     * <li><b>z </b> Time zone General time zone Pacific Standard Time; PST; GMT-08:00
     * <li><b>Z </b> Time zone RFC 822 time zone -0800
     * </ul>
     * </p>
     * @param pattern , default is yyyy-MM-dd
     */
    public void setDatePattern(String pattern) {
        this.datePattern = pattern;
    }

    /**
     * Set which date format shall be delivered. Does only apply for nodeDatas of type=Date. Language according to
     * <code>java.util.Locale</code>.
     * @param language
     */
    public void setDateLanguage(String language) {
        this.dateLanguage = language;
    }

    /**
     * Set the lineBreak String.
     * @param lineBreak
     */
    public void setLineBreak(String lineBreak) {
        this.lineBreak = lineBreak;
    }

    protected String getFilePropertyValue(Content contentNode) {
        NodeData nodeData = I18nContentSupportFactory.getI18nSupport().getNodeData(contentNode, this.nodeDataName);
        FileProperties props = new FileProperties(contentNode, nodeData.getName());
        String value = props.getProperty(this.fileProperty);
        return value;
    }

    /**
     * @see javax.servlet.jsp.tagext.Tag#doEndTag()
     */
    public int doEndTag() {
        // don't reset any value set using a tag attribute here, or it will break any container that does tag pooling!

        Content contentNode = getFirstMatchingNode();
        if (contentNode == null) {
            return EVAL_PAGE;
        }

        NodeData nodeData = I18nContentSupportFactory.getI18nSupport().getNodeData(contentNode, this.nodeDataName);

        String value = null;


        if (!nodeData.isExist()) {
            // either a special case was passed in as the nodeDataName, or a bad value was passed in for the name
            // - handle either case here
            if(UUID_NODEDATANAME.equals(this.nodeDataName)){
                value = contentNode.getUUID();
            }
            else if(PATH_NODEDATANAME.equals(this.nodeDataName) || HANDLE_NODEDATANAME.equals(this.nodeDataName)){
                value = contentNode.getHandle();
            }
            else if(NODE_NAME_NODEDATANAME.equals(this.nodeDataName)){
                value = contentNode.getName();
            }
            else if(StringUtils.isNotEmpty(this.getDefaultValue())){
                value = this.getDefaultValue();
            }
            else {
                return EVAL_PAGE;
            }
        }
        else{
            // the nodeData for the nodeDataName specified exists - determine how to output it according
            // to its type, or any other variables that are set
            int type = nodeData.getType();

            switch (type) {
                case PropertyType.DATE:

                    Date date = nodeData.getDate().getTime();
                    if (date != null) {
                        Locale locale;
                        if (this.dateLanguage == null) {
                             locale = I18nContentSupportFactory.getI18nSupport().getLocale();
                        }
                        else {
                            locale = new Locale(this.dateLanguage);
                        }
                        value = DateUtil.format(date, this.datePattern, locale);
                    }
                    break;

                case PropertyType.BINARY:
                    value = this.getFilePropertyValue(contentNode);
                    break;

                default:
                    value = StringUtils.isEmpty(this.lineBreak) ? nodeData.getString() : nodeData.getString(this.lineBreak);

                    // replace internal links that use the special magnolia link format (looks like ${link: {uuid: ... etc) -
                    // ( - see info.magnolia.cms.link.UUIDLink for an example of the special format that this next line
                    //    handles )
                    value = LinkUtil.convertUUIDsToBrowserLinks(value, Resource.getActivePage().getHandle()); // static actpage

                    if(!StringUtils.equalsIgnoreCase(getUuidToLink(), LINK_RESOLVING_NONE)){
                        // if the uuidToLink type has been explicitly set, reset the output value
                        // the link to the uuid value stored in the node - using whatever method
                        // was specified in the uuidLinkType variable
                        if(StringUtils.equals(this.getUuidToLink(), LINK_RESOLVING_HANDLE)){
                            value = ContentUtil.uuid2path(this.getUuidToLinkRepository(), value);
                        }
                        else if(StringUtils.equals(this.getUuidToLink(), LINK_RESOLVING_ABSOLUTE)){
                            value = LinkHelper.convertUUIDtoAbsolutePath(value, this.getUuidToLinkRepository());
                        }
                        else if(StringUtils.equals(this.getUuidToLink(), LINK_RESOLVING_RELATIVE)){
                            value = LinkUtil.makeRelativePath(LinkHelper.convertUUIDtoAbsolutePath(value, this.getUuidToLinkRepository()), MgnlContext.getAggregationState().getMainContent().getHandle());
                        }
                        else{
                            throw new IllegalArgumentException("not supported value for uuidToLink");
                        }
                    }
                    break;
            }
        }

        value = StringUtils.defaultIfEmpty(value, this.getDefaultValue());

        if (var != null) {
            // set result as a variable
            pageContext.setAttribute(var, value, scope);
        }
        else if (value != null) {
            JspWriter out = pageContext.getOut();
            try {
                out.print(value);
            }
            catch (IOException e) {
                // should never happen
                throw new NestableRuntimeException(e);
            }
        }

        return EVAL_PAGE;
    }

    /**
     * @see javax.servlet.jsp.tagext.TagSupport#release()
     */
    public void release() {
        super.release();

        this.fileProperty = StringUtils.EMPTY;
        this.datePattern = DEFAULT_DATEPATTERN;
        this.dateLanguage = null;
        this.lineBreak = DEFAULT_LINEBREAK;
        this.var = null;
        this.scope = PageContext.PAGE_SCOPE;
    }

    public String getUuidToLink() {
        return this.uuidToLink;
    }

    public void setUuidToLink(String uuidToLink) {
        this.uuidToLink = uuidToLink;
    }

    public String getUuidToLinkRepository() {
        return this.uuidToLinkRepository;
    }

    public void setUuidToLinkRepository(String uuidToLinkRepository) {
        this.uuidToLinkRepository = uuidToLinkRepository;
    }

    public String getDefaultValue() {
        return this.defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

}
