package info.magnolia.cms.util;

import java.io.IOException;

import javax.jcr.RepositoryException;

import info.magnolia.test.MgnlTestCase;

public class FactoryUtilTest extends MgnlTestCase {

    public void testConfiguredImplementation(){
       FactoryUtil.setDefaultImplementation(FactoryUtilTestInterface.class, FactoryUtilTestImplementation.class);
       Object obj = FactoryUtil.getSingleton(FactoryUtilTestInterface.class);
       assertTrue(obj instanceof FactoryUtilTestImplementation);
   }

    public void testDontRedefineTheDefaultImplementation(){
        FactoryUtil.setDefaultImplementation(FactoryUtilTestInterface.class, FactoryUtilTestImplementation.class);
        FactoryUtil.setDefaultImplementation(FactoryUtilTestInterface.class, "a.wrong.class.not.set");
        Object obj = FactoryUtil.getSingleton(FactoryUtilTestInterface.class);
        assertTrue(obj instanceof FactoryUtilTestImplementation);
    }

   public void testDefaultImplementation(){
       Object obj = FactoryUtil.getSingleton(FactoryUtilTestImplementation.class);
       assertTrue(obj instanceof FactoryUtilTestImplementation);
   }

   public void testSingleton(){
       assertEquals(FactoryUtil.getSingleton(FactoryUtilTestImplementation.class), FactoryUtil.getSingleton(FactoryUtilTestImplementation.class));
   }

   public void testNewInstance(){
       assertNotSame(FactoryUtil.newInstance(FactoryUtilTestImplementation.class), FactoryUtil.newInstance(FactoryUtilTestImplementation.class));
   }

   public void testSetSingletonInstance(){
       FactoryUtilTestImplementation instance = new FactoryUtilTestImplementation();
       FactoryUtil.setInstance(FactoryUtilTestInterface.class, instance);
       assertSame(instance, FactoryUtil.getSingleton(FactoryUtilTestInterface.class));
   }

   public void testInstanceFactory(){
       FactoryUtil.setInstanceFactory(FactoryUtilTestInterface.class, new FactoryUtil.InstanceFactory(){
           public Object newInstance() {
               return new FactoryUtilTestOtherImplementation();
           }
       });

       assertTrue(FactoryUtil.getSingleton(FactoryUtilTestInterface.class) instanceof FactoryUtilTestOtherImplementation);
   }

   public void testSingletonDefinedInRepository() throws RepositoryException, IOException{
       FactoryUtil.setDefaultImplementation(FactoryUtilTestInterface.class, "/test");
       initConfigRepository(
           "test.class=" + FactoryUtilTestImplementation.class.getName()
       );
       Object obj = FactoryUtil.getSingleton(FactoryUtilTestInterface.class);
       assertNotNull(obj);
       assertTrue(obj instanceof FactoryUtilTestInterface);
   }


}
