package test.stress.object;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Random;

import test.type.IntegerForJvn;
import jvn.JvnException;
import jvn.JvnObject;
import jvn.JvnServerImpl;

public class StressTestJvnObjectRandom {
	static Random randomGenerator = new Random();
	static HashMap<String, JvnObject> mapObj = new HashMap<String, JvnObject>();
	
	public static JvnObject CreateOrGetObject(){
		try {
			String alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ" ;
			int random = Math.abs(randomGenerator.nextInt()) % alphabet.length();
			String name = alphabet.substring(random, random+1) ;
			// Test if exist :
			if(mapObj.get(name) != null){
				System.out.println("GET SERVER : " + mapObj.get(name).jvnGetObjectId()  + " : " + name);
				return mapObj.get(name) ;
			}
			
			// initialize JVN
			JvnServerImpl js = JvnServerImpl.jvnGetServer();
			// look up the IRC object in the JVN server
			// if not found, create it, and register it in the JVN server
			JvnObject jo = js.jvnLookupObject(name);
			if (jo == null) {
				jo = js.jvnCreateObject((Serializable) new IntegerForJvn());
				// after creation, I have a write lock on the object
				jo.jvnUnLock();
				System.out.println("CREATE : ID " + jo.jvnGetObjectId() + " : " + name);
				js.jvnRegisterObject(name, jo);
			}
			else{
				System.out.println("GET COORD : ID " + jo.jvnGetObjectId() + " : " + name);
			}
			mapObj.put(name, jo);
			return jo;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static void realizeAnInstruction(JvnObject jo){
		switch (Math.abs(randomGenerator.nextInt())%2) {
		case 0:
			try {				
				System.out.println("READ START : ID " + jo.jvnGetObjectId());
				jo.jvnLockRead();
				System.out.println("READ : ID " + jo.jvnGetObjectId()  + " : " +((IntegerForJvn)jo.jvnGetObjectState()).get());
				jo.jvnUnLock();
				System.out.println("READ END : ID " + jo.jvnGetObjectId());
			} catch (JvnException e) {
				e.printStackTrace();
			}
			break;

		case 1:
		default :
			try {
				System.out.println("WRITE START : ID " + jo.jvnGetObjectId());
				jo.jvnLockWrite();
				((IntegerForJvn)jo.jvnGetObjectState()).set(Math.abs(randomGenerator.nextInt()));
				System.out.println("WRITE : ID " + jo.jvnGetObjectId()  + " : " +((IntegerForJvn)jo.jvnGetObjectState()).get());
				jo.jvnUnLock();
				System.out.println("WRITE END : ID " + jo.jvnGetObjectId());
			} catch (JvnException e) {
				e.printStackTrace();
			}
			break;
		}
	}

	public static void main(String argv[]) {

		while(true){
			realizeAnInstruction(CreateOrGetObject());
		}
	}
}
