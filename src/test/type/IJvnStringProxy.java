package test.type;

import java.io.Serializable;

import jvn.annots.JvnMethod;
import jvn.annots.JvnMethod.JvnMethodChange;

/**
 * Interface d'un type string
 * utilisable avec les Jvn Object Proxy.
 */
public interface IJvnStringProxy extends Serializable {

	/**
	 * Get le string.
	 * @return le string reel.
	 */
	@JvnMethod(change = JvnMethodChange.READ)
	public String get() ;

	/**
	 * Set le string.
	 * @param s : le nouveau string.
	 */
	@JvnMethod(change = JvnMethodChange.WRITE)
	public void set(String s) ;
}
