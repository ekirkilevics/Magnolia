package info.magnolia.module.admincentral;

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.ItemType;
import info.magnolia.cms.security.Permission;
import info.magnolia.cms.util.NodeDataUtil;
import info.magnolia.context.MgnlContext;

import java.util.Iterator;

import javax.jcr.RepositoryException;

import com.vaadin.ui.Accordion;
import com.vaadin.ui.Button;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.themes.BaseTheme;


/**
 * TODO Naming? Find a proper package.
 * This class extends {@link Accordion} and is used to create the menu.
 * @author fgrilli
 *
 */
public class Navigation extends Accordion{

    private static final long serialVersionUID = 1L;

    /**
     * @param path the path to the menu
     */
    public Navigation(final String path) throws RepositoryException{
        // get it with system permission
        final Content node = MgnlContext.getSystemContext().getHierarchyManager(ContentRepository.CONFIG).getContent(path);
        // loop over the menu items
        for (Iterator<Content> iter = node.getChildren(ItemType.CONTENTNODE).iterator(); iter.hasNext();) {
            Content menuItem = iter.next();
            Panel panel = new Panel();
            panel.setStyleName(BaseTheme.PANEL_LIGHT);
            // check permission
            if (isMenuItemRenderable(menuItem)) {
                // sub menu items (2 level only)
                for (Iterator<Content> iterator = menuItem.getChildren(ItemType.CONTENTNODE).iterator(); iterator.hasNext();) {
                    Content sub = iterator.next();
                    if (isMenuItemRenderable(sub)) {
                        Button button = new Button(getLabel(sub));
                        button.setStyleName(BaseTheme.BUTTON_LINK);
                        button.setHeight(30f, Button.UNITS_PIXELS);
                        panel.addComponent(button);
                    }
                }
            }
            if(panel.getComponentIterator().hasNext()){
                addTab(panel, getLabel(menuItem), null);
            } else {
                //TODO empty tabs (e.g. website) should not be openable(sp?) but I could not figure out a way to do it with the Tab API so far
                addTab(new Label(), getLabel(menuItem), null);
            }
        }
    }

    /**
     * @param mp
     * @return
     */
    protected String getLabel(Content mp) {
        return NodeDataUtil.getI18NString(mp, "label");
    }

    /**
     * @param menuItem
     * @return
     */
    protected boolean isMenuItemRenderable(Content menuItem) {
        return MgnlContext.getAccessManager(ContentRepository.CONFIG).isGranted(menuItem.getHandle(), Permission.READ);
    }
}
