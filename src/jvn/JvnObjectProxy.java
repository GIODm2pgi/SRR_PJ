package jvn;

import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import jvn.annots.JvnMethod;
import jvn.annots.JvnReadMethod;
import jvn.annots.JvnWriteMethod;
import jvn.annots.JvnMethod.JvnMethodChange;

/**
 * Class defining an interceptor by dynamic proxy.
 * It is used to create Javanaise objects dynamically.
 * To use it, you must annotate your methods to see 
 * the changes on the object they produce.
 */
public class JvnObjectProxy implements InvocationHandler {

	/**
	 * The real JvnObject.
	 */
	private JvnObject jvnObject = null ;

	/**
	 * Used to sleep (to debug).
	 */
	private long sleepTime = 2000 ;

	/**
	 * Default constructor of a JvnObjectProxy.
	 * @param obj : The real object Jvn.
	 */
	public JvnObjectProxy(JvnObject obj) {
		this.jvnObject = obj ;
	}

	/**
	 * Instantiates an object (if it does not exist) or 
	 * retrieves the current object (if exist) whose 
	 * reference is given as a parameter.
	 * @param o : The real object if there are creation.
	 * @param objName : The reference name of the object.
	 * @return The new or current object (as a proxy).
	 * @throws JvnException
	 */
	public static Object instanceJvn(Serializable o, String objName) throws JvnException{
		JvnServerImpl js = JvnServerImpl.jvnGetServer();

		// look up the IRC object in the JVN server
		// if not found, create it, and register it in the JVN server
		JvnObject jo = js.jvnLookupObject(objName);

		if (jo == null) {
			jo = js.jvnCreateObject(o);
			// after creation, I have a write lock on the object
			jo.jvnUnLock();
			js.jvnRegisterObject(objName, jo);
		}
		// Return the Proxy Object (interceptor).
		return JvnObjectProxy.newInstance(jo);
	}

	/**
	 * Instantiates an object whose is JvnObject interceptor
	 * is given as a parameter.
	 * @param o : The real object if there are creation.
	 * @param objName : The reference name of the object.
	 * @return The new or current object (as a proxy).
	 * @throws JvnException
	 */
	public static Object newInstance(JvnObject obj) throws IllegalArgumentException, JvnException{
		return java.lang.reflect.Proxy.newProxyInstance(
				obj.jvnGetObjectState().getClass().getClassLoader(),
				obj.jvnGetObjectState().getClass().getInterfaces(),
				new JvnObjectProxy(obj));
	} 

	/**
	 * Invoke a method of the proxy object.
	 * Generate lock read section at the runtime if the method is annotate with
	 * {@code JvnMethod(change = JvnMethodChange.READ)} or {@code JvnReadMethod}.
	 * Generate lock write section at the runtime if the method is annotate with
	 * {@code JvnMethod(change = JvnMethodChange.WRITE)} or {@code JvnWriteMethod}.
	 */
	public Object invoke(Object proxy, Method method, Object[] args)
			throws Throwable {
		// The object return by the real method.
		Object toReturn = null ;
		// Case jvnLockRead (method with annotation JvnWriteMethod).
		if(method.isAnnotationPresent(JvnWriteMethod.class)
				|| (method.isAnnotationPresent(JvnMethod.class)
						&& method.getAnnotation(JvnMethod.class).change().compareTo(JvnMethodChange.WRITE) == 0)){
			jvnObject.jvnLockWrite();
			toReturn = method.invoke(jvnObject.jvnGetObjectState(), args);
			Thread.sleep(sleepTime);
			jvnObject.jvnUnLock();
		}
		// Case jvnLockRead (method with annotation JvnReadMethod).
		else if(method.isAnnotationPresent(JvnReadMethod.class)
				|| (method.isAnnotationPresent(JvnMethod.class)
						&& method.getAnnotation(JvnMethod.class).change().compareTo(JvnMethodChange.READ) == 0)){
			jvnObject.jvnLockRead();
			toReturn = method.invoke(jvnObject.jvnGetObjectState(), args);
			Thread.sleep(sleepTime);
			jvnObject.jvnUnLock();
		}
		return toReturn ;
	}

}
