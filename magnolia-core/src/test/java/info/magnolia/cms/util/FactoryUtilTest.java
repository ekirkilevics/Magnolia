package info.magnolia.cms.util;

import java.io.IOException;

import javax.jcr.RepositoryException;

import info.magnolia.test.MgnlTestCase;

public class FactoryUtilTest extends MgnlTestCase {

    public static interface TestInterface{

    }

    public static class TestImplementation implements TestInterface{

    }

    public static class TestOtherImplementation extends TestImplementation{

    }

    public static final class TestInstanceFactory implements FactoryUtil.InstanceFactory {

        public Object newInstance() {
               return new TestOtherImplementation();
           }
    }

    public void testConfiguredImplementation(){
       FactoryUtil.setDefaultImplementation(TestInterface.class, TestImplementation.class);
       Object obj = FactoryUtil.getSingleton(TestInterface.class);
       assertTrue(obj instanceof TestImplementation);
   }

    public void testDontRedefineTheDefaultImplementation(){
        FactoryUtil.setDefaultImplementation(TestInterface.class, TestImplementation.class);
        FactoryUtil.setDefaultImplementation(TestInterface.class, "a.wrong.class.not.set");
        Object obj = FactoryUtil.getSingleton(TestInterface.class);
        assertTrue(obj instanceof TestImplementation);
    }

   public void testDefaultImplementation(){
       Object obj = FactoryUtil.getSingleton(TestImplementation.class);
       assertTrue(obj instanceof TestImplementation);
   }

   public void testSingleton(){
       assertEquals(FactoryUtil.getSingleton(TestImplementation.class), FactoryUtil.getSingleton(TestImplementation.class));
   }

   public void testNewInstance(){
       assertNotSame(FactoryUtil.newInstance(TestImplementation.class), FactoryUtil.newInstance(TestImplementation.class));
   }

   public void testSetSingletonInstance(){
       TestImplementation instance = new TestImplementation();
       FactoryUtil.setInstance(TestInterface.class, instance);
       assertSame(instance, FactoryUtil.getSingleton(TestInterface.class));
   }

   public void testInstanceFactory(){
       FactoryUtil.setInstanceFactory(TestInterface.class, new TestInstanceFactory());

       assertTrue(FactoryUtil.getSingleton(TestInterface.class) instanceof TestOtherImplementation);
   }

   public void testSingletonDefinedInRepository() throws RepositoryException, IOException{
       FactoryUtil.setDefaultImplementation(TestInterface.class, "/test");
       initConfigRepository(
           "test.class=" + TestImplementation.class.getName()
       );
       Object obj = FactoryUtil.getSingleton(TestInterface.class);
       assertNotNull(obj);
       assertTrue(obj instanceof TestImplementation);
   }

   public void testSingletonDefinedInRepositoryUsingRepositoryPrefix() throws RepositoryException, IOException{
       FactoryUtil.setDefaultImplementation(TestInterface.class, "config:/test");
       initConfigRepository(
           "test.class=" + TestImplementation.class.getName()
       );
       Object obj = FactoryUtil.getSingleton(TestInterface.class);
       assertNotNull(obj);
       assertTrue(obj instanceof TestImplementation);
   }

   public void testUseInstanceFactoryAsProperty() throws RepositoryException, IOException{
       FactoryUtil.setDefaultImplementation(TestInterface.class, TestInstanceFactory.class.getName());
       Object obj = FactoryUtil.getSingleton(TestInterface.class);
       assertNotNull(obj);
       assertTrue(obj instanceof TestImplementation);
   }



}
