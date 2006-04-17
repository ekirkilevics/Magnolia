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
package info.magnolia.cms.taglibs;

import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.NodeData;
import info.magnolia.cms.gui.misc.FileProperties;
import info.magnolia.cms.util.LinkUtil;
import info.magnolia.cms.util.Resource;

import java.io.IOException;
import java.util.Date;
import java.util.Locale;

import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.TagSupport;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.NestableRuntimeException;
import org.apache.commons.lang.time.DateFormatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author Sameer Charles
 * @author Fabrizio Giustina
 * @version $Revision$ ($Author$)
 */
public class Out extends TagSupport {

    /**
     * Stable serialVersionUID.
     */
    private static final long serialVersionUID = 222L;

    /**
     * Logger.
     */
    private static Logger log = LoggerFactory.getLogger(Out.class);

    private static final String DEFAULT_LINEBREAK = "<br />"; //$NON-NLS-1$

    private static final String DEFAULT_DATEPATTERN = "yyyy-MM-dd"; //$NON-NLS-1$

    private String nodeDataName;

    private String contentNodeName;

    private String contentNodeCollectionName;

    private String fileProperty = StringUtils.EMPTY;

    private String datePattern = DEFAULT_DATEPATTERN; // according to ISO 8601

    private String dateLanguage;

    private String lineBreak = DEFAULT_LINEBREAK;

    private boolean inherit;

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
     * Set the node data name, e.g. "mainText".
     * @param name
     */
    public void setNodeDataName(String name) {
        this.nodeDataName = name;
    }

    /**
     * Set the content node name name, e.g. "01".
     * @param name
     */
    public void setContentNodeName(String name) {
        this.contentNodeName = name;
    }

    /**
     * Set the content node collection name name, e.g. "mainColumnParagraphs".
     * @param name
     */
    public void setContentNodeCollectionName(String name) {
        this.contentNodeCollectionName = name;
    }

    /**
     * @deprecated does nothing
     */
    public void setActpage(String set) {
    }

    /**
     * <p/> set which information of a file to retrieve
     * </p>
     * <p/> does only apply for nodeDatas of type=Binary
     * </p>
     * <p/> supported values (sample value):
     * <ul>
     * <li><b>path (default): </b> path inlcuding the filename (/dev/mainColumnParagraphs/0/image/Alien.png)
     * <li><b>name </b>: name and extension (Alien.png)
     * <li><b>extension: </b> extension as is (Png)
     * <li><b>extensionLowerCase: </b> extension lower case (png)
     * <li><b>extensionUpperCase: </b> extension upper case (PNG)
     * <li><b>nameWithoutExtension: </b> (Alien)
     * <li><b>handle: </b> /dev/mainColumnParagraphs/0/image
     * <li><b>pathWithoutName: </b> (/dev/mainColumnParagraphs/0/image.png)
     * <li><b>size: </b> size in bytes (2827)
     * <li><b>sizeString: </b> size in bytes, KB or MB - max. 3 digits before comma - with unit (2.7 KB)
     * <li><b>contentType: </b> (image/png)
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
     * Setter for <code>inherit</code>.
     * @param inherit <code>true</code> to inherit from parent pages if value is not set.
     */
    public void setInherit(boolean inherit) {
        this.inherit = inherit;
    }

    /**
     * Set the lineBreak String.
     * @param lineBreak
     */
    public void setLineBreak(String lineBreak) {
        this.lineBreak = lineBreak;
    }

    public String getFilePropertyValue(Content contentNode) {
        FileProperties props = new FileProperties(contentNode, this.nodeDataName);
        String value = props.getProperty(this.fileProperty);
        return value;
    }

    protected Content resolveNode(Content currentPage) {
        HttpServletRequest request = (HttpServletRequest) pageContext.getRequest();

        Content currentParagraph = Resource.getLocalContentNode(request);

        if (StringUtils.isNotEmpty(contentNodeName)) {
            // contentNodeName is defined
            try {
                if (StringUtils.isEmpty(contentNodeCollectionName)) {
                    // e.g. <cms:out nodeDataName="title" contentNodeName="footer"/>
                    return currentPage.getContent(contentNodeName);
                }

                // e.g. <cms:out nodeDataName="title" contentNodeName="01" contentNodeCollectionName="mainPars"/>
                // e.g. <cms:out nodeDataName="title" contentNodeName="footer" contentNodeCollectionName=""/>
                return currentPage.getContent(contentNodeCollectionName).getContent(contentNodeName);

            }
            catch (RepositoryException re) {
                if (log.isDebugEnabled())
                    log.debug(re.getMessage());
            }
        }
        else {
            if (currentParagraph == null) {
                // outside collection iterator
                if (StringUtils.isEmpty(contentNodeCollectionName)) {
                    // e.g. <cms:out nodeDataName="title"/>
                    // e.g. <cms:out nodeDataName="title" contentNodeName=""/>
                    // e.g. <cms:out nodeDataName="title" contentNodeCollectionName=""/>
                    return currentPage;
                }
                // ERROR: no content node assignable because contentNodeName is empty
                // e.g. <cms:out nodeDataName="title" contentNodeCollectionName="mainPars"/>
            }
            else {
                // inside collection iterator
                if (contentNodeName == null && contentNodeCollectionName == null) {
                    // e.g. <cms:out nodeDataName="title"/>
                    return currentParagraph;
                }
                else if ((contentNodeName != null && StringUtils.isEmpty(contentNodeName))
                    || (contentNodeCollectionName != null && StringUtils.isEmpty(contentNodeCollectionName))) {
                    // empty collection name -> use actpage
                    // e.g. <cms:out nodeDataName="title" contentNodeCollectionName=""/>
                    return currentPage;
                }
            }
        }
        return null;
    }

    /**
     * @see javax.servlet.jsp.tagext.Tag#doEndTag()
     */
    public int doEndTag() {
        // don't reset any value set using a tag attribute here, or it will break any container that does tag pooling!

        HttpServletRequest request = (HttpServletRequest) pageContext.getRequest();
        Content currentPage = Resource.getCurrentActivePage(request);
        Content contentNode = resolveNode(currentPage);
        if (contentNode == null) {
            return EVAL_PAGE;
        }

        String value = null;

        // @todo //check if multiple values (checkboxes) -> not nodeData but contentNode

        NodeData nodeData = contentNode.getNodeData(this.nodeDataName);

        try {
            while (inherit && currentPage.getLevel() > 0 && !nodeData.isExist()) {
                currentPage = currentPage.getParent();
                contentNode = resolveNode(currentPage);
                nodeData = contentNode.getNodeData(this.nodeDataName);
            }
        }
        catch (RepositoryException e) {
            log.error(e.getMessage(), e);
        }

        if (!nodeData.isExist()) {
            return EVAL_PAGE;
        }

        int type = nodeData.getType();

        switch (type) {
            case PropertyType.DATE:

                Date date = nodeData.getDate().getTime();
                if (date != null) {
                    if (this.dateLanguage == null) {
                        value = DateFormatUtils.formatUTC(date, this.datePattern);
                    }
                    else {
                        value = DateFormatUtils.formatUTC(date, this.datePattern, new Locale(this.dateLanguage));
                    }
                }
                break;

            case PropertyType.BINARY:
                value = this.getFilePropertyValue(contentNode);
                break;

            default:
                value = StringUtils.isEmpty(this.lineBreak) ? nodeData.getString() : nodeData.getString(this.lineBreak);
                // replace internal links
                value = LinkUtil.convertUUIDsToRelativeLinks(value, Resource
                    .getActivePage((HttpServletRequest) pageContext.getRequest())); // static actpage
                break;
        }

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

        this.nodeDataName = null;
        this.contentNodeName = null;
        this.contentNodeCollectionName = null;
        this.fileProperty = StringUtils.EMPTY;
        this.datePattern = DEFAULT_DATEPATTERN;
        this.dateLanguage = null;
        this.lineBreak = DEFAULT_LINEBREAK;
        this.var = null;
        this.scope = PageContext.PAGE_SCOPE;
        this.inherit = false;
    }

}