package test.tool;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

import test.type.IIntegerForJvnProxy;
import test.type.IntegerForJvnProxy;
import jvn.JvnException;
import jvn.JvnObject;
import jvn.JvnObjectProxy;
import jvn.JvnServerImpl;

/**
 * Classe offrant une aide dans la génération 
 * de test par trace de JVN par JvnObjectProxy.
 */
public class TraceTestJvnHelperProxy {

	/**
	 * Les temps d'attente entre les traces.
	 */
	private long timeToSleepNothing = 1000 ;
	private long timeToSleepRead = 1000 ;
	private long timeToSleepWrite = 1000 ;
	
	/**
	 * Les traces de tous les processus.
	 */
	private ArrayList<String> traces = null ;
	
	/**
	 * Permet de stocker les objets Jvn.
	 */
	private HashMap<String, IIntegerForJvnProxy> myObjects = null ;
	
	/**
	 * Construit un aider de test de trace pour
	 * Javanaise.
	 * @param nbOfProcess : Le nombre de processus du test.
	 * @param variables : La liste des variable du test, de
	 * 			la fome suivante "ABCD" où A, B, C, D sont des
	 * 			variable.
	 */
	public TraceTestJvnHelperProxy(int nbOfProcess,String variables){
		this.traces = new ArrayList<String>(nbOfProcess) ;
		this.myObjects = new HashMap<String, IIntegerForJvnProxy>() ;
		// initialize JVN
		JvnServerImpl js = JvnServerImpl.jvnGetServer();
		for (int i = 0; i < variables.length(); i++) {
			String key = variables.substring(i, i+1) ;
			try {
				JvnObject jo = js.jvnLookupObject(key);
				if (jo == null) {
					jo = js.jvnCreateObject((Serializable) new IntegerForJvnProxy());
					// after creation, I have a write lock on the object
					jo.jvnUnLock();
					js.jvnRegisterObject(key, jo);
				}
				this.myObjects.put(key, (IIntegerForJvnProxy) JvnObjectProxy.newInstance(jo)) ;
			} catch (JvnException e) {
				System.err.println("Erreur lors du mécanisme de trace.");
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Ajoute une trace d'un processus.
	 * @param processus : L'id du processus.
	 * @param trace : La trace de la forme "W(X)1;R(X)1....".
	 */
	public void addTraces(int processus, String trace){
		this.traces.add(processus-1, trace);
	}
	
	/**
	 * Lance l'exécution de la trace d'un processus.
	 * @param processus : L'id du processus.
	 * @throws JvnException
	 */
	public void runTrace(int processus) throws JvnException{
		String trace = this.traces.get(processus-1);
		int t = 0 ;
		int i = 0 ;
		while (i < trace.length()){
			String next = trace.substring(i, i + 5) ; 
			// Cas rien faire : 
			if(next.equals("-----")){
				try {
					Thread.sleep(this.timeToSleepNothing);
				} catch (InterruptedException e) {
					System.err.println("Erreur lors du mécanisme d'exécution de trace.");
					e.printStackTrace();
				}
			}
			// Cas Write
			else if(next.startsWith("W")){
				Integer value = new Integer(next.substring(4, 5));
				String variable = next.substring(2, 3);
				System.out.println("P" + processus + " (t" + t + ")=> W(" + variable + ")=" + value);
				IIntegerForJvnProxy jo = this.myObjects.get(variable);
				jo.set(value);
				try {
					Thread.sleep(this.timeToSleepWrite);
				} catch (InterruptedException e) {
					System.err.println("Erreur lors du mécanisme d'exécution de trace.");
					e.printStackTrace();
				}
			}
			// Cas read.
			else if(next.startsWith("R")){
				Integer test = null ;
				Integer value = new Integer(next.substring(4, 5));
				String variable = next.substring(2, 3);
				IIntegerForJvnProxy jo = this.myObjects.get(variable);
				test = jo.get();
				if(test.compareTo(value) != 0){
					throw new JvnException("Erreur de trace.");
				}
				System.out.println("P" + processus + " (t" + t + ")=> R(" + variable + ")=" + value);
				try {
					Thread.sleep(this.timeToSleepRead);
				} catch (InterruptedException e) {
					System.err.println("Erreur lors du mécanisme d'exécution de trace.");
					e.printStackTrace();
				}
			}
			
			// Mis à jour de i;
			i+=5;
			t++ ;
			if(i < trace.length()){
				i++ ;
			}
		}
	JvnServerImpl.jvnGetServer().jvnTerminate();
	}
}
