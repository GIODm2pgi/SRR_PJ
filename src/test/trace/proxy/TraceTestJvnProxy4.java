package test.trace.proxy;

import test.tool.AttenteRunProcess;
import test.tool.TraceTestJvnHelperProxy;
import jvn.JvnException;

/**
 * Test Trace 4 with JvnObject.
 * Variables : A B C D
 * Processus : 4
 * 
 * 1 : W(A)5;-----;R(C)3;-----;W(B)7;R(A)5;W(D)5;-----;-----;R(A)5;W(D)4;-----;R(D)4
 * 2 : -----;W(C)3;-----;-----;-----;-----;-----;W(B)8;R(C)3;-----;-----;R(D)4;-----
 * 3 : -----;-----;-----;W(D)4;-----;R(B)7;-----;-----;-----;R(B)8;-----;-----;-----
 * 4 : -----;-----;-----;-----;-----;-----;-----;-----;-----;R(C)3;-----;-----;-----
 */
public class TraceTestJvnProxy4 {

	private static String vars = "ABCD" ;
	private static int procs = 4 ;
	
	public static void main(String[] args) {
		AttenteRunProcess sleeper = new AttenteRunProcess("sleeper", procs);
		sleeper.setTimeToSleep(50);
		TraceTestJvnHelperProxy tracer = new TraceTestJvnHelperProxy(procs, vars);
		tracer.addTraces(1, "W(A)5;-----;R(C)3;-----;W(B)7;R(A)5;W(D)5;-----;-----;R(A)5;W(D)4;-----;R(D)4");
		tracer.addTraces(2, "-----;W(C)3;-----;-----;-----;-----;-----;W(B)8;R(C)3;-----;-----;R(D)4;-----");
		tracer.addTraces(3, "-----;-----;-----;W(D)4;-----;R(B)7;-----;-----;-----;R(B)8;-----;-----;-----");
		tracer.addTraces(4, "-----;-----;-----;-----;-----;-----;-----;-----;-----;R(C)3;-----;-----;-----");
		sleeper.synchronisationParAttente();
		try {
			tracer.runTrace(sleeper.getMyId());
		} catch (JvnException e) {
			e.printStackTrace();
		}
		System.out.println("Fini");
	}
	
}
