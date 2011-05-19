package info.magnolia.test.mock.jcr;

import java.io.InputStream;

import javax.jcr.NamespaceRegistry;
import javax.jcr.Session;
import javax.jcr.Workspace;
import javax.jcr.lock.LockManager;
import javax.jcr.nodetype.NodeTypeManager;
import javax.jcr.observation.ObservationManager;
import javax.jcr.query.QueryManager;
import javax.jcr.version.Version;
import javax.jcr.version.VersionManager;

import org.xml.sax.ContentHandler;

public class MockWorkspace implements Workspace {

    final private String name;

    private MockSession session;

    public MockWorkspace(String name) {
        this.name = name;
    }

    @Override
    public void clone(String srcWorkspace, String srcAbsPath, String destAbsPath, boolean removeExisting) {
        throw new UnsupportedOperationException("Not implemented. This is a fake class.");

    }

    @Override
    public void copy(String srcAbsPath, String destAbsPath) {
        throw new UnsupportedOperationException("Not implemented. This is a fake class.");

    }

    @Override
    public void copy(String srcWorkspace, String srcAbsPath, String destAbsPath) {
        throw new UnsupportedOperationException("Not implemented. This is a fake class.");

    }

    @Override
    public void createWorkspace(String name) {
        throw new UnsupportedOperationException("Not implemented. This is a fake class.");

    }

    @Override
    public void createWorkspace(String name, String srcWorkspace) {
        throw new UnsupportedOperationException("Not implemented. This is a fake class.");

    }

    @Override
    public void deleteWorkspace(String name) {
        throw new UnsupportedOperationException("Not implemented. This is a fake class.");

    }

    @Override
    public String[] getAccessibleWorkspaceNames() {
        throw new UnsupportedOperationException("Not implemented. This is a fake class.");
    }

    @Override
    public ContentHandler getImportContentHandler(String parentAbsPath, int uuidBehavior) {
        throw new UnsupportedOperationException("Not implemented. This is a fake class.");
    }

    @Override
    public LockManager getLockManager() {
        throw new UnsupportedOperationException("Not implemented. This is a fake class.");
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public NamespaceRegistry getNamespaceRegistry() {
        throw new UnsupportedOperationException("Not implemented. This is a fake class.");
    }

    @Override
    public NodeTypeManager getNodeTypeManager() {
        throw new UnsupportedOperationException("Not implemented. This is a fake class.");
    }

    @Override
    public ObservationManager getObservationManager() {
        throw new UnsupportedOperationException("Not implemented. This is a fake class.");
    }

    @Override
    public QueryManager getQueryManager() {
        throw new UnsupportedOperationException("Not implemented. This is a fake class.");
    }

    @Override
    public Session getSession() {
        return session;
    }

    @Override
    public VersionManager getVersionManager() {
        throw new UnsupportedOperationException("Not implemented. This is a fake class.");
    }

    @Override
    public void importXML(String parentAbsPath, InputStream in, int uuidBehavior) {
        throw new UnsupportedOperationException("Not implemented. This is a fake class.");

    }

    @Override
    public void move(String srcAbsPath, String destAbsPath) {
        throw new UnsupportedOperationException("Not implemented. This is a fake class.");

    }

    @Override
    public void restore(Version[] versions, boolean removeExisting) {
        throw new UnsupportedOperationException("Not implemented. This is a fake class.");

    }

    protected void setSession(MockSession session) {
        this.session = session;
    }

}
