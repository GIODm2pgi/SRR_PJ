package test;

import java.io.Serializable;

import jvn.JvnObject;
import jvn.JvnServerImpl;

public class IncrementTestProcessus {
	static int increment = 100000 ;
	static int nb_of_process = 4 ;
	static String ressource1 = "int_to_incr" ;
	static String ressource2 = "nb_of_process" ;
	static String ressource3 = "end_process" ;
	
	public static void main(String[] args) {
		try {
			   
			// initialize JVN
			JvnServerImpl js = JvnServerImpl.jvnGetServer();
			
			// look up the IRC object in the JVN server
			// if not found, create it, and register it in the JVN server
			
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
			
			JvnObject jo3 = js.jvnLookupObject(ressource3);
			if (jo3 == null) {
				jo3 = js.jvnCreateObject((Serializable) new MyIntegerIncr());
				// after creation, I have a write lock on the object
				jo3.jvnUnLock();
				js.jvnRegisterObject(ressource3, jo3);
			}

			jo2.jvnLockWrite();
			((MyIntegerIncr)jo2.jvnGetObjectState()).increment() ;
			jo2.jvnUnLock();
			
			boolean fini = false ;
			while(!fini){
				jo2.jvnLockRead();
				if(((MyIntegerIncr)jo2.jvnGetObjectState()).get() == nb_of_process)
					fini = true ;
				System.out.println("Attente process : " + ((MyIntegerIncr)jo2.jvnGetObjectState()).get());
				jo2.jvnUnLock();
				Thread.sleep((long) (500));
			}
			
			// create the loop increment
			for (int i = 0; i < increment ; i++) {
				jo1.jvnLockWrite();
				((MyIntegerIncr)jo1.jvnGetObjectState()).increment() ;
				Thread.sleep((long) (Math.random() % 100));
				jo1.jvnUnLock();
			}
			
			jo3.jvnLockWrite();
			((MyIntegerIncr)jo3.jvnGetObjectState()).increment();
			jo3.jvnUnLock();
			
			fini = false ;
			while(!fini){
				jo3.jvnLockRead();
				if(((MyIntegerIncr)jo3.jvnGetObjectState()).get() == nb_of_process)
					fini = true ;
				System.out.println("End process : " + ((MyIntegerIncr)jo3.jvnGetObjectState()).get());
				jo3.jvnUnLock();
				Thread.sleep((long) (500));
			}
				
			jo1.jvnLockRead();
			System.out.println(((MyIntegerIncr)jo1.jvnGetObjectState()).get());
			System.out.println(((MyIntegerIncr)jo1.jvnGetObjectState()).is(nb_of_process * (increment)));
			jo1.jvnUnLock();
		   
		   } catch (Exception e) {
			   System.out.println("Problem : " + e.getMessage());
		   }
	}
}
