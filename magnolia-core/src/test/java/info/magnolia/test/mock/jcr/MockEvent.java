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
package info.magnolia.test.mock.jcr;

import java.util.LinkedHashMap;
import java.util.Map;
import javax.jcr.observation.Event;

/**
 * @version $Id$
 */
public class MockEvent implements Event {

    private int type;
    private String path;
    private String userID;
    private String userData;
    private long date = System.currentTimeMillis();
    private String identifier;
    private Map info = new LinkedHashMap();

    @Override
    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    @Override
    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    @Override
    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    @Override
    public String getUserData() {
        return userData;
    }

    public void setUserData(String userData) {
        this.userData = userData;
    }

    @Override
    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }

    @Override
    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    @Override
    public Map getInfo() {
        return info;
    }

    public void setInfo(Map info) {
        this.info = info;
    }

    public static MockEvent nodeAdded(String absPath) {
        MockEvent event = new MockEvent();
        event.setType(Event.NODE_ADDED);
        event.setPath(absPath);
        return event;
    }

    public static MockEvent nodeRemoved(String absPath) {
        MockEvent event = new MockEvent();
        event.setType(Event.NODE_REMOVED);
        event.setPath(absPath);
        return event;
    }

    public static MockEvent propertyAdded(String path) {
        MockEvent event = new MockEvent();
        event.setType(Event.PROPERTY_ADDED);
        event.setPath(path);
        return event;
    }
}
