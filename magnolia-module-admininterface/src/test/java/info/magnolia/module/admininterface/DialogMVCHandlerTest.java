/**
 * This file Copyright (c) 2011 Magnolia International
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
package info.magnolia.module.admininterface;

import static org.easymock.EasyMock.createNiceMock;
import static org.easymock.EasyMock.createStrictMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import info.magnolia.cms.beans.runtime.MultipartForm;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.core.ItemType;
import info.magnolia.cms.gui.dialog.Dialog;
import info.magnolia.cms.gui.i18n.DefaultI18nAuthoringSupport;
import info.magnolia.cms.gui.i18n.I18nAuthoringSupport;
import info.magnolia.cms.i18n.DefaultI18nContentSupport;
import info.magnolia.cms.i18n.DefaultMessagesManager;
import info.magnolia.cms.i18n.I18nContentSupport;
import info.magnolia.cms.i18n.Messages;
import info.magnolia.cms.i18n.MessagesManager;
import info.magnolia.cms.util.RequestFormUtil;
import info.magnolia.context.MgnlContext;
import info.magnolia.context.WebContext;
import info.magnolia.test.ComponentsTestUtil;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Test;

import junit.framework.TestCase;


/**
 * @version $Id:$
 */
public class DialogMVCHandlerTest extends TestCase{
    
    private final class CustomValidatingSaveHandler implements ValidatingSaveHandler{

        private String repository;

        @Override
        public boolean validate() {
            if(repository.equals("website")){
                return true;
            }else{
                return false;
            }
        }

        @Override
        public ItemType getCreationItemType() {
            return null;
        }

        @Override
        public String getNodeCollectionName() {
            return null;
        }

        @Override
        public String getNodeName() {
            return null;
        }

        @Override
        public String getParagraph() {
            return null;
        }

        @Override
        public String getPath() {
            return null;
        }

        @Override
        public String getRepository() {
            return null;
        }
        
        @Override
        public void init(MultipartForm form) {    
        }
        
        @Override
        public boolean isCreate() {
            return false;
        }
        
        @Override
        public boolean save() {
            return false;
        }

        @Override
        public void setCreate(boolean create) {     
        }

        @Override
        public void setCreationItemType(ItemType creationItemType) {
        }
        
        @Override
        public void setNodeCollectionName(String nodeCollectionName) {   
        }
        
        @Override
        public void setNodeName(String nodeName) { 
        }
        
        @Override
        public void setParagraph(String paragraph) { 
        }
        
        @Override
        public void setPath(String path) {  
        }
        
        @Override
        public void setRepository(String repository) {
            this.repository = repository;
        }

        @Override
        public ItemType getCollectionNodeCreationItemType() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public void setCollectionNodeCreationItemType(
                ItemType collectionNodeCreationItemType) {
            // TODO Auto-generated method stub
            
        }  
    }
    
    private final class DummyDialogMVCHandler extends DialogMVCHandler {
        
        private CustomValidatingSaveHandler validSH = new CustomValidatingSaveHandler();
        
        public DummyDialogMVCHandler(String name, HttpServletRequest request, HttpServletResponse response) {
            super(name, request, response);
        }
        
        @Override
        protected SaveHandler getSaveHandler(){
            return validSH;
        }
        
    }
    
    private final class DummyDialog extends Dialog {
        
        @Override
        protected void setValidationMessage(String msg) {  
        }  
    }
    
    private HttpServletRequest request;
    private HttpServletResponse response;
    private WebContext context;
    private HierarchyManager hm;
    private MultipartForm form;
    private Dialog dialog;
    private Object[] obj;
    
    private DialogMVCHandler handler;
           
    @Override
    public void setUp(){
        request = createNiceMock(HttpServletRequest.class);
        response = createNiceMock(HttpServletResponse.class);
        context = createNiceMock(WebContext.class);
        hm = createStrictMock(HierarchyManager.class);
        
        form = new MultipartForm();
        form.addParameter("mgnlPath", "/test-project");
        form.addParameter("mgnlNodeCollection", "");
        form.addParameter("mgnlNode", "");
        form.addParameter("mgnlRichE", "true");
        form.addParameter("mgnlRichEPaste", "test");
        form.addParameter("mgnlRepository", "website");
        form.addParameter("mgnlLocale", "en");
        form.addParameter("mgnlParagraph", "");
        form.addParameter("mgnlCollectionNodeCreationItemType", "");
        form.addParameter("mgnlCreationItemType", "");
        
        ComponentsTestUtil.setInstance(MessagesManager.class, new DefaultMessagesManager());
        ComponentsTestUtil.setInstance(I18nContentSupport.class, new DefaultI18nContentSupport());
        ComponentsTestUtil.setInstance(I18nAuthoringSupport.class, new DefaultI18nAuthoringSupport());
        MgnlContext.setInstance(context);
        
        expect(context.getPostedForm()).andReturn(form);
        expect(context.getHierarchyManager("website")).andReturn(hm);
        obj = new Object[]{request, response, context, hm};
        replay(obj);
        
        handler = new DummyDialogMVCHandler("test", request, response); 
    }
    
    //Dialog, CustomValidatingSaveHandler - both pass validation
    @Test
    public void testCustomValidatingSaveHandlerValid(){
        
        dialog = new DummyDialog();        
        handler.setDialog(dialog);
        handler.getSaveHandler().setRepository("website");
               
        assertTrue(handler.validate());
        verify(obj); 
    }
  
    //Dialog pass validation, but CustomValidatingSaveHandler didn't pass validation
    @Test
    public void testCustomValidatingSaveHandlerInvalid(){
        
        dialog = new DummyDialog();        
        handler.setDialog(dialog);
        handler.getSaveHandler().setRepository("dms");
               
        assertFalse(handler.validate());
        verify(obj); 
    }
    
    //Dialog didn't pass validation
    @Test
    public void testCustomValidatingSaveHandlerInvalid2(){
        
        dialog = new DummyDialog();
        dialog.setRequired(true);
        dialog.setValue("");
        handler.setDialog(dialog);
        handler.getSaveHandler().setRepository("website");
        
        assertFalse(handler.validate());
        verify(obj); 
    }
    
    @Override
    public void tearDown(){
        ComponentsTestUtil.clear();
        MgnlContext.setInstance(null);
    }
}
