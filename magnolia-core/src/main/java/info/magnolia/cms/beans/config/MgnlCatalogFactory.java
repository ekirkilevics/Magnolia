package info.magnolia.cms.beans.config;

import info.magnolia.cms.core.Path;
import org.apache.commons.chain.Catalog;
import org.apache.commons.chain.CatalogFactory;
import org.apache.commons.chain.config.ConfigParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;

/**
 * Created by IntelliJ IDEA.
 * User: niko
 * Date: Mar 24, 2006
 * Time: 6:30:32 PM
 * To change this template use File | Settings | File Templates.
 */
public class MgnlCatalogFactory extends CatalogFactory {

    static Logger log = LoggerFactory.getLogger(MgnlCatalogFactory.class);
    static MgnlCatalog catalog;

    public Catalog getCatalog() {
        if (catalog == null) {
            catalog = new MgnlCatalog();
            ConfigParser parser = new ConfigParser();
            URL url = null;
            try {
                url = new URL("file://" + Path.getAbsoluteFileSystemPath("WEB-INF/config/default/commands.xml"));
            } catch (MalformedURLException e) {
                log.error("Could not find the command file.Commands will be unusable", e);
            }
            try {
                parser.parse(catalog, url);
            }
            catch (Exception e) {
                log.error("Could not parse the command file.Commands will be unusable", e);
            }
            return catalog;
        } else
            return catalog;
    }

    public void setCatalog(Catalog catalog_) {
        throw new RuntimeException("Not Implemented Yet");
    }

    public Catalog getCatalog(String string) {
        throw new RuntimeException("Not Implemented Yet");
    }

    public void addCatalog(String string, Catalog catalog_) {
        throw new RuntimeException("Not Implemented Yet");
    }

    public Iterator getNames() {
        throw new RuntimeException("Not Implemented Yet");
    }
}
