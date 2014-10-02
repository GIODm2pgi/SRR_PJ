/***
 * JAVANAISE API
 * Contact: 
 *
 * Authors: 
 */

package jvn;

/**
 * JAVANAISE Exception. 
 */
public class JvnException extends Exception {

	/**
	 * UID of the class.
	 */
	private static final long serialVersionUID = 5040142103568889147L;

	/**
	 * The message of the exception.
	 */
	String message;

	/**
	 * Default constructor.
	 */
	public JvnException() {
	}

	/**
	 * Constructor with a message.
	 * @param message : The message of the exception.
	 */
	public JvnException(String message) {
		this.message = message;
	}	

	/**
	 * Gets the message of the exception.
	 */
	public String getMessage(){
		return message;
	}
	
}
