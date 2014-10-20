/***
 * JAVANAISE Implementation
 * JvnServerImpl class
 * Contact: 
 *
 * Authors: 
 */

package jvn;

import java.io.Serializable;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Implementation of the JvnServer.
 */
public class JvnServerImpl extends UnicastRemoteObject implements JvnLocalServer, JvnRemoteServer{

	/**
	 * UID of the class.
	 */
	private static final long serialVersionUID = -1834717182805714127L;

	/**
	 * A JVN server is managed as a singleton 
	 */
	private static JvnServerImpl js = null;

	/**
	 * The local store of the JvnObject.
	 */
	private HashMap<Integer, JvnObject> cacheJvnObject = null;

	/**
	 * Lists and tools to treat the invalidates.
	 */
	private Lock lockLookUp = null;
	private HashMap<Integer, Lock> listLockObject = null;
	private HashMap<Integer, Boolean> listBooleanInv = null;
	private HashMap<Integer, Boolean> listIntercepInv = null;

	/**
	 * The coordinator to interact.
	 */
	private JvnRemoteCoord coordinator;

	/**
	 * Default constructor
	 * @throws JvnException
	 **/
	private JvnServerImpl() throws Exception {
		super();

		// Treat security.
		System.setProperty("java.security.policy","file:./java.policy");
		if (System.getSecurityManager() == null) { 
			System.setSecurityManager(new SecurityManager()); 
		}

		// Get the coordinator with RMI.
		coordinator = jvnGetRemoteCoord();
		System.out.println ("Coordinator ready on server");

		// Create the local store.
		this.cacheJvnObject = new HashMap<Integer, JvnObject>();
		this.lockLookUp = new ReentrantLock();
		this.listLockObject = new HashMap<Integer, Lock>();
		this.listBooleanInv = new HashMap<Integer, Boolean>();
		this.listIntercepInv = new HashMap<Integer, Boolean>();
	}

	/**
	 * Get the coordinator from RMI.
	 * @return the instance of the coordinator.
	 * @throws RemoteException
	 * @throws NotBoundException
	 */
	private JvnRemoteCoord jvnGetRemoteCoord () throws RemoteException, NotBoundException {
		String host = "127.0.0.1";
		Registry registry = LocateRegistry.getRegistry(host);
		return (JvnRemoteCoord) registry.lookup("COORDINATOR");
	}

	/**
	 * Static method allowing an application to get a reference to 
	 * a JVN server instance
	 * @throws JvnException
	 */
	public static JvnServerImpl jvnGetServer() {
		if (js == null){
			try {
				js = new JvnServerImpl();
			} catch (Exception e) {
				System.err.println("Breakdown of the coordinator (next check in 5s)");
				try {Thread.sleep(5000);} catch (InterruptedException e1) {}
				return jvnGetServer();
			}
		}
		return js;
	}

	/**
	 * Get the local store of the JvnObject.
	 * @return the current local store of the JvnObject.
	 */
	public HashMap<Integer, JvnObject> getCacheJvnObject() {
		return cacheJvnObject;
	}

	/**
	 * Get the coordinator.
	 * @return the current coordinator.
	 */
	public JvnRemoteCoord getCoordinator() {
		return coordinator;
	}

	/**
	 * Wake up the server.
	 */
	public synchronized void jvnWakeUpServer () throws Exception {
		this.coordinator = jvnGetRemoteCoord();
		for (JvnObject o : cacheJvnObject.values()){
			o.jvnUnLock();
			o.setLock_state(JvnLOCK_STATE.NL);
		}
		notifyAll();
	}

	/**
	 * The JVN service is not used anymore
	 * @throws JvnException
	 */
	public void jvnTerminate() throws jvn.JvnException {
		try {
			this.getCoordinator().jvnTerminate(this);
		} catch (RemoteException e) {
			breakdown();
			jvnTerminate();
		}
	} 

	/**
	 * creation of a JVN object
	 * @param o : the JVN object state
	 * @throws JvnException
	 */
	public JvnObject jvnCreateObject(Serializable o) throws jvn.JvnException {
		// Lock.
		lockLookUp.lock();

		// Get new id.
		int jvnObjectId = 0;
		try {
			jvnObjectId = this.getCoordinator().jvnGetObjectId();
		} catch (RemoteException e) {
			breakdown();
			// Unlock.
			lockLookUp.unlock();
			return jvnCreateObject(o);
		}

		// Create the interceptor object.
		JvnObject toReturn = new JvnObjectImpl(jvnObjectId, o);

		// Add the object to the store.
		cacheJvnObject.put(jvnObjectId, toReturn);

		// Unlock.
		lockLookUp.unlock();

		// Return the jvn object.
		return toReturn ; 
	}

	/**
	 *  Associate a symbolic name with a JVN object
	 * @param jon : the JVN object name
	 * @param jo : the JVN object 
	 * @throws JvnException
	 **/
	public void jvnRegisterObject(String jon, JvnObject jo) throws jvn.JvnException {
		try {
			// Register.
			this.getCoordinator().jvnRegisterObject(jon, jo, this);

			// Lock.
			lockLookUp.lock();

			// Treatments.
			cacheJvnObject.put(jo.jvnGetObjectId(), jo);
			listLockObject.put(jo.jvnGetObjectId(), new ReentrantLock());
			listBooleanInv.put(jo.jvnGetObjectId(), false);
			listIntercepInv.put(jo.jvnGetObjectId(), false);

			// Unlock.
			lockLookUp.unlock();
		} catch (RemoteException e) {
			breakdown();
			jvnRegisterObject(jon, jo);
		}
	}

	/**
	 * Provide the reference of a JVN object beeing given its symbolic name
	 * @param jon : the JVN object name
	 * @return the JVN object 
	 * @throws JvnException
	 **/
	public  JvnObject jvnLookupObject(String jon) throws jvn.JvnException {

		// Lookup.
		JvnObject toReturn = null ;
		try {
			toReturn = this.getCoordinator().jvnLookupObject(jon, this);
		} catch (RemoteException e) {
			e.printStackTrace();
			breakdown();
			return jvnLookupObject(jon);
		}
		// Lock.
		lockLookUp.lock();

		if (toReturn != null){
			// Treatments.
			cacheJvnObject.put(toReturn.jvnGetObjectId(), toReturn);
			listLockObject.put(toReturn.jvnGetObjectId(), new ReentrantLock());
			listBooleanInv.put(toReturn.jvnGetObjectId(), false);
			listIntercepInv.put(toReturn.jvnGetObjectId(), false);
			cacheJvnObject.get(toReturn.jvnGetObjectId()).setLock_state(JvnLOCK_STATE.RLC);
			toReturn = cacheJvnObject.get(toReturn.jvnGetObjectId());
		}

		// Unlock.
		lockLookUp.unlock();

		return toReturn;
	}	

	/**
	 * Get a Read lock on a JVN object 
	 * @param joi : the JVN object identification
	 * @return the current JVN object state
	 * @throws  JvnException
	 **/
	public Serializable jvnLockRead(int joi) throws JvnException {
		// Lock.
		listLockObject.get(joi).lock();

		Serializable toReturn = null;
		if (cacheJvnObject.get(joi).getLock_state() == JvnLOCK_STATE.RLC){
			cacheJvnObject.get(joi).setLock_state(JvnLOCK_STATE.RLT);
			toReturn = cacheJvnObject.get(joi).jvnGetObjectState();
		}
		else if (cacheJvnObject.get(joi).getLock_state() == JvnLOCK_STATE.WLC){
			cacheJvnObject.get(joi).setLock_state(JvnLOCK_STATE.RLT_WLC);
			toReturn = cacheJvnObject.get(joi).jvnGetObjectState();
		}
		else if (cacheJvnObject.get(joi).getLock_state() == JvnLOCK_STATE.NL){
			listIntercepInv.put(joi,true);
			// Unlock.
			listLockObject.get(joi).unlock();
			try {
				toReturn = this.getCoordinator().jvnLockRead(joi, this);
			} catch (RemoteException e) {
				breakdown();
				return jvnLockRead(joi);
			}
			// Lock.
			listLockObject.get(joi).lock();
			listIntercepInv.put(joi,false);
			cacheJvnObject.get(joi).setLock_state(JvnLOCK_STATE.RLT);
			cacheJvnObject.get(joi).setObjectState(toReturn);
			toReturn = cacheJvnObject.get(joi).jvnGetObjectState();
			synchronized (listBooleanInv.get(joi)) {
				listBooleanInv.get(joi).notifyAll();
				listBooleanInv.put(joi,false);
			}
		}

		// Unlock.
		listLockObject.get(joi).unlock();

		return toReturn;
	}	

	/**
	 * Get a Write lock on a JVN object 
	 * @param joi : the JVN object identification
	 * @return the current JVN object state
	 * @throws  JvnException
	 **/
	public Serializable jvnLockWrite(int joi) throws JvnException {
		// Lock.
		listLockObject.get(joi).lock();

		Serializable toReturn = null;
		if (cacheJvnObject.get(joi).getLock_state() == JvnLOCK_STATE.WLC){
			cacheJvnObject.get(joi).setLock_state(JvnLOCK_STATE.WLT);
			toReturn = cacheJvnObject.get(joi).jvnGetObjectState();
		}
		else{
			listIntercepInv.put(joi,true);
			// Unlock.
			listLockObject.get(joi).unlock();
			try {
				toReturn = this.getCoordinator().jvnLockWrite(joi, this);
			} catch (RemoteException e) {
				breakdown();
				return jvnLockWrite(joi);
			}
			// Lock.
			listLockObject.get(joi).lock();
			listIntercepInv.put(joi,false);
			cacheJvnObject.get(joi).setLock_state(JvnLOCK_STATE.WLT);
			cacheJvnObject.get(joi).setObjectState(toReturn);
			toReturn = cacheJvnObject.get(joi).jvnGetObjectState();
			synchronized (listBooleanInv.get(joi)) {
				listBooleanInv.get(joi).notifyAll();
				listBooleanInv.put(joi,false);
			}
		}

		// Unlock.
		listLockObject.get(joi).unlock();

		return toReturn;
	}	

	/**
	 * Invalidate the Read lock of the JVN object identified by id 
	 * called by the JvnCoord
	 * @param joi : the JVN object id
	 * @return void
	 * @throws java.rmi.RemoteException,JvnException
	 **/
	public void jvnInvalidateReader(int joi) throws java.rmi.RemoteException, jvn.JvnException {
		// Lock.
		lockLookUp.lock();
		if (listLockObject.containsKey(joi)){

			synchronized (listBooleanInv.get(joi)) {			
				if (listBooleanInv.get(joi))
					try {
						// Unlock.
						lockLookUp.unlock();
						listBooleanInv.get(joi).wait();
						// Lock.
						lockLookUp.lock();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
			}

			// Lock.
			listLockObject.get(joi).lock();
			if (listIntercepInv.get(joi)){
			}
			else
				this.cacheJvnObject.get(joi).jvnInvalidateReader();
			// Unlock.
			listLockObject.get(joi).unlock();
		}
		// Unlock.
		lockLookUp.unlock();
	}

	/**
	 * Invalidate the Write lock of the JVN object identified by id 
	 * @param joi : the JVN object id
	 * @return the current JVN object state
	 * @throws java.rmi.RemoteException,JvnException
	 **/
	public Serializable jvnInvalidateWriter(int joi) throws java.rmi.RemoteException,jvn.JvnException { 
		// Lock.
		lockLookUp.lock();

		synchronized (listBooleanInv.get(joi)) {			
			if (listBooleanInv.get(joi))
				try {
					// Unlock.
					lockLookUp.unlock();
					listBooleanInv.get(joi).wait();
					// Lock.
					lockLookUp.lock();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
		}

		// Lock.
		listLockObject.get(joi).lock();
		Serializable toReturn = null;
		if (listIntercepInv.get(joi)){
			toReturn = this.cacheJvnObject.get(joi).jvnGetObjectState();
		}
		else
			toReturn = this.cacheJvnObject.get(joi).jvnInvalidateWriter();
		// Unlock.
		listLockObject.get(joi).unlock();
		// Unlock.
		lockLookUp.unlock();
		return toReturn;
	}

	/**
	 * Reduce the Write lock of the JVN object identified by id 
	 * @param joi : the JVN object id
	 * @return the current JVN object state
	 * @throws java.rmi.RemoteException,JvnException
	 **/
	public Serializable jvnInvalidateWriterForReader(int joi) throws java.rmi.RemoteException,jvn.JvnException { 
		// Lock.
		lockLookUp.lock();

		synchronized (listBooleanInv.get(joi)) {			
			if (listBooleanInv.get(joi))
				try {
					// Unlock.
					lockLookUp.unlock();
					listBooleanInv.get(joi).wait();
					// Lock.
					lockLookUp.lock();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
		}

		// Lock.
		listLockObject.get(joi).lock();
		Serializable toReturn = null;
		if (listIntercepInv.get(joi)){
			toReturn = this.cacheJvnObject.get(joi).jvnGetObjectState();
		}
		else
			toReturn = this.cacheJvnObject.get(joi).jvnInvalidateWriterForReader();
		// Unlock.
		listLockObject.get(joi).unlock();
		// Unlock.
		lockLookUp.unlock();
		return toReturn;
	}

	/**
	 * End of intercept.
	 */
	public void deIntercept(int joi) throws RemoteException, JvnException {		
		synchronized (listBooleanInv.get(joi)) {
			listBooleanInv.put(joi,true);
		}
	}

	/**
	 * The coordinator is breakdown.
	 */
	private synchronized void breakdown (){
		System.err.println("Breakdown of the coordinator");
		try {
			wait();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}


