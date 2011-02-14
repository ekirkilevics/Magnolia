/**
 * This file Copyright (c) 2011 Magnolia International
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
package info.magnolia.module.vaadin.place;

import info.magnolia.module.vaadin.event.EventBus;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.UriFragmentUtility;
import com.vaadin.ui.UriFragmentUtility.FragmentChangedEvent;
import com.vaadin.ui.UriFragmentUtility.FragmentChangedListener;



/**
 * Monitors {@link PlaceChangeEvent}s and
 * browser's history events (via Vaadin's {@link UriFragmentUtility}) and keep them in sync.
 * <p>
 * Inspired by {@link com.google.gwt.place.shared.PlaceHistoryHandler}
 * <p>
 * TODO: At the moment this is a Vaadin's {@link CustomComponent} and must to be added to an app component <strong>(except those wrapped in {@link ComponentContainerWrappingRegion} else it is removed!)</strong>.
 * This is because internally it uses {@link UriFragmentUtility} which MUST be attached to the application main window in order to work. An alternative to
 * using UriFragmentUtility would be to implement our own Vaadin widget (server and client sides) to mimic GWT's {@link Historian}.
 * @author fgrilli
 */
@SuppressWarnings("serial")
public class PlaceHistoryHandler extends CustomComponent implements FragmentChangedListener{
  private static final Logger log = LoggerFactory.getLogger(PlaceHistoryHandler.class.getName());

  private final UriFragmentUtility historian = new UriFragmentUtility();
  //TODO we need this to set it as composition root else null parent exception arises.
  //We cannot set historian itself as root else we get java.lang.UnsupportedOperationException
  //at com.vaadin.ui.CustomComponent.removeComponent(CustomComponent.java:248)
  private final UriFragmentUtility dummy = new UriFragmentUtility();

  private final PlaceHistoryMapper mapper;

  private PlaceController placeController;

  private Place defaultPlace = Place.NOWHERE;

  /**
   * Create a new PlaceHistoryHandler.
   *
   * @param mapper a {@link PlaceHistoryMapper} instance
   */
  public PlaceHistoryHandler(PlaceHistoryMapper mapper) {
      this.mapper = mapper;
      setCompositionRoot(dummy);
      addListener(this);
  }

  /**
   * Handle the current history token. Typically called at application start, to
   * ensure bookmark launches work.
   */
  public void handleCurrentHistory() {
    handleHistoryToken(historian.getFragment());
  }

  public void fragmentChanged(FragmentChangedEvent source) {
      String token = source.getUriFragmentUtility().getFragment();
      log.debug("fragmentChanged with token {}", token);
      handleHistoryToken(token);
  }

  /**
   * Initialize this place history handler.
   *
   */
  public void register(PlaceController placeController, EventBus eventBus, Place defaultPlace) {
    this.placeController = placeController;
    this.defaultPlace = defaultPlace;

    eventBus.addListener(new PlaceChangeListener() {
        public void onPlaceChange(PlaceChangeEvent event) {
            Place newPlace = event.getNewPlace();
            log.debug("onPlaceChange...");
            historian.setFragment(tokenForPlace(newPlace), false);
        }
    });
  }

  public final void addListener(FragmentChangedListener fragmentChangedListener) {
      historian.addListener(fragmentChangedListener);
  }

  public final void removeListener(FragmentChangedListener fragmentChangedListener) {
      historian.removeListener(fragmentChangedListener);
  }

  @Override
  public void detach() {
      log.debug("detaching listener...");
      super.detach();
      removeListener(this);
  }

  @Override
  public void attach() {
      log.debug("attaching listener...");
      super.attach();
      //In order to work, UriFragmentUtility MUST be attached to the application main window
      getApplication().getMainWindow().addComponent(historian);
  }

    private void handleHistoryToken(String token) {

        Place newPlace = null;

        if ("".equals(token)) {
            newPlace = defaultPlace;
        }

        if (newPlace == null) {
            newPlace = mapper.getPlace(token);
        }

        if (newPlace == null) {
            log.warn("Unrecognized history token: {}, falling back to default place...", token);
            newPlace = defaultPlace;
        }
        log.debug("handleHistoryToken with place {}", newPlace);
        placeController.goTo(newPlace);
    }

    private String tokenForPlace(Place newPlace) {
        //FIXME if uncommented the URI fragment won't be written
        /*if (defaultPlace.equals(newPlace)) {
            return "";
        }*/

        String token = mapper.getToken(newPlace);
        if (token != null) {
            log.debug("tokenForPlace returns token [{}]", token);
            return token;
        }

        log.debug("Place not mapped to a token: {}", newPlace);
        return "";
    }
}
