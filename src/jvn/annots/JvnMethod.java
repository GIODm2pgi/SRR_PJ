package jvn.annots;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation of a method who not modify
 * the object (just read : JvnMethodChange.READ). It is used to
 * lock read section.
 * Annotation of a method who modify
 * the object (write : JvnMethodChange.WRITE).
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface JvnMethod {
	public enum JvnMethodChange {READ, WRITE} ;
	JvnMethodChange change() ;
}
