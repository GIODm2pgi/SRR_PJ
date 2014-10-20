/***
 * JAVANAISE Implementation
 * JvnServerImpl class
 * Contact: 
 *
 * Authors: 
 */

package jvn;

import java.io.Serializable;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Realization of the coordinator.
 */
public class JvnCoordImpl extends UnicastRemoteObject implements JvnRemoteCoord {

	/**
	 * UID of the class.
	 */
	private static final long serialVersionUID = -353610607093461332L;

	/**
	 * The tables used by the coordinator (map of objects,...).
	 */
	private JvnSerializableTables tables = null;

	/**
	 * Lock to lock tables (concurrent changes).
	 */
	private Lock lockLookUp = null;

	/**
	 * To know if the mode restore is activated.
	 */
	public static Boolean RESTORE = true;

	/**
	 * Default constructor : Instantiate all maps.
	 * @throws JvnException
	 */
	private JvnCoordImpl() throws Exception {
		this.lockLookUp = new ReentrantLock();
		this.tables = new JvnSerializableTables(RESTORE);	
	}

	/**
	 * The main : runs the coordinator and treats
	 * the requests from the servers.
	 * @param args
	 */
	public static void main(String[] args) {

		// Treat security.
		System.setProperty("java.security.policy","file:./java.policy");
		if (System.getSecurityManager() == null) { 
			System.setSecurityManager(new SecurityManager());
		}

		// Run the registry RMI and the coordinator.
		JvnCoordImpl jvncoordimpl;
		try {
			jvncoordimpl = new JvnCoordImpl();
			Registry registry=LocateRegistry.createRegistry(1099);
			registry.bind("COORDINATOR", jvncoordimpl);
			if (jvncoordimpl.tables.isNeedWakeUp())
				jvncoordimpl.jvnWakeUpServers();
			System.out.println ("Coordinator ready");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * To start.
	 * @param restore : if you want to run with the mode restore.
	 */
	public static void start (Boolean restore){
		RESTORE = restore;
		if (!restore)
			System.out.println("RESTORE mode desactivate");
		main(null);
	}


	/**
	 * Allocate a NEW JVN object id (usually allocated to a 
	 * newly created JVN object)
	 * @throws java.rmi.RemoteException,JvnException
	 */
	public int jvnGetObjectId() throws java.rmi.RemoteException,jvn.JvnException {
		int toReturn = this.tables.nextStoreJvnObjectID ;
		this.tables.nextStoreJvnObjectID++ ;
		return toReturn ;
	}

	/**
	 * Associate a symbolic name with a JVN object
	 * @param jon : the JVN object name
	 * @param jo  : the JVN object 
	 * @param joi : the JVN object identification
	 * @param js  : the remote reference of the JVNServer
	 * @throws java.rmi.RemoteException,JvnException
	 */
	public void jvnRegisterObject(String jon, JvnObject jo, JvnRemoteServer js) throws java.rmi.RemoteException,jvn.JvnException {
		// Lock.
		lockLookUp.lock();

		// Id of the object.
		Integer idJvnO = jo.jvnGetObjectId() ;

		// Check
		if(this.tables.getStoreJvnObject().get(idJvnO) != null){
			lockLookUp.unlock();
			throw new JvnException("This id of object already exist. " + jon) ;
		}
		if(this.tables.getStoreNameObject().get(jon) != null){
			lockLookUp.unlock();
			throw new JvnException("This symbolic name of object already exist. >" + jon + "<") ;
		}

		// Treatments for register the object.
		this.tables.getStoreJvnObject().put(idJvnO, jo);
		this.tables.getStoreNameObject().put(jon, idJvnO);
		this.tables.getStoreLockReadObject().put(idJvnO, new ArrayList<JvnRemoteServer>());
		this.tables.getStoreLockWriteObject().put(idJvnO, js);

		// Treatments for errors.
		this.tables.getListServer().add(js);
		this.tables.saveCoordState();

		// Unlock.
		lockLookUp.unlock();
	}

	/**
	 * Get the reference of a JVN object managed by a given JVN server 
	 * @param jon : the JVN object name
	 * @param js : the remote reference of the JVNServer
	 * @throws java.rmi.RemoteException,JvnException
	 */
	public JvnObject jvnLookupObject(String jon, JvnRemoteServer js) throws java.rmi.RemoteException,jvn.JvnException{
		// Lock.	
		lockLookUp.lock();		

		// Get the object from tables.
		Integer joid = this.tables.getStoreNameObject().get(jon) ;

		// Treat a Write Lock.
		if (this.tables.getStoreLockWriteObject().containsKey(joid)){
			Serializable updated = this.tables.getStoreLockWriteObject().get(joid).jvnInvalidateWriterForReader(joid);
			this.tables.getStoreJvnObject().get(joid).setObjectState(updated);
			this.tables.getStoreLockReadObject().get(joid).add(this.tables.getStoreLockWriteObject().get(joid));
			this.tables.getStoreLockWriteObject().remove(joid);
		}
		
		// Object to return, by default is null
		// if the jvn object doesn't exist.
		JvnObject toReturn = null;
		toReturn = this.tables.getStoreJvnObject().get(joid) ;

		// Add Read Lock.
		if (toReturn != null){
			this.tables.getStoreLockReadObject().get(joid).add(js);
			this.tables.getListServer().add(js);
		}

		// Treatments for errors.
		this.tables.saveCoordState();

		// Unlock.
		lockLookUp.unlock();

		// Return null if the jvn object doesn't exist, else
		// return the jvn object register with a lock in read.
		return toReturn ;
	}

	/**
	 * Get a Read lock on a JVN object managed by a given JVN server 
	 * @param joi : the JVN object identification
	 * @param js  : the remote reference of the server
	 * @return the current JVN object state
	 * @throws java.rmi.RemoteException, JvnException
	 */
	public Serializable jvnLockRead(int joi, JvnRemoteServer js) throws java.rmi.RemoteException, JvnException{
		// Lock.	
		lockLookUp.lock();
		
		// Synchronize the object treated.
		synchronized (tables.getStoreJvnObject().get(joi)) {

			// The real object to return. 
			Serializable updated = null;

			// Treat a Write Lock.
			if (this.tables.getStoreLockWriteObject().containsKey(joi)){
				// Update the jvn object.
				updated = this.tables.getStoreLockWriteObject().get(joi).jvnInvalidateWriterForReader(joi);
				this.tables.getStoreJvnObject().get(joi).setObjectState(updated);
				this.tables.getStoreLockReadObject().get(joi).add(this.tables.getStoreLockWriteObject().get(joi));
				this.tables.getStoreLockWriteObject().remove(joi);
			}
			else{
				updated = this.tables.getStoreJvnObject().get(joi).jvnGetObjectState();
			}

			// Add Read Lock.
			this.tables.getStoreLockReadObject().get(joi).add(js);

			// Notify end intercept to the Server.
			js.deIntercept(joi);

			// Treatments for errors.
			this.tables.saveCoordState();

			// Unlock.
			lockLookUp.unlock();

			// Return the last version of the real object 
			// with a lock in read.
			return updated;
		}
	}

	/**
	 * Get a Write lock on a JVN object managed by a given JVN server 
	 * @param joi : the JVN object identification
	 * @param js  : the remote reference of the server
	 * @return the current JVN object state
	 * @throws java.rmi.RemoteException, JvnException
	 */
	public Serializable jvnLockWrite(int joi, JvnRemoteServer js) throws java.rmi.RemoteException, JvnException{
		// Lock.
		lockLookUp.lock();
		
		// Synchronize the object treated.
		synchronized (tables.getStoreJvnObject().get(joi)) {

			// The real object to return. 
			Serializable updated = null;

			// Check
			if(this.tables.getStoreJvnObject().containsKey(joi) == false){
				throw new JvnException("This id not exist.") ;
			}

			// Treat a Write Lock.
			if (this.tables.getStoreLockWriteObject().containsKey(joi)){
				// Update the jvn object.
				updated = this.tables.getStoreLockWriteObject().get(joi).jvnInvalidateWriter(joi);
				this.tables.getStoreJvnObject().get(joi).setObjectState(updated);
			}
			else{
				updated = this.tables.getStoreJvnObject().get(joi).jvnGetObjectState();
			}

			// Treat Read Lock.
			for (JvnRemoteServer s : this.tables.getStoreLockReadObject().get(joi)){
				if (!s.equals(js)){
					s.jvnInvalidateReader(joi);
				}
			}
			this.tables.getStoreLockReadObject().put(joi, new ArrayList<JvnRemoteServer>());

			// Add Write Lock.
			this.tables.getStoreLockWriteObject().put(joi, js);

			// Notify end intercept to the Server.
			js.deIntercept(joi);

			// Treatments for errors.
			this.tables.saveCoordState();

			// Unlock.
			lockLookUp.unlock();

			// Return the last version of the real object 
			// with a lock in write.
			return updated;
		}
	}

	/**
	 * A JVN server terminates
	 * @param js  : the remote reference of the server
	 * @throws java.rmi.RemoteException, JvnException
	 */
	public void jvnTerminate(JvnRemoteServer js) throws java.rmi.RemoteException, JvnException {
		// Lock.
		lockLookUp.lock();

		// Treat Write Lock.
		List<Integer> tmp = new ArrayList<Integer>();
		for(Entry<Integer, JvnRemoteServer> e : this.tables.getStoreLockWriteObject().entrySet()){
			if(e.getValue().equals(js)){
				tmp.add(e.getKey());
			}
		}

		// Delete writers.
		for(Integer todel : tmp){
			Serializable updated = null;
			updated = this.tables.getStoreLockWriteObject().get(todel).jvnInvalidateWriter(todel);
			this.tables.getStoreJvnObject().get(todel).setObjectState(updated);
			this.tables.getStoreLockWriteObject().remove(todel);
		}

		// Treat Read Lock.
		tmp = new ArrayList<Integer>();
		for(Entry<Integer, List<JvnRemoteServer>> e : this.tables.getStoreLockReadObject().entrySet()){
			for(JvnRemoteServer jsR : e.getValue()){
				if(jsR.equals(js)){
					tmp.add(e.getKey());
				}
			}
		}

		// Delete readers.
		for(Integer todel : tmp){
			this.tables.getStoreLockReadObject().get(todel).remove(js) ;
		}

		// Treatments for errors.
		this.tables.getListServer().remove(js);
		this.tables.saveCoordState();

		// Unlock.
		lockLookUp.unlock();
	}

	/**
	 * Wake up the servers after a problem of the coordinator.
	 * @throws java.rmi.RemoteException
	 * @throws jvn.JvnException
	 */
	public void jvnWakeUpServers () throws java.rmi.RemoteException, jvn.JvnException {
		// Lock.
		lockLookUp.lock();

		// Treatments Wake Up Servers.
		List <JvnRemoteServer> toDelete = new ArrayList<JvnRemoteServer>();		
		int i = 1;		
		for (JvnRemoteServer js : tables.getListServer())
			try {
				js.jvnWakeUpServer();
			} catch (Exception e) {
				System.out.println("Server <" + (i++) + "> no longer available");
				toDelete.add(js);
			}
		for (JvnRemoteServer js : toDelete){
			tables.getListServer().remove(js);
		}

		// Treatments for errors.
		this.tables.saveCoordState();

		// Unlock.
		lockLookUp.unlock();
	}
	
}