package info.magnolia.cms.beans.commands;

import org.apache.commons.chain.impl.CatalogBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base class for a  catalog implementation for magnolia
 *
 * @author <a href="mailto:niko@macnica.com">Nicolas Modrzyk</a>
 */
public abstract class MgnlBaseCatalog extends CatalogBase implements MgnlCatalog {

    static Logger log = LoggerFactory.getLogger(MgnlBaseCatalog.class);

    public abstract void initCatalog(String defaultCatalog);

    public void initCatalog() {
        initCatalog(MgnlBaseCatalogFactory.DEFAULT_CATALOG);
    }

}
