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
package info.magnolia.module.vaadin.shell;

import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

/**
 * Allows tto get sub fragments of a url fragment. Each fragment is separated with the
 * {@link #FRAGMENT_SEPARATOR} and has the format <code>id:subfragment</code>.
 */
public class Fragmenter {

    private static final String ID_SEPARATOR = ":";

    public static final String FRAGMENT_SEPARATOR = "~";

    private Map<String, Fragment> fragments = new LinkedHashMap<String, Fragment>();


    public Fragmenter(String fragment) {
        if(fragment == null){
            return;
        }
        String[] subFragments = StringUtils.split(fragment, FRAGMENT_SEPARATOR);
        for (String subFragment : subFragments) {
            String id = StringUtils.substringBefore(subFragment, ID_SEPARATOR);
            String token = StringUtils.substringAfter(subFragment, ID_SEPARATOR);
            fragments.put(id, new Fragment(id, token));
        }
    }

    public String getSubFragment(String id) {
        Fragment fragment = fragments.get(id);
        if(fragment != null){
            return fragment.getFragment();
        }
        return null;
    }

    public void setSubFragment(String id, String fragment) {
        // updated the fragment and don't replace it to avoid re-ordering
        if(fragments.containsKey(id)){
            fragments.get(id).fragment = fragment;
        }
        else{
            fragments.put(id, new Fragment(id, fragment));
        }
    }

    @Override
    public String toString() {
        StringBuffer str = new StringBuffer();
        for (Fragment fragment : fragments.values()) {
            str.append(fragment.id).append(ID_SEPARATOR).append(fragment.getFragment()).append(FRAGMENT_SEPARATOR);
        }
        if(str.length()>0){
            str.deleteCharAt(str.length()-1);
        }
        return str.toString();
    }

    private static class Fragment {

        private String id;

        private String fragment;

        public Fragment(String id, String fragment) {
            this.id = id;
            this.fragment = fragment;
        }

        public String getId() {
            return id;
        }

        public String getFragment() {
            return fragment;
        }

    }
}
