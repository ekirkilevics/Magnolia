package info.magnolia.module.admininterface.pages;

import java.util.Collection;
import java.util.Iterator;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.ItemType;
import info.magnolia.cms.core.search.Query;
import info.magnolia.cms.util.ContentUtil;
import info.magnolia.cms.util.DumperUtil;
import info.magnolia.cms.util.QueryUtil;
import info.magnolia.context.MgnlContext;
import info.magnolia.module.admininterface.TemplatedMVCHandler;


public class JCRUtilsPage extends TemplatedMVCHandler {
    public JCRUtilsPage(String name, HttpServletRequest request, HttpServletResponse response) {
        super(name, request, response);
    }

    private String repository = "";
    
    private int level = 1;
    
    private String path = "/";
    
    private String result  = "";
    
    private String statement = "";

    private String language = Query.SQL;

    private String itemType = ItemType.CONTENT.getSystemName();

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
        Collection nodes = QueryUtil.query(repository, statement, language, this.itemType);
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
