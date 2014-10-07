package test;

public class JvnStringProxy implements IJvnStringProxy {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -3430578129620916250L;
	private String data = null ;
	
	public String get() {
		return data;
	}

	public void set(String s) {
		this.data = s ;
	}
	
}
