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

import java.util.TreeMap;

/**
 * 
 * @author DutchDude
 * 
 */
public enum Privileges {
	OWNER(100),
	OPERATOR(50), 
	AUTHORIZED(10), 
	USER(0), // default access
	IGNORE(-10), 
	KILLONSIGHT(-200); //For those we truly hate

	private final int Privilegelevel;

	Privileges(int level) {
		this.Privilegelevel = level;
	}

	public int getValue() {
		return this.Privilegelevel;
	}

	private static TreeMap<Integer, Privileges> _map;
	static {
		_map = new TreeMap<Integer, Privileges>();
		for (Privileges num : Privileges.values()) {
			_map.put(num.getValue(), num);
		}
	}

	public static Privileges lookup(int value) {
		return _map.get(value);
	}

	@Override
	public String toString() {
		switch (this) {
		case OWNER:
			return "owner";
		case OPERATOR:
			return "operator";
		case AUTHORIZED:
			return "authorized";
		case USER:
			return "user";
		case IGNORE:
			return "ignore";
		case KILLONSIGHT:
			return "killonsight";
		default:
			return "undefined";
		}

	}

}