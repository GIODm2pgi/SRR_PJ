/***
 * Sentence class : used for representing the text exchanged between users
 * during a chat application
 * Contact: 
 *
 * Authors: 
 */

package test.ircproxy;


public class SentenceProxy implements ISentenceProxy {
	private static final long serialVersionUID = -5744335023076980519L;
	
	String data;
  
	public SentenceProxy() {
		data = new String("");
	}
	
	public void write(String text) {
		data = text;
	}
	public String read() {
		return data;	
	}
	
}