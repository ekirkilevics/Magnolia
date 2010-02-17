/**
 * This file Copyright (c) 2003-2009 Magnolia International
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
package info.magnolia.cms.security;

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.content2bean.Content2BeanTransformer;
import info.magnolia.content2bean.PropertyTypeDescriptor;
import info.magnolia.content2bean.TransformationState;
import info.magnolia.content2bean.TypeDescriptor;
import info.magnolia.content2bean.impl.Content2BeanTransformerImpl;
import info.magnolia.objectfactory.ObservedComponentFactory;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class IPSecurityManagerImpl implements IPSecurityManager {
    private static final String ALL = "*";
    private Map<String, Rule> rules;

    public IPSecurityManagerImpl() {
        this.rules = new HashMap<String, Rule>();
    }

    public boolean isAllowed(HttpServletRequest req) {
        final Rule rule = getRule(req.getRemoteAddr());
        return rule != null && rule.allowsMethod(req.getMethod());
    }

    public boolean isAllowed(String ip) {
        return getRule(ip) != null;
    }

    protected Rule getRule(String ip) {
        if (rules.containsKey(ip)) {
            return rules.get(ip);
        } else {
            return rules.get(ALL);
        }
    }

    public Map<String, Rule> getRules() {
        return rules;
    }

    public void setRules(Map<String, Rule> rules) {
        this.rules = rules;
    }

    public void addRule(String name, Rule rule) {
        rules.put(name, rule);
    }

    public static final class InstanceFactory extends ObservedComponentFactory<IPSecurityManager> {
        public InstanceFactory() {
            super(ContentRepository.CONFIG, "/server/IPConfig", IPSecurityManager.class);
        }

        protected Content2BeanTransformer getContent2BeanTransformer() {
            return new IPSecurityManagerTransformer();
        }
    }

    public static final class IPSecurityManagerTransformer extends Content2BeanTransformerImpl {

        public void setProperty(TransformationState state, PropertyTypeDescriptor descriptor, Map<String, Object> values) {
            final Object currentBean = state.getCurrentBean();
            if (currentBean instanceof IPSecurityManagerImpl) {
                final IPSecurityManagerImpl ipSecMan = (IPSecurityManagerImpl) currentBean;
                for (Object o : values.values()) {
                    if (o instanceof Rule) {
                        final Rule rule = (Rule) o;
                        ipSecMan.addRule(rule.getIP(), rule);
                    }
                }
            }
            super.setProperty(state, descriptor, values);
        }

        protected TypeDescriptor onResolveType(TransformationState state,
                TypeDescriptor resolvedType) {
            if (state.getLevel() == 2 && resolvedType == null) {
                return this.getTypeMapping().getTypeDescriptor(Rule.class);
            }
            return super.onResolveType(state, resolvedType);
        }

    }

    public static final class Rule {
        private String name;
        private String ip;
        private Set<String> methods;

        public Rule() {
            this.methods = Collections.emptySet();
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getIP() {
            return ip;
        }

        public void setIP(String ip) {
            this.ip = ip;
        }

        public boolean allowsMethod(String s) {
            return methods.contains(s);
        }

        public String getMethods() {
            throw new IllegalStateException("Just faking a getter for content2bean's sake.");
        }

        public void setMethods(String methods) {
            this.methods = new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);
            this.methods.addAll(Arrays.asList(methods.split(",")));
        }
    }

}
