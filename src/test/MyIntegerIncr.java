package test;

public class MyIntegerIncr implements java.io.Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -8202152307287899472L;
	Integer data ;
	
	public MyIntegerIncr(){
		data = new Integer(0) ;
	}
	
	public void increment(){
		data++ ;
	}
	
	public boolean is(int i){
		return data == i ;
	}
	
	public Integer get(){
		return data ;
	}
}
