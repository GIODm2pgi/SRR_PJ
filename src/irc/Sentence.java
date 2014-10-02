/***
 * Sentence class : used for representing the text exchanged between users
 * during a chat application
 * Contact: 
 *
 * Authors: 
 */

package irc;


public class Sentence implements java.io.Serializable {
	private static final long serialVersionUID = -5744335023076980519L;
	
	String data;
  
	public Sentence() {
		data = new String("");
	}
	
	public void write(String text) {
		data = text;
		//try {Thread.sleep(4000);} catch (InterruptedException e) {}
	}
	public String read() {
		return data;	
	}
	
}