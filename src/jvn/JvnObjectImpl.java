package jvn;

import java.io.Serializable;
import java.rmi.RemoteException;

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
		this.objectJvn = JvnServerImpl.jvnGetServer().jvnLockRead(this.jvnObjectId);
	}

	public void jvnLockWrite() throws JvnException {
		// TODO Auto-generated method stub
		this.objectJvn = JvnServerImpl.jvnGetServer().jvnLockWrite(this.jvnObjectId);
	}

	public void jvnUnLock() throws JvnException {
		// TODO Auto-generated method stub
		try {
			JvnServerImpl.jvnGetServer().jvnUpdate(this);
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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
