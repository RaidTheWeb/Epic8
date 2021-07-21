package tech.raidtheweb.epicsys.hexdump;

import javax.swing.JPopupMenu;

public interface IMenuCreator {

	/**
	 * This function is called to generate a popup menu after the user
	 * right-clicked somewhere in the hex control.
	 * 
	 * @param offset The offset of the right-click.
	 * 
	 * @return The popup menu suitable for that offset or null if no popup menu
	 *         should be shown.
	 */
	JPopupMenu createMenu(long offset);
}
