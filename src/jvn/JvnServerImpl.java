/***
 * JAVANAISE Implementation
 * JvnServerImpl class
 * Contact: 
 *
 * Authors: 
 */

package jvn;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;




public class JvnServerImpl extends UnicastRemoteObject implements JvnLocalServer, JvnRemoteServer{
	private static final long serialVersionUID = -1834717182805714127L;

	// A JVN server is managed as a singleton 
	private static JvnServerImpl js = null;

	private HashMap<Integer, JvnObject> cacheJvnObject = null ;
	public HashMap<Integer, JvnObject> getCacheJvnObject() {
		return cacheJvnObject;
	}

	private JvnRemoteCoord coordinator;
	public JvnRemoteCoord getCoordinator() {
		return coordinator;
	}

	/**
	 * Default constructor
	 * @throws JvnException
	 **/
	private JvnServerImpl() throws Exception {
		super();

		System.setProperty("java.security.policy","file:./java.policy");
		if (System.getSecurityManager() == null) { System.setSecurityManager(new SecurityManager()); }
		String host = "localhost";
		Registry registry = LocateRegistry.getRegistry(host);
		coordinator = (JvnRemoteCoord) registry.lookup("COORDINATOR");
		System.out.println ("Coordinator ready on server");

		this.cacheJvnObject = new HashMap<Integer, JvnObject>() ;
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
			e.printStackTrace();
		}
	} 

	/**
	 * creation of a JVN object
	 * @param o : the JVN object state
	 * @throws JvnException
	 **/
	public  JvnObject jvnCreateObject(Serializable o) throws jvn.JvnException { 
		int jvnObjectId = 0;

		try {
			jvnObjectId = this.getCoordinator().jvnGetObjectId();
		} catch (RemoteException e) {
			e.printStackTrace();
		}

		JvnObject toReturn = new JvnObjectImpl(jvnObjectId, o);

		this.cacheJvnObject.put(jvnObjectId, toReturn);

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

			cacheJvnObject.put(jo.jvnGetObjectId(), jo);

		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Provide the reference of a JVN object beeing given its symbolic name
	 * @param jon : the JVN object name
	 * @return the JVN object 
	 * @throws JvnException
	 **/
	public  JvnObject jvnLookupObject(String jon) throws jvn.JvnException {
		JvnObject toReturn = null ;
		try {
			toReturn = this.getCoordinator().jvnLookupObject(jon, this);

			if (toReturn != null){
				cacheJvnObject.put(toReturn.jvnGetObjectId(), toReturn);
				cacheJvnObject.get(toReturn.jvnGetObjectId()).jvnUnLock();
				toReturn = cacheJvnObject.get(toReturn.jvnGetObjectId());
			}
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		return toReturn;
	}	

	/**
	 * Get a Read lock on a JVN object 
	 * @param joi : the JVN object identification
	 * @return the current JVN object state
	 * @throws  JvnException
	 **/
	public Serializable jvnLockRead(int joi) throws JvnException {
		Serializable toReturn = null;
		JvnObject o = cacheJvnObject.get(joi);	
		try {
			if (o.getLock_state() == JvnLOCK_STATE.RLC){
				o.setLock_state(JvnLOCK_STATE.RLT);
				toReturn = cacheJvnObject.get(joi).jvnGetObjectState();
			}
			else if (o.getLock_state() == JvnLOCK_STATE.WLC){
				o.setLock_state(JvnLOCK_STATE.RLT_WLC);
				toReturn = cacheJvnObject.get(joi).jvnGetObjectState();
			}
			else if (o.getLock_state() == JvnLOCK_STATE.NL){
				toReturn = this.getCoordinator().jvnLockRead(joi, this);
				JvnObject updated = new JvnObjectImpl(joi, toReturn);
				updated.setLock_state(JvnLOCK_STATE.RLT);
				cacheJvnObject.put(joi, updated);
				toReturn = cacheJvnObject.get(joi).jvnGetObjectState();
			}
		} catch (RemoteException e) {
			e.printStackTrace();
		}	
		return toReturn;
	}	

	/**
	 * Get a Write lock on a JVN object 
	 * @param joi : the JVN object identification
	 * @return the current JVN object state
	 * @throws  JvnException
	 **/
	public Serializable jvnLockWrite(int joi) throws JvnException {
		Serializable toReturn = null;
		JvnObject o = cacheJvnObject.get(joi);	
		try {		
			if (o.getLock_state() == JvnLOCK_STATE.WLC || o.getLock_state() == JvnLOCK_STATE.RLT_WLC){
				o.setLock_state(JvnLOCK_STATE.WLT);
				toReturn = cacheJvnObject.get(joi).jvnGetObjectState();
			}
			else{	
				toReturn = this.getCoordinator().jvnLockWrite(joi, this);
				JvnObject updated = new JvnObjectImpl(joi, toReturn);
				updated.setLock_state(JvnLOCK_STATE.WLT);
				cacheJvnObject.put(joi, updated);
				toReturn = cacheJvnObject.get(joi).jvnGetObjectState();
			}
		} catch (RemoteException e) {
			e.printStackTrace();
		}		
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
		this.cacheJvnObject.get(joi).jvnInvalidateReader();
	};

	/**
	 * Invalidate the Write lock of the JVN object identified by id 
	 * @param joi : the JVN object id
	 * @return the current JVN object state
	 * @throws java.rmi.RemoteException,JvnException
	 **/
	public Serializable jvnInvalidateWriter(int joi) throws java.rmi.RemoteException,jvn.JvnException { 
		Serializable toReturn = this.cacheJvnObject.get(joi).jvnInvalidateWriter();
		return toReturn;
	};

	/**
	 * Reduce the Write lock of the JVN object identified by id 
	 * @param joi : the JVN object id
	 * @return the current JVN object state
	 * @throws java.rmi.RemoteException,JvnException
	 **/
	public Serializable jvnInvalidateWriterForReader(int joi) throws java.rmi.RemoteException,jvn.JvnException { 
		Serializable toReturn = this.cacheJvnObject.get(joi).jvnInvalidateWriterForReader();
		return toReturn;
	}

}


