package info.magnolia.cms.beans.commands;

import org.apache.commons.chain.impl.CatalogBase;
import org.apache.log4j.Logger;

/**
 * Base class for a  catalog implementation for magnolia
 *
 * @author <a href="mailto:niko@macnica.com">Nicolas Modrzyk</a>
 */
public abstract class MgnlBaseCatalog extends CatalogBase implements MgnlCatalog {

    static Logger log = Logger.getLogger(MgnlBaseCatalog.class);

    public abstract void initCatalog(String defaultCatalog);

    public void initCatalog() {
        initCatalog(MgnlBaseCatalogFactory.DEFAULT_CATALOG);
    }

}
