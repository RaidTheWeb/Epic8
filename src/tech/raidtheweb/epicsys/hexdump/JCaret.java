package tech.raidtheweb.epicsys.hexdump;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Timer;

public class JCaret
{
	/**
	 * The default blink time of the caret in milliseconds.
	 */
	private static final int DEFAULT_BLINK_TIME = 500;

	/**
	 * The default color of the caret.
	 */
	private static final Color DEFAULT_CARET_COLOR = Color.RED;

	/**
	 * List of listeners that are notified when the caret status changes.
	 */
	private final List<ICaretListener> m_listeners = new ArrayList<ICaretListener>();

	/**
	 * Timer that is used to make the caret blink.
	 */
	private final Timer m_caretTimer;

	/**
	 * Flag that determines whether the caret is visible or not.
	 */
	private boolean m_isCaretVisible = false;

	/**
	 * The color of the caret.
	 */
	private final Color m_caretColor = Color.RED;

	/**
	 * Listeners that are notified about changes in the caret.
	 */
	private final InternalListener m_listener = new InternalListener();

	/**
	 * Creates a new caret with a default blink period of 500ms and
	 * the default caret color red.
	 */
	public JCaret()
	{
		this(DEFAULT_BLINK_TIME, DEFAULT_CARET_COLOR);
	}

	/**
	 * Creates a new caret with the default blink period and a custom caret color.
	 *
	 * @param caretColor The color of the caret.
	 *
	 * @throws NullPointerException Thrown if the color is null.
	 */
	public JCaret(final Color caretColor)
	{
		this(DEFAULT_BLINK_TIME, caretColor);
	}

	/**
	 * Creates a new caret with a custom blink period and the default caret color red.
	 *
	 * @param blinkPeriod The blink period in milliseconds.
	 *
	 * @throws IllegalArgumentException Thrown if the blink period is negative.
	 */
	public JCaret(final int blinkPeriod)
	{
		this(blinkPeriod, DEFAULT_CARET_COLOR);
	}

	/**
	 * Creates a new caret with a custom blink period and a custom
	 * caret color.
	 *
	 * @param blinkPeriod The blink period in milliseconds.
	 * @param caretColor The color of the caret.
	 * @throws IllegalArgumentException Thrown if the blink period is negative.
	 * @throws NullPointerException Thrown if the color is null.
	 */
	public JCaret(final int blinkPeriod, final Color caretColor)
	{

		if (blinkPeriod < 0)
		{
			throw new IllegalArgumentException("Error: Blink period can't be negative");
		}

		if (caretColor == null)
		{
			throw new NullPointerException("Error: Caret color can't be null");
		}

		// Initialize the timer that makes the caret blink
		m_caretTimer = new Timer(blinkPeriod, m_listener);
		m_caretTimer.setRepeats(true);
		m_caretTimer.start();
	}

	/**
	 * Notifies all listeners of a status change of the caret.
	 */
	private void notifyListeners()
	{
		for (final ICaretListener listener : m_listeners)
		{
			listener.caretStatusChanged(JCaret.this);
		}
	}

	/**
	 * Adds a new status change listener to the list of listeners.
	 *
	 * @param listener The new listener.
	 *
	 * @throws NullPointerException Thrown if the passed listener is null.
	 */
	public void addCaretListener(final ICaretListener listener)
	{

		if (listener == null)
		{
			throw new NullPointerException("Error: Listener can't be null");
		}

		// No duplicate listeners
		if (!m_listeners.contains(listener))
		{
			m_listeners.add(listener);
		}
	}

	/**
	 * Draws the caret on a given graphics surface.
	 *
	 * @param g The graphics surface where the caret is drawn.
	 * @param x The x coordinate of the caret.
	 * @param y The y coordinate of the caret.
	 * @param height The height of the caret.
	 *
	 * @throws NullPointerException Thrown if the graphics context is null.
	 */
	public void draw(final Graphics g, final int x, final int y, final int height)
	{

		if (g == null)
		{
			throw new NullPointerException("Error: Graphics context can't be null");
		}

		if (isVisible())
		{

			// Save the old color.
			final Color oldColor = g.getColor();

			// Draw the caret
			g.setColor(m_caretColor);
			g.drawLine(x, y, x, y + height - 1);

			// Restore the old color.
			g.setColor(oldColor);
		}
	}

	/**
	 * Determines whether the caret is currently visible nor not.
	 *
	 * @return True, if the caret is visible. False, otherwise.
	 */
	public boolean isVisible()
	{
		return m_isCaretVisible;
	}

	/**
	 * Removes a registered listeners.
	 * 
	 * @param listener The listener to remove.
	 */
	public void removeListener(final ICaretListener listener)
	{
		m_listeners.remove(listener);
	}

	/**
	 * Sets the visibility status of the caret.
	 *
	 * @param isCaretVisible The new visibility status of the caret.
	 */
	public void setVisible(final boolean isCaretVisible)
	{
		this.m_isCaretVisible = isCaretVisible;

		notifyListeners();
	}

	/**
	 * Stops the caret from blinking.
	 */
	public void stop()
	{
		m_caretTimer.stop();
		m_caretTimer.removeActionListener(m_listener);

		setVisible(false);
	}

	/**
	 * Class of internal listeners that are used to hide
	 * the callback functions from the public interface.
	 */
	private class InternalListener implements ActionListener
	{

		/**
		 * This function is called every time the caret timer
		 * ticks.
		 */
		@Override
		public void actionPerformed(final ActionEvent arg0)
		{

			// Switch the caret status at every timer click.
			m_isCaretVisible = !m_isCaretVisible;

			notifyListeners();
		}
	}

}
