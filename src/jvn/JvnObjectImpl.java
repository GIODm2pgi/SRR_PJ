package jvn;

import java.io.Serializable;

public class JvnObjectImpl implements JvnObject {
	private static final long serialVersionUID = -3809665057739668303L;
	
	private int jvnObjectId = -1 ;
	private Serializable objectJvn = null ;
	
	public JvnObjectImpl(int jvnObjectId, Serializable o){
		this.jvnObjectId = jvnObjectId ;
		this.objectJvn = o ;
	}

	public void jvnLockRead() throws JvnException {
		// TODO Auto-generated method stub
		
	}

	public void jvnLockWrite() throws JvnException {
		// TODO Auto-generated method stub
		
	}

	public void jvnUnLock() throws JvnException {
		// TODO Auto-generated method stub
		
	}

	public int jvnGetObjectId() throws JvnException {
		return this.jvnObjectId;
	}

	public Serializable jvnGetObjectState() throws JvnException {
		return this.objectJvn;
	}

	public void jvnInvalidateReader() throws JvnException {
		// TODO Auto-generated method stub
		
	}

	public Serializable jvnInvalidateWriter() throws JvnException {
		// TODO Auto-generated method stub
		return null;
	}

	public Serializable jvnInvalidateWriterForReader() throws JvnException {
		// TODO Auto-generated method stub
		return null;
	}

}
