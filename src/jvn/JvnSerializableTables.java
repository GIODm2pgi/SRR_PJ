package jvn;

import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Class to stock data of the coordinator.
 */
public class JvnSerializableTables implements Serializable {
	private static final long serialVersionUID = -7793153879732350544L;

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
	public int nextStoreJvnObjectID = 0 ;	

	/**
	 * List of all server.
	 */
	private List<JvnRemoteServer> listServer = null;

	/**
	 * If we need wake up.
	 */
	private Boolean needWakeUp = false;

	/**
	 * To lock the tables.
	 */
	private Lock lockTables = new ReentrantLock();

	/**
	 * Know if we need wake up.
	 * @return if we need wake up.
	 */
	public Boolean isNeedWakeUp() {
		synchronized (lockTables) {
			return needWakeUp;
		}
	}

	/**
	 * Get the store of object.
	 * @return the store of object.
	 */
	public HashMap<Integer, JvnObject> getStoreJvnObject() {
		synchronized (lockTables) {
			return storeJvnObject;
		}
	}

	/**
	 * Get the store of symbolic name.
	 * @return the store of symbolic name.
	 */
	public HashMap<String, Integer> getStoreNameObject() {
		synchronized (lockTables) {
			return storeNameObject;
		}
	}

	/**
	 * Get the store of lock in write.
	 * @return the store of lock in write.
	 */
	public HashMap<Integer, JvnRemoteServer> getStoreLockWriteObject() {
		synchronized (lockTables) {
			return storeLockWriteObject;
		}
	}

	/**
	 * Get the store of lock in read.
	 * @return the store of lock in read.
	 */
	public HashMap<Integer, List<JvnRemoteServer>> getStoreLockReadObject() {
		synchronized (lockTables) {
			return storeLockReadObject;
		}
	}

	/**
	 * Get the store of servers.
	 * @return the store of servers.
	 */
	public List<JvnRemoteServer> getListServer() {
		synchronized (lockTables) {
			return listServer;
		}
	}

	/**
	 * Create the tables or import the tables.
	 * @param ser : true if the files exist, else false.
	 */
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
			if (load("savecoord.ser"))
				load("savecoord_backup.ser");			
		}
	}

	/**
	 * Load the tables from a file.
	 * @param name : the file.
	 * @return
	 */
	private Boolean load (String name){
		ObjectInputStream ois = null;
		try {
			final FileInputStream fichier = new FileInputStream("save/"+name);
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
		} catch (IOException e) {
			if (e instanceof EOFException && name.compareTo("savecoord.ser") == 0)
				return true;
			else if (name.compareTo("savecoord_backup.ser") == 0){
				System.out.println("The coordinator failed to restore the tables.");
				this.storeJvnObject = new HashMap<Integer, JvnObject>() ;
				this.nextStoreJvnObjectID = 0 ;
				this.storeNameObject = new HashMap<String, Integer>() ;
				this.storeLockWriteObject = new HashMap<Integer, JvnRemoteServer>() ;
				this.storeLockReadObject = new HashMap<Integer, List<JvnRemoteServer>>() ;
				this.listServer = new ArrayList<JvnRemoteServer>() ;
				needWakeUp = false;
			}
		} catch (Exception e) {
			System.out.println("The coordinator failed to restore the tables.");
			this.storeJvnObject = new HashMap<Integer, JvnObject>() ;
			this.nextStoreJvnObjectID = 0 ;
			this.storeNameObject = new HashMap<String, Integer>() ;
			this.storeLockWriteObject = new HashMap<Integer, JvnRemoteServer>() ;
			this.storeLockReadObject = new HashMap<Integer, List<JvnRemoteServer>>() ;
			this.listServer = new ArrayList<JvnRemoteServer>() ;
			needWakeUp = false;
		}
		finally {
			try {
				if (ois != null) {
					ois.close();
				}
			} catch (final IOException ex) {
				ex.printStackTrace();
			}
		}
		return false;
	}

	/**
	 * Save the tables in the files.
	 */
	public synchronized void saveCoordState (){
		synchronized (lockTables) {

			copier(new File("save/savecoord.ser"),new File("save/savecoord_backup.ser"));

			ObjectOutputStream oos = null;

			try {
				final FileOutputStream fichier = new FileOutputStream("save/savecoord.ser");
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

	// http://forum.hardware.fr/hfr/Programmation/Java/copier-coller-java-sujet_68491_1.htm
	public boolean copier( File source, File destination ){
		boolean resultat = false;

		java.io.FileInputStream sourceFile=null;
		java.io.FileOutputStream destinationFile=null;
		try {
			destination.createNewFile();
			sourceFile = new java.io.FileInputStream(source);
			destinationFile = new java.io.FileOutputStream(destination);
			byte buffer[]=new byte[512*1024];
			int nbLecture;
			while( (nbLecture = sourceFile.read(buffer)) != -1 ) {
				destinationFile.write(buffer, 0, nbLecture);
			}  

			resultat = true;
		} catch( java.io.FileNotFoundException f ) {
		} catch( java.io.IOException e ) {
		} finally {
			try {
				sourceFile.close();
			} catch(Exception e) { }
			try {
				destinationFile.close();
			} catch(Exception e) { }
		}  
		return( resultat );
	} 

}
