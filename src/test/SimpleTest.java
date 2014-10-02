package test;

import java.io.Serializable;

import jvn.JvnException;
import jvn.JvnObject;
import jvn.JvnServerImpl;

public class SimpleTest {

	public static void main(String[] args) {
		JvnServerImpl js = JvnServerImpl.jvnGetServer();
		JvnObject jo1 = null;
		JvnObject jo2 = null;
		try {
			jo1 = js.jvnLookupObject("Integer1");
			if (jo1 == null) {
				jo1 = js.jvnCreateObject((Serializable) new Integer(123));

				// after creation, I have a write lock on the object
				jo1.jvnUnLock();
				js.jvnRegisterObject("Integer1", jo1);
			}
			jo2 = js.jvnLookupObject("Integer2");
			if (jo2 == null) {
				jo2 = js.jvnCreateObject((Serializable) new Integer(123));

				// after creation, I have a write lock on the object
				jo2.jvnUnLock();
				js.jvnRegisterObject("Integer1", jo2);
			}
		} catch (JvnException e) {
			e.printStackTrace();
		}
	}
}
