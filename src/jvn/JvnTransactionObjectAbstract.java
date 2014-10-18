package jvn;

/**
 * Interface for an object proxy used
 * by the service jvn.
 */
public abstract class JvnTransactionObjectAbstract implements JvnTransactionObject {
	
	/**
	 * UID of the class.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Start a transaction.
	 */
	public void start (){}

	/**
	 * Commit a transaction.
	 */
	public void commit (){}
	
	/**
	 * Rollback a transaction (get state from the coordinator).
	 */
	public void rollback (){}

	/**
	 * Rollback a transaction (set state to the coordinator).
	 */
	public void rollbackToMe (){}
	
}
