/***
 * JAVANAISE Implementation
 * JvnServerImpl class
 * Contact: 
 *
 * Authors: 
 */

package jvn;

import java.io.File;
import java.io.Serializable;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

/**
 * Realization of the coordinator.
 */
public class JvnCoordImpl extends UnicastRemoteObject implements JvnRemoteCoord {

	/**
	 * UID of the class.
	 */
	private static final long serialVersionUID = -353610607093461332L;

	private JvnSerializableTables tables;

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
	 * Default constructor : Instantiate all maps.
	 * @throws JvnException
	 */
	private JvnCoordImpl() throws Exception {
		this.tables = new JvnSerializableTables(new File("savecoord.ser").exists());		
	}

	/**
	 *  Allocate a NEW JVN object id (usually allocated to a 
	 *  newly created JVN object)
	 * @throws java.rmi.RemoteException,JvnException
	 */
	public synchronized int jvnGetObjectId() throws java.rmi.RemoteException,jvn.JvnException {
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
	public synchronized void jvnRegisterObject(String jon, JvnObject jo, JvnRemoteServer js) throws java.rmi.RemoteException,jvn.JvnException {
		Integer idJvnO = jo.jvnGetObjectId() ;

		// Check
		if(this.tables.storeJvnObject.get(idJvnO) != null){
			throw new JvnException("This id of object already exist.") ;
		}
		if(this.tables.storeNameObject.get(jon) != null){
			throw new JvnException("This symbolic name of object already exist.") ;
		}

		// Treat
		this.tables.storeJvnObject.put(idJvnO, jo);
		this.tables.storeNameObject.put(jon, idJvnO);
		this.tables.storeLockReadObject.put(idJvnO, new ArrayList<JvnRemoteServer>());
		this.tables.storeLockWriteObject.put(idJvnO, js);

		this.tables.listServer.add(js);
		
		tables.saveCoordState();
	}

	/**
	 * Get the reference of a JVN object managed by a given JVN server 
	 * @param jon : the JVN object name
	 * @param js : the remote reference of the JVNServer
	 * @throws java.rmi.RemoteException,JvnException
	 */
	public synchronized JvnObject jvnLookupObject(String jon, JvnRemoteServer js) throws java.rmi.RemoteException,jvn.JvnException{
		Integer joid = this.tables.storeNameObject.get(jon) ;
		JvnObject toReturn = null;

		// Treat a Write Lock.
		if (this.tables.storeLockWriteObject.containsKey(joid)){
			Serializable updated = this.tables.storeLockWriteObject.get(joid).jvnInvalidateWriterForReader(joid);
			this.tables.storeJvnObject.get(joid).setObjectState(updated);
			this.tables.storeLockReadObject.get(joid).add(this.tables.storeLockWriteObject.remove(joid));
		}

		toReturn = this.tables.storeJvnObject.get(joid) ;

		// Add Read Lock.
		if (toReturn != null){
			this.tables.storeLockReadObject.get(joid).add(js);
			this.tables.listServer.add(js);
		}

		tables.saveCoordState();
		return toReturn ;
	}

	/**
	 * Get a Read lock on a JVN object managed by a given JVN server 
	 * @param joi : the JVN object identification
	 * @param js  : the remote reference of the server
	 * @return the current JVN object state
	 * @throws java.rmi.RemoteException, JvnException
	 */
	public synchronized Serializable jvnLockRead(int joi, JvnRemoteServer js) throws java.rmi.RemoteException, JvnException{
		Serializable updated = null;

		// Treat a Write Lock.
		if (this.tables.storeLockWriteObject.containsKey(joi)){
			updated = this.tables.storeLockWriteObject.get(joi).jvnInvalidateWriterForReader(joi);
			this.tables.storeJvnObject.get(joi).setObjectState(updated);
			this.tables.storeLockReadObject.get(joi).add(this.tables.storeLockWriteObject.remove(joi));
		}
		else{
			updated = this.tables.storeJvnObject.get(joi).jvnGetObjectState();
		}

		// Add Read Lock.
		this.tables.storeLockReadObject.get(joi).add(js);

		tables.saveCoordState();
		return updated;
	}

	/**
	 * Get a Write lock on a JVN object managed by a given JVN server 
	 * @param joi : the JVN object identification
	 * @param js  : the remote reference of the server
	 * @return the current JVN object state
	 * @throws java.rmi.RemoteException, JvnException
	 */
	public synchronized Serializable jvnLockWrite(int joi, JvnRemoteServer js) throws java.rmi.RemoteException, JvnException{
		Serializable updated = null;

		// Check
		if(this.tables.storeJvnObject.containsKey(joi) == false){
			throw new JvnException("This id not exist.") ;
		}

		// Treat a Write Lock.
		if (this.tables.storeLockWriteObject.containsKey(joi)){
			updated = this.tables.storeLockWriteObject.get(joi).jvnInvalidateWriter(joi);
			this.tables.storeJvnObject.get(joi).setObjectState(updated);
			this.tables.storeLockWriteObject.remove(joi);
		}
		else{
			updated = this.tables.storeJvnObject.get(joi).jvnGetObjectState();
		}

		// Treat Read Lock.
		for (JvnRemoteServer s : this.tables.storeLockReadObject.get(joi)){
			if (!s.equals(js)){
				s.jvnInvalidateReader(joi);
			}
		}
		this.tables.storeLockReadObject.put(joi, new ArrayList<JvnRemoteServer>());

		// Add Write Lock.
		this.tables.storeLockWriteObject.put(joi, js);

		tables.saveCoordState();
		return updated;
	}

	/**
	 * A JVN server terminates
	 * @param js  : the remote reference of the server
	 * @throws java.rmi.RemoteException, JvnException
	 */
	public synchronized void jvnTerminate(JvnRemoteServer js) throws java.rmi.RemoteException, JvnException {
		// Treat Write Lock.
		List<Integer> tmp = new ArrayList<Integer>();
		for(Entry<Integer, JvnRemoteServer> e : this.tables.storeLockWriteObject.entrySet()){
			if(e.getValue().equals(js)){
				tmp.add(e.getKey());
			}
		}

		for(Integer todel : tmp){
			Serializable updated = null;
			updated = this.tables.storeLockWriteObject.get(todel).jvnInvalidateWriter(todel);
			this.tables.storeJvnObject.get(todel).setObjectState(updated);
			this.tables.storeLockWriteObject.remove(todel);
		}

		// Treat Read Lock.
		tmp = new ArrayList<Integer>();
		for(Entry<Integer, List<JvnRemoteServer>> e : this.tables.storeLockReadObject.entrySet()){
			for(JvnRemoteServer jsR : e.getValue()){
				if(jsR.equals(js)){
					tmp.add(e.getKey());
				}
			}
		}

		for(Integer todel : tmp){
			this.tables.storeLockReadObject.get(todel).remove(js) ;
		}

		tables.listServer.remove(js);

		tables.saveCoordState();
	}

	public void jvnWakeUpServers () throws java.rmi.RemoteException, jvn.JvnException {
		List <JvnRemoteServer> toDelete = new ArrayList<JvnRemoteServer>();		
		int i = 1;		
		for (JvnRemoteServer js : tables.listServer)
			try {
				js.jvnWakeUpServer();
			} catch (Exception e) {
				//e.printStackTrace();
				System.out.println("Server <" + (i++) + "> no longer available");
				toDelete.add(js);
			}
		for (JvnRemoteServer js : toDelete){
			tables.listServer.remove(js);
		}

		tables.saveCoordState();
	}
}