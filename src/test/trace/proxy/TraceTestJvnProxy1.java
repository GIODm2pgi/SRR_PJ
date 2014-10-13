package test.trace.proxy;

import test.tool.AttenteRunProcess;
import test.tool.TraceTestJvnHelperProxy;
import jvn.JvnException;

/**
 * Test Trace 1 with JvnObjectProxy.
 * Variables : A B
 * Processus : 3
 * 
 * 1 : W(A)5;-----;-----;-----;R(A)8;W(B)6;R(B)6;-----;-----
 * 2 : -----;-----;-----;R(B)3;-----;-----;-----;W(A)8;-----
 * 3 : -----;W(B)3;W(A)8;-----;-----;-----;-----;-----;R(A)8
 */
public class TraceTestJvnProxy1 {

	private static String vars = "AB" ;
	private static int procs = 3 ;
	
	public static void main(String[] args) {
		AttenteRunProcess sleeper = new AttenteRunProcess("sleeper", procs);
		sleeper.setTimeToSleep(50);
		TraceTestJvnHelperProxy tracer = new TraceTestJvnHelperProxy(procs, vars);
		tracer.addTraces(1, "W(A)5;-----;-----;-----;R(A)8;W(B)6;R(B)6;-----;-----");
		tracer.addTraces(2, "-----;-----;-----;R(B)3;-----;-----;-----;W(A)8;-----");
		tracer.addTraces(3, "-----;W(B)3;W(A)8;-----;-----;-----;-----;-----;R(A)8");
		sleeper.synchronisationParAttente();
		try {
			tracer.runTrace(sleeper.getMyId());
		} catch (JvnException e) {
			e.printStackTrace();
		}
		System.out.println("Fini");
	}
	
}
