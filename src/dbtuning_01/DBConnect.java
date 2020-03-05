//Brezovic, Demir, Mrazovic

package dbtuning_01;

import java.io.FileInputStream;
import java.sql.*;
import java.util.Scanner;

import org.postgresql.copy.CopyManager;
import org.postgresql.core.BaseConnection;

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
	
		//----------------------Connection to DB----------------------
		
		//---DBTuningss2020
		String host = "biber.cosy.sbg.ac.at";
		String port = "5432";
		String database = "dbtuning_ss2020";
		String pwd = "huth8lithe5E";
		String user = "ibrezovic";

		
		//---Localhost
		/*String host = "localhost";
		String port = "5432";
		String database = "postgres";
		String pwd = "postgres";
		String user = "postgres";*/
		
		
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
		
		//----------------------Create and Clean----------------------
		createandclean(con);
		
		//----------------------StraightForward----------------------
		straightForward(con);
		
		//----------------------Check----------------------
		//outputSelect(con, database);
		
		//----------------------Create and Clean----------------------
		createandclean(con);
		
		//----------------------Faster Approach----------------------
		copyManagerCopy(con);
    }
    
    private static void outputSelect(Connection con, String database) throws Exception {
    	String qry = "SELECT *" + "FROM \"Auth\";" ;
		ResultSet rs = con.createStatement().executeQuery(qry);
		System.out.println("Query sucessful.\n---\n" +
	       "List of entries in database '" +
	       database + "':");
		while (rs.next()) {
			System.out.println(rs.getString(1));
		}
    }
    
    private static void createandclean(Connection con) throws SQLException {
        con.createStatement().execute("DROP TABLE IF EXISTS \"Auth\";");
        con.createStatement().execute("CREATE TABLE \"Auth\" (\n" +
                                      "	\"name\"	varchar(49),\n" +
                                      "	\"pubID\"	varchar(129)\n" +
                                      ");");
        con.createStatement().execute("DROP TABLE IF EXISTS \"Publ\";");
        con.createStatement().execute("CREATE TABLE \"Publ\" (\n" +
                                      "	\"pubID\"	varchar(129),\n" +
                                      "	\"type\"	varchar(13),\n" +
                                      "	\"title\"	varchar(700),\n" +
                                      "	\"booktitle\"	varchar(132),\n" +
                                      "	\"year\"	varchar(4),\n" +
                                      "	\"publisher\"	varchar(196)\n" +
                                      ");");
    }
    
    //Den Code hier noch umschreiben!
    private static void straightForward(Connection con) throws Exception{
		//----------------------Read the file----------------------
		System.out.println("Straight Forward Approach...");
		String fileName = "dblp/auth_small.tsv";
        FileInputStream is = new FileInputStream(fileName);
        Scanner sc = new Scanner(is);
        
        //----------------------Time Measurment Start----------------------
        long startTS = System.nanoTime();
        int numRows = 0;
        
      //----------------------Zeilenweise Input----------------------
      while (sc.hasNextLine()) {
         String ln = sc.nextLine();
         String[] fields = ln.split("\t", 2);
         if (fields.length != 2) {
        	 System.err.println("Column count mismatch. Stopping!");
             return;
         }
                
         String valstr = "'" + fields[0].replaceAll("'","''");
         for (int i = 1; i < fields.length; i++) {
        	 // Escape ticks
             fields[i] = fields[i].replaceAll("'","''");
             valstr += "','" + fields[i];
         }
         valstr += "'";
                
         // INSERT query...
         int rs = con.createStatement().executeUpdate("INSERT INTO \"" + "Auth" + "\" VALUES (" + valstr + ")");
         if (rs == 0) {
        	 System.out.println("Error inserting a row!");
         } else {
        	 numRows++;
         }
       }
        
        //----------------------Time Measurment End----------------------
        long stopTS = System.nanoTime();        
        long duration = stopTS - startTS;
        float durationS = ((float) duration)/1000000000;
        
        System.out.printf("Test complete. Elapsed time: %.1fs (%.1f rows/s)\n", durationS, numRows/durationS);//TODO:
    }
    
    //Den Code hier noch umschreiben!
    private static void copyManagerCopy(Connection con) throws Exception {
    	String fileName = "dblp/auth.tsv";
        System.out.println("Copy Manager Approach...");
        CopyManager cm = new CopyManager((BaseConnection) con);
        
      //----------------------Time Measurment Start----------------------
        long startTS = System.nanoTime();
        long numRows = 0;
        
        // use copymanager to handle the stdin side
        numRows = cm.copyIn("COPY \"" + "Auth" + "\" FROM stdin", new FileInputStream(fileName));
        
      //----------------------Time Measurment Stop----------------------
        long stopTS = System.nanoTime();        
        long duration = stopTS - startTS;
        float durationS = ((float) duration)/1000000000;
        
        System.out.printf("Test complete. Elapsed time: %.1fs (%.1f rows/s)\n", durationS, numRows/durationS);
    }
}
