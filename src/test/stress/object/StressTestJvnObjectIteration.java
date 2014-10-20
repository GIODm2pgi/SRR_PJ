package test.stress.object;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import jvn.JvnException;
import jvn.JvnObject;
import jvn.JvnServerImpl;
import test.tool.AttenteRunProcess;
import test.type.IntegerForJvn;

public class StressTestJvnObjectIteration {
	static Random randomGenerator = new Random();
	static HashMap<String, JvnObject> mapObj = new HashMap<String, JvnObject>();

	public static int nb_process = 4;
	public static int nb_iteration = 300;

	static List<Integer> l = new ArrayList<Integer>();
	static String alphabet = "A" + "BCDE" /*+ "FGHIJKLMNOPQRSTUVWXYZ"*/  ;

	public static void init (){
		for (int i = 0; i < alphabet.length();i++){
			l.add(new Integer(0));
		}
	}

	public static boolean isFull(int rang){
		return l.get(rang) == nb_iteration ;
	}

	public static boolean isEnd() {
		for(int i =0 ; i<l.size() ; i++){
			if(!isFull(i))
				return false;
		}
		return true ;
	}

	public static class Couple {

		JvnObject o;
		int i;

	}

	public static Couple CreateOrGetObject(){
		try {
			int random = Math.abs(randomGenerator.nextInt()) % alphabet.length();
			String name = alphabet.substring(random, random+1) ;
			// Test if exist :
			if(mapObj.get(name) != null){
				//System.out.println("GET SERVER : " + mapObj.get(name).jvnGetObjectId()  + " : " + name);
				Couple toReturn = new Couple();
				toReturn.i = random ;
				toReturn.o = mapObj.get(name) ;
				return toReturn ;
			}

			// initialize JVN
			JvnServerImpl js = JvnServerImpl.jvnGetServer();
			// look up the IRC object in the JVN server
			// if not found, create it, and register it in the JVN server
			JvnObject jo = js.jvnLookupObject(name);
			if (jo == null) {
				try {
					jo = js.jvnCreateObject((Serializable) new IntegerForJvn());
					// after creation, I have a write lock on the object
					jo.jvnUnLock();
					//System.out.println("CREATE : ID " + jo.jvnGetObjectId() + " : " + name);
					js.jvnRegisterObject(name, jo);
				} catch (JvnException e){
					jo = js.jvnLookupObject(name);
				}
			}
			else{
				//System.out.println("GET COORD : ID " + jo.jvnGetObjectId() + " : " + name);
			}
			mapObj.put(name, jo);
			Couple toReturn = new Couple();
			toReturn.i = random ;
			toReturn.o = mapObj.get(name) ;
			return toReturn ;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static void realizeAnInstruction(Couple joC){
		JvnObject jo = joC.o ;
		int rang = joC.i;
		switch (1) {
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
			if(!isFull(rang)){
				try {
					System.out.println("WRITE START : ID " + jo.jvnGetObjectId());
					jo.jvnLockWrite();
					((IntegerForJvn)jo.jvnGetObjectState()).increment();
					jo.jvnUnLock();
					System.out.println("WRITE END : ID " + jo.jvnGetObjectId());
					l.set(rang, l.get(rang)+1);
				} catch (JvnException e) {
					e.printStackTrace();
				}
			}
			break;
		}
	}

	public static void main(String argv[]) {

		init();

		AttenteRunProcess sleeper1 = new AttenteRunProcess("sleeper1", nb_process);
		sleeper1.setTimeToSleep(50);
		sleeper1.synchronisationParAttente();

		while(!isEnd()){
			realizeAnInstruction(CreateOrGetObject());
			/*try {
				Thread.sleep(200 + Math.abs(randomGenerator.nextInt(200)));
			} catch (NumberFormatException e) {
				e.printStackTrace();
			}
			catch (InterruptedException e) {
				e.printStackTrace();
			}*/
		}

		/*AttenteRunProcess sleeper2 = new AttenteRunProcess("sleeper2", nb_process);
		sleeper2.setTimeToSleep(50);
		sleeper2.synchronisationParAttente();
		 */

		AttenteRunProcess sleeper2 = new AttenteRunProcess("sleeper2", nb_process);
		sleeper2.setTimeToSleep(50);
		sleeper2.synchronisationParAttente();

		for(int i = 0 ; i < alphabet.length() ; i++){
			String name = alphabet.charAt(i) + "";
			JvnObject jo = mapObj.get(name);
			try {
				jo.jvnLockRead();
				if(!((IntegerForJvn)jo.jvnGetObjectState()).is(nb_process*nb_iteration)){
					System.err.println(nb_process*nb_iteration + " != " + ((IntegerForJvn)jo.jvnGetObjectState()).get());
				}
				else
					System.err.println(nb_process*nb_iteration + " == " + ((IntegerForJvn)jo.jvnGetObjectState()).get());
				jo.jvnUnLock();

			} catch (JvnException e) {
				e.printStackTrace();
			}
		}	

		try {
			JvnServerImpl.jvnGetServer().jvnTerminate();
			System.exit(0);
		} catch (JvnException e) {
			e.printStackTrace();
		}
	}
}
