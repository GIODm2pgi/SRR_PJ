package test;

import jvn.annots.JvnReadMethod;
import jvn.annots.JvnWriteMethod;

/**
 * Classe définissant un entier utilisable
 * par Javanaise. 
 */
public interface IIntegerForJvnProxy extends java.io.Serializable {

	
	/**
	 * Incrémente de 1 l'entier.
	 */
	@JvnWriteMethod
	public void increment();
	
	/**
	 * Compare l'entier.
	 * @param i : Le comparateur.
	 * @return : True si l'entier vaut i.
	 */
	@JvnReadMethod
	public boolean is(int i);
	
	/**
	 * Get l'int courant.
	 * @return : La valeur courante de l'entier.
	 */
	@JvnReadMethod
	public Integer get();
	
	/**
	 * Set l'int courant.
	 * @param i : La nouvelle valeur de l'entier.
	 */
	@JvnWriteMethod
	public void set(Integer i);
	
}
