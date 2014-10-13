package test.tool;

import java.io.Serializable;

import test.type.IntegerForJvn;
import jvn.JvnException;
import jvn.JvnObject;
import jvn.JvnServerImpl;

/**
 * Cette classe est utilisé pour réaliser
 * une attente pseudo-active de synchronisation
 * de tous les processus d'un test pour Javanaise.
 */
public class AttenteRunProcess {

	/**
	 * Le nom symbolique de l'object jvn
	 * pour réalizer l'attente. 
	 */
	private String jvnName = null ;
	
	/**
	 * Le nombre de processus du test de 
	 * Javanaise (le nombre de server).
	 */
	private int nbOfProcess = 0 ;
	
	/**
	 * L'objet jvn utiliser pour la
	 * synchronisation (un int à incrémenter).
	 */
	private JvnObject jvnObject = null ;
	
	/**
	 * Le temps d'attente avant rafraichissement.
	 */
	private long timeToSleep = 500 ;
	
	/**
	 * L'id du processus.
	 */
	private int myId = -1 ;
	
	/**
	 * Le constructeur par defaut, construit
	 * le système d'attente.
	 * @param jvnName : Le nom de l'objet jvn.
	 * @param nbOfProcess : Le nombre de server du test.
	 */
	public AttenteRunProcess(String jvnName, int nbOfProcess){
		this.jvnName = jvnName ;
		this.nbOfProcess = nbOfProcess ;
		try {
			// Création ou récupération de l'objet Jvn.
			JvnServerImpl js = JvnServerImpl.jvnGetServer();
			this.jvnObject = js.jvnLookupObject(this.jvnName);
			if (this.jvnObject == null) {
				this.jvnObject = js.jvnCreateObject((Serializable) new IntegerForJvn());
				// after creation, I have a write lock on the object
				this.jvnObject.jvnUnLock();
				js.jvnRegisterObject(this.jvnName, this.jvnObject);
			}
			// Incrément du compteur.
			this.jvnObject.jvnLockWrite();
			((IntegerForJvn)this.jvnObject.jvnGetObjectState()).increment() ;
			this.myId = ((IntegerForJvn)this.jvnObject.jvnGetObjectState()).get().intValue() ;
			this.jvnObject.jvnUnLock();
		} catch (JvnException e) {
			System.err.println("Erreur lors du mécanisme de sychronisation par attente.");
			e.printStackTrace();
		}
	}
	
	/**
	 * Permet d'attendre que tous les processus du
	 * test de Javanaise soient prêt.
	 * Cette attente est bloquante.
	 */
	public void synchronisationParAttente(){
		boolean fini = false ;
		while(!fini){
			try {
				this.jvnObject.jvnLockRead();
				if(((IntegerForJvn)this.jvnObject.jvnGetObjectState()).get() == this.nbOfProcess)
					fini = true ;
				this.jvnObject.jvnUnLock();
				if(!fini)
					Thread.sleep(this.timeToSleep);
			} catch (JvnException e) {
				System.err.println("Erreur lors du mécanisme de sychronisation par attente.");
				e.printStackTrace();
			} catch (InterruptedException e) {
				System.err.println("Erreur lors du mécanisme de sychronisation par attente.");
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Get l'id du processus.
	 * @return L'id courant du processus.
	 */
	public int getMyId(){
		return this.myId ;
	}
	
	/**
	 * Set le  temps d'attente avant rafraichissement.
	 * @param time : Le nouveau temps d'attente.
	 */
	public void setTimeToSleep(long time){
		this.timeToSleep = time ;
	}
	
}
