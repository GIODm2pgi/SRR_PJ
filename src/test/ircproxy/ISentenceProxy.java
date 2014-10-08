package test.ircproxy;

import jvn.JvnTransactionObject;
import jvn.annots.JvnReadMethod;
import jvn.annots.JvnWriteMethod;

public interface ISentenceProxy extends JvnTransactionObject {
	
	@JvnWriteMethod
	public void write(String text);
	
	@JvnWriteMethod
	public void duplicate();
	
	@JvnReadMethod
	public String read() ;
	
}
