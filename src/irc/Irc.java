/***
 * Irc class : simple implementation of a chat using JAVANAISE
 * Contact: 
 *
 * Authors: 
 */

package irc;

import java.awt.Button;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.TextArea;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serializable;

import javax.swing.JFrame;

import jvn.JvnException;
import jvn.JvnObject;
import jvn.JvnServerImpl;


public class Irc {
	public TextArea		text;
	public TextField	data;
	JFrame 			frame;
	JvnObject       sentence;
	
	Button unlock_button;


	/**
	 * main method
	 * create a JVN object nammed IRC for representing the Chat application
	 **/
	public static void main(String argv[]) {
		try {

			// initialize JVN
			JvnServerImpl js = JvnServerImpl.jvnGetServer();

			// look up the IRC object in the JVN server
			// if not found, create it, and register it in the JVN server
			JvnObject jo = js.jvnLookupObject("IRC");

			if (jo == null) {
				jo = js.jvnCreateObject((Serializable) new Sentence());
				// after creation, I have a write lock on the object
				jo.jvnUnLock();
				js.jvnRegisterObject("IRC", jo);
			}
			// create the graphical part of the Chat application
			new Irc(jo);

		} catch (Exception e) {
			System.out.println("IRC problem : " + e.getMessage());
		}
	}

	/**
	 * IRC Constructor
   @param jo the JVN object representing the Chat
	 **/
	public Irc(JvnObject jo) {
		sentence = jo;
		frame=new JFrame();
		//frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE );
		frame.setLayout(new GridLayout(1,1));
		frame.setLocation(400, 50);
		text=new TextArea(10,60);
		text.setEditable(false);
		text.setForeground(Color.red);
		frame.add(text);
		data=new TextField(40);
		frame.add(data);
		Button read_button = new Button("read");
		read_button.addActionListener(new readListener(this));
		frame.add(read_button);
		Button write_button = new Button("write");
		unlock_button = new Button("unlock write");
		unlock_button.setBackground(Color.GRAY);
		unlock_button.addActionListener(new unlockListener(this));
		write_button.addActionListener(new writeListener(this));
		frame.add(write_button);
		frame.add(unlock_button);
		Button terminate_button = new Button("exit");
		terminate_button.addActionListener(new terminateListener(this));
		frame.add(terminate_button);
		frame.setSize(545,201);
		text.setBackground(Color.black); 
		frame.setVisible(true);
	}
}


/**
 * Internal class to manage user events (read) on the CHAT application
 **/
class readListener implements ActionListener {
	Irc irc;

	public readListener (Irc i) {
		irc = i;
	}

	/**
	 * Management of user events
	 **/
	public void actionPerformed (ActionEvent e) {
		try {
			// lock the object in read mode
			irc.sentence.jvnLockRead();

			// invoke the method
			String s = ((Sentence)(irc.sentence.jvnGetObjectState())).read();

			// unlock the object
			irc.sentence.jvnUnLock();

			// display the read value
			irc.data.setText(s);
			irc.text.append(s+"\n");
		} catch (JvnException je) {
			System.out.println("IRC problem : " + je.getMessage());
		}
	}

}

/**
 * Internal class to manage user events (write) on the CHAT application
 **/
class writeListener implements ActionListener {
	Irc irc;

	public writeListener (Irc i) {
		irc = i;
	}

	/**
	 * Management of user events
	 **/
	public void actionPerformed (ActionEvent e) {
		try {	
			// get the value to be written from the buffer
			String s = irc.data.getText();

			// lock the object in write mode
			irc.sentence.jvnLockWrite();

			// invoke the method
			((Sentence)(irc.sentence.jvnGetObjectState())).write(s);
			
			irc.unlock_button.setBackground(Color.RED);

			// unlock the object
			//irc.sentence.jvnUnLock();
		} catch (JvnException je) {
			System.out.println("IRC problem  : " + je.getMessage());
		}
	}

}

/**
 * Internal class to manage user events (unlock) on the CHAT application
 **/
class unlockListener implements ActionListener {
	Irc irc;

	public unlockListener (Irc i) {
		irc = i;
	}

	/**
	 * Management of user events
	 **/
	public void actionPerformed (ActionEvent e) {
		try {	
			// unlock the object
			irc.sentence.jvnUnLock();
			
			irc.unlock_button.setBackground(Color.GRAY);
			
		} catch (JvnException je) {
			System.out.println("IRC problem  : " + je.getMessage());
		}
	}
}

/**
 * Internal class to manage user events (terminate) on the CHAT application
 **/
class terminateListener implements ActionListener {
	Irc irc;

	public terminateListener (Irc i) {
		irc = i;
	}

	/**
	 * Management of user events
	 **/
	public void actionPerformed (ActionEvent e) {
		try {	
			JvnServerImpl.jvnGetServer().jvnTerminate();
			System.exit(0);
		} catch (JvnException je) {
			System.out.println("IRC problem  : " + je.getMessage());
		}
	}
}


