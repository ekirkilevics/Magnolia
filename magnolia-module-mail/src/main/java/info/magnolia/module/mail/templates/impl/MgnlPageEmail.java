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
package info.magnolia.module.mail.templates.impl;

import freemarker.template.Template;
import info.magnolia.cms.core.Path;
import info.magnolia.cms.security.User;
import info.magnolia.context.MgnlContext;
import info.magnolia.module.mail.MailTemplate;
import info.magnolia.module.mail.templates.MailAttachment;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.lang.StringUtils;
import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.filter.Filter;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


/**
 * Date: Apr 6, 2006 Time: 9:24:29 PM
 * @author <a href="mailto:niko@macnica.com">Nicolas Modrzyk</a>
 *
 */
public class MgnlPageEmail extends FreemarkerEmail {

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

    private static final String HTTP = "http://";

    private static final String A_LINK = "a";

    private static final String FORM = "form";

    private static final String BODY = "body";

    private static final String FORM_ACTION = "action";

    private static final String DEFAULT_FORM_ACTION = "#";


    public MgnlPageEmail(MailTemplate template) {
        super(template);
    }

    public void setBodyFromResourceFile() throws Exception {
        String resourceFile = this.getTemplate().getTemplateFile();

        if(!StringUtils.contains(resourceFile, "http") ) {
            String oriurl = MgnlContext.getAggregationState().getOriginalURL();
            String temp = StringUtils.substring(oriurl, 0, StringUtils.indexOf(oriurl, MgnlContext.getContextPath()));
            resourceFile = temp + MgnlContext.getContextPath() + resourceFile;
        }

        URL url = new URL(resourceFile);
        // retrieve the html content
        String _content = retrieveContentFromMagnolia(resourceFile);
        StringReader reader = new StringReader(_content);

        // filter the images
        String urlBasePath = url.getProtocol() + "://" + url.getHost() + (url.getPort() > -1? ":" + url.getPort() : "" + "/");
        String tmp = filterImages(urlBasePath, reader, url.toString());

        tmp = StringUtils.remove(tmp, "&#xD;");
        super.setBody(tmp);
    }

    // TODO : this is not used !
    public void setBodyFromTemplate(Template template, Map _map) throws Exception {
        final StringWriter writer = new StringWriter();
        template.process(_map, writer);
        writer.flush();
        setBody(writer.toString());
    }


    private String filterImages(String urlBasePath, StringReader reader, String pageUrl) throws Exception {
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
        Element body = null;
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
                String url = getUrl(pageUrl, value);

                if (log.isDebugEnabled()) {
                    log.debug("Url is:" + url);
                }
                this.getTemplate().addAttachment(new MailAttachment(getAttachmentFile(url).toURL(), String.valueOf(this.cid)));
            }
            else if (name.equalsIgnoreCase(LINK)) {
                // stream the css and put the content into a <style> tag and add to body tag
                Attribute att = elem.getAttribute(HREF);
                Element el = (Element) elem.clone();
                //Element el = elem;
                if (log.isDebugEnabled()) {
                    log.debug("Found new css:" + att.toString());
                }
                String url = getUrl(pageUrl, att.getValue());
                el.setName(STYLE);
                el.removeAttribute(HREF);
                el.removeAttribute(REL);
                GetMethod streamCss = new GetMethod(url);
                getHttpClient(url).executeMethod(streamCss);
                String tmp = streamCss.getResponseBodyAsString();
                tmp = processUrls(tmp, url);
                el.setText(tmp);
                toremove.add(elem);
                toadd.add(el);

            } else if(name.equalsIgnoreCase(A_LINK)) {
                Attribute att = elem.getAttribute(HREF);

                String url = getUrl(pageUrl, att.getValue());
                if(!att.getValue().startsWith(DEFAULT_FORM_ACTION)) {
                    att.setValue(url);
                }

            } else if(name.equalsIgnoreCase(FORM)) {
                Attribute att = elem.getAttribute(FORM_ACTION);
                String url = att.getValue();
                if(att.getValue().equals(DEFAULT_FORM_ACTION)) {
                    url = pageUrl;
                }
                att.setValue(url);

            } else if(name.equalsIgnoreCase(BODY)) {
                body = elem;
            }
        }

        // this is ugly but is there to
        // avoid concurrent modification exception on the Document
        for (int i = 0; i < toremove.size(); i++) {
            Element elem = (Element) toremove.get(i);
            Element parent = elem.getParentElement();

            body.addContent(0, (Element) toadd.get(i));
            parent.removeContent(elem);

        }

        // create the return string reader with new document content
        Format format = Format.getRawFormat();
        format.setExpandEmptyElements(true);


        XMLOutputter outputter = new XMLOutputter(format);
        StringWriter writer = new StringWriter();
        return outputter.outputString(doc);
    }


    private String getUrl(String currentPagePath, String path) {
        String urlBasePath = currentPagePath.substring(0, currentPagePath.indexOf(MgnlContext.getContextPath()) );
        if(!StringUtils.contains(path, HTTP) && !StringUtils.contains(path, MgnlContext.getContextPath())) {
            return currentPagePath.substring(0, currentPagePath.lastIndexOf("/") + 1) + path;
        } else if(!StringUtils.contains(path, HTTP)) {
            return urlBasePath + path;
        }
        return path;
    }

    private String processUrls(String responseBodyAsString, String cssPath) throws MalformedURLException, Exception {
        String tmp = "";
        Map map = new HashMap();

        int urlIndex = 0;
        int closeIndex = 0;
        int begin = 0;
        int cid;

      //  List urls = new Array
        while(StringUtils.indexOf(responseBodyAsString, "url(", begin) >= 0) {
            urlIndex = StringUtils.indexOf(responseBodyAsString, "url(", begin) + "url(".length();
            closeIndex = StringUtils.indexOf(responseBodyAsString, ")", urlIndex) ;

            String url = StringUtils.substring(responseBodyAsString, urlIndex, closeIndex);
            url = getUrl(cssPath, url).replaceAll("\"", "");
            url = "\"" + url + "\"";
            if(!StringUtils.isEmpty(url) && !map.containsKey(url)) {
                map.put(url, url);

            } else if (map.containsKey(url)) {
                url = (String) map.get(url);

            }
            tmp += StringUtils.substring(responseBodyAsString, begin, urlIndex) + url;
            begin = closeIndex;

        }

        tmp += StringUtils.substring(responseBodyAsString, closeIndex);
        return tmp;
    }


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


    private HttpClient getHttpClient(String baseURL) throws Exception {
        if (this.client == null) {
            URL location = new URL(baseURL);
            User user = MgnlContext.getUser();
            this.client = getHttpClientForUser(location, user);
        }
        return this.client;
    }


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

    private String retrieveContentFromMagnolia(String _url) throws Exception {
        log.info("Retrieving content from magnolia:" + _url);
        GetMethod redirect = new GetMethod(_url + SUFFIX);
        getHttpClient(_url).executeMethod(redirect);
        String response = redirect.getResponseBodyAsString();
        redirect.releaseConnection();
        return response;
    }

   static class ContentFilter implements Filter {


        private static final long serialVersionUID = 1L;

        public boolean matches(Object object) {
            if (object instanceof Element) {
                Element e = (Element) object;
                return e.getName().equalsIgnoreCase(LINK) || e.getName().equalsIgnoreCase(IMG)
                || e.getName().equalsIgnoreCase(A_LINK) || e.getName().equalsIgnoreCase(FORM)
                || e.getName().equalsIgnoreCase(BODY);
            }

            return false;

        }
    }
}

