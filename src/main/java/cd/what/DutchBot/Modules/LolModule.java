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

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cd.what.DutchBot.AccessList;
import cd.what.DutchBot.DatabaseConnection;
import cd.what.DutchBot.DutchBot;
import cd.what.DutchBot.ModuleAbstract;
import cd.what.DutchBot.Privileges;
import cd.what.DutchBot.Events.IChannelMessageEvent;


/**
 * @author DutchDude
 * 
 */
public class LolModule extends ModuleAbstract implements IChannelMessageEvent {

	private final Pattern helpCommandPattern;
	private final Pattern randomCommandPattern;
	private final Pattern addCommandPattern;
	private final Pattern delCommandPattern;
	private final Pattern getidCommandPattern;
	private final Pattern getCommandPattern;
	private final String helpResponse;
	private final DatabaseConnection db;

	private final LinkedList<String> last10Random = new LinkedList<String>();

	/**
	 * @param bot
	 */
	public LolModule(DutchBot bot) {
		super(bot);
		helpCommandPattern = Pattern.compile(
				Pattern.quote(bot.getCommandPrefix()) + "help(\\s.*)?",
				Pattern.CASE_INSENSITIVE);
		randomCommandPattern = Pattern.compile(
				Pattern.quote(bot.getCommandPrefix()) + "lol(\\s.*)?",
				Pattern.CASE_INSENSITIVE);
		addCommandPattern = Pattern
				.compile(Pattern.quote(bot.getCommandPrefix())
						+ "add\\s+(\\S+)\\s+(.+)", Pattern.CASE_INSENSITIVE);
		delCommandPattern = Pattern.compile(
				Pattern.quote(bot.getCommandPrefix()) + "del\\s+(\\d+)",
				Pattern.CASE_INSENSITIVE);
		getCommandPattern = Pattern.compile(Pattern.quote(bot
				.getCommandPrefix()) + "get\\s+(.+)");
		getidCommandPattern = Pattern.compile(
				Pattern.quote(bot.getCommandPrefix()) + "getid\\s+(\\d+)",
				Pattern.CASE_INSENSITIVE);

		String cp = bot.getCommandPrefix();
		helpResponse = String
				.format("To get random lulz: %slol, to add something: %sadd <url> [<tag>,...], to get a specific id: %sgetid <id>,"
						+ " to get a certain tag: %sget <tag> [<tag>,...]. No response? Check your syntax!",
						cp, cp, cp, cp);

		db = DatabaseConnection.getInstance();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * cd.what.DutchBot.Events.IChannelMessageEvent#notifyChannelMessageEvent
	 * (java.lang.String, java.lang.String, java.lang.String, java.lang.String,
	 * java.lang.String)
	 */
	@Override
	public void notifyChannelMessageEvent(final String channel,
			final String sender, final String login, final String hostname,
			final String message) {
		// ignore mode.
		if (!AccessList.isAllowed(login, hostname, Privileges.USER)
				|| !message.startsWith(bot.getCommandPrefix())) {
			return;
		}

		Matcher helpMatcher = helpCommandPattern.matcher(message);
		Matcher randomMatcher = randomCommandPattern.matcher(message);
		Matcher addMatcher = addCommandPattern.matcher(message);
		Matcher delMatcher = delCommandPattern.matcher(message);
		Matcher getMatcher = getCommandPattern.matcher(message);
		Matcher getidMatcher = getidCommandPattern.matcher(message);

		if (helpMatcher.matches()) {
			bot.sendMessage(channel, helpResponse);
			return;
		} else if (randomMatcher.matches()) {
			bot.sendMessage(channel, getRandomFromDatabase());
			return;
		} else if (addMatcher.matches() && addMatcher.groupCount() >= 2) {
			final String lol = addMatcher.group(1);
			final String[] tags = addMatcher.group(2).toLowerCase()
					.split("(\\s+|,)");

			// This is threaded because it tends to lock up for a while.
			Thread t = new Thread(new Runnable() {

				@Override
				public void run() {
					// validate URL
					try {
						if (!lol.startsWith("http"))
							throw new MalformedURLException("NOPE.avi");
						URL test = new URL(lol);
						URLConnection con = test.openConnection();
						con.connect();
						if (con instanceof HttpURLConnection) {
							if (((HttpURLConnection) con).getResponseCode() >= 400) {
								bot.sendMessage(channel, "NOPE.avi: URL dead.");
								return;
							}
						} else
							throw new MalformedURLException("NOPE.avi");
					} catch (MalformedURLException e) {
						bot.sendMessage(channel, "NOPE.avi: Invalid url");
						return;
					} catch (IOException e) {
						bot.sendMessage(channel, "NOPE.avi: Invalid url");
						return;
					}

					int id = insertIntoDb(lol, tags);
					System.out.println("added into id " + id);

					StringBuilder implodedTags = new StringBuilder();
					for (String tag : tags) {
						implodedTags.append(tag);
						implodedTags.append(" ");
					}

					if (id > 0) {
						bot.sendMessage(channel, "Added " + lol + " with tags "
								+ implodedTags.toString().trim() + " (id: "
								+ id + "). Thanks " + sender + "!");
					}
					return;
				}
			});
			t.start();

			return;
		} else if (getidMatcher.matches() && getidMatcher.groupCount() == 1) {
			bot.sendMessage(channel,
					getIdFromDatabase(Integer.parseInt(getidMatcher.group(1))));
			return;
		} else if (getMatcher.matches() && getMatcher.groupCount() == 1) {
			final String[] tags = getMatcher.group(1).toLowerCase()
					.split("(\\s+|,)");
			bot.sendMessage(channel, getTagsRandomFromDatabase(tags));
			return;
		} else if (delMatcher.matches()
				&& AccessList.isAllowed(login, hostname, Privileges.AUTHORIZED)) {
			bot.sendMessage(channel,
					deleteIdFromDatabase(Integer.parseInt(delMatcher.group(1))));
			return;
		}

	}

	protected int insertIntoDb(String lol, String[] tags) {
		String insertQuery = "INSERT INTO lol_urls (url) VALUES (?) EXCEPT SELECT url FROM lol_urls;";
		String findQuery = "SELECT id FROM lol_urls WHERE url = ?";
		String tagQuery = "INSERT INTO lol_tags VALUES (?, ?) EXCEPT SELECT tag, urlid FROM lol_tags";

		try {
			CallableStatement stmt = db.getDb().prepareCall(insertQuery);
			stmt.setString(1, lol);
			stmt.execute();
			stmt = db.getDb().prepareCall(findQuery);
			stmt.setString(1, lol);
			stmt.execute();
			ResultSet rs = stmt.getResultSet();
			if (!rs.next()) {
				throw new SQLException("no row found");
			}
			int id = rs.getInt(1);

			for (String tag : tags) {
				stmt = db.getDb().prepareCall(tagQuery);
				stmt.setString(1, tag);
				stmt.setInt(2, id);
				stmt.execute();
			}
			return id;
		} catch (SQLException e) {
			e.printStackTrace();
			return -1;
		}
	}

	private String getRandomFromDatabase() {
		String randomQuery = "SELECT id, url, string_agg(tag, ', ') AS tags "
				+ "FROM lol_urls JOIN lol_tags ON id=urlid "
				+ "GROUP BY id, urlid ORDER BY random() LIMIT 2";
		ResultSet rs = db.query(randomQuery);
		try {
			if (!rs.next()) {
				return "No lulz found! Try adding first.";
			} else {
				String response = String.format("[%d] %s with tags: %s",
						rs.getInt("id"), rs.getString("url"),
						rs.getString("tags"));
				if (last10Random.contains(response) && rs.next()) {
					response = String.format("[%d] %s with tags: %s",
							rs.getInt("id"), rs.getString("url"),
							rs.getString("tags"));
				}
				last10Random.add(response);
				while (last10Random.size() > 10) {
					last10Random.poll();
				}
				return response;
			}
		} catch (SQLException e) {
			e.printStackTrace();
			return "SQL problems, contact " + bot.getOwner();
		}

	}

	private String getTagsRandomFromDatabase(String[] tags) {
		StringBuilder wherePart = new StringBuilder("tag = ");

		for (int i = 0; i < tags.length - 1; i++) {
			wherePart.append("? AND tag = ");
		}
		wherePart.append('?');

		String query = "SELECT urlid FROM lol_tags WHERE "
				+ wherePart.toString() + " ORDER BY random() LIMIT 1";
		try {
			CallableStatement stmt = db.getDb().prepareCall(query);
			for (int i = 1; i <= tags.length; i++)
				stmt.setString(i, tags[i - 1]);
			stmt.execute();
			ResultSet rs = stmt.getResultSet();
			if (!rs.next())
				return "No such tag found!";
			int id = rs.getInt(1);
			return getIdFromDatabase(id);
		} catch (SQLException e) {
			e.printStackTrace();
			return "SQL Error! Contact " + bot.getOwner();
		}

	}

	private String getIdFromDatabase(final int id) {
		String query = "SELECT id, url, string_agg(tag, ', ') AS tags "
				+ "FROM lol_urls JOIN lol_tags ON id=urlid " + "WHERE id = "
				+ id + "GROUP BY id, urlid";
		ResultSet rs = db.query(query);
		try {
			if (!rs.next()) {
				return "No lulz found!";
			} else {
				String response = String.format("[%d] %s with tags: %s",
						rs.getInt("id"), rs.getString("url"),
						rs.getString("tags"));
				last10Random.add(response);
				while (last10Random.size() > 10) {
					last10Random.poll();
				}
				return response;
			}
		} catch (SQLException e) {
			e.printStackTrace();
			return "SQL problems, contact " + bot.getOwner();
		}
	}

	private String deleteIdFromDatabase(final int id) {
		String deleted = getIdFromDatabase(id);
		try {
			db.getDb().prepareCall("DELETE FROM lol_tags WHERE urlid = " + id)
					.execute();
			db.getDb().prepareCall("DELETE FROM lol_urls WHERE id = " + id)
					.execute();
		} catch (SQLException e) {
			e.printStackTrace();
			return "SQL ERROR: Contact " + bot.getOwner();
		}

		return "Deleted: " + deleted;
	}

}
