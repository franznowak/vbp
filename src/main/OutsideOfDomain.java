package main;

public class OutsideOfDomain extends Exception{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public OutsideOfDomain(){
		super.getMessage();
	}
}
