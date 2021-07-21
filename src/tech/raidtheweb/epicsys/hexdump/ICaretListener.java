package tech.raidtheweb.epicsys.hexdump;

public interface ICaretListener
{
	/**
	 * Invoked after the caret status changed.
	 * 
	 * @param caret The caret whose status changed.
	 */
	public void caretStatusChanged(JCaret caret);
}