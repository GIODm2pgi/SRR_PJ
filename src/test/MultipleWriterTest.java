package test;

import java.io.Serializable;

import jvn.JvnException;
import jvn.JvnObject;
import jvn.JvnServerImpl;

public class MultipleWriterTest {
	static int ite_end = 10 ;
	static int nb_of_process = 4 ;
	static long sleep_time = 2000 ;
	static String ressource1 = "jo1" ;
	static String ressource2 = "jo2" ;

	public static void main(String[] args) {
		try {
			// initialize JVN
			JvnServerImpl js = JvnServerImpl.jvnGetServer();
			
			JvnObject jo1 = js.jvnLookupObject(ressource1);
			if (jo1 == null) {
				jo1 = js.jvnCreateObject((Serializable) new MyIntegerIncr());
				// after creation, I have a write lock on the object
				jo1.jvnUnLock();
				js.jvnRegisterObject(ressource1, jo1);
			}
			
			JvnObject jo2 = js.jvnLookupObject(ressource2);
			if (jo2 == null) {
				jo2 = js.jvnCreateObject((Serializable) new MyIntegerIncr());
				// after creation, I have a write lock on the object
				jo2.jvnUnLock();
				js.jvnRegisterObject(ressource2, jo2);
			}
			
			for (int i = 0; i < ite_end; i++) {
				jo1.jvnLockWrite();
				((MyIntegerIncr)jo1.jvnGetObjectState()).increment() ;
				Thread.sleep(sleep_time);
				jo1.jvnUnLock();
			}
			
			jo2.jvnLockWrite();
			((MyIntegerIncr)jo2.jvnGetObjectState()).increment() ;
			jo2.jvnUnLock();
			
			boolean fini = false ;
			while(!fini){
				jo2.jvnLockRead();
				if(((MyIntegerIncr)jo2.jvnGetObjectState()).get() == nb_of_process)
					fini = true ;
				jo2.jvnUnLock();
				Thread.sleep((long) (500));
			}
			
			jo1.jvnLockRead();
			System.out.println(((MyIntegerIncr)jo1.jvnGetObjectState()).get());
			System.out.println(((MyIntegerIncr)jo1.jvnGetObjectState()).is(nb_of_process *ite_end));
			jo1.jvnUnLock();
			
			} catch (JvnException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			} 
	}

}
