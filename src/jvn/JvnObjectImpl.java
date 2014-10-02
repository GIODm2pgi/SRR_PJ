package jvn;

import java.io.Serializable;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import jvn.JvnCoordImpl.LOCK_STATE;

public class JvnObjectImpl implements JvnObject {
	private static final long serialVersionUID = -3809665057739668303L;

	private int jvnObjectId = -1 ;
	private Serializable objectJvn = null ;

	public final Lock lock = new ReentrantLock();
	public final Condition lockCondition = lock.newCondition();
	
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
		JvnServerImpl.jvnGetServer().getCacheJvnObject().get(this.jvnObjectId).getlock().lock();
		try {
			JvnObject o = JvnServerImpl.jvnGetServer().getCacheJvnObject().get(this.jvnObjectId);
			if (o.getLock_state() == LOCK_STATE.RLT)
				o.setLock_state(LOCK_STATE.RLC);
			else if (o.getLock_state() == LOCK_STATE.WLT || o.getLock_state() == LOCK_STATE.RLT_WLC)
				o.setLock_state(LOCK_STATE.WLC);
			else
				o.setLock_state(LOCK_STATE.NL);
		} finally {
			JvnServerImpl.jvnGetServer().getCacheJvnObject().get(this.jvnObjectId).getlockCondition().signalAll();
			JvnServerImpl.jvnGetServer().getCacheJvnObject().get(this.jvnObjectId).getlock().unlock();
		}
	}

	public int jvnGetObjectId() throws JvnException {
		return this.jvnObjectId;
	}

	public Serializable jvnGetObjectState() throws JvnException {
		return this.objectJvn;
	}

	public void jvnInvalidateReader() throws JvnException {
		JvnServerImpl.jvnGetServer().getCacheJvnObject().get(this.jvnObjectId).getlock().lock();
		try {
			if (lock_state == LOCK_STATE.RLT){
				try {
					JvnServerImpl.jvnGetServer().getCacheJvnObject().get(this.jvnObjectId).getlockCondition().await();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			JvnObject o = JvnServerImpl.jvnGetServer().getCacheJvnObject().get(this.jvnObjectId);
			o.setLock_state(LOCK_STATE.NL); 
		} finally {
			JvnServerImpl.jvnGetServer().getCacheJvnObject().get(this.jvnObjectId).getlock().unlock();
		}
	}

	public Serializable jvnInvalidateWriter() throws JvnException {
		JvnServerImpl.jvnGetServer().getCacheJvnObject().get(this.jvnObjectId).getlock().lock();
		try {
			if (lock_state == LOCK_STATE.WLT){
				try {
					JvnServerImpl.jvnGetServer().getCacheJvnObject().get(this.jvnObjectId).getlockCondition().await();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			this.setLock_state(LOCK_STATE.NL);
			JvnServerImpl.jvnGetServer().getCacheJvnObject().put(this.jvnObjectId, this);
		} finally {
			JvnServerImpl.jvnGetServer().getCacheJvnObject().get(this.jvnObjectId).getlock().unlock();
		}
		return this.objectJvn;
	}

	public Serializable jvnInvalidateWriterForReader() throws JvnException {
		JvnServerImpl.jvnGetServer().getCacheJvnObject().get(this.jvnObjectId).getlock().lock();
		try {
			if (lock_state == LOCK_STATE.WLT){
				try {
					JvnServerImpl.jvnGetServer().getCacheJvnObject().get(this.jvnObjectId).getlockCondition().await();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			this.setLock_state(LOCK_STATE.RLC);
			JvnServerImpl.jvnGetServer().getCacheJvnObject().put(this.jvnObjectId, this);
		} finally {
			JvnServerImpl.jvnGetServer().getCacheJvnObject().get(this.jvnObjectId).getlock().unlock();
		}
		return this.objectJvn;
	}
	public Lock getlock() {
		return this.lock;
	}
	public Condition getlockCondition() {
		return this.lockCondition;
	}
}
