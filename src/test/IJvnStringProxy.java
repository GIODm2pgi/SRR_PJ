package test;

import java.io.Serializable;

import jvn.annots.JvnMethod;
import jvn.annots.JvnMethod.JvnMethodChange;

public interface IJvnStringProxy extends Serializable {

	@JvnMethod(change = JvnMethodChange.READ)
	public String get() ;

	@JvnMethod(change = JvnMethodChange.WRITE)
	public void set(String s) ;
}
