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

import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jibble.pircbot.IrcException;
import org.jibble.pircbot.PircBot.Hostmask;

import cd.what.DutchBot.AccessList;
import cd.what.DutchBot.DatabaseConnection;
import cd.what.DutchBot.DutchBot;
import cd.what.DutchBot.ModuleAbstract;
import cd.what.DutchBot.Privileges;
import cd.what.DutchBot.Events.IChannelMessageEvent;
import cd.what.DutchBot.Events.IPrivateMessageEvent;


import de.umass.lastfm.Artist;
import de.umass.lastfm.CallException;
import de.umass.lastfm.Caller;
import de.umass.lastfm.PaginatedResult;
import de.umass.lastfm.Result;
import de.umass.lastfm.Tasteometer;
import de.umass.lastfm.Tasteometer.ComparisonResult;
import de.umass.lastfm.Track;
import de.umass.lastfm.User;

/**
 * @author DutchDude
 * 
 */
public class LastfmModule extends ModuleAbstract implements
		IChannelMessageEvent, IPrivateMessageEvent {

	private final String prefix;
	private final Pattern helpCommand;
	private final Pattern npCommand;
	private final Pattern setUserCommand;
	private final Pattern compareCommand;

	private final String apiKey;

	private static final String HELP_RESPONSE = "Use the command ?setuser <lastfmusername> to associate your last.fm account with this now playing bot. "
			+ " You can use the other commands ?np (or ?np <lastfmnick>) to display the currently playing track, and ?compare <otheruser> to get your musical "
			+ "compatibility with that other user. ?setuser also works via PM. There is a cooldown of ## seconds on ?np and ?compare";
	private static final String HELP_RESPONSE2 = "You can make the bot ignore you in ?compare by using the ?ignoreme command. " +
			"Source code of the bot is available at https://github.com/DutchDude/DutchBot.git";

	private static final String SELECT_USERNAME_QUERY = "SELECT lastfmusername FROM lastfm WHERE ident = ? AND hostname = ? LIMIT 1";
	private static final String INSERT_USERNAME_QUERY = "INSERT INTO lastfm (ident, hostname, lastfmusername) VALUES (?, ?, ?)";
	private static final String UPDATE_USERNAME_QUERY = "UPDATE lastfm SET lastfmusername = ? WHERE ident = ? AND hostname = ?";

	private final DatabaseConnection dbc;

	private final HashMap<String, Long> lastExecuted = new HashMap<String, Long>();
	private final Float cooldownTime;
	private final Pattern ignoreCommand;
	private final Pattern ignoreMeCommand;

	public LastfmModule(DutchBot bot) {
		super(bot);
		TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
		Caller.getInstance().setUserAgent("DutchBot " + bot.getVersion());
		Caller.getInstance().getLogger().setLevel(Level.SEVERE);
		prefix = bot.getConfig().getString("modules.lastfm.prefix",
				bot.getCommandPrefix());
		apiKey = bot.getConfig().getString("modules.lastfm.apikey");
		cooldownTime = bot.getConfig().getFloat("modules.lastfm.cooldowntime",
				30F);
		String qp = Pattern.quote(prefix);
		helpCommand = Pattern.compile(qp + "(help|commands)", Pattern.CASE_INSENSITIVE);
		npCommand = Pattern.compile(qp + "np(\\s+(?<nick>\\S+))?",
				Pattern.CASE_INSENSITIVE);
		setUserCommand = Pattern.compile(qp + "setuser(\\s+(?<nick>\\S+))?",
				Pattern.CASE_INSENSITIVE);
		compareCommand = Pattern.compile(qp + "compare\\s+(\\S+)",
				Pattern.CASE_INSENSITIVE);
		ignoreCommand = Pattern.compile(qp + "ignore\\s+(?<nick>\\S+)");
		ignoreMeCommand = Pattern.compile(qp + "ignoreme");

		dbc = DatabaseConnection.getInstance();
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
	public void notifyChannelMessageEvent(String channel, String sender,
			String login, String hostname, String message) {
		if (!AccessList.isAllowed(login, hostname, Privileges.USER))
			return;

		// sanity checking
		message = message.trim();
		if (!message.startsWith(prefix))
			return;

		// matchers for the messages
		Matcher helpCommandMatcher = helpCommand.matcher(message);
		Matcher npCommandMatcher = npCommand.matcher(message);
		Matcher setUserCommandMatcher = setUserCommand.matcher(message);
		Matcher compareCommandMatcher = compareCommand.matcher(message);
		Matcher ignoreCommandMatcher = ignoreCommand.matcher(message);
		Matcher ignoreMeCommandMatcher = ignoreMeCommand.matcher(message);

		if (helpCommandMatcher.matches()) {
			bot.sendNotice(
					sender,
					HELP_RESPONSE.replace("?", prefix).replace("##",
							cooldownTime.toString()));
			bot.sendNotice(sender, HELP_RESPONSE2.replace("?", prefix));
			return;
		} else if (npCommandMatcher.matches()) {
			if (!mayExecute(sender, login, hostname)) {
				return;
			}
			if (npCommandMatcher.groupCount() == 2
					&& npCommandMatcher.group("nick") != null) {
				sendNowPlaying(channel,
						sender + " (" + npCommandMatcher.group("nick")
								+ " on last.fm)",
						npCommandMatcher.group("nick"));
			} else
				sendNowPlaying(channel, sender,
						getLastfmNick(channel, sender, login, hostname));
			return;
		} else if (setUserCommandMatcher.matches()) {
			if (setUserCommandMatcher.group("nick") == null) {
				bot.sendMessage(channel, "Usage: .setuser <lastfmaccount> ");
				return;
			}
			linkUser(channel, login, hostname,
					setUserCommandMatcher.group("nick"));
			return;
		} else if (compareCommandMatcher.matches()) {
			if (!mayExecute(sender, login, hostname)) {
				return;
			}
			compareUser(channel, sender, login, hostname,
					compareCommandMatcher.group(1));
		} else if (ignoreCommandMatcher.matches()) {
			if (!AccessList.isAllowed(login, hostname, Privileges.OPERATOR)) {
				bot.sendMessage(
						channel,
						"No. If I were drone. I'd have /KILLed you now. "
								+ "For now, why don't you try !rippy. (ERROR: Need OPERATOR privileges)");
				return;
			} else {
				ignoreUser(channel, sender, ignoreCommandMatcher.group("nick"));
			}
			return;
		} else if (ignoreMeCommandMatcher.matches()) {
			this.linkUser(channel, login, hostname, "");
			bot.sendMessage(
					channel,
					("ok, put you on the ignore list. You won't show up in ?compare anymore. "
							+ "You can do ?setuser to remove this ignore")
							.replace("?", prefix));
		}

	}

	private void ignoreUser(final String channel, final String sender,
			final String nick) {
		Thread t = new Thread(new Runnable() {
			
			@Override
			public void run() {
				Hostmask targethostmask = null;
				try {
					if (bot.getChannel(channel).getUser(nick) != null)
						targethostmask = bot.getHostmask(nick);
				} catch (IrcException e) {

				}
				
				if (targethostmask == null) {
					bot.sendNotice(sender, "Apparently " + nick
							+ " has no host mask and/or soul.");
					return;
				}
				
				if (AccessList.isAllowed(targethostmask.login, targethostmask.hostname,
						Privileges.OPERATOR)){
					bot.sendMessage(channel, "Demote them first. If you know how.");
					return;
				}

				linkUser(channel, targethostmask.login,
						targethostmask.hostname, "");
				AccessList.addUser(targethostmask.login,
						targethostmask.hostname, Privileges.IGNORE);
				bot.sendMessage(
						channel,
						"put "
								+ nick
								+ " on the ignore list for lastfm and with IGNORE on the access list.");

			}
		});
		t.start();
	}

	/**
	 * Do a tasteometer compare on two users.
	 * 
	 * @param channel
	 * @param sender
	 * @param login
	 * @param hostname
	 * @param compareWith
	 */
	private void compareUser(final String channel, final String sender,
			final String login, final String hostname,
			final String compareWithNick) {
		Thread stuff = new Thread(new Runnable() {
			@Override
			public void run() {
				final String lastfmnick = getLastfmNick(channel, sender, login,
						hostname);
				if (lastfmnick.equals("")) {
					return;
				}

				Hostmask targethostmask = null;

				try {
					if (bot.getChannel(channel).getUser(compareWithNick) != null)
						targethostmask = bot.getHostmask(compareWithNick);
				} catch (IrcException e) {
					return;
				}

				String target = compareWithNick;
				if (targethostmask != null)
					target = getLastfmNick(channel, compareWithNick,
							targethostmask.login, targethostmask.hostname);

				if (target.equals("")) {
					bot.sendNotice(
							sender,
							compareWithNick
									+ (" has asked to be left out of ?compare. "
											+ "Probably just embarrassed.")
											.replace("?", prefix));
					return;
				}

				final String compareTarget = target;

				Thread t = new Thread(new Runnable() {

					@Override
					public void run() {
						ComparisonResult res = null;
						String errormsg = "";
						try {
							res = Tasteometer.compare(
									Tasteometer.InputType.USER, lastfmnick,
									Tasteometer.InputType.USER, compareTarget,
									apiKey);
						} catch (CallException e) {
							res = null;
							errormsg = e.getMessage();
						}
						if (res == null) {
							bot.sendMessage(channel,
									"Error, could not use tasteometer on "
											+ sender + " and "
											+ compareWithNick + " " + errormsg);
							return;
						}
						StringBuilder response = new StringBuilder(sender);
						response.append(" and ")
								.append(compareWithNick)
								.append(" are ")
								.append(Math.round(res.getScore() * 100))
								.append("% compatible! Artists they have in common: ");
						Collection<Artist> matches = res.getMatches();
						for (Artist a : matches) {
							response.append(a.getName()).append(", ");
						}
						// clean up the comma.
						if (!matches.isEmpty())
							response.replace(response.lastIndexOf(","),
									response.length(), ".");

						bot.sendMessage(channel, response.toString());
					}
				});
				t.start();
			}
		});
		stuff.start();

	}

	/**
	 * @param target
	 *            where to post the message
	 * @param login
	 *            login to link
	 * @param hostname
	 *            hostname to link
	 * @param lastfmnick
	 *            lastfm nick to link
	 */
	private void linkUser(String target, String login, String hostname,
			String lastfmnick) {
		try {
			CallableStatement stmt = dbc.getDb().prepareCall(
					UPDATE_USERNAME_QUERY);
			stmt.setString(1, lastfmnick);
			stmt.setString(2, login);
			stmt.setString(3, hostname);
			stmt.execute();
			if (stmt.getUpdateCount() != 1) {
				// inserten
				stmt = dbc.getDb().prepareCall(INSERT_USERNAME_QUERY);
				stmt.setString(1, login);
				stmt.setString(2, hostname);
				stmt.setString(3, lastfmnick);
				stmt.execute();
			}
			if (!lastfmnick.equals(""))
				bot.sendMessage(target, "Ok, so you are http://last.fm/user/"
						+ lastfmnick);
		} catch (SQLException e) {
			bot.sendMessage(target,
					"Shit broke. badly. :( (SQL " + e.getMessage() + ")");
			bot.sendMessage(target, "Paging " + bot.getOwner()
					+ ", blame them.");
			e.printStackTrace();
		}
	}

	/**
	 * @param target
	 * @param nick
	 * @param lastfmnick
	 */
	private void sendNowPlaying(final String target, String nick,
			final String lastfmnick) {

		final String usernick = nick;

		Thread thread = new Thread(new Runnable() {

			@Override
			public void run() {
				StringBuilder response = new StringBuilder(usernick);
				PaginatedResult<Track> result = null;
				Result lastResult = null;
				try {
					result = User.getRecentTracks(lastfmnick, 1, 1, apiKey);
					lastResult = Caller.getInstance().getLastResult();
				} catch (CallException e) {
					bot.sendMessage(target, "Error: " + e.getMessage());

					return;
				}
				if (!lastResult.isSuccessful()) {
					bot.sendMessage(target,
							usernick + ": " + lastResult.getErrorMessage());
					return;
				}
				Collection<Track> tracks = result.getPageResults();
				if (tracks.size() == 0) {
					response.append(" has no recently played tracks according to Last.fm");

				}
				for (Track t : tracks) {

					Track fetchedTrack = null;
					try {
						if (t.getMbid() != null) {
							fetchedTrack = Track.getInfo(null, t.getMbid(),
									Locale.ENGLISH, lastfmnick, apiKey);
						} else {
							fetchedTrack = Track.getInfo(t.getArtist(),
									t.getName(), Locale.ENGLISH, lastfmnick,
									apiKey);
						}
					} catch (CallException e) {
						response.append(" ").append(e.getMessage());
						bot.sendMessage(target, response.toString());
						return;
					}
					if (fetchedTrack == null) {
						fetchedTrack = t;
					}

					if (t.isNowPlaying())
						response.append(" is now playing: ");
					else {
						if (System.currentTimeMillis()
								- t.getPlayedWhen().getTime() > 1000 * 60 * 20) {
							bot.sendMessage(
									target,
									usernick
											+ " has not played anything in the last 20 minutes.");
							return;
						}
						response.append(" was just playing: ");
					}

					response.append(t.getArtist());
					response.append(" - ");
					response.append(t.getName());

					// from the album xyz
					if (t.getAlbum() != null && t.getAlbum().trim() != "") {
						response.append(" from the album ");
						response.append(t.getAlbum());
					}

					// plays
					if (fetchedTrack.getUserPlaycount() > 1) {
						response.append(" (")
								.append(fetchedTrack.getUserPlaycount())
								.append(" plays)");
					} else if(fetchedTrack.getUserPlaycount() == 1) {
						response.append(" (1 play)");
					}

					// tags
					Collection<String> tags = fetchedTrack.getTags();
					if (tags.size() > 0) {
						response.append(" (");
						int i = 0;
						for (String tag : tags) {
							if (++i > 4)
								break;
							response.append(tag).append(", ");
						}

						response.replace(response.lastIndexOf(", "),
								response.length(), ")");
					}

					if (!t.isNowPlaying()) {
						response.append(" (Played ");
						response.append(formatDate(t.getPlayedWhen()));
						response.append(")");
					}

					// can't really just do one.
					break;
				}

				bot.sendMessage(target, response.toString());

			}

			private String formatDate(Date playedWhen) {
				long diff = System.currentTimeMillis() - playedWhen.getTime();
				int seconds = (int) Math.floor(((diff / 1000F) % 60));
				int minutes = (int) Math.floor(((diff / 1000F / 60 % 60)));
				int hours = (int) Math.floor((diff / 1000F / 60 / 60 % 24));
				int days = (int) Math.floor(((diff / 1000F / 60 / 60 / 24)));

				StringBuilder formattedDate = new StringBuilder();

				if (days > 1)
					formattedDate.append(days).append(" days,");
				else if (days == 1)
					formattedDate.append(days).append(" day,");
				if (hours > 1)
					formattedDate.append(hours).append(" hours, ");
				else if (hours == 1)
					formattedDate.append(hours).append(" hour, ");
				if (minutes > 1)
					formattedDate.append(minutes).append(" minutes, ");
				else if (minutes == 1)
					formattedDate.append(minutes).append(" minute, ");
				if (seconds > 1)
					formattedDate.append(seconds).append(" seconds  ");
				else if (seconds == 1)
					formattedDate.append(seconds).append(" second  ");

				String response;
				if (formattedDate.length() <= 2) {
					return "just now";
				}
				response = formattedDate.substring(0,
						formattedDate.length() - 2);

				return response + " ago";
			}
		});
		thread.start();
	}

	/**
	 * @param target
	 * @param nick
	 * @param login
	 * @param hostname
	 */
	private String getLastfmNick(final String target, final String nick,
			final String login, final String hostname) {
		String lastfmname = nick;
		try {
			CallableStatement stmt = dbc.getDb().prepareCall(
					SELECT_USERNAME_QUERY);
			stmt.setString(1, login.toLowerCase());
			stmt.setString(2, hostname.toLowerCase());
			if (stmt.execute()) {
				ResultSet rs = stmt.getResultSet();
				if (rs.next())
					lastfmname = rs.getString(1);
			}
			return lastfmname;
		} catch (SQLException e) {
			e.printStackTrace();
			bot.sendMessage(target,
					"Shit broke. badly. :( (SQL ERROR " + e.getMessage() + ")");
			bot.sendMessage(target, "Paging " + bot.getOwner()
					+ ", blame them.");
			return "";
		}

	}

	/**
	 * 
	 * 
	 * @param sender
	 * @param login
	 * @param hostname
	 * @return if the timeout has expired for sender
	 */
	private boolean mayExecute(String sender, String login, String hostname) {
		if (AccessList.isAllowed(login, hostname, Privileges.AUTHORIZED))
			return true;
		String hostmask = login + "@" + hostname;

		if (this.lastExecuted.containsKey(hostmask)) {
			long diff = System.currentTimeMillis() - lastExecuted.get(hostmask);
			if (diff / 1000F < cooldownTime) {
				bot.sendNotice(
						sender,
						"You need to wait another "
								+ Math.round(cooldownTime - (diff / 1000F))
								+ " seconds before you can use this command again");
				return false;
			}
		}
		lastExecuted.put(hostmask, System.currentTimeMillis());
		return true;

	}

	/**
	 * @param sender
	 * @param login
	 * @param hostname
	 * @param message
	 */
	@Override
	public void notifyPrivateMessageEvent(String sender, String login,
			String hostname, String message) {
		Matcher setUserCommandMatcher = setUserCommand.matcher(message);
		Matcher helpCommandMatcher = helpCommand.matcher(message);
		if (setUserCommandMatcher.matches()) {
			linkUser(sender, login, hostname, setUserCommandMatcher.group(1)
					.trim());
		} else if(helpCommandMatcher.matches()) {
			bot.sendMessage(
					sender,
					HELP_RESPONSE.replace("?", prefix).replace("##",
							cooldownTime.toString()));
			bot.sendMessage(sender, HELP_RESPONSE2.replace("?", prefix));
			return;
		}

	}

}
