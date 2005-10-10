/**
 *
 * Magnolia and its source-code is licensed under the LGPL.
 * You may copy, adapt, and redistribute this file for commercial or non-commercial use.
 * When copying, adapting, or redistributing this document in keeping with the guidelines above,
 * you are required to provide proper attribution to obinary.
 * If you reproduce or distribute the document without making any substantive modifications to its content,
 * please use the following attribution line:
 *
 * Copyright 1993-2005 obinary Ltd. (http://www.obinary.com) All rights reserved.
 *
 */
package info.magnolia.cms.security;

import java.util.Iterator;
import java.util.Set;

import javax.security.auth.Subject;
import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;


/**
 * This class wrapps a user content object to provide some nice methods
 * @author philipp
 * @author Sameer Charles
 * @version $Revision$ ($Author$)
 */
public class User {

    public static Logger log = Logger.getLogger(User.class);
    
    /**
     * user properties
     */
    private Entity userDetails;

    /**
     * user roles
     * */
    private RoleList roleList;

    /**
     * @param subject as created by login module
     */
    public User(Subject subject) {
        Set principalSet = subject.getPrincipals(Entity.class);
        Iterator entityIterator = principalSet.iterator();
        this.userDetails = (Entity) entityIterator.next();
        principalSet = subject.getPrincipals(RoleList.class);
        Iterator roleListIterator = principalSet.iterator();
        this.roleList = (RoleList) roleListIterator.next();
    }

    /**
     * Is this user in a specified role?
     * @param roleName the name of the role
     * @return true if in role
     */
    public boolean hasRole(String roleName) {
        return this.roleList.hasRole(roleName);
    }

    /**
     * TODO: JAAS
     */
    public void removeRole(String roleName) {
        throw new UnsupportedOperationException("not implemented with JAAS");

        //        try {
        //            Content rolesNode = userNode.getContent(NODE_ROLES);
        //
        //            for (Iterator iter = rolesNode.getChildren().iterator(); iter.hasNext();) {
        //                Content node = (Content) iter.next();
        //                if (node.getNodeData("path").getString().equals("/" + roleName)) { //$NON-NLS-1$ //$NON-NLS-2$
        //                    node.delete();
        //                }
        //            }
        //            if (rolesNode.hasContent(roleName)) {
        //                rolesNode.delete(roleName);
        //            }
        //            userNode.save();
        //        }
        //        catch (RepositoryException e) {
        //            log.error("can't remove role from user [" + this.getName() + "]", e );
        //        }
                
    }
    /**
     * TODO: JAAS
     * Adds a role to this user
     * @param roleName the name of the role
     */
    public void addRole(String roleName){
        throw new UnsupportedOperationException("not implemented with JAAS");
        
        //        try {
        //            if(!this.hasRole(roleName)){
        //                Content rolesNode = userNode.getContent(NODE_ROLES);
        //    
        //                // used only to get the unique label
        //                HierarchyManager hm = ContentRepository.getHierarchyManager(ContentRepository.USERS);
        //                if (!rolesNode.hasContent(roleName)) {
        //                    String nodename = Path.getUniqueLabel(hm, rolesNode.getHandle(), "0");
        //                    Content node = rolesNode.createContent(nodename, ItemType.CONTENTNODE);
        //                    node.createNodeData("path").setValue("/" + roleName);
        //                    userNode.save();
        //                }
        //            }
        //        }
        //        catch (RepositoryException e) {
        //            log.error("can't add role to user [" + this.getName() + "]", e );
        //        }
    }


    /**
     * get user language
     * @return language string
     */

    public String getLanguage() {
        return (String) this.userDetails.getProperty(Entity.LANGUAGE); //$NON-NLS-1$
    }

    /**
     * get user name
     * @return name string
     */
    public String getName() {
        return (String) this.userDetails.getProperty(Entity.NAME); //$NON-NLS-1$
    }

    
    /**
     * Returns the current user
     * @param request
     * @return the current user
     */
    public static User getCurrent(HttpServletRequest request){
        return (new User(Authenticator.getSubject(request)));
    }
    
    
    /**
     * TODO:JAAS
     * Find a specific user
     * @param name the name of the user
     * @param request the request. used for security reasons
     * @return the user object
     */
    public static User findUser(String name, HttpServletRequest request){
        throw new UnsupportedOperationException("not implemented with JAAS");
        //        HierarchyManager hm = SessionAccessControl.getHierarchyManager(request, ContentRepository.USERS);
        //        
        //        try {
        //            return new User(hm.getContent(name));
        //        }
        //        catch (Exception e) {
        //            log.info("can't find user [" + name + "]", e );
        //            return null;
        //        }
    }
}