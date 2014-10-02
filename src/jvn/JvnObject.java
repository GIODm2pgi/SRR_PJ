/***
 * JAVANAISE API
 * Contact: 
 *
 * Authors: 
 */

package jvn;

import java.io.*;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;


/**
 * Interface of a JVN object. 
 * The serializable property is required in order to be able to transfer 
 * a reference to a JVN object remotely
 */

public interface JvnObject extends Serializable {

	/**
	 * Get a Read lock on the object 
	 * @throws JvnException
	 **/
	public void jvnLockRead() throws jvn.JvnException; 

	/**
	 * Get a Write lock on the object 
	 * @throws JvnException
	 **/
	public void jvnLockWrite() throws jvn.JvnException; 

	/**
	 * Unlock  the object 
	 * @throws JvnException
	 **/
	public void jvnUnLock() throws jvn.JvnException; 


	/**
	 * Get the object identification
	 * @throws JvnException
	 **/
	public int jvnGetObjectId() throws jvn.JvnException; 

	/**
	 * Get the object state
	 * @throws JvnException
	 **/
	public Serializable jvnGetObjectState() throws jvn.JvnException; 


	/**
	 * Invalidate the Read lock of the JVN object 
	 * @throws JvnException
	 **/
	public void jvnInvalidateReader() throws jvn.JvnException;

	/**
	 * Invalidate the Write lock of the JVN object  
	 * @return the current JVN object state
	 * @throws JvnException
	 **/
	public Serializable jvnInvalidateWriter() throws jvn.JvnException;

	/**
	 * Reduce the Write lock of the JVN object 
	 * @return the current JVN object state
	 * @throws JvnException
	 **/
	public Serializable jvnInvalidateWriterForReader() throws jvn.JvnException;	

	/**
	 * Get the lock state.
	 * @return the current JVN object lock state. 
	 */
	public JvnLOCK_STATE getLock_state();

	/**
	 * Set the current JVN object lock state.
	 * @param lock_state : the new JVN object lock state.
	 */
	public void setLock_state(JvnLOCK_STATE lock_state);

	/**
	 * Get the real lock of the current JVN object.
	 * @return the real lock of the current JVN object.
	 */
	public Lock getlock();

	/**
	 * Get the condition of the real lock 
	 * of the current JVN object.
	 * @return the condition of the real lock.
	 */
	public Condition getlockCondition();

}
