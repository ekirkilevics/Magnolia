/**
 * This file Copyright (c) 2010-2011 Magnolia International
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
package info.magnolia.ui.admincentral.tree.column;

import static junit.framework.Assert.assertEquals;
import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.core.MetaData;
import info.magnolia.ui.model.tree.definition.MetaDataColumnDefinition;

import java.util.Calendar;
import java.util.Date;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.apache.commons.lang.time.FastDateFormat;
import org.junit.Before;
import org.junit.Test;

/**
 * @author dlipp
 * @version $Id$
 */
public class MetaDataColumnTest {
    private Calendar cal = Calendar.getInstance();
    private FastDateFormat dateFormat = FastDateFormat.getInstance(MetaDataColumn.DEFAULT_DATE_PATTERN);
    private Date now = new Date();

    @Before
    public void setUp (){
        cal.setTime(now);
    }

    @Test
    public void testGetValue() throws RepositoryException {
        Node node = new MockNode();
        Node metaData = node.addNode(MetaData.DEFAULT_META_NODE);
        metaData.setProperty(ContentRepository.NAMESPACE_PREFIX + ":" + MetaData.CREATION_DATE, cal);
        MetaDataColumn column = new MetaDataColumn(new MetaDataColumnDefinition());
        Object result = column.getValue(node);
        assertEquals(dateFormat.format(now), result);
    }
}
