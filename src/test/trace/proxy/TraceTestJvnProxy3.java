package test.trace.proxy;

import test.tool.AttenteRunProcess;
import test.tool.TraceTestJvnHelperProxy;
import jvn.JvnException;

/**
 * Test Trace 3 with JvnObject.
 * TODO: FAILED
 * Variables : A
 * Processus : 2
 * 
 * 1 : W(A)5;-----;R(A)3;-----;W(A)7;R(A)7;W(A)5;-----;-----;R(A)8;W(A)4;-----;R(A)4
 * 2 : -----;W(A)3;-----;R(A)3;-----;-----;-----;W(A)8;R(A)8;-----;-----;R(A)4;-----
 */
public class TraceTestJvnProxy3 {

	private static String vars = "A" ;
	private static int procs = 2 ;
	
	public static void main(String[] args) {
		AttenteRunProcess sleeper = new AttenteRunProcess("sleeper", procs);
		sleeper.setTimeToSleep(50);
		TraceTestJvnHelperProxy tracer = new TraceTestJvnHelperProxy(procs, vars);
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
