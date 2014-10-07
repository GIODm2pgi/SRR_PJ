package irc;

import java.io.Serializable;

import jvn.JvnReadMethod;
import jvn.JvnWriteMethod;

public interface ISentenceProxy extends Serializable {
	@JvnWriteMethod
	public void write(String text); 
	
	@JvnReadMethod
	public String read() ;
}
