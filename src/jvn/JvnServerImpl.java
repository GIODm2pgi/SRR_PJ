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
	private HashMap<Integer, JvnObject> cacheJvnObject = null ;

	private final int R = 0;
	private final int W = 1;
	private final int WFR = 2;

	private HashMap<Integer, Integer> listNeedInvalid = null ;
	private Boolean intercepInv = false;

	/**
	 * The coordinator to interact.
	 */
	private JvnRemoteCoord coordinator;

	/**
	 * Get the local store of the JvnObject.
	 * @return the current local store of the JvnObject.
	 */
	public HashMap<Integer, JvnObject> getCacheJvnObject() {
		return cacheJvnObject;
	}

	private Lock lockLookUp = new ReentrantLock();
	private HashMap<Integer, Lock> listLockObject = null ;

	/**
	 * Get the coordinator.
	 * @return the current coordinator.
	 */
	public JvnRemoteCoord getCoordinator() {
		return coordinator;
	}

	public void jvnWakeUpServer () throws Exception {
		this.coordinator = jvnGetRemoteCoord();
		for (JvnObject o : cacheJvnObject.values()){
			o.jvnUnLock();
			o.setLock_state(JvnLOCK_STATE.NL);
		}
	}

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
		this.cacheJvnObject = new HashMap<Integer, JvnObject>() ;
		this.listNeedInvalid = new HashMap<Integer, Integer>() ;
		this.listLockObject = new HashMap<Integer, Lock>();
	}

	private JvnRemoteCoord jvnGetRemoteCoord () throws RemoteException, NotBoundException {
		String host = "127.0.0.1";
		Registry registry = LocateRegistry.getRegistry(host);
		return (JvnRemoteCoord) registry.lookup("COORDINATOR");
	}

	/**
	 * Static method allowing an application to get a reference to 
	 * a JVN server instance
	 * @throws JvnException
	 **/
	public static JvnServerImpl jvnGetServer() {
		if (js == null){
			try {
				js = new JvnServerImpl();
			} catch (Exception e) {
				System.out.println("Breakdown of the coordinator");
				e.printStackTrace();
				return null;
			}
		}
		return js;
	}

	/**
	 * The JVN service is not used anymore
	 * @throws JvnException
	 **/
	public  void jvnTerminate() throws jvn.JvnException {
		try {
			this.getCoordinator().jvnTerminate(this);
		} catch (RemoteException e) {
			System.out.println("Breakdown of the coordinator");
			e.printStackTrace();
		}
	} 

	/**
	 * creation of a JVN object
	 * @param o : the JVN object state
	 * @throws JvnException
	 **/
	public  JvnObject jvnCreateObject(Serializable o) throws jvn.JvnException { 
		lockLookUp.lock();
		int jvnObjectId = 0;

		try {
			jvnObjectId = this.getCoordinator().jvnGetObjectId();
		} catch (RemoteException e) {
			e.printStackTrace();
		}

		JvnObject toReturn = new JvnObjectImpl(jvnObjectId, o);
		cacheJvnObject.put(jvnObjectId, toReturn);

		lockLookUp.unlock();
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
			this.getCoordinator().jvnRegisterObject(jon, jo, this);
			lockLookUp.lock();
			cacheJvnObject.put(jo.jvnGetObjectId(), jo);
			listLockObject.put(jo.jvnGetObjectId(), new ReentrantLock());
			lockLookUp.unlock();
		} catch (Exception e) {
			System.out.println("HERE");
			this.jvnLookupObject(jon);
		}
		/* Version avant tantative de debuggage du multiple register
		 lockLookUp.lock();
		try {
			this.getCoordinator().jvnRegisterObject(jon, jo, this);
			cacheJvnObject.put(jo.jvnGetObjectId(), jo);
			listLockObject.put(jo.jvnGetObjectId(), new ReentrantLock());
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		lockLookUp.unlock();
		 */
	}

	/**
	 * Provide the reference of a JVN object beeing given its symbolic name
	 * @param jon : the JVN object name
	 * @return the JVN object 
	 * @throws JvnException
	 **/
	public  JvnObject jvnLookupObject(String jon) throws jvn.JvnException {
		lockLookUp.lock();

		JvnObject toReturn = null ;

		lockLookUp.unlock();
		try {
			System.out.println("LOOCK UP " + jon + js.toString().split("endpoint")[1].split("\\(")[0]);
			toReturn = this.getCoordinator().jvnLookupObject(jon, this);
			System.out.println("AP");
		} catch (RemoteException e) {
			System.out.println("Breakdown of the coordinator");
			e.printStackTrace();
			return toReturn;
		}
		lockLookUp.lock();

		if (toReturn != null){
			cacheJvnObject.put(toReturn.jvnGetObjectId(), toReturn);
			listLockObject.put(toReturn.jvnGetObjectId(), new ReentrantLock());
			cacheJvnObject.get(toReturn.jvnGetObjectId()).setLock_state(JvnLOCK_STATE.RLC);
			toReturn = cacheJvnObject.get(toReturn.jvnGetObjectId());
		}

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
			intercepInv = true;
			listLockObject.get(joi).unlock();
			try {
				toReturn = this.getCoordinator().jvnLockRead(joi, this);
			} catch (RemoteException e) {
				System.out.println("Breakdown of the coordinator");
				return cacheJvnObject.get(joi).jvnGetObjectState();
			}
			listLockObject.get(joi).lock();
			intercepInv = false;
			cacheJvnObject.get(joi).setLock_state(JvnLOCK_STATE.RLT);
			cacheJvnObject.get(joi).setObjectState(toReturn);
			toReturn = cacheJvnObject.get(joi).jvnGetObjectState();
		}

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
		listLockObject.get(joi).lock();

		Serializable toReturn = null;
		if (cacheJvnObject.get(joi).getLock_state() == JvnLOCK_STATE.WLC){
			cacheJvnObject.get(joi).setLock_state(JvnLOCK_STATE.WLT);
			toReturn = cacheJvnObject.get(joi).jvnGetObjectState();
		}
		else{
			System.out.println("Unlock W");
			intercepInv = true;
			listLockObject.get(joi).unlock();
			try {
				toReturn = this.getCoordinator().jvnLockWrite(joi, this);
			} catch (RemoteException e) {
				System.out.println("Breakdown of the coordinator");
				return cacheJvnObject.get(joi).jvnGetObjectState();
			}
			listLockObject.get(joi).lock();
			intercepInv = false;
			System.out.println("Lock W");
			cacheJvnObject.get(joi).setLock_state(JvnLOCK_STATE.WLT);
			cacheJvnObject.get(joi).setObjectState(toReturn);
			toReturn = cacheJvnObject.get(joi).jvnGetObjectState();
		}

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
	public void jvnInvalidateReader(int joi) throws java.rmi.RemoteException,jvn.JvnException {
		lockLookUp.lock();
		if (listLockObject.containsKey(joi)){
			listLockObject.get(joi).lock();
			if (intercepInv){
				listNeedInvalid.put(joi, R);
			}
			else
				this.cacheJvnObject.get(joi).jvnInvalidateReader();
			listLockObject.get(joi).unlock();
		}
		lockLookUp.unlock();
	}

	/**
	 * Invalidate the Write lock of the JVN object identified by id 
	 * @param joi : the JVN object id
	 * @return the current JVN object state
	 * @throws java.rmi.RemoteException,JvnException
	 **/
	public Serializable jvnInvalidateWriter(int joi) throws java.rmi.RemoteException,jvn.JvnException { 
		lockLookUp.lock();
		listLockObject.get(joi).lock();
		Serializable toReturn = null;
		if (intercepInv){
			listNeedInvalid.put(joi, W);
			toReturn = this.cacheJvnObject.get(joi).jvnGetObjectState();
		}
		else
			toReturn = this.cacheJvnObject.get(joi).jvnInvalidateWriter();
		listLockObject.get(joi).unlock();
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
		lockLookUp.lock();
		listLockObject.get(joi).lock();
		Serializable toReturn = null;
		if (intercepInv){
			listNeedInvalid.put(joi, WFR);
			toReturn = this.cacheJvnObject.get(joi).jvnGetObjectState();
		}
		else
			toReturn = this.cacheJvnObject.get(joi).jvnInvalidateWriterForReader();
		listLockObject.get(joi).unlock();
		lockLookUp.unlock();
		return toReturn;
	}

	public void callBackInv (int joi) throws java.rmi.RemoteException,jvn.JvnException {
		if (listNeedInvalid.containsKey(joi)){
			switch (listNeedInvalid.remove(joi)){
			case R : this.cacheJvnObject.get(joi).jvnInvalidateReader(); break;
			case W : coordinator.jvnUpdate(joi, this.cacheJvnObject.get(joi).jvnInvalidateWriter()); break;
			case WFR : coordinator.jvnUpdate(joi, this.cacheJvnObject.get(joi).jvnInvalidateWriterForReader()); break;
			}
		}
	}
}


