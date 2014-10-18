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

	private JvnSerializableTables tables;

	private Lock lockLookUp = new ReentrantLock();

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
	
	public static void start (Boolean restore){
		RESTORE = restore;
		if (!restore)
			System.out.println("RESTORE mode desactivate");
		main(null);
	}

	public static Boolean RESTORE = true;
	
	/**
	 * Default constructor : Instantiate all maps.
	 * @throws JvnException
	 */
	private JvnCoordImpl() throws Exception {
		this.tables = new JvnSerializableTables(RESTORE);	
	}

	/**
	 *  Allocate a NEW JVN object id (usually allocated to a 
	 *  newly created JVN object)
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
		lockLookUp.lock();

		Integer idJvnO = jo.jvnGetObjectId() ;
		//System.out.println("0 -> " + jon +" " +idJvnO);
		// Check
		if(this.tables.getStoreJvnObject().get(idJvnO) != null){
			lockLookUp.unlock();
			throw new JvnException("This id of object already exist. " + jon) ;
		}
		if(this.tables.getStoreNameObject().get(jon) != null){
			lockLookUp.unlock();
			throw new JvnException("This symbolic name of object already exist. >" + jon + "<") ;
		}


		//System.out.println("1 -> " + jon +" " +idJvnO);


		// Treat
		this.tables.getStoreJvnObject().put(idJvnO, jo);
		this.tables.getStoreNameObject().put(jon, idJvnO);
		this.tables.getStoreLockReadObject().put(idJvnO, new ArrayList<JvnRemoteServer>());
		this.tables.getStoreLockWriteObject().put(idJvnO, js);

		this.tables.getListServer().add(js);

		tables.saveCoordState();

		//System.out.println("fin reg");
		lockLookUp.unlock();
	}

	/**
	 * Get the reference of a JVN object managed by a given JVN server 
	 * @param jon : the JVN object name
	 * @param js : the remote reference of the JVNServer
	 * @throws java.rmi.RemoteException,JvnException
	 */
	public JvnObject jvnLookupObject(String jon, JvnRemoteServer js) throws java.rmi.RemoteException,jvn.JvnException{
		lockLookUp.lock();		

		Integer joid = this.tables.getStoreNameObject().get(jon) ;
		JvnObject toReturn = null;

		//System.out.println("LK -> " + jon +" " +joid);

		// Treat a Write Lock.
		if (this.tables.getStoreLockWriteObject().containsKey(joid)){
			//System.out.println("AV lookup");
			Serializable updated = this.tables.getStoreLockWriteObject().get(joid).jvnInvalidateWriterForReader(joid);
			//System.out.println("AP lookup");
			this.tables.getStoreJvnObject().get(joid).setObjectState(updated);
			this.tables.getStoreLockReadObject().get(joid).add(this.tables.getStoreLockWriteObject().get(joid));
			this.tables.getStoreLockWriteObject().remove(joid);
		}

		toReturn = this.tables.getStoreJvnObject().get(joid) ;

		// Add Read Lock.
		if (toReturn != null){
			//System.out.println("not null " + jon);
			this.tables.getStoreLockReadObject().get(joid).add(js);
			this.tables.getListServer().add(js);
		}

		//System.out.println("av save " + js.toString().split("endpoint")[1].split("\\(")[0]);

		tables.saveCoordState();

		lockLookUp.unlock();

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
		lockLookUp.lock();
		synchronized (tables.getStoreJvnObject().get(joi)) {

			Serializable updated = null;

			// Treat a Write Lock.
			if (this.tables.getStoreLockWriteObject().containsKey(joi)){
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

			js.deIntercept(joi);

			tables.saveCoordState();

			lockLookUp.unlock();

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
		lockLookUp.lock();

		synchronized (tables.getStoreJvnObject().get(joi)) {

			//System.out.println("JE FONCTIONNE !");

			Serializable updated = null;

			// Check
			if(this.tables.getStoreJvnObject().containsKey(joi) == false){
				throw new JvnException("This id not exist.") ;
			}

			// Treat a Write Lock.
			if (this.tables.getStoreLockWriteObject().containsKey(joi)){
				//System.out.println(this.tables.getStoreLockWriteObject().get(joi));
				updated = this.tables.getStoreLockWriteObject().get(joi).jvnInvalidateWriter(joi);
				this.tables.getStoreJvnObject().get(joi).setObjectState(updated);
				//this.tables.getStoreLockWriteObject().remove(joi);
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

			js.deIntercept(joi);

			tables.saveCoordState();

			lockLookUp.unlock();

			return updated;
		}
	}

	/**
	 * A JVN server terminates
	 * @param js  : the remote reference of the server
	 * @throws java.rmi.RemoteException, JvnException
	 */
	public void jvnTerminate(JvnRemoteServer js) throws java.rmi.RemoteException, JvnException {
		lockLookUp.lock();

		// Treat Write Lock.
		List<Integer> tmp = new ArrayList<Integer>();
		for(Entry<Integer, JvnRemoteServer> e : this.tables.getStoreLockWriteObject().entrySet()){
			if(e.getValue().equals(js)){
				tmp.add(e.getKey());
			}
		}

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

		for(Integer todel : tmp){
			this.tables.getStoreLockReadObject().get(todel).remove(js) ;
		}

		tables.getListServer().remove(js);

		tables.saveCoordState();

		lockLookUp.unlock();
	}

	public void jvnWakeUpServers () throws java.rmi.RemoteException, jvn.JvnException {
		lockLookUp.lock();
		
		List <JvnRemoteServer> toDelete = new ArrayList<JvnRemoteServer>();		
		int i = 1;		
		for (JvnRemoteServer js : tables.getListServer())
			try {
				js.jvnWakeUpServer();
			} catch (Exception e) {
				//e.printStackTrace();
				System.out.println("Server <" + (i++) + "> no longer available");
				toDelete.add(js);
			}
		for (JvnRemoteServer js : toDelete){
			tables.getListServer().remove(js);
		}

		tables.saveCoordState();
		
		lockLookUp.unlock();
	}
}