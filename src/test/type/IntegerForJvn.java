package test.type;

/**
 * Classe définissant un entier utilisable
 * par Javanaise. 
 */
public class IntegerForJvn implements java.io.Serializable {
	
	/**
	 * UID de la classe.
	 */
	private static final long serialVersionUID = 6349581223290538606L;
	
	/**
	 * Le nombre courant.
	 */
	private Integer data ;
	
	/**
	 * Construit un Integer pour une utilisation
	 * avec Javanaise. Initialisé à 0.
	 */
	public IntegerForJvn(){
		this.data = new Integer(0) ;
	}
	
	/**
	 * Incrémente de 1 l'entier.
	 */
	public void increment(){
		data++ ;
	}
	
	/**
	 * Compare l'entier.
	 * @param i : Le comparateur.
	 * @return : True si l'entier vaut i.
	 */
	public boolean is(int i){
		return data == i ;
	}
	
	/**
	 * Get l'int courant.
	 * @return : La valeur courante de l'entier.
	 */
	public Integer get(){
		return data ;
	}
	
	/**
	 * Set l'int courant.
	 * @param i : La nouvelle valeur de l'entier.
	 */
	public void set(Integer i){
		this.data = i ;
	}
	
}
