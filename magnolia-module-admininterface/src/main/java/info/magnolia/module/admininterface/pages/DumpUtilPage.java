package info.magnolia.module.admininterface.pages;

import java.util.Iterator;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.util.ContentUtil;
import info.magnolia.cms.util.DumperUtil;
import info.magnolia.module.admininterface.TemplatedMVCHandler;


public class DumpUtilPage extends TemplatedMVCHandler {
    public DumpUtilPage(String name, HttpServletRequest request, HttpServletResponse response) {
        super(name, request, response);
    }

    private String repository = "";
    
    private int level;
    
    private String path = "";
    
    public String getDump(){
        if(StringUtils.isNotEmpty(repository) && StringUtils.isNotEmpty(path)){
            Content node = ContentUtil.getContent(repository, path);
            if(node == null){
                return "path not found: " + this.path;
            }
            return DumperUtil.dump(node, level);
        }
        return "";
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

}
