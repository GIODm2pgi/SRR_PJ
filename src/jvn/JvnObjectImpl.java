package jvn;

import java.io.Serializable;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Realization of the JvnObject.
 */
public class JvnObjectImpl implements JvnObject {

	/**
	 * UID of the class.
	 */
	private static final long serialVersionUID = -3809665057739668303L;

	/**
	 * The id of the JvnObject.
	 */
	private int jvnObjectId = -1 ;

	/**
	 * The real object of the JvnObject.
	 */
	private Serializable objectJvn = null ;

	/**
	 * The real lock of the JvnObject.
	 */
	public final Lock lock = new ReentrantLock();

	/**
	 * The condition of the real lock of the JvnObject.
	 */
	public final Condition lockCondition = lock.newCondition();

	/**
	 * The lock state of the JvnObject.
	 */
	private JvnLOCK_STATE lock_state = JvnLOCK_STATE.RLT;

	/**
	 * Get the current lock state of the JvnObject.
	 */
	public JvnLOCK_STATE getLock_state() {
		return lock_state;
	}

	/**
	 * Set the current lock state of the JvnObject.
	 */
	public void setLock_state(JvnLOCK_STATE lock_state) {
		this.lock_state = lock_state;
	}

	/**
	 * Default constructor : Instantiate the object.
	 * @param jvnObjectId : the id of the JvnObject.
	 * @param o : the real object of the JvnObject.
	 */
	public JvnObjectImpl(int jvnObjectId, Serializable o){
		this.jvnObjectId = jvnObjectId ;
		this.objectJvn = o ;
	}

	/**
	 * Ask for the read lock.
	 * Just transfer the request at the server.
	 */
	public void jvnLockRead() throws JvnException {
		this.objectJvn = JvnServerImpl.jvnGetServer().jvnLockRead(this.jvnObjectId);
	}

	/**
	 * Ask for the write lock.
	 * Just transfer the request at the server.
	 */
	public void jvnLockWrite() throws JvnException {
		this.objectJvn = JvnServerImpl.jvnGetServer().jvnLockWrite(this.jvnObjectId);
	}

	/**
	 * Ask for the unlock.
	 * Just transfer the request at the server.
	 */
	public void jvnUnLock() throws JvnException {
		// Lock
		JvnServerImpl.jvnGetServer().getCacheJvnObject().get(this.jvnObjectId).getlock().lock();
		try {
			JvnObject o = JvnServerImpl.jvnGetServer().getCacheJvnObject().get(this.jvnObjectId);
			switch (o.getLock_state()) {

			case RLT:
				o.setLock_state(JvnLOCK_STATE.RLC);
				break;

			case RLT_WLC:
			case WLT:
				o.setLock_state(JvnLOCK_STATE.WLC);
				break;

			default:
				o.setLock_state(JvnLOCK_STATE.NL);
				break;

			}
		} finally {
			// Unlock an condition.
			JvnServerImpl.jvnGetServer().getCacheJvnObject().get(this.jvnObjectId).getlockCondition().signalAll();
			JvnServerImpl.jvnGetServer().getCacheJvnObject().get(this.jvnObjectId).getlock().unlock();
		}
	}

	/**
	 * Get the current object Id.
	 */
	public int jvnGetObjectId() throws JvnException {
		return this.jvnObjectId;
	}

	/**
	 * Get the current real object.
	 */
	public Serializable jvnGetObjectState() throws JvnException {
		return this.objectJvn;
	}

	/**
	 * Request from server : try unlock read.
	 */
	public void jvnInvalidateReader() throws JvnException {
		// Lock
		JvnServerImpl.jvnGetServer().getCacheJvnObject().get(this.jvnObjectId).getlock().lock();
		try {
			// Case Wait
			if (lock_state == JvnLOCK_STATE.RLT){
				try {
					// Wait
					JvnServerImpl.jvnGetServer().getCacheJvnObject().get(this.jvnObjectId).getlockCondition().await();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			// Unlock the reader.
			JvnObject o = JvnServerImpl.jvnGetServer().getCacheJvnObject().get(this.jvnObjectId);
			o.setLock_state(JvnLOCK_STATE.NL); 
		} finally {
			// Unlock
			JvnServerImpl.jvnGetServer().getCacheJvnObject().get(this.jvnObjectId).getlock().unlock();
		}
	}

	/**
	 * Request from server : try unlock write.
	 */
	public Serializable jvnInvalidateWriter() throws JvnException {
		// Lock
		JvnServerImpl.jvnGetServer().getCacheJvnObject().get(this.jvnObjectId).getlock().lock();
		try {
			// Case Wait
			if (lock_state == JvnLOCK_STATE.WLT){
				try {
					// Wait
					JvnServerImpl.jvnGetServer().getCacheJvnObject().get(this.jvnObjectId).getlockCondition().await();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			// Unlock the writer.
			this.setLock_state(JvnLOCK_STATE.NL);
			JvnServerImpl.jvnGetServer().getCacheJvnObject().put(this.jvnObjectId, this);
		} finally {
			// Unlock
			JvnServerImpl.jvnGetServer().getCacheJvnObject().get(this.jvnObjectId).getlock().unlock();
		}
		return this.objectJvn;
	}

	/**
	 * Request from server : try unlock write for read.
	 */
	public Serializable jvnInvalidateWriterForReader() throws JvnException {
		// Lock
		JvnServerImpl.jvnGetServer().getCacheJvnObject().get(this.jvnObjectId).getlock().lock();
		try {
			// Case Wait
			if (lock_state == JvnLOCK_STATE.WLT){
				try {
					// Wait
					JvnServerImpl.jvnGetServer().getCacheJvnObject().get(this.jvnObjectId).getlockCondition().await();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			// Unlock the writer for reader.
			this.setLock_state(JvnLOCK_STATE.RLC);
			JvnServerImpl.jvnGetServer().getCacheJvnObject().put(this.jvnObjectId, this);
		} finally {
			// Unlock
			JvnServerImpl.jvnGetServer().getCacheJvnObject().get(this.jvnObjectId).getlock().unlock();
		}
		return this.objectJvn;
	}

	/**
	 * Get the current real lock.
	 */
	public Lock getlock() {
		return this.lock;
	}

	/**
	 * Get the current condition of the real lock.
	 */
	public Condition getlockCondition() {
		return this.lockCondition;
	}

}
