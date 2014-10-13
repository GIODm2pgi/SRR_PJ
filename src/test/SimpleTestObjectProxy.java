package test;

import java.io.Serializable;

import test.type.IIntegerForJvnProxy;
import test.type.IntegerForJvnProxy;
import jvn.JvnObject;
import jvn.JvnObjectProxy;
import jvn.JvnServerImpl;

public class SimpleTestObjectProxy {

	public static void main(String[] args) {

		try {
			// initialize JVN
			JvnServerImpl js = JvnServerImpl.jvnGetServer();

			// look up the IRC object in the JVN server
			// if not found, create it, and register it in the JVN server
			JvnObject jo = js.jvnLookupObject("TEST_PROXY");

			if (jo == null) {
				jo = js.jvnCreateObject((Serializable) new IntegerForJvnProxy());
				// after creation, I have a write lock on the object
				jo.jvnUnLock();
				js.jvnRegisterObject("TEST_PROXY", jo);
			}
		
			//System.out.println(JvnObjectProxy.newInstance(jo).getClass().getName());
			IIntegerForJvnProxy i = (IIntegerForJvnProxy) JvnObjectProxy.newInstance(jo);
			
			i.set(123);
			
			System.out.println(i.get());

		} catch (Exception e) {
			System.out.println("IRC problem : " + e.getMessage());
			e.printStackTrace();
		}
	}

}
