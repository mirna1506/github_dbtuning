package dbtuning_02;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

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
        con.createStatement().execute("DROP TABLE IF EXISTS \"Employee\";");
        con.createStatement().execute("CREATE TABLE \"Exmployee\" (\n" +
                                      " \"ssnum\"       integer,\n" +
                                      " \"name\"        varchar(70),\n" +
                                      " \"manager\"     varchar(70),\n" +
                                      " \"dept\"        varchar(120),\n" +
                                      " \"salary\"      integer,\n" +
                                      " \"numfriends\"  smallint,\n" +
                                      " PRIMARY KEY     (\"ssnum\", \"name\")\n" +
                                      ");");
        con.createStatement().execute("DROP TABLE IF EXISTS \"Student\";");
        con.createStatement().execute("CREATE TABLE \"Student\" (\n" +
                                      " \"ssnum\"       integer,\n" +
                                      " \"name\"        varchar(70),\n" +
                                      " \"course\"      varchar(120),\n" +
                                      " \"grade\"       smallint,\n" +
                                      " PRIMARY KEY     (\"ssnum\", \"name\"),\n" +
                                      //" FOREIGN KEY     (\"ssnum\", \"name\") REFERENCES \"Employee\"(\"ssnum\", \"name\")\n" +
                                      ");");
        con.createStatement().execute("DROP TABLE IF EXISTS \"Techdept\";");
        con.createStatement().execute("CREATE TABLE \"Techdept\" (" +
                                      " \"dept\"        varchar(120),\n" +
                                      " \"manager\"     varchar(70),\n" +
                                      " \"location\"    varchar(120),\n" +
                                      " PRIMARY KEY     (\"dept\"),\n" +
                                      ");");
        // add missing foreign keys to "Employee"
        con.createStatement().execute("ALTER TABLE \"Employee\"\n" +
                                      //"ADD FOREIGN KEY (\"ssnum\", \"name\") REFERENCES \"Student\"(\"ssnum\", \"name\"),\n" +
                                      "ADD FOREIGN KEY (\"dept\") REFERENCES \"Techdept\"(\"dept\");");
    }
    
    //fills the tables with random data
    private static void filltableswithdata(Connection con) throws SQLException, IOException {
    	
    }
}
