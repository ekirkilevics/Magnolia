package info.magnolia.module.vaadin.component;

import com.vaadin.ui.Component;
import com.vaadin.ui.CustomComponent;


public class ComponentDisplay extends CustomComponent implements HasComponent {

    public void setComponent(Component component) {
        setCompositionRoot(component);
    }

}
