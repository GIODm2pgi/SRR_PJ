package test.trace.object;

import test.tool.AttenteRunProcess;
import test.tool.TraceTestJvnHelperObj;
import jvn.JvnException;

/**
 * Test Trace 2 with JvnObject.
 * Variables : A B C
 * Processus : 2
 * 
 * 1 : W(A)5;-----;W(C)3;-----;R(B)3;W(B)2;R(A)5;-----;-----;R(C)3;W(B)4;-----
 * 2 : -----;W(B)3;-----;R(B)3;-----;-----;-----;W(B)8;R(A)5;-----;-----;R(B)4
 */
public class TraceTestJvnObj2 {

	private static String vars = "ABC" ;
	private static int procs = 2 ;
	
	public static void main(String[] args) {
		AttenteRunProcess sleeper = new AttenteRunProcess("sleeper", procs);
		sleeper.setTimeToSleep(50);
		TraceTestJvnHelperObj tracer = new TraceTestJvnHelperObj(procs, vars);
		tracer.addTraces(1, "W(A)5;-----;W(C)3;-----;R(B)3;W(B)2;R(A)5;-----;-----;R(C)3;W(B)4;-----");
		tracer.addTraces(2, "-----;W(B)3;-----;R(B)3;-----;-----;-----;W(B)8;R(A)5;-----;-----;R(B)4");
		sleeper.synchronisationParAttente();
		try {
			tracer.runTrace(sleeper.getMyId());
		} catch (JvnException e) {
			e.printStackTrace();
		}
		System.out.println("Fini");
	}
	
}
