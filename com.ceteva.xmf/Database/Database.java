package Database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Hashtable;

import Engine.Machine;
import Engine.Value;

public class Database {
	
	private static Hashtable connections = new Hashtable();
	private static Hashtable queries = new Hashtable();

	public static void autoCommit(Machine machine) {
		Connection con = getConnection(machine,machine.frameLocal(0));
		boolean auto = true;
		if(machine.frameLocal(1) == machine.falseValue)
			auto = false;
		try {
			con.setAutoCommit(auto);
			machine.pushStack(machine.trueValue);
			machine.popFrame();
		} catch (SQLException e) {
			e.printStackTrace();
			machine.error(machine.ERROR,"An error occured during setting the mode of a DB auto commit: " + e);
		}
	}
	
	public static void close(Machine machine) {
		try {
			Connection con = getConnection(machine,machine.frameLocal(0));
			con.close();
			int word = machine.value(machine.frameLocal(0));
			connections.remove(new Integer(word));
			machine.pushStack(machine.trueValue);
			machine.popFrame();
		} catch (SQLException e) {
			e.printStackTrace();
			machine.error(machine.ERROR,"An error occured during closing a DB connection: " + e);
		}
	}
	
	public static void commit(Machine machine) {
		Connection con = getConnection(machine,machine.frameLocal(0));
		try {
			con.commit();
			machine.pushStack(machine.trueValue);
			machine.popFrame();
		} catch (SQLException e) {
			e.printStackTrace();
			machine.error(machine.ERROR,"An error occured during a DB commit: " + e);
		}
	}

	public static void connect(Machine machine) {
		try {
			Class.forName("com.mysql.jdbc.Driver").newInstance();
		} catch (InstantiationException e1) {
			e1.printStackTrace();
		} catch (IllegalAccessException e1) {
			e1.printStackTrace();
		} catch (ClassNotFoundException e1) {
			e1.printStackTrace();
		}
		String url = machine.valueToString(machine.frameLocal(0));
		String username = machine.valueToString(machine.frameLocal(1));
		String password = machine.valueToString(machine.frameLocal(2));
		try {
			Connection con = DriverManager.getConnection(url,username,password);
			int size = connections.size();
	        connections.put(new Integer(size),con);
	        machine.pushStack(mkDatabaseChannel(machine,size));
	        machine.popFrame();
		} catch (SQLException e) {
			e.printStackTrace();
			machine.error(machine.ERROR,"An error occured during a DB connection: " + e);
		}
	}
	
	public static Connection getConnection(Machine machine,int obj) {
		int word = machine.value(obj);
		return (Connection)connections.get(new Integer(machine.intValue(word)));
	}
	
	public static ResultSet getQueryResult(Machine machine,int obj) {
		int word = machine.value(obj);
		return (ResultSet)queries.get(new Integer(machine.intValue(word)));
	}
	
	public static void loadDriver(Machine machine) {
		String connection = machine.valueToString(machine.frameLocal(0));
		try {
			Class.forName(connection);
			machine.pushStack(Machine.trueValue);
	        machine.popFrame();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			machine.error(machine.ERROR,"An error occured during loading a DB driver: " + e);
		}
	}
	
	public static final int mkDatabaseChannel(Machine machine,int index) {
        return machine.mkImmediate(Value.DATABASE, machine.mkInt(index));
    }
	
	public static final int mkQueryResult(Machine machine,int index) {
        return machine.mkImmediate(Value.QUERYRESULT, machine.mkInt(index));
    }
	
	public static void query(Machine machine) {
		int connection = machine.intValue(machine.frameLocal(0));
		String query = machine.valueToString(machine.frameLocal(1));
		Connection con = (Connection)connections.get(new Integer(connection));
		try {
			Statement stmt = con.createStatement();
			ResultSet resultSet = stmt.executeQuery(query);
			int size = queries.size();
		    queries.put(new Integer(size),resultSet);
		    machine.pushStack(mkQueryResult(machine,size));
			machine.popFrame(); 
		} catch (SQLException e) {
			e.printStackTrace();
			machine.pushStack(machine.mkInt(-1));
			machine.error(machine.ERROR,"An error occured during loading a DB query: " + e);
		}
	}

	public static void queryClose(Machine machine) {
		int word = machine.value(machine.frameLocal(0));
		queries.remove(new Integer(word));
		machine.popFrame();
	}
	
	public static void queryResultLookup(Machine machine) {
		ResultSet resultSet = getQueryResult(machine,machine.frameLocal(0));
		int column = machine.intValue(machine.frameLocal(1));
		String type = machine.valueToString(machine.frameLocal(2));
		try {
			if(type.equals("String")) {
			  int string = machine.mkString(resultSet.getString(column));
			  machine.pushStack(string);
			  machine.popFrame();
			}
			else if(type.equals("Integer")) {
		      int integer = machine.mkInt(resultSet.getInt(column));
			  machine.pushStack(integer);
			  machine.popFrame();
			}
			else if(type.equals("Boolean")) {
			  int bool = machine.mkBool(resultSet.getBoolean(column));
			  machine.pushStack(bool);
		      machine.popFrame();
			}
			else {
		      machine.error(machine.ERROR,"A query lookup must be of type String/Integer/Boolean");	
			}
		} 
		catch (SQLException e) {
			e.printStackTrace();
			machine.error(machine.ERROR,"An error occured during loading a DB query result lookup: " + e);
		}
	}
	
	public static void queryResultNext(Machine machine) {
		ResultSet resultSet = getQueryResult(machine,machine.frameLocal(0));
		try {
			int value = resultSet.next() ? machine.trueValue : machine.falseValue; 
			machine.pushStack(value);
			machine.popFrame();
		} 
		catch (SQLException e) {
			e.printStackTrace();
			machine.error(machine.ERROR,"An error occured during loading a DB query result next: " + e);
		}
	}
	
	public static void queryResultPrevious(Machine machine) {
		ResultSet resultSet = getQueryResult(machine,machine.frameLocal(0));
		try {
			int value = resultSet.previous() ? machine.trueValue : machine.falseValue; 
			machine.pushStack(value);
			machine.popFrame(); 
		} 
		catch (SQLException e) {
			e.printStackTrace();
			machine.error(machine.ERROR,"An error occured during loading a DB query result previous: " + e);
		}
	}
	
	public static void update(Machine machine) {
		int connection = machine.intValue(machine.frameLocal(0));
		String statement = machine.valueToString(machine.frameLocal(1));
		Connection con = (Connection)connections.get(new Integer(connection));
		try {
			Statement stmt = con.createStatement();
			stmt.executeUpdate(statement);
			machine.pushStack(machine.trueValue);
			machine.popFrame();
		} catch (SQLException e) {
			e.printStackTrace();
			machine.error(machine.ERROR,"An error occured during loading a DB update: " + e);
		} 
	}
	
}
