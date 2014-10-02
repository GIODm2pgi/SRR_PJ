package jvn;

/**
 * Enumeration of the different states
 * possible of locked.
 */
public enum JvnLOCK_STATE {

	/**
	 * No lock.
	 */
	NL , 

	/**
	 * Lock read cached.
	 */
	RLC , 

	/**
	 * Lock write cached.
	 */
	WLC , 

	/**
	 * Lock read taken.
	 */
	RLT , 

	/**
	 * Lock write taken.
	 */
	WLT, 

	/**
	 * Lock read taken and lock write cached.
	 */
	RLT_WLC

}
