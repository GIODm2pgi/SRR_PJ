package jvn;

import java.io.Serializable;

/**
 * Interface for an object proxy used
 * by the service jvn.
 */
public interface JvnTransactionObject extends Serializable {

	/**
	 * Start a transaction.
	 */
	public void start ();
	
	/**
	 * Commit a transaction.
	 */
	public void commit ();
	
	/**
	 * Rollback a transaction (get state from the coordinator).
	 */
	public void rollback ();

	/**
	 * Rollback a transaction (set state to the coordinator).
	 */
	public void rollbackToMe ();
	
}
