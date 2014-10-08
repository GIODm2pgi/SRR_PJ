/***
 * Sentence class : used for representing the text exchanged between users
 * during a chat application
 * Contact: 
 *
 * Authors: 
 */

package test.ircproxy;

import jvn.JvnTransactionObjectAbstract;



public class SentenceProxy extends JvnTransactionObjectAbstract implements ISentenceProxy {
	private static final long serialVersionUID = -5744335023076980519L;
	
	String data;
  
	public SentenceProxy() {
		data = new String("");
	}
	
	public void write(String text) {
		data = text;
	}
	
	public void duplicate(){
		data += data;
	}
	
	public String read() {
		return data;	
	}
	
}