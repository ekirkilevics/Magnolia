package info.magnolia.custom.search.lucene;

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.HierarchyManager;

import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Collection;
import java.util.Iterator;
import java.util.TimerTask;

import javax.servlet.ServletConfig;

import org.apache.log4j.Logger;


/**
 * User: Sameer Charles Date: Mar 5, 2004 Time: 11:00:11 AM
 */
public class BaseTask extends TimerTask {

    /**
     * logger
     */
    private static Logger log = Logger.getLogger(BaseTask.class);

    private static final String INDEX_DIRECTORY = "lucene.index.directory";

    private static final String HOST = "host";

    private static final String INDEX_PATH = "indexPath";

    private static final String PROTOCOL = "protocol";

    private ServletConfig servletConfig;

    private HierarchyManager hierachyManager;

    private Index index;

    BaseTask(ServletConfig config) throws Exception {
        this.servletConfig = config;
        this.hierachyManager = ContentRepository.getHierarchyManager(ContentRepository.WEBSITE);
    }

    public void run() {
    }

    protected void executeIndexer() {
        try {
            this.index = new Index(getIndexDirectory());
            this.navigate(this.hierachyManager.getPage(getIndexPath()));
            this.index.closeIndex();
        }
        catch (Exception e) {
        }
    }

    public void indexPage(String handle) throws Exception {
        URL url = new URL(getProtocol() + "://" + getHost() + handle + ".html?indexer=true");
        URLConnection urlConnection = url.openConnection();
        StringBuffer sb = new StringBuffer();
        byte[] buffer = new byte[8192];

        InputStream in = urlConnection.getInputStream();
        while (in.read(buffer) > 0) {
            sb.append(new String(buffer));
        }
        in.close();
        /*
         * if (this.index == null) { this.index = new Index(getIndexDirectory()); }
         * this.index.deleteDocument("handle",handle); this.index.closeIndex();
         */
        this.deleteFromIndex(handle);
        this.index = new Index(getIndexDirectory());
        this.index.addDocument();
        this.index.addField("handle", handle);
        this.index.addField("data", sb.toString().toLowerCase());
        this.index.closeDocument();
        this.index.closeIndex();
    }

    public void deleteFromIndex(String handle) {
        try {
            this.index = new Index(getIndexDirectory());
            recursiveDelete(this.hierachyManager.getPage(handle));
            this.index.closeIndex();
        }
        catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    private void recursiveDelete(Content startPage) {
        try {
            Index.deleteDocument("handle", startPage.getHandle());
            Collection c = startPage.getChildren();
            if (c.size() > 0) {
                Iterator it = c.iterator();
                while (it.hasNext()) {
                    recursiveDelete((Content) it.next());
                }
            }
        }
        catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    private void navigate(Content startPage) {
        try {
            indexPage(startPage.getHandle());
            Collection c = startPage.getChildren();
            if (c.size() > 0) {
                Iterator it = c.iterator();
                while (it.hasNext()) {
                    navigate((Content) it.next());
                }
            }
        }
        catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    private String getIndexDirectory() {
        return this.servletConfig.getInitParameter(INDEX_DIRECTORY);
    }

    private String getHost() {
        return this.servletConfig.getInitParameter(HOST);
    }

    private String getProtocol() {
        return this.servletConfig.getInitParameter(PROTOCOL);
    }

    private String getIndexPath() {
        return this.servletConfig.getInitParameter(INDEX_PATH);
    }
}
