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