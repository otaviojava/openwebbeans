package org.apache.webbeans.newtests.interceptors.resolution;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.InterceptionType;
import javax.enterprise.inject.spi.Interceptor;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.webbeans.intercept.DefaultInterceptorHandler;
import org.apache.webbeans.intercept.InterceptorResolutionService;
import org.apache.webbeans.newtests.AbstractUnitTest;
import org.apache.webbeans.newtests.interceptors.factory.beans.ClassMultiInterceptedClass;
import org.apache.webbeans.proxy.InterceptorDecoratorProxyFactory;
import org.apache.webbeans.proxy.InterceptorHandler;
import org.apache.webbeans.test.component.intercept.webbeans.ActionInterceptor;
import org.apache.webbeans.test.component.intercept.webbeans.SecureInterceptor;
import org.apache.webbeans.test.component.intercept.webbeans.TransactionalInterceptor;
import org.apache.webbeans.test.component.intercept.webbeans.bindings.Action;
import org.apache.webbeans.test.component.intercept.webbeans.bindings.Secure;
import org.apache.webbeans.test.component.intercept.webbeans.bindings.Transactional;
import org.junit.Assert;
import org.junit.Test;

/**
 * Test the interceptor Resolution and creating a proxy from it.
 */
public class InterceptorProxyChainTest extends AbstractUnitTest
{

    @Test
    public void testInterceptorProxyChain() throws Exception
    {
        Collection<String> beanXmls = new ArrayList<String>();
        beanXmls.add(getXmlPath(this.getClass().getPackage().getName(), InterceptorResolutionServiceTest.class.getSimpleName()));

        Collection<Class<?>> beanClasses = new ArrayList<Class<?>>();
        beanClasses.add(ClassMultiInterceptedClass.class);
        beanClasses.add(Transactional.class);
        beanClasses.add(Secure.class);
        beanClasses.add(Action.class);
        beanClasses.add(ActionInterceptor.class);
        beanClasses.add(SecureInterceptor.class);
        beanClasses.add(TransactionalInterceptor.class);

        startContainer(beanClasses, beanXmls);

        InterceptorResolutionService ir = new InterceptorResolutionService(getWebBeansContext());
        AnnotatedType<ClassMultiInterceptedClass> annotatedType = getBeanManager().createAnnotatedType(ClassMultiInterceptedClass.class);
        Bean<ClassMultiInterceptedClass> bean = (Bean<ClassMultiInterceptedClass>) getBeanManager().resolve(getBeanManager().getBeans(ClassMultiInterceptedClass.class));

        InterceptorResolutionService.BeanInterceptorInfo interceptorInfo = ir.calculateInterceptorInfo(bean, annotatedType);
        Assert.assertNotNull(interceptorInfo);


        // not via BeanManager but native. We will proxy it ourselfs
        ClassMultiInterceptedClass internalInstance = new ClassMultiInterceptedClass();
        CreationalContext<ClassMultiInterceptedClass> cc = getBeanManager().createCreationalContext(bean);

        Map<Interceptor<?>,Object> interceptorInstances  = new HashMap<Interceptor<?>, Object>();
        for (Interceptor interceptorBean : interceptorInfo.getInterceptors())
        {
            Object interceptorInstance = interceptorBean.create(cc);
            interceptorInstances.put(interceptorBean, interceptorInstance);
        }

        Map<Method, List<Interceptor<?>>> methodInterceptors = new HashMap<Method, List<Interceptor<?>>>();
        List<Method> nonBusinessMethods = new ArrayList<Method>();
        for (Map.Entry<Method, InterceptorResolutionService.MethodInterceptorInfo> miEntry : interceptorInfo.getBusinessMethodsInfo().entrySet())
        {
            Method interceptedMethod = miEntry.getKey();
            InterceptorResolutionService.MethodInterceptorInfo mii = miEntry.getValue();
            if (InterceptionType.AROUND_INVOKE.equals(mii.getInterceptionType()))
            {
                List<Interceptor<?>> activeInterceptors = new ArrayList<Interceptor<?>>();
                if (mii.getEjbInterceptors() != null)
                {
                    for (Interceptor<?> i : mii.getEjbInterceptors())
                    {
                        activeInterceptors.add(i);
                    }
                }
                if (mii.getCdiInterceptors() != null)
                {
                    for (Interceptor<?> i : mii.getCdiInterceptors())
                    {
                        activeInterceptors.add(i);
                    }
                }
                if (activeInterceptors.size() > 0)
                {
                    methodInterceptors.put(interceptedMethod, activeInterceptors);
                }
            }
            else
            {
                nonBusinessMethods.add(interceptedMethod);
            }
        }

        InterceptorHandler interceptorHandler
                = new DefaultInterceptorHandler<ClassMultiInterceptedClass>(internalInstance, methodInterceptors, interceptorInstances);

        InterceptorDecoratorProxyFactory pf = new InterceptorDecoratorProxyFactory();

        // we take a fresh URLClassLoader to not blur the test classpath with synthetic classes.
        ClassLoader classLoader = new URLClassLoader(new URL[0]);

        Method[] businessMethods = methodInterceptors.keySet().toArray(new Method[methodInterceptors.size()]);
        Method[] nonInterceptedMethods = interceptorInfo.getNonInterceptedMethods().toArray(new Method[interceptorInfo.getNonInterceptedMethods().size()]);

        Class<? extends ClassMultiInterceptedClass> proxyClass = pf.createProxyClass(classLoader, ClassMultiInterceptedClass.class, businessMethods, nonInterceptedMethods);
        Assert.assertNotNull(proxyClass);

        ClassMultiInterceptedClass proxyInstance = pf.createProxyInstance(proxyClass, internalInstance, interceptorHandler);
        Assert.assertNotNull(proxyInstance);

        Assert.assertEquals(internalInstance, proxyInstance.getSelf());

        proxyInstance.setMeaningOfLife(42);
        Assert.assertEquals(42, proxyInstance.getMeaningOfLife());

        shutDownContainer();
    }


}