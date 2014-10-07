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

import javax.swing.JFrame;

import jvn.JvnException;
import jvn.JvnObjectProxy;
import jvn.JvnServerImpl;


public class IrcWithJvnProxy {
	public TextArea		text;
	public TextField	data;
	JFrame 			frame;
	ISentenceProxy sentence ;

	/**
	 * main method
	 * create a JVN object nammed IRC for representing the Chat application
	 **/
	public static void main(String argv[]) {
		try {
			// create the graphical part of the Chat application
			new IrcWithJvnProxy();

		} catch (Exception e) {
			System.out.println("IRC problem : " + e.getMessage());
		}
	}

	/**
	 * IRC Constructor
	 * @throws JvnException 
	 **/
	public IrcWithJvnProxy() throws JvnException {
		sentence = (ISentenceProxy) JvnObjectProxy.instanceJvn(new SentenceProxy(), "IRC");
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
		read_button.addActionListener(new readListenerForProxy(this));
		frame.add(read_button);
		Button write_button = new Button("write");
		write_button.addActionListener(new writeListenerForProxy(this));
		frame.add(write_button);
		Button terminate_button = new Button("exit");
		terminate_button.addActionListener(new terminateListenerForProxy(this));
		frame.add(terminate_button);
		frame.setSize(545,201);
		text.setBackground(Color.black); 
		frame.setVisible(true);
	}


	/**
	 * Internal class to manage user events (read) on the CHAT application
	 **/
	class readListenerForProxy implements ActionListener {
		IrcWithJvnProxy irc;

		public readListenerForProxy (IrcWithJvnProxy i) {
			irc = i;
		}

		/**
		 * Management of user events
		 **/
		public void actionPerformed (ActionEvent e) {
			// invoke the method
			String s = irc.sentence.read();
			// display the read value
			irc.data.setText(s);
			irc.text.append(s+"\n");
		}

	}

	/**
	 * Internal class to manage user events (write) on the CHAT application
	 **/
	class writeListenerForProxy implements ActionListener {
		IrcWithJvnProxy irc;

		public writeListenerForProxy (IrcWithJvnProxy i) {
			irc = i;
		}

		/**
		 * Management of user events
		 **/
		public void actionPerformed (ActionEvent e) {
			// get the value to be written from the buffer
			String s = irc.data.getText();

			// lock the object in write mode
			irc.sentence.write(s);
		}

	}

	/**
	 * Internal class to manage user events (terminate) on the CHAT application
	 **/
	class terminateListenerForProxy implements ActionListener {
		IrcWithJvnProxy irc;

		public terminateListenerForProxy (IrcWithJvnProxy i) {
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

}

