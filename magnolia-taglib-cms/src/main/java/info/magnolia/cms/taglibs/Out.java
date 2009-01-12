/**
 * This file Copyright (c) 2003-2007 Magnolia International
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
package info.magnolia.cms.taglibs;

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.beans.runtime.FileProperties;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.NodeData;
import info.magnolia.cms.i18n.I18nContentSupportFactory;
import info.magnolia.cms.util.ContentUtil;
import info.magnolia.cms.util.DateUtil;
import info.magnolia.link.LinkUtil;
import info.magnolia.context.MgnlContext;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.NestableRuntimeException;

import javax.jcr.PropertyType;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.PageContext;
import java.io.IOException;
import java.util.Date;
import java.util.Locale;


/**
 * Writes out the content of a nodeData or - for nodeData of type binary - information of the nodeData.
 * @jsp.tag name="out" body-content="empty"
 * @jsp.tag-example
 * <!-- EXAMPLE - outputting a nodes value into the page -->
 * <!-- output the value stored in the node namd "title" -->
 *
 * <cms:out nodeDataName="title"/>
 *
 * <!-- EXAMPLE - outputting a node into an EL variable -->
 * <!-- output the value stored in the node namd "myprop" to a variable named "check" -->
 * <!-- thus exposing it to EL functionality -->
 *
 * <cms:out nodeDataName="myprop" var="check"/>
 * <c:if test="${check == 'ok'}">
 * done
 * </if>
 *
 * <!-- EXAMPLE - outputing a link from a uuid stored in a node -->
 * <!-- output a relative link to the page whose UUID is stored in the node named "link" to a variable "relative_link" -->
 *
 * <cms:out nodeDataName="link" var="relative_link uuidToLink="relative" />
 * <a href="${relative_link}">go to page</a>
 *
 * <!-- EXAMPLE - writing a binary file's URL out as a variable -->
 * <!-- this example shows how to display an image stored in the content repository using cms:out -->
 *
 * <cms:ifNotEmpty nodeDataName="image">
 * <cms:out nodeDataName="image" var="imageurl" />
 * <img class="navIcon" src="${pageContext.request.contextPath}${imageurl}" />
 * </cms:ifNotEmpty>
 *
 * @author Sameer Charles
 * @author Fabrizio Giustina
 * @version $Revision$ ($Author$)
 */
public class Out extends BaseContentTag {

    /**
     * Fake nodeDataName returning the node's name.
     */
    public static final String NODE_NAME_NODEDATANAME = "name";

    /**
     * Fake nodeDataName returning the node's handle.
     */
    private static final String PATH_NODEDATANAME = "path";

    /**
     * Fake nodeDataName returning the node's handle.
     */
    private static final String HANDLE_NODEDATANAME = "handle";

    /**
     * Fake nodeDataName returning the node's uuid.
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

    private boolean escapeXml = false;

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
     * The name of the nodeData you wish to write out (required). There are following special values supported
     * name, uuid, path, handle. If you specify one of special values, the behavior changes to output the name, uuid,
     * path, or handle of the node instead of the value it stores.
     * @jsp.attribute required="true" rtexprvalue="true"
     * TODO ... this is just overriding BaseContentTag.setNodeDataName() to set proper description and attributes ... :(
     */
    public void setNodeDataName(String name) {
        super.setNodeDataName(name);
    }

    /**
     * If set, the result of the evaluation will be set to a variable named from this attribute (and in the scope
     * defined using the "scope" attribute, defaulting to PAGE) instead of being written directly to the page.
     * @jsp.attribute required="false" rtexprvalue="true"
     */
    public void setVar(String var) {
        this.var = var;
    }

    /**
     * Determines whether output will be XML escaped.
     * @jsp.attribute required="false" rtexprvalue="true" type="boolean"
     */
    public void setEscapeXml(boolean escapeXml) {
        this.escapeXml = escapeXml;
    }

    /**
     * Scope for the attribute named from the "var" parameter.
     * Setting this attribute doesn't have any effect if "var" is not set.
     * @jsp.attribute required="false" rtexprvalue="true"
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
     * Sets which information of a file to retrieve. Only applies for nodeDatas of type=Binary.
     * Supported values (sample value):
     *
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
     *
     * @jsp.attribute required="false" rtexprvalue="true"
     */
    public void setFileProperty(String property) {
        this.fileProperty = property;
    }

    /**
     * Sets the output date format, as per java.text.SimpleDateFormat. Default is "yyyy-MM-dd".
     * Only applies for nodeDatas of type=Date.
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
     * @jsp.attribute required="false" rtexprvalue="true"
     */
    public void setDatePattern(String pattern) {
        this.datePattern = pattern;
    }

    /**
     * Set which date format shall be delivered. Does only apply for nodeDatas of type=Date. Language according to
     * <code>java.util.Locale</code>.
     * @jsp.attribute required="false" rtexprvalue="true"
     */
    public void setDateLanguage(String language) {
        this.dateLanguage = language;
    }

    /**
     * Determines how line breaks are converted. Defaults to "<br />".
     * Set to "" to have no line break at all, or any other value to be used as the line break.
     * @jsp.attribute required="false" rtexprvalue="true"
     */
    public void setLineBreak(String lineBreak) {
        this.lineBreak = lineBreak;
    }

    protected String getFilePropertyValue(Content contentNode) {
        NodeData nodeData = I18nContentSupportFactory.getI18nSupport().getNodeData(contentNode, this.getNodeDataName());
        FileProperties props = new FileProperties(contentNode, nodeData.getName());
        return props.getProperty(this.fileProperty);
    }

    public int doEndTag() {
        // don't reset any value set using a tag attribute here, or it will break any container that does tag pooling!

        Content contentNode = getFirstMatchingNode();
        if (contentNode == null) {
            return EVAL_PAGE;
        }

        NodeData nodeData = I18nContentSupportFactory.getI18nSupport().getNodeData(contentNode, this.getNodeDataName());

        String value = null;


        if (!nodeData.isExist()) {
            // either a special case was passed in as the nodeDataName, or a bad value was passed in for the name
            // - handle either case here
            if(UUID_NODEDATANAME.equals(this.getNodeDataName())){
                value = contentNode.getUUID();
            }
            else if(PATH_NODEDATANAME.equals(this.getNodeDataName()) || HANDLE_NODEDATANAME.equals(this.getNodeDataName())){
                value = contentNode.getHandle();
            }
            else if(NODE_NAME_NODEDATANAME.equals(this.getNodeDataName())){
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
                    // ( - see info.magnolia.link.UUIDLink for an example of the special format that this next line
                    //    handles )
                    value = LinkUtil.convertToBrowserLinks(value, MgnlContext.getAggregationState().getMainContent().getHandle()); // static actpage

                    if(!StringUtils.equalsIgnoreCase(getUuidToLink(), LINK_RESOLVING_NONE)){
                        // if the uuidToLink type has been explicitly set, reset the output value
                        // the link to the uuid value stored in the node - using whatever method
                        // was specified in the uuidLinkType variable
                        if(StringUtils.equals(this.getUuidToLink(), LINK_RESOLVING_HANDLE)){
                            value = ContentUtil.uuid2path(this.getUuidToLinkRepository(), value);
                        }
                        else if(StringUtils.equals(this.getUuidToLink(), LINK_RESOLVING_ABSOLUTE)){
                            value = LinkUtil.convertUUIDtoURI(value, this.getUuidToLinkRepository());
                        }
                        else if(StringUtils.equals(this.getUuidToLink(), LINK_RESOLVING_RELATIVE)){
                            value = LinkUtil.makePathRelative(MgnlContext.getAggregationState().getMainContent().getHandle(), LinkUtil.convertUUIDtoHandle(value, this.getUuidToLinkRepository()));
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

            if ( escapeXml ) {
                value = StringEscapeUtils.escapeXml( value );
            }

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


    /**
     * Transform a uuid value to a link. The following values are supported:
     * <ul>
     *   <li>none: no uuid to link resolving. (default value)</li>
     *   <li>absolute: Resolve to a absolute link using the repository to uri mapping feature.</li>
     *   <li>handle: resolve to a absolute link but do not use the repository to uri mapping.</li>
     *   <li>relative: resolve to relative path. Path is relative to current page.</li>
     * </ul>
     * @jsp.attribute required="false" rtexprvalue="true"
     */
    public void setUuidToLink(String uuidToLink) {
        this.uuidToLink = uuidToLink;
    }

    public String getUuidToLinkRepository() {
        return this.uuidToLinkRepository;
    }

    /**
     * Used if the uuidToLink attribute is set. The content is found in this repository. Defaults to "website".
     * @jsp.attribute required="false" rtexprvalue="true"
     */
    public void setUuidToLinkRepository(String uuidToLinkRepository) {
        this.uuidToLinkRepository = uuidToLinkRepository;
    }

    public String getDefaultValue() {
        return this.defaultValue;
    }

    /**
     * Default value used if the expresion evaluates to null or an empty string.
     * @jsp.attribute required="false" rtexprvalue="true"
     */
    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

}
