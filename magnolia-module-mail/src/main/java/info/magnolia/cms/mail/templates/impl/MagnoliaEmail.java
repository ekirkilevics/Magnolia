/**
 * This file Copyright (c) 2003-2008 Magnolia International
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
package info.magnolia.cms.mail.templates.impl;

import freemarker.template.Template;
import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapper;
import freemarker.cache.ClassTemplateLoader;
import freemarker.cache.TemplateLoader;
import freemarker.cache.MultiTemplateLoader;
import info.magnolia.cms.core.Path;
import info.magnolia.cms.mail.templates.MailAttachment;
import info.magnolia.cms.security.User;
import info.magnolia.context.MgnlContext;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

import javax.mail.Session;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.filter.Filter;
import org.jdom.input.SAXBuilder;
import org.jdom.output.XMLOutputter;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;


/**
 * Date: Apr 6, 2006 Time: 9:24:29 PM
 * @author <a href="mailto:niko@macnica.com">Nicolas Modrzyk</a>
 */
public class MagnoliaEmail extends FreemarkerEmail {

    public static final String SUFFIX = "?mgnlIntercept=PREVIEW&mgnlPreview=true&mail=draw";

    private HttpClient client;

    private int cid = 0;

    private static final String UTF_8 = "UTF-8";

    private static final String MAGNOLIA = "magnolia";

    private static final String IMG = "img";

    private static final String SRC = "src";

    private static final String CID = "cid:";

    private static final String LINK = "link";

    private static final String HREF = "href";

    private static final String STYLE = "style";

    private static final String REL = "rel";

    private static final String ACTION = "action";

    private static final String LOGIN = "login";

    private static final String URL = "url";

    private static final String MGNL_USER_ID = "mgnlUserId";

    private static final String MGNL_USER_PSWD = "mgnlUserPSWD";

    private static final String SLASH = "/";

    private static final Configuration freemarkerCfg = new Configuration();

    static {
        freemarkerCfg.setObjectWrapper(new DefaultObjectWrapper());
        ClassTemplateLoader ctl = new ClassTemplateLoader(FreemarkerEmail.class, "/");
        TemplateLoader[] loaders = new TemplateLoader[]{ctl};
        MultiTemplateLoader mtl = new MultiTemplateLoader(loaders);
        freemarkerCfg.setTemplateLoader(mtl);
        freemarkerCfg.setDefaultEncoding("UTF8");
    }

    public MagnoliaEmail(Session _session) throws Exception {
        super(_session);
    }

    public void setBodyFromResourceFile(String resourceFile, Map _map) throws Exception {
        URL url = new URL(resourceFile);
        // retrieve the html content
        String _content = retrieveContentFromMagnolia(resourceFile);
        StringReader reader = new StringReader(_content);

        // filter the images
        String urlBasePath = url.getProtocol() + "://" + url.getHost() + ":" + url.getPort();
        reader = filterImages(urlBasePath, reader);

        // create the template from the previously filtered stream
        Template template = new Template(MAGNOLIA, reader, freemarkerCfg, UTF_8);

        // execute the template, calling super will make use of Freemarker capabilities
        setBodyFromTemplate(template, _map);
    }

    public void setBodyFromTemplate(Template template, Map _map) throws Exception {
        final StringWriter writer = new StringWriter();
        template.process(_map, writer);
        writer.flush();
        setBody(writer.toString(), _map);
    }

    /**
     * Filter the images from the content of the reader. For example:<br>
     * &lt;img src="/magnolia/info.gif"/> is replaced by &lt;img src="cid:1"/> and the attachment cid 1 is created and
     * linked to this email
     * @param urlBasePath needed to resolve path of images
     * @param reader where the content is
     * @return a new <code>StringReader</code> with the filtered content
     * @throws Exception
     */
    private StringReader filterImages(String urlBasePath, StringReader reader) throws Exception {
        log.info("Filtering images");
        SAXBuilder parser = new SAXBuilder();
        parser.setEntityResolver(new EntityResolver() {

            public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
                return new InputSource(new java.io.ByteArrayInputStream(new byte[0]));
            }

        });
        Document doc = parser.build(reader);
        ArrayList toremove = new ArrayList();
        ArrayList toadd = new ArrayList();

        // Filter content
        Iterator iter = doc.getDescendants(new ContentFilter());
        while (iter.hasNext()) {
            Element elem = (Element) iter.next();
            String name = elem.getName();
            if (name.equalsIgnoreCase(IMG)) {
                // stream image and attach it to the email
                Attribute att = elem.getAttribute(SRC);
                if (log.isDebugEnabled()) {
                    log.debug("Found new img:" + att.toString());
                }
                String value = att.getValue();
                this.cid++;
                att.setValue(CID + (this.cid));
                String url = urlBasePath + value;
                if (log.isDebugEnabled()) {
                    log.debug("Url is:" + url);
                }
                this.addAttachment(new MailAttachment(getAttachmentFile(url).toURL(), String.valueOf(this.cid)));
            }
            else if (name.equalsIgnoreCase(LINK)) {
                // stream the css and put the content into a <style> tag
                Attribute att = elem.getAttribute(HREF);
                Element el = (Element) elem.clone();
                if (log.isDebugEnabled()) {
                    log.debug("Found new css:" + att.toString());
                }
                String url = urlBasePath + att.getValue();
                el.setName(STYLE);
                el.removeAttribute(HREF);
                el.removeAttribute(REL);
                GetMethod streamCss = new GetMethod(url);
                getHttpClient(url).executeMethod(streamCss);
                el.setText(streamCss.getResponseBodyAsString());

                toremove.add(elem);
                toadd.add(el);
            }
        }

        // this is ugly but is there to
        // avoid concurrent modification exception on the Document
        for (int i = 0; i < toremove.size(); i++) {
            Element elem = (Element) toremove.get(i);
            Element parent = elem.getParentElement();
            doc.removeContent(elem);
            parent.addContent((Element) toadd.get(i));
        }

        // create the return string reader with new document content
        StringWriter writer = new StringWriter();
        new XMLOutputter().output(doc, writer);
        return new StringReader(writer.toString());
    }

    /**
     * Retrieve the content of the file. Need this because the content requires login. So temporarily creates the file
     * @param url the full url where the image is located. The image will be retrieved with the httpclient of this class
     * @return <code>File</code> with the full content of the image (or whatever is downloaded)
     * @throws Exception if fails
     */
    private File getAttachmentFile(String url) throws Exception {
        log.info("Streaming content of url:" + url + " to a temporary file");

        // Execute an http get on the url
        GetMethod redirect = new GetMethod(url);
        getHttpClient(url).executeMethod(redirect);

        URL _url = new URL(url);
        String file = _url.getFile();

        // create file in temp dir, with just the file name.
        File tempFile = new File(Path.getTempDirectoryPath()
            + File.separator
            + file.substring(file.lastIndexOf(SLASH) + 1));
        // if same file and same size, return, do not process
        if (tempFile.exists() && redirect.getResponseContentLength() == tempFile.length()) {
            redirect.releaseConnection();
            return tempFile;
        }

        // stream the content to the temp file
        FileOutputStream out = new FileOutputStream(tempFile);
        final int BUFFER_SIZE = 1 << 10 << 3; // 8KiB buffer
        byte[] buffer = new byte[BUFFER_SIZE];
        int bytesRead;
        InputStream in = new BufferedInputStream(redirect.getResponseBodyAsStream());
        while (true) {
            bytesRead = in.read(buffer);
            if (bytesRead > -1) {
                out.write(buffer, 0, bytesRead);
            }
            else {
                break;
            }
        }

        // cleanup
        in.close();
        out.close();
        redirect.releaseConnection();

        return tempFile;
    }

    /**
     * Need to login into the site, so this method create an httpclient of many calls. This authenticated the user as
     * well for an http session
     * @param baseURL the url to use to login in the site
     * @return <code>HttpClient</code> that can be used.
     * @throws Exception if fails
     */
    private HttpClient getHttpClient(String baseURL) throws Exception {
        if (this.client == null) {
            URL location = new URL(baseURL);
            User user = MgnlContext.getUser();
            this.client = getHttpClientForUser(location, user);
        }
        return this.client;
    }

    /**
     * Separate this method from HttpClient in case we want to get the http client for a different user than the current
     * one
     * @param location the url to login to
     * @param _user the user to get credentials from
     * @return <code>HttpClient</code> logged in to the system
     * @throws IOException if fails
     */
    private HttpClient getHttpClientForUser(URL location, User _user) throws IOException {
        HttpClient _client = new HttpClient();
        _client.getHostConfiguration().setHost(location.getHost(), location.getPort(), location.getProtocol());
        _client.getParams().setCookiePolicy(CookiePolicy.BROWSER_COMPATIBILITY);
        String user = _user.getName();
        String pass = _user.getPassword();
        log.info("Creating http client for user:" + user);
        // login using the id and password of the current user
        PostMethod authpost = new PostMethod(location.getPath());
        NameValuePair action = new NameValuePair(ACTION, LOGIN);
        NameValuePair url = new NameValuePair(URL, location.getPath());
        NameValuePair userid = new NameValuePair(MGNL_USER_ID, user);
        NameValuePair password = new NameValuePair(MGNL_USER_PSWD, pass);
        authpost.setRequestBody(new NameValuePair[]{action, url, userid, password});
        _client.executeMethod(authpost);
        authpost.releaseConnection();
        return _client;
    }

    /**
     * Get the html content from a magnolia page
     * @param _url the url of the page to get the content from. Note that this url will be suffixed with
     * <code>SUFFIX</code> to remove editing content
     * @return a <code>String</code> with the html content
     * @throws Exception if fails
     */
    private String retrieveContentFromMagnolia(String _url) throws Exception {
        log.info("Retrieving content from magnolia:" + _url);
        GetMethod redirect = new GetMethod(_url + SUFFIX);
        getHttpClient(_url).executeMethod(redirect);
        String response = redirect.getResponseBodyAsString();
        redirect.releaseConnection();
        return response;
    }

    /**
     * Class to filter content when parsing the html
     */
    static class ContentFilter implements Filter {

        /**
         *
         */
        private static final long serialVersionUID = 1L;

        public boolean matches(Object object) {
            if (object instanceof Element) {
                Element e = (Element) object;
                return e.getName().equalsIgnoreCase(LINK) || e.getName().equalsIgnoreCase(IMG);
            }

            return false;

        }
    }
}
