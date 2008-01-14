/**
 * This file Copyright (c) 2003-2008 Magnolia International
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
package info.magnolia.module.admininterface.pages;

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.search.Query;
import info.magnolia.cms.util.ContentUtil;
import info.magnolia.cms.util.DumperUtil;
import info.magnolia.cms.util.QueryUtil;
import info.magnolia.context.MgnlContext;
import info.magnolia.module.admininterface.TemplatedMVCHandler;

import java.util.Collection;
import java.util.Iterator;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;


public class JCRUtilsPage extends TemplatedMVCHandler {
    public JCRUtilsPage(String name, HttpServletRequest request, HttpServletResponse response) {
        super(name, request, response);
    }

    private String repository = "";

    private int level = 1;

    private String path = "/";

    private String result  = "";

    private String statement = "";

    private static String[] languages = new String[] {Query.SQL, Query.XPATH};

    private String language = Query.SQL;

    private String itemType = "nt:base";

    public String dump(){
        if(StringUtils.isNotEmpty(repository) && StringUtils.isNotEmpty(path)){
            Content node = ContentUtil.getContent(repository, path);
            if(node == null){
                return "path not found: " + this.path;
            }
            result = DumperUtil.dump(node, level);
        }
        return VIEW_SHOW;
    }

    public String query(){
        long start = System.currentTimeMillis();
        Collection nodes = QueryUtil.query(repository, statement, language, this.itemType);
        this.result +=(System.currentTimeMillis() - start) + "ms\n";

        for (Iterator iter = nodes.iterator(); iter.hasNext();) {
            Content node = (Content) iter.next();
            this.result += node.getHandle() + "\n";
        }
        return VIEW_SHOW;
    }

    public String delete(){
        try {
            MgnlContext.getHierarchyManager(repository).delete(path);
        }
        catch (Exception e) {
            result = e.toString();
        }
        return VIEW_SHOW;
    }

    public Iterator getRepositories() {
        return ContentRepository.getAllRepositoryNames();
    }

    public String[] getLanguages() {
        return languages;
    }

    public int getLevel() {
        return level;
    }


    public void setLevel(int level) {
        this.level = level;
    }


    public String getPath() {
        return path;
    }


    public void setPath(String path) {
        this.path = path;
    }


    public String getRepository() {
        return repository;
    }


    public void setRepository(String repositroy) {
        this.repository = repositroy;
    }


    public String getResult() {
        return result;
    }


    public String getStatement() {
        return statement;
    }


    public void setStatement(String statement) {
        this.statement = statement;
    }


    public String getLanguage() {
        return language;
    }


    public void setLanguage(String language) {
        this.language = language;
    }


    public String getItemType() {
        return itemType;
    }


    public void setItemType(String itemType) {
        this.itemType = itemType;
    }

}
