package info.magnolia.module.data.controls;

import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.gui.dialog.DialogMultiSelect;
import info.magnolia.cms.security.AccessDeniedException;
import info.magnolia.context.MgnlContext;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.jcr.ItemNotFoundException;
import javax.jcr.RepositoryException;

public class ReferenceMultiSelect extends DialogMultiSelect implements UUIDConversionControl {

	public List getValues() {
		final ArrayList result = new ArrayList();
		final String repository = getConfigValue("repository");
		final HierarchyManager hm = MgnlContext.getHierarchyManager(repository);
		for(Iterator it = super.getValues().iterator(); it.hasNext();) {
			String uuid = (String)it.next();
			try {
				Content c = hm.getContentByUUID(uuid);
				result.add(c.getHandle());
			} catch (AccessDeniedException e) {
				// ???
				throw new RuntimeException(e);
			} catch (ItemNotFoundException e) {
				// its probably deleted so we can ignore
			} catch (RepositoryException e) {
				throw new RuntimeException(e);
			} catch (RuntimeException e) {
				// currently ignore
				// TODO: think about this again
			}
		}
		return result;
	}
}
