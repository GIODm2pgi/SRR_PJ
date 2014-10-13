package jvn;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class JvnSerializableTables implements Serializable {
	private static final long serialVersionUID = -7793153879732350544L;

	/**
	 * The Map to store the objects.
	 */
	public HashMap<Integer, JvnObject> storeJvnObject = null ;

	/**
	 * The Map to store the symbolic names of the objects.
	 */
	public HashMap<String, Integer> storeNameObject = null ;

	/**
	 * The Map to store the servers who lock in write the objects.
	 */
	public HashMap<Integer, JvnRemoteServer> storeLockWriteObject = null ;

	/**
	 * The Map to store the servers who lock in read the objects.
	 */
	public HashMap<Integer, List<JvnRemoteServer>> storeLockReadObject = null ;

	/**
	 * The next object id available.
	 */
	public int nextStoreJvnObjectID = 0 ;	
	
	/**
	 * List of all server.
	 */
	public List<JvnRemoteServer> listServer = null;
	
	private Boolean needWakeUp = false;
	
	public Boolean isNeedWakeUp() {
		return needWakeUp;
	}

	public JvnSerializableTables (Boolean ser){
		if (!ser){
			this.storeJvnObject = new HashMap<Integer, JvnObject>() ;
			this.nextStoreJvnObjectID = 0 ;
			this.storeNameObject = new HashMap<String, Integer>() ;
			this.storeLockWriteObject = new HashMap<Integer, JvnRemoteServer>() ;
			this.storeLockReadObject = new HashMap<Integer, List<JvnRemoteServer>>() ;
			this.listServer = new ArrayList<JvnRemoteServer>() ;
		}
		else {
			needWakeUp = true;
			
			ObjectInputStream ois = null;

			try {
				final FileInputStream fichier = new FileInputStream("savecoord.ser");
				ois = new ObjectInputStream(fichier);
				final JvnSerializableTables tables = (JvnSerializableTables) ois.readObject();
				this.storeJvnObject = tables.storeJvnObject;
				this.nextStoreJvnObjectID = tables.nextStoreJvnObjectID ;
				this.storeNameObject = tables.storeNameObject ;
				this.storeLockWriteObject = new HashMap<Integer, JvnRemoteServer>() ;
				this.storeLockReadObject = new HashMap<Integer, List<JvnRemoteServer>>() ;
				
				for (Integer i : tables.storeJvnObject.keySet())
					this.storeLockReadObject.put(i, new ArrayList<JvnRemoteServer>());
									
				this.listServer = tables.listServer;
			} catch (final java.io.IOException e) {
				e.printStackTrace();
			} catch (final ClassNotFoundException e) {
				e.printStackTrace();
			} finally {
				try {
					if (ois != null) {
						ois.close();
					}
				} catch (final IOException ex) {
					ex.printStackTrace();
				}
			}
		}
	}

	public synchronized void saveCoordState (){
		ObjectOutputStream oos = null;

		try {
			final FileOutputStream fichier = new FileOutputStream("savecoord.ser");
			oos = new ObjectOutputStream(fichier);
			oos.writeObject(this);
			oos.flush();
		} catch (final java.io.IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (oos != null) {
					oos.flush();
					oos.close();
				}
			} catch (final IOException ex) {
				ex.printStackTrace();
			}
		}
	}
}
