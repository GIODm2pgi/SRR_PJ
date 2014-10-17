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
	private JvnLOCK_STATE lock_state = JvnLOCK_STATE.WLT;

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
			switch (JvnServerImpl.jvnGetServer().getCacheJvnObject().get(this.jvnObjectId).getLock_state()) {

			case RLT:
				JvnServerImpl.jvnGetServer().getCacheJvnObject().get(this.jvnObjectId).setLock_state(JvnLOCK_STATE.RLC);
				break;

			case RLT_WLC:
			case WLT:
				JvnServerImpl.jvnGetServer().getCacheJvnObject().get(this.jvnObjectId).setLock_state(JvnLOCK_STATE.WLC);
				break;

			default:
				JvnServerImpl.jvnGetServer().getCacheJvnObject().get(this.jvnObjectId).setLock_state(JvnLOCK_STATE.NL);
				break;

			}
		} finally {
			// Unlock an condition.
			JvnServerImpl.jvnGetServer().getCacheJvnObject().get(this.jvnObjectId).getlockCondition().signal();
			JvnServerImpl.jvnGetServer().getCacheJvnObject().get(this.jvnObjectId).getlock().unlock();
			
			JvnServerImpl.jvnGetServer().callBackInv(this.jvnObjectId);
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
			while (lock_state == JvnLOCK_STATE.RLT){
				try {
					// Wait
					System.out.println("wait");
					JvnServerImpl.jvnGetServer().getCacheJvnObject().get(this.jvnObjectId).getlockCondition().await();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			// Unlock the reader.
			JvnServerImpl.jvnGetServer().getCacheJvnObject().get(this.jvnObjectId).setLock_state(JvnLOCK_STATE.NL);
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
			while (lock_state == JvnLOCK_STATE.WLT || lock_state == JvnLOCK_STATE.RLT_WLC){
				try {
					// Wait
					System.out.println("wait");
					JvnServerImpl.jvnGetServer().getCacheJvnObject().get(this.jvnObjectId).getlockCondition().await();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			// Unlock the writer.
			JvnServerImpl.jvnGetServer().getCacheJvnObject().get(this.jvnObjectId).setLock_state(JvnLOCK_STATE.NL);
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
			while (lock_state == JvnLOCK_STATE.WLT || lock_state == JvnLOCK_STATE.RLT_WLC){
				try {
					// Wait
					System.out.println("wait");
					JvnServerImpl.jvnGetServer().getCacheJvnObject().get(this.jvnObjectId).getlockCondition().await();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			// Unlock the writer for reader.
			JvnServerImpl.jvnGetServer().getCacheJvnObject().get(this.jvnObjectId).setLock_state(JvnLOCK_STATE.RLC);
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
	
	/**
	 * Set the current state of the JvnObject.
	 */
	public void setObjectState(Serializable s){
		this.objectJvn=s;
	}
}
