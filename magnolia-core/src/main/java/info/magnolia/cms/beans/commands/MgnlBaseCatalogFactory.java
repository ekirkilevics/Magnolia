package info.magnolia.cms.beans.commands;

import info.magnolia.cms.util.FactoryUtil;
import org.apache.commons.chain.Catalog;
import org.apache.commons.chain.CatalogFactory;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.Iterator;

/**
 * This is the base class to create CatalogFactory for magnolia. Most of the code is common except the way of loading
 * a new catalog.
 *
 * @author <a href="mailto:niko@macnica.com">Nicolas Modrzyk</a>
 */
public class MgnlBaseCatalogFactory extends CatalogFactory implements MgnlCatalogFactory {

    static Logger log = Logger.getLogger(MgnlBaseCatalogFactory.class);

    static final String DEFAULT_CATALOG = "default";

    HashMap catalogs = new HashMap();

    static MgnlBaseCatalogFactory instance = new MgnlBaseCatalogFactory();

    public static CatalogFactory getMgnlInstance() {
        return instance;
    }

    public Catalog getCatalog() {
        return getCatalog(DEFAULT_CATALOG);
    }

    public void setCatalog(Catalog catalog_) {
        addCatalog(DEFAULT_CATALOG, catalog_);
    }

    public Catalog getCatalog(String string) {
        if (log.isDebugEnabled())
            log.debug("Gettings catalog:" + string);
        Catalog catalog = (Catalog) catalogs.get(string);
        if (catalog == null) {
            catalog = initCatalog(string);
            catalogs.put(string, catalog);
            return catalog;
        } else
            return catalog;
    }

    public Catalog initCatalog(final String string) {
        // Prefer not to use Catalog straight away here, so we use MgnlCatalogInterface.
        MgnlBaseCatalog catalog = (MgnlBaseCatalog) FactoryUtil.getInstance(MgnlCatalog.class);
        catalog.initCatalog(string);
        return catalog;
    }

    public void addCatalog(String string, Catalog catalog_) {
        catalogs.put(string, catalog_);
    }

    public Iterator getNames() {
        return catalogs.keySet().iterator();
    }
}
