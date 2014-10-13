package test.trace.proxy;

import test.tool.AttenteRunProcess;
import test.tool.TraceTestJvnHelperProxy;
import jvn.JvnException;

/**
 * Test Trace 5 with JvnObject.
 * Variables : A B C D E F G H I
 * Processus : 6
 * 
 * 1 : W(A)1;W(B)2;W(C)3;W(D)4;W(E)5;W(F)6;W(G)7;W(H)8;W(I)9;-----;R(A)1;R(B)2;R(C)3;R(D)4;R(E)5;R(F)6;R(G)7;R(H)8;R(I)9
 * 2 : -----;R(A)1;R(B)2;R(C)3;R(D)4;R(E)5;R(F)6;R(G)7;R(H)8;R(I)9;R(A)1;R(B)2;R(C)3;R(D)4;R(E)5;R(F)6;R(G)7;R(H)8;R(I)9
 * 3 : -----;R(A)1;R(B)2;R(C)3;R(D)4;R(E)5;R(F)6;R(G)7;R(H)8;R(I)9;R(A)1;R(B)2;R(C)3;R(D)4;R(E)5;R(F)6;R(G)7;R(H)8;R(I)9
 * 4 : -----;R(A)1;R(B)2;R(C)3;R(D)4;R(E)5;R(F)6;R(G)7;R(H)8;R(I)9;R(A)1;R(B)2;R(C)3;R(D)4;R(E)5;R(F)6;R(G)7;R(H)8;R(I)9
 * 5 : -----;R(A)1;R(B)2;R(C)3;R(D)4;R(E)5;R(F)6;R(G)7;R(H)8;R(I)9;R(A)1;R(B)2;R(C)3;R(D)4;R(E)5;R(F)6;R(G)7;R(H)8;R(I)9
 * 6 : -----;R(A)1;R(B)2;R(C)3;R(D)4;R(E)5;R(F)6;R(G)7;R(H)8;R(I)9;R(A)1;R(B)2;R(C)3;R(D)4;R(E)5;R(F)6;R(G)7;R(H)8;R(I)9
 */
public class TraceTestJvnProxy5 {

	private static String vars = "ABCDEFGHI" ;
	private static int procs = 6 ;
	
	public static void main(String[] args) {
		AttenteRunProcess sleeper = new AttenteRunProcess("sleeper", procs);
		sleeper.setTimeToSleep(50);
		TraceTestJvnHelperProxy tracer = new TraceTestJvnHelperProxy(procs, vars);
		tracer.addTraces(1, "W(A)1;W(B)2;W(C)3;W(D)4;W(E)5;W(F)6;W(G)7;W(H)8;W(I)9;-----;R(A)1;R(B)2;R(C)3;R(D)4;R(E)5;R(F)6;R(G)7;R(H)8;R(I)9");
		tracer.addTraces(2, "-----;R(A)1;R(B)2;R(C)3;R(D)4;R(E)5;R(F)6;R(G)7;R(H)8;R(I)9;R(A)1;R(B)2;R(C)3;R(D)4;R(E)5;R(F)6;R(G)7;R(H)8;R(I)9");
		tracer.addTraces(3, "-----;R(A)1;R(B)2;R(C)3;R(D)4;R(E)5;R(F)6;R(G)7;R(H)8;R(I)9;R(A)1;R(B)2;R(C)3;R(D)4;R(E)5;R(F)6;R(G)7;R(H)8;R(I)9");
		tracer.addTraces(4, "-----;R(A)1;R(B)2;R(C)3;R(D)4;R(E)5;R(F)6;R(G)7;R(H)8;R(I)9;R(A)1;R(B)2;R(C)3;R(D)4;R(E)5;R(F)6;R(G)7;R(H)8;R(I)9");
		tracer.addTraces(5, "-----;R(A)1;R(B)2;R(C)3;R(D)4;R(E)5;R(F)6;R(G)7;R(H)8;R(I)9;R(A)1;R(B)2;R(C)3;R(D)4;R(E)5;R(F)6;R(G)7;R(H)8;R(I)9");
		tracer.addTraces(6, "-----;R(A)1;R(B)2;R(C)3;R(D)4;R(E)5;R(F)6;R(G)7;R(H)8;R(I)9;R(A)1;R(B)2;R(C)3;R(D)4;R(E)5;R(F)6;R(G)7;R(H)8;R(I)9");
		sleeper.synchronisationParAttente();
		try {
			tracer.runTrace(sleeper.getMyId());
		} catch (JvnException e) {
			e.printStackTrace();
		}
		System.out.println("Fini");
	}
	
}
