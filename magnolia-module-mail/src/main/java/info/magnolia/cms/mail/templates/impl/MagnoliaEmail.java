package info.magnolia.cms.mail.templates.impl;

import freemarker.template.Template;
import info.magnolia.cms.beans.runtime.MgnlContext;
import info.magnolia.cms.core.Path;
import info.magnolia.cms.mail.templates.MailAttachment;
import info.magnolia.cms.security.User;
import info.magnolia.cms.security.UserManager;
import info.magnolia.cms.util.FactoryUtil;
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

import javax.mail.Session;
import java.io.*;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Date: Apr 6, 2006
 * Time: 9:24:29 PM
 *
 * @author <a href="mailto:niko@macnica.com">Nicolas Modrzyk</a>
 */
public class MagnoliaEmail extends FreemarkerEmail {
    public static final String SUFFIX = "?mgnlIntercept=PREVIEW&mgnlPreview=true&mail=draw";
    private HttpClient client;
    private int cid = 0;

    public MagnoliaEmail(Session _session) throws Exception {
        super(_session);
    }


    public void setBodyFromResourceFile(String resourceFile, HashMap _map) throws Exception {
        URL url = new URL(resourceFile);
        // retrieve the html content
        String _content = retrieveContentFromMagnolia(resourceFile);
        StringReader reader = new StringReader(_content);

        // filter the images
        String urlBasePath = url.getProtocol() + "://" + url.getHost() + ":" + url.getPort();
        reader = FilterImages(urlBasePath, reader);

        // create the template from the previously filtered stream
        Template template = new Template("magnolia", reader, FreemarkerEmail.cfg, "UTF-8");

        // execute the template, calling super will make use of Freemarker capabilities
        super.setBodyFromTemplate(template, _map);
    }

    /**
     * Filter the images from the content of the reader. For example:<br>
     * &lt;img src="/magnolia/info.gif"/>
     * is replaced by
     * &lt;img src="cid:1"/>
     * and the attachment cid 1 is created and linked to this email
     *
     * @param urlBasePath needed to resolve path of images
     * @param reader      where the content is
     * @return a new <code>StringReader</code> with the filtered content
     * @throws Exception
     */
    private StringReader FilterImages(String urlBasePath, StringReader reader) throws Exception {
        log.info("Filtering images");
        SAXBuilder parser = new SAXBuilder();
        Document doc = parser.build(reader);
        Iterator iter = doc.getDescendants(new Filter() {
            public boolean matches(Object object) {
                if (object instanceof Element) {
                    Element e = (Element) object;
                    return e.getName().equalsIgnoreCase("img");
                } else
                    return false;
            }
        });
        while (iter.hasNext()) {
            Element elem = (Element) iter.next();
            Attribute att = elem.getAttribute("src");
            log.info("Found new elem:" + att.toString());
            String value = att.getValue();

            cid ++;
            att.setValue("cid:" + (cid));
            String url = urlBasePath + value;
            log.info("Url is:" + url);

            this.addAttachment(new MailAttachment(getAttachmentFile(url), String.valueOf(cid)));
        }

        StringWriter writer = new StringWriter();
        new XMLOutputter().output(doc, writer);
        return new StringReader(writer.toString());
    }

    /**
     * Retrieve the content of the file. Need this because the content requires login. So temporarily creates the file
     *
     * @param url the full url where the image is located. The image will be retrieved with the httpclient of this class
     * @return <code>File</code> with the full content of the image (or whatever is downloaded)
     * @throws Exception if fails
     */
    private File getAttachmentFile(String url) throws Exception {
        log.info("Downloading file:" + url);
        GetMethod redirect = new GetMethod(url);
        getHttpClient(url).executeMethod(redirect);

        URL _url = new URL(url);
        String file = _url.getFile();
        log.info("Looking for file:" + file);

        File tempFile = new File(Path.getTempDirectoryPath() + File.separator + file.substring(file.lastIndexOf("/") + 1));
        // if same file and same size, return, do not process
        if (tempFile.exists() && redirect.getResponseContentLength() == tempFile.length()) {
            redirect.releaseConnection();
            return tempFile;
        }

        // stream the content to the file
        FileOutputStream out = new FileOutputStream(tempFile);
        final int BUFFER_SIZE = 1 << 10 << 3; //8KiB buffer
        byte[] buffer = new byte[BUFFER_SIZE];
        int bytesRead = -1;
        InputStream in = new BufferedInputStream(redirect.getResponseBodyAsStream());
        while ((bytesRead = in.read(buffer)) > -1) {
            out.write(buffer, 0, bytesRead);
        }
        // cleanup
        in.close();
        out.close();
        redirect.releaseConnection();

        return tempFile;
    }

    /**
     * Need to login into the site, so this method create an httpclient of many calls. This authenticated the user as well for an http session
     *
     * @param baseURL the url to use to login in the site
     * @return <code>HttpClient</code> that can be used.
     * @throws Exception if fails
     */
    private HttpClient getHttpClient(String baseURL) throws Exception {
        if (client == null) {
            log.info("logging in...");
            URL location = new URL(baseURL);
            HttpClient _client = new HttpClient();
            _client.getHostConfiguration().setHost(location.getHost(), location.getPort(), location.getProtocol());
            _client.getParams().setCookiePolicy(CookiePolicy.BROWSER_COMPATIBILITY);


            MgnlContext.setInstance(MgnlContext.getSystemContext());
            UserManager manager = (UserManager) FactoryUtil.getInstance(UserManager.class);
            Iterator iter = manager.getAllUsers().iterator();
            User user = null;

            if (iter.hasNext()) {
                user = (User) iter.next();
            }
            if (user != null && log.isDebugEnabled()) {
                log.debug(user.getName());
                log.debug(user.getPassword());
            } else
                throw new Exception("No user found");

            // login
            PostMethod authpost = new PostMethod(location.getPath());
            NameValuePair action = new NameValuePair("action", "login");
            NameValuePair url = new NameValuePair("url", location.getPath());
            NameValuePair userid = new NameValuePair("mgnlUserId", user.getName());
            NameValuePair password = new NameValuePair("mgnlUserPSWD", user.getPassword());
            authpost.setRequestBody(new NameValuePair[]{action, url, userid, password});
            _client.executeMethod(authpost);
            authpost.releaseConnection();
            this.client = _client;
        }

        return client;
    }

    /**
     * Get the html content from a magnolia page
     *
     * @param _url the url of the page to get the content from. Note that this url will be suffixed with <code>SUFFIX</code> to remove editing content
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
}
