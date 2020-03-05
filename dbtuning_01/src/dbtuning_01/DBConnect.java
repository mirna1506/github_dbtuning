package dbtuning_01;

import java.sql.*;

public class DBConnect {
    public static void main(String[] args) throws Exception {
	try {
	    Class.forName ( "org.postgresql.Driver" );
	    System.err.println("Driver found.");
	} catch ( java.lang.ClassNotFoundException e ) {
	    System.err.println("PostgreSQL JDBC Driver not found ... ");
	    e.printStackTrace();
	    return;
	}

	String host = "biber.cosy.sbg.ac.at";
	String port = "5432";
	String database = "dbtuning_ss2020";
	String pwd = "huth8lithe5E";
	String user = "ibrezovic";
	String url = "jdbc:postgresql://" + host + ":" + port + "/" + database;
	Connection con = null;
	try {
	    con = DriverManager.getConnection(url, user, pwd);
	    System.err.println("Connection established.");
	} catch (Exception e) {
	    System.err.println("Could not establish connection.");
	    e.printStackTrace();
	    return;
	}
	
	
	
	try {
	    String qry = "SELECT table_name " +
		"FROM information_schema.tables " +
		"WHERE table_schema='public' "+
		"AND table_type='BASE TABLE'";
	    ResultSet rs = con.createStatement().executeQuery(qry);
	    System.out.println("Query sucessful.\n---\n" +
			       "List of tables in database '" +
			       database + "':");
	    while (rs.next()) {
		System.out.println(rs.getString(1));
	    }
	} catch (Exception e) {
	    System.err.println("Query was not successful.");
	    e.printStackTrace();
	}
    }
}
