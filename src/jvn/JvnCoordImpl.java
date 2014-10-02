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

public class JvnCoordImpl extends UnicastRemoteObject implements JvnRemoteCoord {
	private static final long serialVersionUID = -353610607093461332L;

	//public enum LOCK_STATE { NL , RLC , WLC , RLT , WLT, RLT_WLC }

	private HashMap<Integer, JvnObject> storeJvnObject = null ;
	private HashMap<String, Integer> storeNameObject = null ;
	private HashMap<Integer, JvnRemoteServer> storeLockWriteObject = null ;
	private HashMap<Integer, List<JvnRemoteServer>> storeLockReadObject = null ;
	private int nextStoreJvnObjectID = 0 ;


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
	public synchronized void jvnRegisterObject(String jon, JvnObject jo, JvnRemoteServer js) throws java.rmi.RemoteException,jvn.JvnException{
		Integer idJvnO = jo.jvnGetObjectId() ;
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
	 **/
	public synchronized JvnObject jvnLookupObject(String jon, JvnRemoteServer js) throws java.rmi.RemoteException,jvn.JvnException{
		Integer joid = this.storeNameObject.get(jon) ;

		JvnObject toReturn = null;
		if (storeLockWriteObject.containsKey(joid)){
			Serializable updated = storeLockWriteObject.get(joid).jvnInvalidateWriterForReader(joid);
			toReturn = new JvnObjectImpl(joid,updated);			
			storeJvnObject.put(joid,toReturn);
			storeLockReadObject.get(joid).add(storeLockWriteObject.get(joid));
			storeLockWriteObject.remove(joid);
		}
		else
			toReturn = this.storeJvnObject.get(joid) ;

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
	 **/
	public synchronized Serializable jvnLockRead(int joi, JvnRemoteServer js) throws java.rmi.RemoteException, JvnException{
		Serializable updated = null;
		if (storeLockWriteObject.containsKey(joi)){
			updated = storeLockWriteObject.get(joi).jvnInvalidateWriterForReader(joi);
			JvnObject updated_object = new JvnObjectImpl(joi,updated);			
			storeJvnObject.put(joi,updated_object);
			storeLockReadObject.get(joi).add(storeLockWriteObject.get(joi));
			storeLockWriteObject.remove(joi);
		}
		else
			updated = storeJvnObject.get(joi).jvnGetObjectState();

		storeLockReadObject.get(joi).add(js);

		return updated;
	}

	/**
	 * Get a Write lock on a JVN object managed by a given JVN server 
	 * @param joi : the JVN object identification
	 * @param js  : the remote reference of the server
	 * @return the current JVN object state
	 * @throws java.rmi.RemoteException, JvnException
	 **/
	public synchronized Serializable jvnLockWrite(int joi, JvnRemoteServer js) throws java.rmi.RemoteException, JvnException{
		Serializable updated = null;
		if (storeLockWriteObject.containsKey(joi)){
			updated = storeLockWriteObject.get(joi).jvnInvalidateWriter(joi);
			JvnObject updated_object = new JvnObjectImpl(joi,updated);			
			storeJvnObject.put(joi,updated_object);
			storeLockWriteObject.remove(joi);
		}
		else
			updated = storeJvnObject.get(joi).jvnGetObjectState();

		for (JvnRemoteServer s : storeLockReadObject.get(joi)){
			if (!s.equals(js))
				s.jvnInvalidateReader(joi);
		}
		this.storeLockReadObject.put(joi, new ArrayList<JvnRemoteServer>());

		storeLockWriteObject.put(joi, js);

		return updated;
	}

	/**
	 * A JVN server terminates
	 * @param js  : the remote reference of the server
	 * @throws java.rmi.RemoteException, JvnException
	 **/
	public void jvnTerminate(JvnRemoteServer js) throws java.rmi.RemoteException, JvnException {
		for(Entry<Integer, JvnRemoteServer> e : this.storeLockWriteObject.entrySet()){
			if(e.getValue().equals(js)){
				Serializable updated = null;
				updated = storeLockWriteObject.get(e.getKey()).jvnInvalidateWriter(e.getKey());
				JvnObject updated_object = new JvnObjectImpl(e.getKey(),updated);			
				storeJvnObject.put(e.getKey(),updated_object);
				this.storeLockWriteObject.remove(e.getKey());
			}
		}
		for(Entry<Integer, List<JvnRemoteServer>> e : this.storeLockReadObject.entrySet()){
			for(JvnRemoteServer jsR : e.getValue()){
				if(jsR.equals(js)){
					this.storeLockReadObject.get(e.getKey()).remove(js) ;
				}
			}
		}
	}
}