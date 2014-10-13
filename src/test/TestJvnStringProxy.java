package test;

import test.type.IJvnStringProxy;
import test.type.JvnStringProxy;
import jvn.JvnObjectProxy;

public class TestJvnStringProxy {
	public static void main(String[] args) {
		try {
			IJvnStringProxy s = (IJvnStringProxy) JvnObjectProxy.instanceJvn(new JvnStringProxy(), "TEST_STRING") ;
			System.out.println(s.get());
			s.set("Azerty");
			System.out.println(s.get());
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("Fini");
	}
}
