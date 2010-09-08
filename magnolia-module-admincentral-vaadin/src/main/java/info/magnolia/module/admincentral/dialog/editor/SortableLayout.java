/**
 * This file Copyright (c) 2010 Magnolia International
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
package info.magnolia.module.admincentral.dialog.editor;

import java.util.Iterator;

import com.vaadin.event.Transferable;
import com.vaadin.event.LayoutEvents.LayoutClickEvent;
import com.vaadin.event.LayoutEvents.LayoutClickListener;
import com.vaadin.event.dd.DragAndDropEvent;
import com.vaadin.event.dd.DropHandler;
import com.vaadin.event.dd.DropTarget;
import com.vaadin.event.dd.TargetDetails;
import com.vaadin.event.dd.acceptcriteria.AcceptCriterion;
import com.vaadin.event.dd.acceptcriteria.Not;
import com.vaadin.event.dd.acceptcriteria.SourceIsTarget;
import com.vaadin.terminal.gwt.client.ui.dd.VerticalDropLocation;
import com.vaadin.ui.AbstractOrderedLayout;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.DragAndDropWrapper;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.Label;

/**
 * Custom container with support for reordering components.
 * @author had
 * @version $Id: $
 */
public class SortableLayout extends CustomComponent {
    private final AbstractOrderedLayout layout;
    private final DropHandler dropHandler;
    private final Component defaultComponent;
    protected DialogEditorField lastSelectedDEF;
    private FormLayout fieldEditingTarget;

    public SortableLayout(AbstractOrderedLayout layout, final Component defaultComponent) {
        this.defaultComponent = defaultComponent;
        this.layout = layout;
        dropHandler = new ReorderLayoutDropHandler(layout, defaultComponent);

        DragAndDropWrapper pane = new DragAndDropWrapper(layout);
        setCompositionRoot(pane);
    }

    public SortableLayout(AbstractOrderedLayout layout, Label dropFieldsLabel, FormLayout fieldEditingTarget) {
        this(layout, dropFieldsLabel);
        this.fieldEditingTarget = fieldEditingTarget;
    }

    @Override
    public void addComponent(Component component) {
        DraggableComponent wrapper = new DraggableComponent(component,
                dropHandler);
        wrapper.setSizeUndefined();

        // we support only vertical drops
        component.setWidth("100%");
        wrapper.setWidth("100%");

        layout.addComponent(wrapper);
    }

    public DropHandler getDropHandler() {
       return dropHandler;
    }


    private static class ReorderLayoutDropHandler implements DropHandler {

        private AbstractOrderedLayout targetLayout;
        private Component defaultComponent;

        public ReorderLayoutDropHandler(AbstractOrderedLayout layout, Component defaultComponent) {
            this.targetLayout = layout;
            this.defaultComponent = defaultComponent;
        }

        public AcceptCriterion getAcceptCriterion() {
            return new Not(SourceIsTarget.get());
        }

        public void drop(DragAndDropEvent dropEvent) {
            Transferable transferable = dropEvent.getTransferable();
            Component sourceComponent = transferable.getSourceComponent();
            if (sourceComponent instanceof DraggableComponent) {
                TargetDetails dropTargetData = dropEvent.getTargetDetails();
                DropTarget target = dropTargetData.getTarget();

                // find the location where to move the dragged component
                boolean sourceWasAfterTarget = true;
                int index = 0;
                Iterator<Component> componentIterator = targetLayout
                        .getComponentIterator();
                Component draggedComponnent = null;
                while (draggedComponnent != target && componentIterator.hasNext()) {
                    draggedComponnent = componentIterator.next();
                    if (draggedComponnent != sourceComponent) {
                        index++;
                    } else {
                        sourceWasAfterTarget = false;
                    }
                }
                if (draggedComponnent == null || draggedComponnent != target) {
                    // component not found - if dragging from another layout
                    System.out.println("WARN: dragged from other layout ... ");
                    //return;
                }

                // drop on top of target?
                if (dropTargetData.getData("verticalLocation").equals(
                        VerticalDropLocation.MIDDLE.toString())) {
                    if (sourceWasAfterTarget) {
                        index--;
                    }
                }

                // drop before the target?
                else if (dropTargetData.getData("verticalLocation").equals(
                        VerticalDropLocation.TOP.toString())) {
                    index--;
                    if (index < 0) {
                        index = 0;
                    }
                }

                System.out.println("LAy:" + targetLayout + ", DC:" + defaultComponent);
                DraggableComponent wrappedSource = ((DraggableComponent) sourceComponent);
                // adding new or moving existing???
                if (wrappedSource.getTransferredComponent() instanceof TemplateControl) {
                    TemplateControl template = (TemplateControl) wrappedSource.getTransferredComponent();

                    // comps are in Vertical (or horizontal) Layout that is wrapped by D&DWrapper wrapped by SortableLayout ... how ugly is that?
                    final SortableLayout targetTopLayout = (SortableLayout) targetLayout.getParent().getParent();

                    final DialogEditorField controlComponent = template.getControlComponent(targetLayout.getWindow());

                    controlComponent.addListener(new LayoutClickListener() {

                        public void layoutClick(LayoutClickEvent event) {
                            System.out.println("Clicked on " + controlComponent.getCaption());

                            controlComponent.setSelected(true);
                            controlComponent.getOriginalTemplate().configureIn(controlComponent, targetTopLayout.fieldEditingTarget);
                            // TODO: why this doesn't repaint ALL children?
                            //DialogEditorField.this.getParent().getParent().requestRepaint();
                            for (Iterator<Component> iter = targetLayout.getComponentIterator(); iter.hasNext();) {
                                DialogEditorField tc = (DialogEditorField) ((DraggableComponent) iter.next()).getTransferredComponent();
                                if (controlComponent != tc)  {
                                    tc.setSelected(false);
                                }
                                tc.requestRepaint();
                            }
                        }
                    });

                    // the drop handler have to be that associated with the target not the one associated with the source !!!!
                    targetLayout.addComponent(new DraggableComponent(controlComponent, targetTopLayout.getDropHandler()), index);
                    if (defaultComponent != null && defaultComponent.getParent() != null) {
                        // this is the default component of the target which is not needed once we have other components in
                        targetLayout.removeComponent(defaultComponent.getParent());
                        // since it doesn't seem to be done automatically here ... TODO: another Vaadin bug?
                        defaultComponent.setParent(null);
                    }
                } else {
                    // already instantiated component ... either it is moved within same container or it is actually removed from the container
                    AbstractOrderedLayout sourceLayout = ((AbstractOrderedLayout) sourceComponent.getParent());
                    // comps are in Vertical (or horizontal) Layout that is wrapped by D&DWrapper wrapped by SortableLayout
                    SortableLayout topLayout = (SortableLayout) sourceLayout.getParent().getParent();

                    // do not remove default placeholder component
                    if (((DraggableComponent) sourceComponent).getTransferredComponent() != topLayout.defaultComponent) {
                        sourceLayout.removeComponent(sourceComponent);
                    }
                    // re-add only when moving within container, otherwise drop for good
                    if (sourceLayout == targetLayout) {
                        targetLayout.addComponent(sourceComponent, index);
                    } else {
                        if (!sourceLayout.getComponentIterator().hasNext() && topLayout.defaultComponent != null && ((DraggableComponent) sourceComponent).getTransferredComponent() != topLayout.defaultComponent) {
                            topLayout.addComponent(topLayout.defaultComponent);
                        }
                    }
                }
            }
        }
    };
}
