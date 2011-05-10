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
package info.magnolia.ui.framework.place;

/**
 * Abstract implementation of {@link PlaceHistoryMapper}.
 */
public abstract class AbstractPlaceHistoryMapper implements PlaceHistoryMapper  {

  /**
   * Return value for
   * {@link AbstractPlaceHistoryMapper#getPrefixAndToken(Place)}.
   */
  public static class PrefixAndToken {
    public final String prefix;
    public final String token;

    public PrefixAndToken(String prefix, String token) {
      assert prefix != null && !prefix.contains(":");
      this.prefix = prefix;
      this.token = token;
    }

    @Override
    public String toString() {
      return (prefix.length() == 0) ? token : prefix + ":" + token;
    }
  }

  @Override
public Place getPlace(String token) {
    int colonAt = token.indexOf(':');
    if (colonAt > 0) {
      String initial = token.substring(0, colonAt);
      String rest = token.substring(colonAt + 1);
      PlaceTokenizer<?> tokenizer = getTokenizer(initial);
      if (tokenizer != null) {
        return tokenizer.getPlace(rest);
      }
    }
    return null;
  }

  @Override
public String getToken(Place place) {
    PrefixAndToken token = getPrefixAndToken(place);
    if (token != null) {
      return token.toString();
    }
    return null;
  }

  /**
   * @param newPlace what needs tokenizing
   * @return the token, or null
   */
  protected abstract PrefixAndToken getPrefixAndToken(Place newPlace);

  /**
   * @param prefix the prefix found on the history token
   * @return the PlaceTokenizer registered with that token, or null
   */
  protected abstract PlaceTokenizer<?> getTokenizer(String prefix);
}
