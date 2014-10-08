package jvn;

import java.io.Serializable;

public interface JvnTransactionObject extends Serializable {

	public void start ();
	
	public void commit ();
	
	public void rollback ();
	
}
