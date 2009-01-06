/**
 * This file Copyright (c) 2003-2009 Magnolia International
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
package info.magnolia.cms.mail.templates;

import info.magnolia.cms.beans.config.MIMEMapping;

import java.io.File;
import java.net.URL;

import org.apache.commons.lang.StringUtils;


/**
 * Date: Apr 1, 2006 Time: 8:38:07 PM
 * @author <a href="mailto:niko@macnica.com">Nicolas Modrzyk</a>
 */
public class MailAttachment {

    public static final String DISPOSITION_INLINE = "inline";

    public static final String DISPOSITION_NORMAL = "normal";

    public static final String DISPOSITION_ATTACHMENT = "attachment";

    private static final String FILE_URL_PREFIX = "file://";

    private String description;

    private String disposition;

    private String name;

    private URL url;

    public MailAttachment() {

    }

    public MailAttachment(File file, String name, String description, String disposition) {
        this.setPath(file.getAbsolutePath());
        this.name = name;
        this.description = description;
        this.disposition = disposition;
    }

    public MailAttachment(URL url, String _name, String _description, String _disposition) {
        this.url = url;
        this.name = _name;
        this.description = _description;
        this.disposition = _disposition;
    }

    public MailAttachment(URL _url, String name) {
        this.url = _url;
        this.name = name;
        this.disposition = DISPOSITION_INLINE;
        this.description = StringUtils.EMPTY;
    }

    public java.lang.String getDescription() {
        return this.description;
    }

    public java.lang.String getDisposition() {
        if(StringUtils.isEmpty(disposition)){
            return DISPOSITION_INLINE;
        }
        return this.disposition;
    }

    public java.lang.String getName() {
        return this.name;
    }

    public java.lang.String getPath() {
        return this.url.getFile();
    }

    public java.net.URL getUrl() {
        if (this.url.getProtocol().startsWith("file")) {
            try {
                return new URL( this.url.toExternalForm());
            }
            catch (Exception e) {
                return null;
            }
        }

        return this.url;

    }

    public java.io.File getFile() {
        return new File(this.url.getFile());
    }

    public void setDescription(java.lang.String desc) {
        this.description = desc;
    }

    public void setDisposition(java.lang.String aDisposition) {
        this.disposition = aDisposition;
    }

    public void setName(java.lang.String aName) {
        this.name = aName;
    }

    public void setPath(java.lang.String aPath) {
        try {
            this.url = new URL(FILE_URL_PREFIX + aPath);
        }
        catch (Exception e) {
            e.printStackTrace();
            this.url = null;
        }
    }

    public void setUrl(java.net.URL aUrl) {
        this.url = aUrl;
    }

    public String getContentType() {
        return MIMEMapping.getMIMEType(StringUtils.substringAfterLast(this.getPath(), "."));
    }

    public String getFileName() {
        return new File(this.url.getFile()).getName();
    }

}
