package jvn;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;


public class JvnObjectProxy implements InvocationHandler {
	private JvnObject jvnObject = null ;
	private long sleepTime = 2000 ;

	 public static Object newInstance(JvnObject obj){
		 try {
			return java.lang.reflect.Proxy.newProxyInstance(
					 obj.jvnGetObjectState().getClass().getClassLoader(),
					 obj.jvnGetObjectState().getClass().getInterfaces(),
					 new JvnObjectProxy(obj));
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JvnException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null ;
	 } 
	 
	 public JvnObjectProxy(JvnObject obj) {
		 this.jvnObject = obj ;
	}
	
	public Object invoke(Object proxy, Method method, Object[] args)
			throws Throwable {
		
			Object toReturn = null ;
			// Case jvnLockRead (method with annotation JvnWriteMethod).
			if(method.isAnnotationPresent(JvnWriteMethod.class)){
				jvnObject.jvnLockWrite();
				toReturn = method.invoke(jvnObject.jvnGetObjectState(), args);
				Thread.sleep(sleepTime);
				jvnObject.jvnUnLock();
			}
			// Case jvnLockRead (method with annotation JvnReadMethod).
			else if(method.isAnnotationPresent(JvnReadMethod.class)){
				jvnObject.jvnLockRead();
				toReturn = method.invoke(jvnObject.jvnGetObjectState(), args);
				Thread.sleep(sleepTime);
				jvnObject.jvnUnLock();
			}
			
			return toReturn ;
	}

}
