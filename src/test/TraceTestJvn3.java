package test;

import jvn.JvnException;

/**
 * Variables : A
 * Processus : 2
 * 
 * 1 : W(A)5;-----;R(A)3;-----;W(A)7;R(A)7;W(A)5;-----;-----;R(A)8;W(A)4;-----;R(A)4
 * 2 : -----;W(A)3;-----;R(A)3;-----;-----;-----;W(A)8;R(A)8;-----;-----;R(A)4;-----
 */
public class TraceTestJvn3 {

	private static String vars = "A" ;
	private static int procs = 2 ;
	
	public static void main(String[] args) {
		AttenteRunProcess sleeper = new AttenteRunProcess("sleeper", procs);
		sleeper.setTimeToSleep(50);
		TraceTestJvnHelper tracer = new TraceTestJvnHelper(procs, vars);
		tracer.addTraces(1, "W(A)5;-----;R(A)3;-----;W(A)7;R(A)7;W(A)5;-----;-----;R(A)8;W(A)4;-----;R(A)4");
		tracer.addTraces(2, "-----;W(A)3;-----;R(A)3;-----;-----;-----;W(A)8;R(A)8;-----;-----;R(A)4;-----");
		sleeper.synchronisationParAttente();
		try {
			tracer.runTrace(sleeper.getMyId());
		} catch (JvnException e) {
			e.printStackTrace();
		}
		System.out.println("Fini");
	}
	
}
