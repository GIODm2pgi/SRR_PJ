package test.type;

/**
 * Implémentation d'un string
 * utilisable avec les Jvn Object Proxy.
 */
public class JvnStringProxy implements IJvnStringProxy {

	/**
	 * UID de la class.
	 */
	private static final long serialVersionUID = -3430578129620916250L;
	
	/**
	 * Pour stocker le string reel.
	 */
	private String data = null ;
	
	/**
	 * Get le string.
	 * @return le string reel.
	 */
	public String get() {
		return data;
	}

	/**
	 * Set le string.
	 * @param s : le nouveau string.
	 */
	public void set(String s) {
		this.data = s ;
	}
	
}
