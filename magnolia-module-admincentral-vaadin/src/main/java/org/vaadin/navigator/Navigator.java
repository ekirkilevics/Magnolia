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
package org.vaadin.navigator;

import info.magnolia.module.admincentral.views.IFrameView;

import java.util.HashMap;
import java.util.Iterator;

import com.vaadin.Application;
import com.vaadin.terminal.ExternalResource;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.ComponentContainer;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.UriFragmentUtility;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.UriFragmentUtility.FragmentChangedEvent;
import com.vaadin.ui.UriFragmentUtility.FragmentChangedListener;

/**
 * FIXME Reworking this to solve some shortcomings of the current Navigator addon.
 * As of now, this component handles transparently the instancing of the {@link View}s implementations registered with the {@link Navigator#addView(String, Class)} method.
 * It also handles transparently where to place and remove the views in the current layout.
 * TODO: remove limitation concerning not being able to register the same View more than once.
 * TODO: make Navigator aware of the menu so that it can restore its status both when browsing history and bookmarking.
 * (Should a view be a composite of menu and other UI elements, instead of just the container where the main content is shown as it now?)
 *
 *Please notice that the Magnolia header is there only to make checkstyle happy.
 */
@SuppressWarnings("serial")
public class Navigator extends CustomComponent {

    private HashMap<String, Class> uriToClass = new HashMap();
    private HashMap<Class, String> classToUri = new HashMap();
    private HashMap<Class, View> classToView = new HashMap();
    private String mainViewUri = null;
    private VerticalLayout layout = new VerticalLayout();
    private UriFragmentUtility uriFragmentUtil = new UriFragmentUtility();
    private ComponentContainer mainViewContainer;
    private String currentFragment = "";
    private View currentView = null;

    /**
     * @param mainViewContainer an instance of {@link ComponentContainer} holding the main contents (e.g. trees, pages, etc.).
     * In our current layout this is the right component of a split panel.
     */
    public Navigator(ComponentContainer mainViewContainer) {
        this.mainViewContainer = mainViewContainer;
        layout.setSizeFull();
        setSizeFull();
        layout.addComponent(uriFragmentUtil);
        setCompositionRoot(layout);
        uriFragmentUtil.addListener(new FragmentChangedListener() {
            public void fragmentChanged(FragmentChangedEvent source) {
                Navigator.this.fragmentChanged();
            }
        });
    }

    private void fragmentChanged() {
        String newFragment = uriFragmentUtil.getFragment();
        if ("".equals(newFragment)) {
            newFragment = mainViewUri;
        }
        int i = newFragment.indexOf('/');
        String uri = i < 0 ? newFragment : newFragment.substring(0, i);
        final String requestedDataId = i < 0 || i + 1 == newFragment.length() ? null
                : newFragment.substring(i + 1);
        if (uriToClass.containsKey(uri)) {
            final View newView = getOrCreateView(uri);

            String warn = currentView == null ? null : currentView
                    .getWarningForNavigatingFrom();
            if (warn != null && warn.length() > 0) {
                confirmedMoveToNewView(requestedDataId, newView, warn);
            } else {
                moveTo(newView, requestedDataId, false);
            }

        } else {
            uriFragmentUtil.setFragment(currentFragment, false);
        }
    }

    private void confirmedMoveToNewView(final String requestedDataId,
            final View newView, String warn) {
        VerticalLayout lo = new VerticalLayout();
        lo.setMargin(true);
        lo.setSpacing(true);
        lo.setWidth("400px");
        final Window wDialog = new Window("Warning", lo);
        wDialog.setModal(true);
        final Window main = getWindow();
        main.addWindow(wDialog);
        lo.addComponent(new Label(warn));
        lo
                .addComponent(new Label(
                        "If you do not want to navigate away from the current screen, press Cancel."));
        Button cancel = new Button("Cancel", new Button.ClickListener() {

            public void buttonClick(ClickEvent event) {
                uriFragmentUtil.setFragment(currentFragment, false);
                main.removeWindow(wDialog);
            }
        });
        Button cont = new Button("Continue", new Button.ClickListener() {

            public void buttonClick(ClickEvent event) {
                main.removeWindow(wDialog);
                moveTo(newView, requestedDataId, false);
            }

        });
        HorizontalLayout h = new HorizontalLayout();
        h.addComponent(cancel);
        h.addComponent(cont);
        h.setSpacing(true);
        lo.addComponent(h);
        lo.setComponentAlignment(h, "r");
    }

    private View getOrCreateView(String uri) {
        Class newViewClass = uriToClass.get(uri);
        if (!classToView.containsKey(newViewClass)) {
        try {
               View view = (View) newViewClass.newInstance();
               view.init(this, getApplication());
               classToView.put(newViewClass, view);
             } catch (InstantiationException e) {
               e.printStackTrace();
               throw new RuntimeException(e);
             } catch (IllegalAccessException e) {
               e.printStackTrace();
               throw new RuntimeException(e);
             }               
        }    
        final View v = classToView.get(newViewClass);
        return v;    
    }

    private void moveTo(View v, String requestedDataId,
            boolean noFragmentSetting) {
        currentFragment = classToUri.get(v.getClass());
        if (requestedDataId != null) {
            currentFragment += "/" + requestedDataId;
        }
        if (!noFragmentSetting
                && !currentFragment.equals(uriFragmentUtil.getFragment())) {
            uriFragmentUtil.setFragment(currentFragment, false);
        }
        //TODO just a temporary hack for the M1 release as IFrameView currently does not work and would show an ugly gray page.
        if(v instanceof IFrameView){
            return;
        }
        Component removeMe = null;
        for (Iterator<Component> i = mainViewContainer.getComponentIterator(); i.hasNext();) {
            removeMe = i.next();
        }
        if (removeMe != null) {
            mainViewContainer.removeComponent(removeMe);
        }
        mainViewContainer.addComponent(v);
        v.navigateTo(requestedDataId);
        currentView = v;
    }

    /**
     * Get the main view.
     *
     * Main view is the default view shown to user when he opens application
     * without specifying view uri.
     *
     * @return Uri of the main view.
     */
    public String getMainView() {
        return mainViewUri;
    }

    /**
     * Set the main view.
     *
     * Main view is the default view shown to user when he opens application
     * without specifying view uri. If main view has not been set, the first
     * view registered with addView() is used as main view. Note that the view
     * must be registered with addView() before calling this method.
     *
     * @param mainViewUri
     *            Uri of the main view.
     */
    public void setMainView(String mainViewUri) {
        if (uriToClass.containsKey(mainViewUri)) {
            this.mainViewUri = mainViewUri;
            if (currentView == null) {
                moveTo(getOrCreateView(mainViewUri), null, true);
            }
        } else {
            throw new IllegalArgumentException(
                    "No view with given uri can be found in the navigator");
        }
    }

    /**
     * Add a new view to navigator.
     *
     * Register a view to navigator.
     *
     * @param uri
     *            String that identifies a view. This is the string that is
     *            shown in URL after #
     * @param viewClass
     *            Component class that implements Navigator.View interface
     */
    public void addView(String uri, Class viewClass) {

        // Check parameters
        if (!View.class.isAssignableFrom(viewClass)) {
            throw new IllegalArgumentException(
                    "viewClass must implemenent Navigator.View");
        }

        if (uri == null || viewClass == null || uri.length() == 0) {
            throw new IllegalArgumentException(
                    "viewClass and uri must be non-null and not empty");
        }

        if (uriToClass.containsKey(uri)) {
            if (uriToClass.get(uri) == viewClass) {
                return;
            }

            throw new IllegalArgumentException(uriToClass.get(uri).getName()
                    + " is already mapped to '" + uri + "'");
        }

        if (classToUri.containsKey(viewClass)) {
            throw new IllegalArgumentException(
                    "Each view class can only be added to Navigator with one uri");
        }

        if (uri.indexOf('/') >= 0 || uri.indexOf('#') >= 0) {
            throw new IllegalArgumentException(
                    "Uri can not contain # or / characters");
        }

        uriToClass.put(uri, viewClass);
        classToUri.put(viewClass, uri);

        if (getMainView() == null) {
            setMainView(uri);
        }
    }

    /**
     * Remove view from navigator.
     *
     * @param uri
     *            Uri of the view to remove.
     */
    public void removeView(String uri) {
        Class c = uriToClass.get(uri);
        if (c != null) {
            uriToClass.remove(uri);
            classToUri.remove(c);
            if (getMainView() == null || getMainView().equals(getMainView())) {
                if (uriToClass.size() == 0) {
                    mainViewUri = null;
                } else {
                    setMainView(uriToClass.keySet().iterator().next());
                }
            }
        }
    }

    /**
     * Get the uri for given view implementation class.
     *
     * @param viewClass
     *            Class that implements the view.
     * @return Uri registered for the view class.
     */
    public String getUri(Class viewClass) {
        return classToUri.get(viewClass);
    }

    /**
     * Get the view class for given uri.
     *
     * @param uri
     *            Uri to get view for
     * @return View that corresponds to the uri
     */
    public Class getViewClass(String uri) {
        return uriToClass.get(uri);
    }

    /**
     * Switch to view identified with uri.
     *
     * Uri can be either the exact uri registered previously with addView() or
     * it can also contain data id passed to the view. In case data id is
     * included, the format is 'uri/freeFormedDataIdString'.
     *
     * @param uri
     *            Uri where to navigate.
     */
    public void navigateTo(String uri) {
        uriFragmentUtil.setFragment(uri);
    }

    /**
     * Switch to view implemented by given class.
     *
     * Note that the view must be registered to navigator with addView() before
     * calling this method.
     *
     * @param viewClass
     *            Class that implements the view.
     */
    public void navigateTo(Class viewClass) {
        String uri = getUri(viewClass);
        if (uri != null) {
            navigateTo(uri);
        }
    }

    /**
     * Interface for all views controlled by the navigator.
     *
     * Each view added to the navigator must implement this interface.
     */
    public interface View extends Component {

        /**
         * Init view.
         *
         * Convenience method which navigator call before slightly before the
         * view is first time rendered. This is called only once in the lifetime
         * of each view instance. In many cases it is better to construct UI
         * within this method than in constructor as you are guaranteed to get
         * references to application and navigator here.
         *
         * @param navigator
         *            Reference to navigator that controls the window where this
         *            view is attached to.
         * @param application
         *            Application instance this view lives in.
         */
        public void init(Navigator navigator, Application application);

        /**
         * This view is navigated to.
         *
         * This method is always called before the view is shown on screen. If
         * there is any additional id to data what should be shown in the view,
         * it is also optinally passed as parameter.
         *
         * @param requestedDataId
         *            Id of the data extracted from URI fragment or null if not
         *            given. This is the string that appeards in URI after
         *            #viewname/
         */
        public void navigateTo(String requestedDataId);

        /**
         * Get a warning that should be shown to user before navigating away
         * from the view.
         *
         * If the current view is in state where navigating away from it could
         * lead to data loss, this method should return a message that will be
         * shown to user before he confirms that he will leave the screen. If
         * there is no need to ask questions from user, this should return null.
         *
         * @return Message to be shown or null if no message should be shown.
         */
        public String getWarningForNavigatingFrom();
    }

    /**
     * Interface implemented by all applications that uses Navigator.
     *
     */
    public interface NavigableApplication {

        /**
         * Create a new browser window.
         *
         * This method must construc a new window that could be used as a main
         * window for the application. Each call to this method must create a
         * new instance and your application should work when there are multiple
         * instances of concurrently. Each window can contain anything you like,
         * but at least they should contain a new Navigator instance for
         * controlling navigation within the window. Typically one also adds
         * somekind of menu for commanding navigator.
         *
         * @return New window.
         */
        public Window createNewWindow();
    }

    /**
     * Helper for overriding Application.getWindow(String).
     *
     * <p>
     * This helper makes implementing support for multiple browser tabs or
     * browser windows easy. Just override Application.getWindow(String) in your
     * application like this:
     * </p>
     *
     * <pre>
     * &#064;Override
     * public Window getWindow(String name) {
     *     return Navigator.getWindow(this, name, super.getWindow(name));
     * }
     * </pre>
     *
     * @param application
     *            Application instance, which implements
     *            Navigator.NavigableApplication interface.
     * @param name
     *            Name parameter from Application.getWindow(String name)
     * @param superGetWindow
     *            The window returned by super.getWindow(name)
     * @return
     */
    public static Window getWindow(NavigableApplication application,
            String name, Window superGetWindow) {
        if (superGetWindow != null) {
            return superGetWindow;
        }

        Window w = application.createNewWindow();
        w.setName(name);
        ((Application) application).addWindow(w);
        w.open(new ExternalResource(w.getURL()));
        return w;
    }
}
