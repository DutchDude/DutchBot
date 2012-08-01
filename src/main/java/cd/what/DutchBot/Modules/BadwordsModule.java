/**
 * This file is part of DutchBot.
 *
 * DutchBot is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * any later version.
 *
 * DutchBot is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with DutchBot.  If not, see <http://www.gnu.org/licenses/>.
 *
 * @author DutchDude
 * @copyright © 2012, DutchDude
 * 
 * You are encouraged to send any changes you make to this code to the
 * author. See http://github.com/DutchDude/DutchBot.git
 */
package cd.what.DutchBot.Modules;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cd.what.DutchBot.AccessList;
import cd.what.DutchBot.DutchBot;
import cd.what.DutchBot.ModuleAbstract;
import cd.what.DutchBot.Privileges;
import cd.what.DutchBot.Events.IChannelMessageEvent;


/**
 * Class that listens for bad words. Not done.
 * 
 * 
 * @author DutchDude
 * 
 */
public class BadwordsModule extends ModuleAbstract implements
		IChannelMessageEvent {

	public static int LIMIT = 4;

	private final ArrayList<String> badWords = new ArrayList<String>();
	private final HashMap<String, Integer> hits = new HashMap<String, Integer>();
	private boolean active = false;
	private final Timer timer = new Timer();

	/**
	 * Reset hits after an amount of time
	 * 
	 * @author DutchDude
	 * 
	 */
	class ResetTimer extends TimerTask {

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.util.TimerTask#run()
		 */
		@Override
		public void run() {
			hits.clear();

		}

	}

	/**
	 * @param bot
	 */
	public BadwordsModule(DutchBot bot) {
		super(bot);
		this.timer.scheduleAtFixedRate(new ResetTimer(), 0, 5000 * 60);
		try {
			FileInputStream fi = new FileInputStream("badwordList");
			BufferedReader in = new BufferedReader(new InputStreamReader(fi));

			while (in.ready()) {
				String w = in.readLine();
				this.badWords.add(w.toLowerCase());
			}
			in.close();
			fi.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see cd.what.DutchBot.Modules.IChannelMessageEvent#
	 * notifyChannelMessageEvent (java.lang.String, java.lang.String,
	 * java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public void notifyChannelMessageEvent(String channel, String sender,
			String login, String hostname, String message) {
		this.run(channel, sender, login, hostname, message);

		if (AccessList.isAllowed(login, hostname, Privileges.OPERATOR)) {
			Matcher addMatcher = Pattern.compile(
					Pattern.quote(this.getBot().getCommandPrefix())
							+ "badword learn (\\S+)").matcher(message);
			if (addMatcher.matches()) {
				this.badWords.add(addMatcher.group(1).toLowerCase());
			} else if (message.startsWith(this.getBot().getCommandPrefix()
					+ "badword enable")) {
				this.active = true;
			} else if (message.startsWith(this.getBot().getCommandPrefix()
					+ "badword disable")) {
				this.active = false;
			}
		}

	}

	private void run(String channel, String sender, String login,
			String hostname, String message) {
		if (!this.active)
			return;
		String key = login + "@" + hostname;
		boolean hit = false;

		for (String w : message.split("\\s")) {
			if (this.badWords.contains(w.toLowerCase())) {
				if (hits.containsKey(key)) {
					hits.put(key, hits.get(key) + 1);
					hit = true;
				} else {
					hit = true;
					hits.put(key, 1);
				}
			}
		}

		if (hit) {
			this.getBot().sendNotice(
					sender,
					"You've said a word on the banlist! I will kick you if you do that "
							+ (LIMIT - this.hits.get(key)) + " more times.");
			if (this.hits.get(key) <= 0) {
				this.getBot().kick(channel, sender);
			}
			this.getBot()
					.sendMessage(channel, "!MOD removed user for badwords");
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#finalize()
	 */
	@Override
	protected void finalize() {
		try {
			FileOutputStream fo = new FileOutputStream("badwordList");
			PrintStream p = new PrintStream(fo);
			for (String w : this.badWords)
				p.println(w);
			p.close();
			fo.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
