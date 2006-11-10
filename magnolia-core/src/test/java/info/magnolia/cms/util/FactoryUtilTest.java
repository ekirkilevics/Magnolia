package info.magnolia.cms.util;

import junit.framework.TestCase;

public class FactoryUtilTest extends TestCase {

    public void testConfiguredImplementation(){
       FactoryUtil.clear();
       FactoryUtil.setDefaultImplementation(FactoryUtilTestInterface.class, FactoryUtilTestImplementation.class);
       Object obj = FactoryUtil.getSingleton(FactoryUtilTestInterface.class);
       assertTrue(obj instanceof FactoryUtilTestImplementation);
   }

    public void testDontRedefineTheDefaultImplementation(){
        FactoryUtil.clear();
        FactoryUtil.setDefaultImplementation(FactoryUtilTestInterface.class, FactoryUtilTestImplementation.class);
        FactoryUtil.setDefaultImplementation(FactoryUtilTestInterface.class, "a.wrong.class.not.set");
        Object obj = FactoryUtil.getSingleton(FactoryUtilTestInterface.class);
        assertTrue(obj instanceof FactoryUtilTestImplementation);
    }

   public void testDefaultImplementation(){
       FactoryUtil.clear();
       Object obj = FactoryUtil.getSingleton(FactoryUtilTestImplementation.class);
       assertTrue(obj instanceof FactoryUtilTestImplementation);
   }

   public void testSingleton(){
       FactoryUtil.clear();
       assertEquals(FactoryUtil.getSingleton(FactoryUtilTestImplementation.class), FactoryUtil.getSingleton(FactoryUtilTestImplementation.class));
   }
   
   public void testNewInstance(){
       FactoryUtil.clear();
       assertNotSame(FactoryUtil.newInstance(FactoryUtilTestImplementation.class), FactoryUtil.newInstance(FactoryUtilTestImplementation.class));
   }

   public void testSetSingletonInstance(){
       FactoryUtil.clear();
       FactoryUtilTestImplementation instance = new FactoryUtilTestImplementation();
       FactoryUtil.setInstance(FactoryUtilTestInterface.class, instance);
       assertSame(instance, FactoryUtil.getSingleton(FactoryUtilTestInterface.class));
   }

   public void testInstanceFactory(){
       FactoryUtil.clear();
       FactoryUtil.setInstanceFactory(FactoryUtilTestInterface.class, new FactoryUtil.InstanceFactory(){
           public Object newInstance() {
               return new FactoryUtilTestOtherImplementation();
           }
       });
       
       assertTrue(FactoryUtil.getSingleton(FactoryUtilTestInterface.class) instanceof FactoryUtilTestOtherImplementation);
   }

   

}
