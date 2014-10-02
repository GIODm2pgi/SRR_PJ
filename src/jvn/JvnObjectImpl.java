package jvn;

import java.io.Serializable;

import jvn.JvnCoordImpl.LOCK_STATE;

public class JvnObjectImpl implements JvnObject {
	private static final long serialVersionUID = -3809665057739668303L;

	private int jvnObjectId = -1 ;
	private Serializable objectJvn = null ;

	private LOCK_STATE lock_state = LOCK_STATE.RLT;
	public LOCK_STATE getLock_state() {
		return lock_state;
	}
	public void setLock_state(LOCK_STATE lock_state) {
		this.lock_state = lock_state;
	}

	public JvnObjectImpl(int jvnObjectId, Serializable o){
		this.jvnObjectId = jvnObjectId ;
		this.objectJvn = o ;
	}

	public void jvnLockRead() throws JvnException {
		this.objectJvn = JvnServerImpl.jvnGetServer().jvnLockRead(this.jvnObjectId);
	}

	public void jvnLockWrite() throws JvnException {
		this.objectJvn = JvnServerImpl.jvnGetServer().jvnLockWrite(this.jvnObjectId);
	}

	public void jvnUnLock() throws JvnException {
		// signal		

		JvnObject o = JvnServerImpl.jvnGetServer().getCacheJvnObject().get(this.jvnObjectId);
		if (o.getLock_state() == LOCK_STATE.RLT)
			o.setLock_state(LOCK_STATE.RLC);
		else if (o.getLock_state() == LOCK_STATE.WLT || o.getLock_state() == LOCK_STATE.RLT_WLC)
			o.setLock_state(LOCK_STATE.WLC);
		else
			o.setLock_state(LOCK_STATE.NL);
	}

	public int jvnGetObjectId() throws JvnException {
		return this.jvnObjectId;
	}

	public Serializable jvnGetObjectState() throws JvnException {
		return this.objectJvn;
	}

	public void jvnInvalidateReader() throws JvnException {
		
		if (lock_state == LOCK_STATE.RLT){
			// wait unlock
		}
		JvnObject o = JvnServerImpl.jvnGetServer().getCacheJvnObject().get(this.jvnObjectId);
		o.setLock_state(LOCK_STATE.NL);
	}

	public Serializable jvnInvalidateWriter() throws JvnException {
		if (lock_state == LOCK_STATE.WLT){
			// wait unlock
		}
		this.setLock_state(LOCK_STATE.NL);
		JvnServerImpl.jvnGetServer().getCacheJvnObject().put(this.jvnObjectId, this);
		return this.objectJvn;
	}

	public Serializable jvnInvalidateWriterForReader() throws JvnException {
		if (lock_state == LOCK_STATE.WLT){
			// wait unlock
		}
		this.setLock_state(LOCK_STATE.RLC);
		JvnServerImpl.jvnGetServer().getCacheJvnObject().put(this.jvnObjectId, this);
		return this.objectJvn;
	}
}
