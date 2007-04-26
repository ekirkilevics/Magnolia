/**
 * Magnolia and its source-code is licensed under the LGPL.
 * You may copy, adapt, and redistribute this file for commercial or non-commercial use.
 * When copying, adapting, or redistributing this document in keeping with the guidelines above,
 * you are required to provide proper attribution to obinary.
 * If you reproduce or distribute the document without making any substantive modifications to its content,
 * please use the following attribution line:
 *
 * Copyright 1993-2006 obinary Ltd. (http://www.obinary.com) All rights reserved.
 */
package info.magnolia.test;

import static org.easymock.EasyMock.anyBoolean;
import static org.easymock.EasyMock.anyInt;
import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.isA;
import static org.easymock.EasyMock.replay;
import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.core.SystemProperty;
import info.magnolia.cms.util.ClasspathResourcesUtil;
import info.magnolia.cms.util.FactoryUtil;
import info.magnolia.context.MgnlContext;
import info.magnolia.test.mock.MockContext;
import info.magnolia.test.mock.MockHierarchyManager;
import info.magnolia.test.mock.MockUtil;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Properties;

import javax.jcr.RepositoryException;
import javax.jcr.UnsupportedRepositoryOperationException;
import javax.jcr.Workspace;
import javax.jcr.observation.EventListener;
import javax.jcr.observation.ObservationManager;

import junit.framework.TestCase;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Level;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author philipp
 * @version $Id$
 */
public abstract class MgnlTestCase extends TestCase {

    private static final String MGNL_BEANS_PROPERTIES = "/mgnl-beans.properties";
    /**
     * Logger.
     */
    private static Logger log = LoggerFactory.getLogger(MgnlTestCase.class);

    protected void setUp() throws Exception {
        FactoryUtil.clear();
        initDefaultImplementions();
        MockUtil.initMockContext();
    }

    protected void initDefaultImplementions() throws IOException {
        InputStream mgnlbeansStream = ClasspathResourcesUtil.getStream(MGNL_BEANS_PROPERTIES);

        if (mgnlbeansStream != null) {
            Properties mgnlbeans = new Properties();
            mgnlbeans.load(mgnlbeansStream);
            IOUtils.closeQuietly(mgnlbeansStream);

            for (Iterator iter = mgnlbeans.keySet().iterator(); iter.hasNext();) {
                String key = (String) iter.next();
                SystemProperty.setProperty(key, mgnlbeans.getProperty(key));
            }
        }
    }

    protected MockHierarchyManager initConfigRepository(String conf) throws IOException, RepositoryException, UnsupportedRepositoryOperationException {
        // ignore mapping warnings
        org.apache.log4j.Logger.getLogger(ContentRepository.class).setLevel(Level.ERROR);

        MockHierarchyManager hm = MockUtil.createHierarchyManager(conf);

        mockObservation(hm);

        ((MockContext)MgnlContext.getInstance()).addHierarchyManager(ContentRepository.CONFIG, hm);
        ((MockContext)MgnlContext.getSystemContext()).addHierarchyManager(ContentRepository.CONFIG, hm);

        return hm;
    }

    protected void mockObservation(MockHierarchyManager hm) throws RepositoryException, UnsupportedRepositoryOperationException {
        // fake observation
        Workspace ws = createMock(Workspace.class);
        ObservationManager om = createMock(ObservationManager.class);

        om.addEventListener(isA(EventListener.class), anyInt(),isA(String.class),anyBoolean(), (String[])anyObject(), (String[]) anyObject(), anyBoolean());

        expect(ws.getObservationManager()).andStubReturn(om);
        hm.setWorkspace(ws);
        replay(ws, om);
    }
}
