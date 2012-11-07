/**
 * This file Copyright (c) 2011-2012 Magnolia International
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
package info.magnolia.module.admininterface.pages;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import info.magnolia.cms.i18n.Messages;
import info.magnolia.cms.i18n.MessagesManager;
import info.magnolia.cms.security.Group;
import info.magnolia.cms.security.Permission;
import info.magnolia.cms.security.Security;
import info.magnolia.cms.security.User;
import info.magnolia.cms.security.auth.ACL;
import info.magnolia.cms.util.AlertUtil;
import info.magnolia.module.admininterface.TemplatedMVCHandler;

/**
 * Tools for simple show of all groups, roles or permissions assigned to user.
 *
 * @version $Id$
 */
public class PermissionPage extends TemplatedMVCHandler {

    public static Logger log = LoggerFactory.getLogger(PermissionPage.class);

    private static final String VIEW_ERROR = "error";

    private String mgnlUser;
    private String mgnlGroup;
    private Boolean mgnlACLs = false;
    private Collection<String> permissionList = new ArrayList<String>();
    private boolean createPermissionList;

    private static Map<Long, String> mapNamePermissionURL = new Hashtable<Long, String>();
    private static Map<Long, String> mapNamePermissionForum = new Hashtable<Long, String>();
    private static Map<Long, String> mapNamePermission = new Hashtable<Long, String>();

    static {
        mapNamePermission.put(Long.valueOf(0), "roles.permission.deny");
        mapNamePermission.put(Long.valueOf(Permission.READ), "roles.permission.readOnly");
        mapNamePermission.put(Long.valueOf(Permission.ALL), "roles.permission.readWrite");
        mapNamePermissionURL.put(Long.valueOf(0), "roles.permission.deny");
        mapNamePermissionURL.put(Long.valueOf(Permission.READ), "roles.permission.get");
        mapNamePermissionURL.put(Long.valueOf(Permission.ALL), "roles.permission.getAndPost");

        mapNamePermissionForum.put(Long.valueOf(0), "roles.permission.deny");
        mapNamePermissionForum.put(Long.valueOf(Permission.READ), "roles.permission.readOnly");
        mapNamePermissionForum.put(Long.valueOf(Permission.WRITE), "roles.permission.post");
        mapNamePermissionForum.put(Long.valueOf(75), "roles.permission.moderate");
        mapNamePermissionForum.put(Long.valueOf(79), "roles.permission.moderateAndDelete");
        mapNamePermissionForum.put(Long.valueOf(111), "roles.permission.admin");
    }

    /**
     * Getter for <code>mgnlUser</code>.
     * @return Returns the mgnlUser.
     */
    public String getMgnlUser() {
        return this.mgnlUser;
    }

    /**
     * Setter for <code>mgnlUser</code>.
     * @param mgnlUser The mgnlUser to set.
     */
    public void setMgnlUser(String mgnlUser) {
        this.mgnlUser = mgnlUser;
    }

    /**
     * Getter for <code>mgnlGroup</code>.
     * @return Returns the mgnlGroup.
     */
    public String getMgnlGroup() {
        return mgnlGroup;
    }

    /**
     * Setter for <code>mgnlGroup</code>.
     * @param mgnlGroup The mgnlGroup to set.
     */
    public void setMgnlGroup(String mgnlGroup) {
        this.mgnlGroup = mgnlGroup;
    }

    /**
     * Getter for <code>mgnlACLs</code>.
     * @return Returns the mgnlACLs.
     */
    public Boolean isMgnlACLs() {
        return mgnlACLs;
    }

    /**
     * Setter for <code>mgnlACLs</code>.
     * @param mgnlACLs The mgnlACLs to set.
     */
    public void setMgnlACLs(Boolean mgnlACLs) {
        this.mgnlACLs = mgnlACLs;
    }

    /**
     * Getter for <code>permissionList</code>.
     * @return Returns the permissionList.
     */
    public Collection<String> getPermissionList() {
        return permissionList;
    }

    /**
     * Getter for <code>createPermissionList</code>.
     * @return Returns the createPermissionList.
     */
    public boolean isCreatePermissionList() {
        return this.createPermissionList;
    }

    /**
     * Setter for <code>createPermissionList</code>.
     * @param createPermissionList The createPermissionList to set.
     */
    public void setCreatePermissionList(boolean createPermissionList) {
        this.createPermissionList = createPermissionList;
    }

    /**
     * @param name
     * @param request
     * @param response
     */
    public PermissionPage(String name, HttpServletRequest request, HttpServletResponse response) {
        super(name, request, response);
    }

    /**
     * Creation of permission list as such.
     * @throws Exception
     */
    public String createpermissionlist() throws Exception {
        Iterator<String> iterGroups;
        Iterator<String> iterRoles;

        if(!mgnlUser.isEmpty()){
            User user = Security.getUserManager().getUser(mgnlUser);
            if(user != null){
                permissionList.add("<h3> " + getMessages().get("permissionlist.user", new String[]{mgnlUser}) + "</h3>");
                iterGroups = user.getGroups().iterator();
                iterRoles = user.getRoles().iterator();
            }else{
                log.error("User " + mgnlUser + " doesn't exist");
                AlertUtil.setMessage(getMessages().get("permissionlist.user.error", new String[]{mgnlUser}));
                return VIEW_ERROR;
            }        
        }else if(!mgnlGroup.isEmpty()){
            Group group = Security.getGroupManager().getGroup(mgnlGroup);
            if(group != null){
                permissionList.add("<h3> " + getMessages().get("permissionlist.group", new String[]{mgnlGroup}) + "</h3>");
                iterGroups = group.getGroups().iterator();
                iterRoles = group.getRoles().iterator();
            }else{
                log.error("Group " + mgnlGroup + " doesn't exist");
                AlertUtil.setMessage(getMessages().get("permissionlist.group.error", new String[]{mgnlGroup}));
                return VIEW_ERROR;
            }
        }else{
            log.error("Enter the name of the user or group");
            AlertUtil.setMessage(getMessages().get("permissionlist.notselected"));
            return VIEW_ERROR;
        }

        if(iterGroups.hasNext() || iterRoles.hasNext()){
            permissionList.add("<ul>");
            getGroupRole(iterGroups);
            getRole(iterRoles);
            permissionList.add("</ul>");
        }

        return this.show();
    }
    
    /**
     * Finding and adding subgroups to permission list.
     * @throws Exception
     */
    private void getGroupRole(Iterator<String> iterGroup) throws Exception {
        while(iterGroup.hasNext()){
            Group group = Security.getGroupManager().getGroup(iterGroup.next());
            permissionList.add("<li> " + getMessages().get("permissionlist.group", new String[]{group.getName()}) + "</li>");

            Iterator<String> iterSubGroups = group.getGroups().iterator();
            Iterator<String> iterRoles = group.getRoles().iterator();
            if(iterSubGroups.hasNext() || iterRoles.hasNext()){
                permissionList.add("<ul>");
                getGroupRole(iterSubGroups);
                getRole(iterRoles);
                permissionList.add("</ul>");
            }
        }        
    }

    /**
     * Finding and adding roles to permission list.
     * @throws Exception
     */
    private void getRole(Iterator<String> iterRoles) {
        while(iterRoles.hasNext()){
            String role = Security.getRoleManager().getRole(iterRoles.next()).getName();
            permissionList.add("<li> " + getMessages().get("permissionlist.role", new String[]{role}) + "</li>");
            if(mgnlACLs){
                getPermission(role);
            }
        }   
    }

    /**
     * Finding and adding permissions to permission list.
     * @throws Exception
     */
    private void getPermission(String role){
        Iterator<ACL> iterPermission = Security.getRoleManager().getACLs(role).values().iterator();
        permissionList.add("<ul>");
        while(iterPermission.hasNext()){
            ACL acl = iterPermission.next();
            if(!acl.getList().isEmpty()){   
                for (Permission permission : acl.getList()) {
                    String repoName = acl.getName();
                    String message = getMessages().get("permissionlist.permission", new String[]{getPermissionAsName(repoName, permission), repoName, permission.getPattern().getPatternString()});
                    permissionList.add("<li>" + message + "</li>");
                }
            } 
        }
        permissionList.add("</ul>");
    }

    /**
     * Mapping permission to name.
     * @throws Exception
     */
    public String getPermissionAsName(String repoName, Permission permission) {
        String msgName;
        String msgModule = "info.magnolia.module.admininterface.messages";
        if(repoName.equalsIgnoreCase("uri")){
            msgName = mapNamePermissionURL.get(Long.valueOf(permission.getPermissions()));
        }else if(repoName.equalsIgnoreCase("forum")){
            msgName = mapNamePermissionForum.get(Long.valueOf(permission.getPermissions()));
            msgModule = "info.magnolia.module.forum.messages";
        }else{
            msgName = mapNamePermission.get(Long.valueOf(permission.getPermissions()));
        }
        if(msgName == null){
            return ("(" + permission.getPermissions() + ") unknown ");
        }

        return MessagesManager.getMessages(msgModule).get(msgName);
    }
    
    public Messages getMessages() {
        return MessagesManager.getMessages();
    }
}
