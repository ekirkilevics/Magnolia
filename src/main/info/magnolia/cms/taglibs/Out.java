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
package info.magnolia.cms.taglibs;

import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.NodeData;
import info.magnolia.cms.gui.misc.FileProperties;
import info.magnolia.cms.util.LinkUtil;
import info.magnolia.cms.util.Resource;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.TagSupport;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;


/**
 * @author Sameer Charles
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
    private static Logger log = Logger.getLogger(Out.class);

    private static final String DEFAULT_LINEBREAK = "<br />"; //$NON-NLS-1$

    private static final String DEFAULT_DATEPATTERN = "yyyy-MM-dd"; //$NON-NLS-1$

    private String nodeDataName;

    private String contentNodeName;

    private String contentNodeCollectionName;

    /**
     * The current page. This is important to make relative links.
     */
    private transient Content currentActivePage;

    private transient Content contentNode;

    private transient NodeData nodeData;

    private String fileProperty = StringUtils.EMPTY;

    private String datePattern = DEFAULT_DATEPATTERN; // according to ISO 8601

    private String dateLanguage;

    private String lineBreak = DEFAULT_LINEBREAK;

    /**
     * @see javax.servlet.jsp.tagext.Tag#doStartTag()
     */
    public int doStartTag() {

        Content local = Resource.getLocalContentNode((HttpServletRequest) pageContext.getRequest());
        String contentNodeName = this.getContentNodeName();
        String contentNodeCollectionName = this.getContentNodeCollectionName();
        if (StringUtils.isNotEmpty(contentNodeName)) {
            // contentNodeName is defined
            try {
                if (StringUtils.isEmpty(contentNodeCollectionName)) {
                    // e.g. <cms:out nodeDataName="title" contentNodeName="footer"/>
                    this.setContentNode(this.getCurrentActivePage().getContent(contentNodeName));
                }
                else {
                    // e.g. <cms:out nodeDataName="title" contentNodeName="01" contentNodeCollectionName="mainPars"/>
                    // e.g. <cms:out nodeDataName="title" contentNodeName="footer" contentNodeCollectionName=""/>
                    this.setContentNode(this.getCurrentActivePage().getContent(contentNodeCollectionName).getContent(
                        contentNodeName));
                }
            }
            catch (RepositoryException re) {
                log.debug(re.getMessage());
            }
        }
        else {
            if (local == null) {
                // outside collection iterator
                if (StringUtils.isNotEmpty(contentNodeCollectionName)) {
                    // ERROR: no content node assignable because contentNodeName is empty
                    // e.g. <cms:out nodeDataName="title" contentNodeCollectionName="mainPars"/>
                    return SKIP_BODY;
                }
                // e.g. <cms:out nodeDataName="title"/>
                // e.g. <cms:out nodeDataName="title" contentNodeName=""/>
                // e.g. <cms:out nodeDataName="title" contentNodeCollectionName=""/>
                this.setContentNode(this.getCurrentActivePage());
            }
            else {
                // inside collection iterator
                if (contentNodeName == null && contentNodeCollectionName == null) {
                    // e.g. <cms:out nodeDataName="title"/>
                    this.setContentNode(local);
                }
                else if ((contentNodeName != null && StringUtils.isEmpty(contentNodeName))
                    || (contentNodeCollectionName != null && StringUtils.isEmpty(contentNodeCollectionName))) {
                    // empty collection name -> use actpage
                    // e.g. <cms:out nodeDataName="title" contentNodeCollectionName=""/>
                    this.setContentNode(this.getCurrentActivePage());
                }
                else {
                    // ERROR: no content node assignable because contentNodeName is empty
                    // e.g. <cms:out nodeDataName="title" contentNodeCollectionName="mainPars"/>
                    return SKIP_BODY;
                }
            }
        }
        return SKIP_BODY;
    }

    /**
     * @see javax.servlet.jsp.tagext.Tag#doEndTag()
     */
    public int doEndTag() {
        this.display();
        this.setCurrentActivePage(null);
        this.setContentNodeCollectionName(null);
        this.setContentNodeName(null);
        this.setContentNode(null);
        this.setNodeDataName(null);
        this.setNodeData(null);
        this.setDateLanguage(null);
        this.setDatePattern(DEFAULT_DATEPATTERN);
        this.setLineBreak(DEFAULT_LINEBREAK);
        return EVAL_PAGE;
    }

    /**
     * <p>
     * set the requested node data
     * </p>
     * @param node
     */
    public void setNodeData(NodeData node) {
        this.nodeData = node;
    }

    public NodeData getNodeData() {
        return this.nodeData;
    }

    /**
     * <p>
     * set the node data name, e.g. "mainText"
     * </p>
     * @param name
     */
    public void setNodeDataName(String name) {
        this.nodeDataName = name;
    }

    public String getNodeDataName() {
        return this.nodeDataName;
    }

    /**
     * <p>
     * set the content node name name, e.g. "01"
     * </p>
     * @param name
     */
    public void setContentNodeName(String name) {
        this.contentNodeName = name;
    }

    /**
     * <p>
     * set the content node collection name name, e.g. "mainColumnParagraphs"
     * </p>
     * @param name
     */
    public void setContentNodeCollectionName(String name) {
        this.contentNodeCollectionName = name;
    }

    public String getContentNodeCollectionName() {
        return this.contentNodeCollectionName;
    }

    public String getContentNodeName() {
        return this.contentNodeName;
    }

    /**
     * <p>
     * set the content node name
     * </p>
     * @param c
     */
    public void setContentNode(Content c) {
        this.contentNode = c;
    }

    public Content getContentNode() {
        return this.contentNode;
    }

    /**
     * @deprecated
     * <p>
     * set the actpage
     * </p>
     * @param set (true/false; false is default)
     */
    public void setActpage(String set) {
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
     * <p>
     * set which date format shall be delivered
     * </p>
     * <p>
     * does only apply for nodeDatas of type=Date
     * </p>
     * <p>
     * language according to java.text.SimpleDateFormat:
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

    public String getDatePattern() {
        return this.datePattern;
    }

    /**
     * <p>
     * set which date format shall be delivered
     * </p>
     * <p>
     * does only apply for nodeDatas of type=Date
     * </p>
     * <p>
     * language according to java.util.Locale
     * </p>
     * @param language
     */
    public void setDateLanguage(String language) {
        this.dateLanguage = language;
    }

    public String getDateLanguage() {
        return this.dateLanguage;
    }

    /**
     * <p>
     * set the lineBreak String
     * </p>
     * @param lineBreak
     */
    public void setLineBreak(String lineBreak) {
        this.lineBreak = lineBreak;
    }

    public String getLineBreak() {
        return this.lineBreak;
    }

    /**
     *
     */
    private void display() {
        try {

            // @todo //check if multiple values (checkboxes) -> not nodeData but contentNode

            NodeData nodeData = this.getContentNode().getNodeData(this.getNodeDataName());
            String value = StringUtils.EMPTY;
            int type = nodeData.getType();
            if (type == PropertyType.DATE) {
                value = this.getDateFormatted(nodeData.getDate().getTime());
            }
            else if (type == PropertyType.BINARY) {
                value = this.getFilePropertyValue();
            }
            else {
                if (StringUtils.isEmpty(this.getLineBreak())) {
                    value = nodeData.getString();
                }
                else {
                    value = nodeData.getString(this.getLineBreak());
                }
                // replace internal links
                value = LinkUtil.convertUUIDsToRelativeLinks(value, this.getCurrentActivePage());
            }
            JspWriter out = pageContext.getOut();
            try {
                out.print(value);
            }
            catch (IOException e) {
                log.debug("Exception caught: " + e.getMessage(), e); //$NON-NLS-1$
            }
        }
        catch (Exception e) {
            log.debug("Exception caught: " + e.getMessage(), e); //$NON-NLS-1$
        }
    }

    // @todo: place in another package to make it availbable globaly -> NodeData?
    public String getDateFormatted(Date date) {

        if (date == null) {
            return StringUtils.EMPTY;
        }
        SimpleDateFormat formatter;
        String lang = this.getDateLanguage();
        if (lang == null) {
            formatter = new SimpleDateFormat(this.getDatePattern());
        }
        else {
            formatter = new SimpleDateFormat(this.getDatePattern(), new Locale(lang));
        }
        return formatter.format(date);
    }

    public String getFilePropertyValue() {
        FileProperties props = new FileProperties(this.getContentNode(), this.nodeDataName);
        String value = props.getProperty(this.fileProperty);
        return value;
    }

    /**
     * @return the current page
     */
    protected Content getCurrentActivePage() {
        if (this.currentActivePage == null) {
            this.currentActivePage = Resource.getCurrentActivePage((HttpServletRequest) pageContext.getRequest());
        }
        return this.currentActivePage;
    }

    /**
     * @param currentActivePage the current page
     */
    protected void setCurrentActivePage(Content currentActivePage) {
        this.currentActivePage = currentActivePage;
    }

    /**
     * @see javax.servlet.jsp.tagext.TagSupport#release()
     */
    public void release() {
        super.release();

        this.nodeDataName = null;
        this.contentNodeName = null;
        this.contentNodeCollectionName = null;
        this.currentActivePage = null;
        this.contentNode = null;
        this.nodeData = null;
        this.fileProperty = StringUtils.EMPTY;
        this.datePattern = DEFAULT_DATEPATTERN;
        this.dateLanguage = null;
        this.lineBreak = DEFAULT_LINEBREAK;

    }
}