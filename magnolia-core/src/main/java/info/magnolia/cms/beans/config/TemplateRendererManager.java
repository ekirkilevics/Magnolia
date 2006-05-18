package info.magnolia.cms.beans.config;

import info.magnolia.cms.beans.runtime.TemplateRenderer;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.ItemType;
import info.magnolia.cms.util.FactoryUtil;

import java.text.MessageFormat;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.lang.StringUtils;


/**
 * @author fgiust
 * @version $Revision$ ($Author$)
 */
public class TemplateRendererManager extends ObservedManager {

    private static final String ND_RENDERER = "renderer";

	private static final String ND_TYPE = "type";

	private Map renderers = new HashMap();

    /**
     * The current implementation of the TemplateManager. Defined in magnolia.properties.
     */
    private static TemplateRendererManager instance = (TemplateRendererManager) FactoryUtil
        .getSingleton(TemplateRendererManager.class);

    /**
     * @return Returns the instance.
     */
    public static TemplateRendererManager getInstance() {
        return instance;
    }

    /**
     * @see info.magnolia.cms.beans.config.ObservedManager#onRegister(info.magnolia.cms.core.Content)
     */
    protected void onRegister(Content node) {
        Collection list = node.getChildren(ItemType.CONTENTNODE);
        for (Iterator iter = list.iterator(); iter.hasNext();) {
            Content tr = (Content) iter.next();
            String type = tr.getNodeData(ND_TYPE).getString();
            String rendererClass = tr.getNodeData(ND_RENDERER).getString();

            if (StringUtils.isEmpty(type)) {
                type = tr.getName();
            }

            if (StringUtils.isBlank(type) || StringUtils.isBlank(rendererClass)) {
                log.warn("Can't register template render at {}, type=\"{}\" renderer=\"{}\"", new Object[]{
                    tr.getHandle(),
                    type,
                    rendererClass});
                continue;
            }

            TemplateRenderer renderer;

            try {
                renderer = (TemplateRenderer) Class.forName(rendererClass).newInstance();
            }
            catch (Exception e) {
                log.warn(

                MessageFormat.format(
                    "Can't register template render at {0}, type=\"{1}\" renderer=\"{2}\" due to a {3} exception: {4}",
                    new Object[]{tr.getHandle(), type, rendererClass, e.getClass().getName(), e.getMessage()}), e);
                continue;
            }

            if (log.isDebugEnabled())
				log.debug("Registering template render [{}] for type {}",rendererClass, type);
			registerTemplateRenderer(type, renderer);
        }

    }

    /**
     * @see info.magnolia.cms.beans.config.ObservedManager#onClear()
     */
    protected void onClear() {
        this.renderers.clear();
    }

    public void registerTemplateRenderer(String type, TemplateRenderer instance) {
        synchronized (renderers) {
            renderers.put(type, instance);
        }
    }

    public TemplateRenderer getRenderer(String type) {
        return (TemplateRenderer) renderers.get(type);
    }

}
