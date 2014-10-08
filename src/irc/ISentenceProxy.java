package irc;

import java.io.Serializable;

import jvn.annots.JvnReadMethod;
import jvn.annots.JvnWriteMethod;

public interface ISentenceProxy extends Serializable {
	
	@JvnWriteMethod
	public void write(String text); 
	
	@JvnReadMethod
	public String read() ;
}
