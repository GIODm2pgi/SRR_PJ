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
import java.util.HashMap;
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

	/**
	 * The Map to store the objects.
	 */
	private HashMap<Integer, JvnObject> storeJvnObject = null ;

	/**
	 * The Map to store the symbolic names of the objects.
	 */
	private HashMap<String, Integer> storeNameObject = null ;

	/**
	 * The Map to store the servers who lock in write the objects.
	 */
	private HashMap<Integer, JvnRemoteServer> storeLockWriteObject = null ;

	/**
	 * The Map to store the servers who lock in read the objects.
	 */
	private HashMap<Integer, List<JvnRemoteServer>> storeLockReadObject = null ;

	/**
	 * The next object id available.
	 */
	private int nextStoreJvnObjectID = 0 ;

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
		this.storeJvnObject = new HashMap<Integer, JvnObject>() ;
		this.nextStoreJvnObjectID = 0 ;
		this.storeNameObject = new HashMap<String, Integer>() ;
		this.storeLockWriteObject = new HashMap<Integer, JvnRemoteServer>() ;
		this.storeLockReadObject = new HashMap<Integer, List<JvnRemoteServer>>() ;
	}

	/**
	 *  Allocate a NEW JVN object id (usually allocated to a 
	 *  newly created JVN object)
	 * @throws java.rmi.RemoteException,JvnException
	 */
	public synchronized int jvnGetObjectId() throws java.rmi.RemoteException,jvn.JvnException {
		int toReturn = this.nextStoreJvnObjectID ;
		this.nextStoreJvnObjectID++ ;
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
	public synchronized void jvnRegisterObject(String jon, JvnObject jo, JvnRemoteServer js) throws java.rmi.RemoteException,jvn.JvnException{
		Integer idJvnO = jo.jvnGetObjectId() ;

		// Check
		if(this.storeJvnObject.get(idJvnO) != null){
			throw new JvnException("This id of object already exist.") ;
		}
		if(this.storeNameObject.get(jon) != null){
			throw new JvnException("This symbolic name of object already exist.") ;
		}

		// Treat
		this.storeJvnObject.put(idJvnO, jo);
		this.storeNameObject.put(jon, idJvnO);
		this.storeLockReadObject.put(idJvnO, new ArrayList<JvnRemoteServer>());
		this.storeLockReadObject.get(idJvnO).add(js);
	}

	/**
	 * Get the reference of a JVN object managed by a given JVN server 
	 * @param jon : the JVN object name
	 * @param js : the remote reference of the JVNServer
	 * @throws java.rmi.RemoteException,JvnException
	 */
	public synchronized JvnObject jvnLookupObject(String jon, JvnRemoteServer js) throws java.rmi.RemoteException,jvn.JvnException{
		Integer joid = this.storeNameObject.get(jon) ;
		JvnObject toReturn = null;

		// Check
		//if(joid == null){
		//	throw new JvnException("This symbolic name not exist.") ;
		//}

		// Treat a Write Lock.
		if (this.storeLockWriteObject.containsKey(joid)){
			Serializable updated = this.storeLockWriteObject.get(joid).jvnInvalidateWriterForReader(joid);
			toReturn = new JvnObjectImpl(joid,updated);			
			this.storeJvnObject.put(joid,toReturn);
			this.storeLockReadObject.get(joid).add(storeLockWriteObject.get(joid));
			this.storeLockWriteObject.remove(joid);
		}
		else{
			toReturn = this.storeJvnObject.get(joid) ;
		}

		// Add Read Lock.
		if (toReturn != null)
			this.storeLockReadObject.get(joid).add(js);

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

		// Check
		if(this.storeJvnObject.containsKey(joi) == false){
			throw new JvnException("This id not exist.") ;
		}

		// Treat a Write Lock.
		if (this.storeLockWriteObject.containsKey(joi)){
			updated = this.storeLockWriteObject.get(joi).jvnInvalidateWriterForReader(joi);
			JvnObject updated_object = new JvnObjectImpl(joi,updated);			
			this.storeJvnObject.put(joi,updated_object);
			this.storeLockReadObject.get(joi).add(this.storeLockWriteObject.get(joi));
			this.storeLockWriteObject.remove(joi);
		}
		else{
			updated = storeJvnObject.get(joi).jvnGetObjectState();
		}

		// Add Read Lock.
		this.storeLockReadObject.get(joi).add(js);

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
		if(this.storeJvnObject.containsKey(joi) == false){
			throw new JvnException("This id not exist.") ;
		}

		// Treat a Write Lock.
		if (this.storeLockWriteObject.containsKey(joi)){
			updated = this.storeLockWriteObject.get(joi).jvnInvalidateWriter(joi);
			JvnObject updated_object = new JvnObjectImpl(joi,updated);			
			this.storeJvnObject.put(joi,updated_object);
			this.storeLockWriteObject.remove(joi);
		}
		else{
			updated = this.storeJvnObject.get(joi).jvnGetObjectState();
		}

		// Treat Read Lock.
		for (JvnRemoteServer s : this.storeLockReadObject.get(joi)){
			if (!s.equals(js))
				s.jvnInvalidateReader(joi);
		}
		this.storeLockReadObject.put(joi, new ArrayList<JvnRemoteServer>());

		// Add Write Lock.
		this.storeLockWriteObject.put(joi, js);

		return updated;
	}

	/**
	 * A JVN server terminates
	 * @param js  : the remote reference of the server
	 * @throws java.rmi.RemoteException, JvnException
	 */
	public void jvnTerminate(JvnRemoteServer js) throws java.rmi.RemoteException, JvnException {

		// Treat Write Lock.
		List<Integer> tmp = new ArrayList<Integer>();
		for(Entry<Integer, JvnRemoteServer> e : this.storeLockWriteObject.entrySet()){
			if(e.getValue().equals(js)){
				tmp.add(e.getKey());
			}
		}
		for(Integer todel : tmp){
			Serializable updated = null;
			updated = this.storeLockWriteObject.get(todel).jvnInvalidateWriter(todel);
			JvnObject updated_object = new JvnObjectImpl(todel,updated);			
			this.storeJvnObject.put(todel,updated_object);
			this.storeLockWriteObject.remove(todel);
		}

		// Treat Read Lock.
		tmp = new ArrayList<Integer>();
		for(Entry<Integer, List<JvnRemoteServer>> e : this.storeLockReadObject.entrySet()){
			for(JvnRemoteServer jsR : e.getValue()){
				if(jsR.equals(js)){
					tmp.add(e.getKey());
				}
			}
		}
		for(Integer todel : tmp){
			this.storeLockReadObject.get(todel).remove(js) ;
		}
	}

}