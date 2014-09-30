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


public class JvnCoordImpl extends UnicastRemoteObject implements JvnRemoteCoord {
	private static final long serialVersionUID = -353610607093461332L;

	private HashMap<Integer, JvnObject> storeJvnObject = null ;
	private HashMap<String, Integer> storeNameObject = null ;
	private HashMap<Integer, JvnRemoteServer> storeLockWriteObject = null ;
	private HashMap<Integer, List<JvnRemoteServer>> storeLockReadObject = null ;
	private int nextStoreJvnObjectID = 0 ;
	
	public void debugPrintMap(){
		System.out.println("storeJvnObject : " + storeJvnObject);
		System.out.println("storeNameObject : " + storeNameObject);
		System.out.println("storeLockWriteObject : " + storeLockWriteObject);
		System.out.println("storeLockReadObject : " + storeLockReadObject);
	}
	
	public static void main(String[] args) {
		System.setProperty("java.security.policy","file:./java.policy");
		if (System.getSecurityManager() == null) { System.setSecurityManager(new SecurityManager());}
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
	 * Default constructor
	 * @throws JvnException
	 **/
	private JvnCoordImpl() throws Exception {
		// to be completed
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
	 **/
	public synchronized int jvnGetObjectId() throws java.rmi.RemoteException,jvn.JvnException {
		// to be completed 
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
	 **/
	public void jvnRegisterObject(String jon, JvnObject jo, JvnRemoteServer js) throws java.rmi.RemoteException,jvn.JvnException{
		// to be completed 
		Integer idJvnO = jo.jvnGetObjectId() ;
		this.storeJvnObject.put(idJvnO, jo);
		this.storeNameObject.put(jon, idJvnO);
		this.storeLockWriteObject.put(idJvnO, js);
		this.storeLockReadObject.put(idJvnO, new ArrayList<JvnRemoteServer>());

	}

	/**
	 * Get the reference of a JVN object managed by a given JVN server 
	 * @param jon : the JVN object name
	 * @param js : the remote reference of the JVNServer
	 * @throws java.rmi.RemoteException,JvnException
	 **/
	public JvnObject jvnLookupObject(String jon, JvnRemoteServer js) throws java.rmi.RemoteException,jvn.JvnException{
		// to be completed 
		Integer joid = this.storeNameObject.get(jon) ;
		JvnObject toReturn = this.storeJvnObject.get(joid) ;
		return toReturn ;
	}

	/**
	 * Get a Read lock on a JVN object managed by a given JVN server 
	 * @param joi : the JVN object identification
	 * @param js  : the remote reference of the server
	 * @return the current JVN object state
	 * @throws java.rmi.RemoteException, JvnException
	 **/
	public Serializable jvnLockRead(int joi, JvnRemoteServer js) throws java.rmi.RemoteException, JvnException{
		// to be completed
		return null;
	}

	/**
	 * Get a Write lock on a JVN object managed by a given JVN server 
	 * @param joi : the JVN object identification
	 * @param js  : the remote reference of the server
	 * @return the current JVN object state
	 * @throws java.rmi.RemoteException, JvnException
	 **/
	public Serializable jvnLockWrite(int joi, JvnRemoteServer js) throws java.rmi.RemoteException, JvnException{
		// to be completed
		return null;
	}

	/**
	 * A JVN server terminates
	 * @param js  : the remote reference of the server
	 * @throws java.rmi.RemoteException, JvnException
	 **/
	public void jvnTerminate(JvnRemoteServer js) throws java.rmi.RemoteException, JvnException {
		// to be completed
	}
}


