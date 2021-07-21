package tech.raidtheweb.epicsys.assembler;


/**
 * Abstract Wrapper around a <b>char</b> as an address.
 */
public class Address {
	
	private char value;
	
	public Address(char value) {
		this.value = value;
	}
	
	/**
	 * Get value of Address
	 * @return <b>char</b> value
	 */
	public char getValue() {
		return this.value;
	}
}
