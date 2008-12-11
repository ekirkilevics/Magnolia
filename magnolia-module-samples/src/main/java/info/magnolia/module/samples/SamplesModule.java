/**
 * This file Copyright (c) 2008 Magnolia International
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
package info.magnolia.module.samples;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import info.magnolia.module.ModuleLifecycle;
import info.magnolia.module.ModuleLifecycleContext;

/**
 *
 * @author tmiyar
 *
 */
public class SamplesModule implements ModuleLifecycle {

    private static Logger log = LoggerFactory.getLogger(SamplesModule.class);

    private List sampleList = new ArrayList();

    private Map sampleMap = new HashMap();

    private String sampleProperty;


    public void start(ModuleLifecycleContext moduleLifecycleContext) {
        log.info("module extra initializations");

    }

    public void stop(ModuleLifecycleContext moduleLifecycleContext) {
        log.info("module extra module cleanup");

    }

    public List getSampleList() {
        log.info("this is a node of type content");
        return sampleList;
    }

    public void setSampleList(List sampleList) {
        this.sampleList = sampleList;
    }

    public void addSampleList(SampleConfigurations sampleConfigurations) {
        log.info("adds the items to the list, a 'SampleConfigurations' class:" + sampleConfigurations.getName());
        this.sampleList.add(sampleConfigurations);
    }

    public Map getSampleMap() {
        log.info("this is a node of type content node");
        return sampleMap;
    }

    public void setSampleMap(Map sampleMap) {
        this.sampleMap = sampleMap;
    }

    public String getSampleProperty() {
        log.info("this is a node of type node data: " + sampleProperty);
        return sampleProperty;
    }

    public void setSampleProperty(String sampleProperty) {
        this.sampleProperty = sampleProperty;
    }

}
