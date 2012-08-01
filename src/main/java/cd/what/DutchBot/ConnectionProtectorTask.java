/**
 * 
 */
package cd.what.DutchBot;

import java.io.IOException;
import java.util.TimerTask;

import org.jibble.pircbot.IrcException;

/**
 * @author DutchDude
 * 
 */
public class ConnectionProtectorTask extends TimerTask {

	private final DutchBot bot;
	private boolean wasConnected;

	/**
     * 
     */
	public ConnectionProtectorTask(DutchBot bot) {
		this.bot = bot;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.TimerTask#run()
	 */
	@Override
	public void run() {
		if (!this.bot.isConnected() && this.wasConnected) {
			System.out.println("Reconnecting...");
			try {
				bot.tryConnect();
			} catch (IrcException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if (this.bot.isConnected()) {
			this.wasConnected = true;
		}

	}

}
