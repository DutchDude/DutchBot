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
package cd.what.DutchBot;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Manager for the database
 * 
 * @author DutchDude
 * 
 */
public class DatabaseConnection {

	/**
	 * Container for the singleton instance
	 */
	private Connection db = null;

	/**
	 * store the instance of the object
	 */
	private static DatabaseConnection instance = null;

	/**
	 * Singleton accessor
	 * 
	 * @return DatabaseConnection instance
	 */
	public static DatabaseConnection getInstance() {
		if (instance == null) {
			instance = new DatabaseConnection();
		}
		return instance;
	}

	/**
	 * Singleton constructor
	 */
	private DatabaseConnection() {

	}

	/**
	 * Connect to the db
	 * 
	 * @param database
	 * @param username
	 * @param password
	 */
	public void connect(String host, String database, String username,
			String password) {
		try {
			this.db = DriverManager.getConnection("jdbc:postgresql://" + host
					+ "/" + database + "?user=" + username + "&password="
					+ password + "&ssl=true"
					+ "&sslfactory=org.postgresql.ssl.NonValidatingFactory");
		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

	/**
	 * Query the db
	 * 
	 * @param query
	 * @return resultset of the query
	 */
	public ResultSet query(String query) {
		try {
			Statement s = db.createStatement();
			ResultSet rs = s.executeQuery(query);
			return rs;
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}

	public Connection getDb() {
		return db;
	}
}
