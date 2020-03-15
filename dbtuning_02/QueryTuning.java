package dbtuning_02;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import org.postgresql.copy.CopyManager;
import org.postgresql.core.BaseConnection;

public class QueryTuning {
    public static void main(String[] args) throws Exception {
		try {
		    Class.forName ( "org.postgresql.Driver" );
		    System.err.println("Driver found.");
		} catch ( java.lang.ClassNotFoundException e ) {
		    System.err.println("PostgreSQL JDBC Driver not found ... ");
		    e.printStackTrace();
		    return;
		}
	
		//----------------------Connection to DB----------------------
		
		//DBTuningss2020
		/*String host = "biber.cosy.sbg.ac.at";
		String port = "5432";
		String database = "dbtuning_ss2020";
		String pwd = "huth8lithe5E";
		String user = "ibrezovic";*/

		
		//Localhost
		String host = "localhost";
		String port = "5432";
		String database = "postgres";
		String pwd = "postgres";
		String user = "postgres";
		
		
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
		
		//----------------------Sequence Order BEGIN----------------------
		createandclean(con);
		filltableswithdata(con);
		//----------------------Sequence Order END------------------------
    }
    
    //creates the empty tables or cleans them if they existed befores
    private static void createandclean(Connection con) throws SQLException {
        System.out.print("Cleaning db... ");
        con.createStatement().execute("DROP TABLE IF EXISTS \"Employee\";");
        con.createStatement().execute("CREATE TABLE \"Employee\" (\n" +
                                      "	ssnum int UNIQUE,\n" +
                                      "	name	text UNIQUE,\n" +
                                      "	manager text,\n" +
                                      "	dept text,\n" +
                                      "	salary real,\n" +
                                      "	numfriends int,\n" +
                                      " PRIMARY KEY (ssnum, name));");
        
        con.createStatement().execute("DROP TABLE IF EXISTS \"Student\";");
        con.createStatement().execute("CREATE TABLE \"Student\" (\n" +
                                      "	ssnum int UNIQUE,\n" +
                                      "	name text UNIQUE,\n" +
                                      "	course text,\n" +
                                      "	grade real,\n" +
                                      " PRIMARY KEY (ssnum, name));");
        
        con.createStatement().execute("DROP TABLE IF EXISTS \"Techdept\";");
        con.createStatement().execute("CREATE TABLE \"Techdept\" (\n" +
                                      "	dept text UNIQUE,\n" +
                                      "	manager text,\n" +
                                      "	location text,\n" +
                                      " PRIMARY KEY (dept));");
        
        
        con.createStatement().execute("CREATE UNIQUE INDEX ON \"Employee\" (ssnum);");
        con.createStatement().execute("CREATE UNIQUE INDEX ON \"Employee\" (name);");
        con.createStatement().execute("CREATE INDEX ON \"Employee\" (dept);");
        
        con.createStatement().execute("CREATE UNIQUE INDEX ON \"Student\" (ssnum);");
        con.createStatement().execute("CREATE UNIQUE INDEX ON \"Student\" (name);");
        
        con.createStatement().execute("CREATE UNIQUE INDEX ON \"Techdept\" (dept);");
        System.out.println("ok");
    }
    
    //fills the tables with random data
    private static void filltableswithdata(Connection con) throws SQLException, IOException {
    	CopyManager cm = new CopyManager((BaseConnection) con);
    	
    	//Create 10 Techdepart and safe in array
    	
    	//Create 100k of Students with an random Techdepart out of array
    	
    	//Create 100k of Employee
    	
    	//See additional information in assignment02.pdfy
    	
    }
}
