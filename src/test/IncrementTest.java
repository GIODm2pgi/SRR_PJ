package test;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.ArrayList;

import jvn.JvnException;
import jvn.JvnObject;
import jvn.JvnServerImpl;

public class IncrementTest {

	public static void main(String[] args) {
		Runtime runtime = Runtime.getRuntime();
		ArrayList<Process> ps = new ArrayList<Process>();
		for (int i = 0; i < 2; i++) {
			try {
				File f = new File(".");
				System.out.println(f.getAbsolutePath());
				ps.add(runtime.exec("java -cp build test.IncrementTestProcessus")) ;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		for(Process p : ps){
			try {
				p.waitFor();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			InputStream stream = p.getInputStream();
			BufferedReader br = new BufferedReader(new InputStreamReader(stream));
	           for(;;){
	              String s;
				try {
					s = br.readLine();
					if (s == null) break ;
					System.out.println(s);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}         //s contient une ligne envoyée par le processus pCommande
	           }
	           InputStream streamERR = p.getErrorStream();
				BufferedReader brERR = new BufferedReader(new InputStreamReader(streamERR));
		           for(;;){
		              String s;
					try {
						s = brERR.readLine();
						if (s == null) break ;
						System.out.println(s);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}         //s contient une ligne envoyée par le processus pCommande
		           }
		}
	}
}
