/**
 * This file Copyright (c) 2011 Magnolia International
 * Ltd.  (http://www.magnolia.info). All rights reserved.
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
 * is available at http://www.magnolia.info/mna.html
 *
 * Any modifications to this file must keep this entire header
 * intact.
 *
 */
package info.magnolia.module.wcm.activity;

import info.magnolia.cms.beans.config.URI2RepositoryManager;
import info.magnolia.context.MgnlContext;
import info.magnolia.exception.RuntimeRepositoryException;
import info.magnolia.jcr.util.MetaDataUtil;
import info.magnolia.module.wcm.editor.PageChangedEvent;
import info.magnolia.module.wcm.editor.PageChangedHandler;
import info.magnolia.module.wcm.editor.PageEditor;
import info.magnolia.module.wcm.editor.SelectionChangedEvent;
import info.magnolia.module.wcm.editor.SelectionType;
import info.magnolia.module.wcm.place.PageEditorPlace;
import info.magnolia.objectfactory.ComponentProvider;
import info.magnolia.rendering.template.TemplateDefinition;
import info.magnolia.rendering.template.registry.TemplateDefinitionRegistrationException;
import info.magnolia.rendering.template.registry.TemplateDefinitionRegistry;
import info.magnolia.ui.framework.activity.AbstractActivity;
import info.magnolia.ui.framework.event.EventBus;
import info.magnolia.ui.framework.view.View;
import info.magnolia.ui.framework.view.ViewPort;
import info.magnolia.ui.vaadin.integration.view.IsVaadinComponent;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import com.vaadin.ui.Component;

/**
 * Activity for page editing.
 */
public class PageEditorActivity extends AbstractActivity implements PageChangedHandler {

    private ComponentProvider componentProvider;
    private PageEditorPlace place;
    private EventBus eventBus;
    private EditorView editorView;
    private URI2RepositoryManager uri2RepositoryManager;
    private TemplateDefinitionRegistry templateDefinitionRegistry;

    public PageEditorActivity(ComponentProvider componentProvider, PageEditorPlace place, EventBus eventBus, URI2RepositoryManager uri2RepositoryManager, TemplateDefinitionRegistry templateDefinitionRegistry) {
        this.componentProvider = componentProvider;
        this.place = place;
        this.eventBus = eventBus;
        this.uri2RepositoryManager = uri2RepositoryManager;
        this.templateDefinitionRegistry = templateDefinitionRegistry;

        this.eventBus.addHandler(PageChangedEvent.class, this);
    }

    @Override
    public void start(ViewPort viewPort, EventBus eventBus) {
        String path = place.getPath();
        editorView = new EditorView(path);
        viewPort.setView(editorView);

        String workspace = uri2RepositoryManager.getRepository(path);
        path = uri2RepositoryManager.getHandle(path);

        String dialog = getDialogUsedByTemplate(path, workspace);

        eventBus.fireEvent(new SelectionChangedEvent(SelectionType.PAGE, workspace, path, null, null, null, dialog));
    }

    private String getDialogUsedByTemplate(String path, String workspace) {
        try {
            Session session = MgnlContext.getJCRSession(workspace);
            Node node = session.getNode(path);
            String template = MetaDataUtil.getMetaData(node).getTemplate();
            TemplateDefinition templateDefinition = templateDefinitionRegistry.getTemplateDefinition(template);
            return templateDefinition.getDialog();
        } catch (RepositoryException e) {
            throw new RuntimeRepositoryException(e);
        } catch (TemplateDefinitionRegistrationException e1) {
            // TODO dlipp: apply consistent ExceptionHandling.
            throw new RuntimeException(e1);
        }
    }

    @Override
    public void onPageChanged() {
        editorView.getPageEditor().reload();
    }

    private class EditorView implements View, IsVaadinComponent {

        private String path;
        private Component component;
        private PageEditor pageEditor;

        private EditorView(String path) {
            this.path = path;
            pageEditor = componentProvider.newInstance(PageEditor.class, MgnlContext.getContextPath() + path);
            pageEditor.setSizeFull();
            component = pageEditor;
        }

        public PageEditor getPageEditor() {
            return pageEditor;
        }

        @Override
        public Component asVaadinComponent() {
            return component;
        }
    }
}
