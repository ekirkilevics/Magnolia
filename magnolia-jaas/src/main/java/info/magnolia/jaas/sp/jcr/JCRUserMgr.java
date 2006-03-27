package info.magnolia.jaas.sp.jcr;

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.core.ItemType;
import info.magnolia.cms.core.Path;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JCRUserMgr {
	HierarchyManager hm = ContentRepository
	.getHierarchyManager(ContentRepository.USERS);
	HierarchyManager hm_usergroups = ContentRepository
	.getHierarchyManager(ContentRepository.USERS, ContentRepository.USERGROUPS);
	/**
	 * Logger
	 */
	private static Logger log = LoggerFactory.getLogger(JCRUserMgr.class);
	
	public List getGroupsForUser(String userName){
		ArrayList list = new ArrayList();
		List cts = getUserGroupNodes(userName);
		for (int i = 0; i < cts.size(); i++){
			Content ct = (Content)cts.get(i);			
			String s = ct.getTitle();
			list.add(s);
			log.info("group name = " + ct.getTitle());
		}
		return list;
	}
	
	public List getUsersForGroup(String groupName){
		ArrayList list = new ArrayList();
//		try {
//			HierarchyManager hm = ContentRepository
//			.getHierarchyManager(ContentRepository.USERS, ContentRepository.USERGROUPS);
//			Content ct = hm.getContent(groupName);
//			
		return list;
	}

	public List GetUsersForRole(String roleName){
		ArrayList list = new ArrayList();
		//@Todo
		return list;
	}
	
	/**
	 * get group nodes which users are belong to
	 * 
	 * @return
	 */
	public List getUserGroupNodes(String userName) {
		ArrayList list = new ArrayList();

		try {
		

			// get node "user"
			Content user = hm.getContent(userName);

			Content groups = null;
			try {
				// get "groups" node under node "user"
				groups = user.getContent("groups");
			} catch (javax.jcr.PathNotFoundException e) {
				log.warn("the user " + userName + " does have not groups node");
			}

			if (groups != null) {
				Collection c = groups.getChildren(ItemType.CONTENTNODE);
				Iterator it = c.iterator();
				while (it.hasNext()) {
					Content ct = (Content) it.next();

					if (ct == null) {
						log.error("group node is null");
						continue;
					}
					list.add(ct);
				}
			}

		} catch (Exception e) {

			log.warn("can not add group reference to user.", e);
		}

		return list;

	}

	public void createGroupsForUser(Content user, String[] groupsValue) throws Exception{
		 // recreate groups node
        Content groups = null;
        try {
            groups = user.getContent("groups");
        }
        catch (Exception e) {
            log.info("User does not have groups node", e);
        }
        if (groups != null)
            groups.delete();
      
        groups = user.createContent("groups");        

        for (int i = 0; i < groupsValue.length; i++) {
        	createGroupNodeForUser(groups, groupsValue[i]);         
        }
	}
	
	public void createGroupNodeForUser(Content groups, String groupPath) throws Exception{		
        String newLabel = Path.getUniqueLabel(hm, groups.getHandle(), "0");
        Content r = groups.createContent(newLabel, ItemType.CONTENTNODE);
        r.createNodeData("path").setValue(groupPath);
        log.info("added to group " + groupPath);
		
	}
	
    public void addGroupForUser(String groupId, String userId) throws Exception {
        if (log.isDebugEnabled())
            log.debug("group id = " + groupId + ", User Id = " + userId);
        try {
            HierarchyManager hm = ContentRepository.getHierarchyManager(ContentRepository.USERS);

            // get node "user"
            Content user = hm.getContent(userId);

            // get "groups" node under node "user"
            Content groups = null;
            try {
                groups = user.getContent("groups");
            } catch (Exception e) {
                log.info("groups node not found");
            }
            if (groups == null)// create it if no exist
                groups = user.createContent("groups");

            // create <groupid> under node "groups"
            String newLabel = Path.getUniqueLabel(hm, groups.getHandle(), "0");
            Content r = groups.createContent(newLabel, ItemType.CONTENTNODE);
            r.createNodeData("path").setValue(groupId);

            hm.save();

        }
        catch (Exception e) {
            log.error("can not add group reference to user.", e);
        }
    }
}
