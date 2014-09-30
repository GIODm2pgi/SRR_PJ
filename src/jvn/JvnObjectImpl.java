package jvn;

import java.io.Serializable;
import java.rmi.RemoteException;

import jvn.JvnCoordImpl.LOCK_STATE;

public class JvnObjectImpl implements JvnObject {
	private static final long serialVersionUID = -3809665057739668303L;
	
	private int jvnObjectId = -1 ;
	private Serializable objectJvn = null ;
	
	private LOCK_STATE lock_state = LOCK_STATE.NL;
	
	public JvnObjectImpl(int jvnObjectId, Serializable o){
		this.jvnObjectId = jvnObjectId ;
		this.objectJvn = o ;
	}

	public void jvnLockRead() throws JvnException {
		if (lock_state == LOCK_STATE.WLC)
			lock_state=LOCK_STATE.RLT_WLC;
		else
			lock_state=LOCK_STATE.RLT;
		this.objectJvn = JvnServerImpl.jvnGetServer().jvnLockRead(this.jvnObjectId);
	}

	public void jvnLockWrite() throws JvnException {
		lock_state=LOCK_STATE.WLT;
		this.objectJvn = JvnServerImpl.jvnGetServer().jvnLockWrite(this.jvnObjectId);
	}

	public void jvnUnLock() throws JvnException {
		if (lock_state == LOCK_STATE.RLT)
			lock_state=LOCK_STATE.RLC;
		else
			lock_state=LOCK_STATE.WLC;
		
		try {
			JvnServerImpl.jvnGetServer().jvnUpdate(this);
		} catch (RemoteException e) {
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
