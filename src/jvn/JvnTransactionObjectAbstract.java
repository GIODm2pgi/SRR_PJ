package jvn;


public abstract class JvnTransactionObjectAbstract implements JvnTransactionObject {
	private static final long serialVersionUID = 1L;

	public void start (){}
	
	public void commit (){}
	
	public void rollback (){}
	
	public void rollbackToMe (){}
}
