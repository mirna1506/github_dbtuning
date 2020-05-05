package dbtuning_04;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Random;

import org.postgresql.core.BaseConnection;
import org.postgresql.copy.CopyManager;

public class Selection {

	private static final Random RAND_GEN = new Random();
    private static ResultSet randomPubids;
    private static ResultSet randomBooktitles;
    private static ResultSet randomYears;
    private static ResultSet randomList;
    private static ResultSet randomList1;
    private static ResultSet randomList2;
    
    private static int randomPubidsSize;
    private static int randomBooktitlesSize;
    private static int randomYearsSize;
    private static int randomListSize;
    private static int randomListSize1;
    private static int randomListSize2;
    private static boolean firstTest;
    private static int j; //for explain analyze
    
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
		
        if (con != null) {
            firstTest = true;
            
            testIndex(con, "BTREE",  true);
            testIndex(con, "BTREE", false);
            testIndex(con, "HASH",  false);
            testIndex(con,  null,   false); //table scan
            
        } else {
            System.err.println("Can't work with a null value connection."); 
            return;
        }
    }
    
    private static void createandclean(Connection con, boolean initialize) 
    		throws SQLException, IOException, FileNotFoundException {
    	
    	CopyManager cm = new CopyManager((BaseConnection) con);
		
		System.out.print("Cleaning db... ");
		
		con.createStatement().execute("DROP TABLE IF EXISTS \"Auth\";");
		
		con.createStatement().execute("CREATE TABLE \"Auth\" (\n"     +
		                              " \"name\"      varchar(49),\n" +
		                              " \"pubID\"    varchar(129)\n"  +
		                              ");");
		
		con.createStatement().execute("DROP TABLE IF EXISTS \"Publ\";");
		
		con.createStatement().execute("CREATE TABLE \"Publ\"  (\n"      +
		                              " \"pubID\"      varchar(129),\n" +
		                              " \"type\"       varchar(13),\n"  +
		                              " \"title\"      varchar(700),\n" +
		                              " \"booktitle\"  varchar(132),\n" +
		                              " \"year\"       varchar(4),\n"   +
		                              " \"publisher\"  varchar(196),\n" +
		                              " PRIMARY KEY(\"pubID\")"         +
		                              ");");
		System.out.println("ok");
		
		if (initialize) {
		    System.out.print("Initializing tables... ");
		    
		    // use Copymanager to handle the stdin
		    cm.copyIn("COPY \"Auth\" FROM stdin", new FileInputStream("auth.tsv")); 
		    cm.copyIn("COPY \"Publ\" FROM stdin", new FileInputStream("publ.tsv")); 
		    
		    System.out.println("ok");
		    
		}
    }
  
    private static void fetchRandomValues(Connection con) throws SQLException {
        System.out.print("Fetching random values... ");
        
        randomPubids = con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY)
                .executeQuery("SELECT \"pubID\" FROM \"Publ\" TABLESAMPLE BERNOULLI(5);");
        randomPubids.last();
        randomPubidsSize = randomPubids.getRow();
        
        randomBooktitles = con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY)
                .executeQuery("SELECT \"booktitle\" FROM \"Publ\" TABLESAMPLE BERNOULLI(5);");
        randomBooktitles.last();
        randomBooktitlesSize = randomBooktitles.getRow();
        
        randomList = con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY)
                .executeQuery("SELECT \"name\", \"pubID\" FROM \"Auth\" TABLESAMPLE BERNOULLI(5);");
        randomList.last();
        randomListSize = randomList.getRow();
        
        randomList1 = con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY)
                .executeQuery("SELECT \"name\", \"pubID\" FROM \"Auth\" TABLESAMPLE BERNOULLI(5);");
        randomList1.last();
        randomListSize1 = randomList1.getRow();
        
        randomList2 = con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY)
                .executeQuery("SELECT \"name\", \"pubID\" FROM \"Auth\" TABLESAMPLE BERNOULLI(5);");
        randomList2.last();
        randomListSize2 = randomList2.getRow();

        
        randomYears = con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY)
                .executeQuery("SELECT \"year\" FROM \"Publ\" TABLESAMPLE BERNOULLI(5);");
        randomYears.last();
        randomYearsSize = randomYears.getRow();
        
        System.out.println("ok");
        System.out.println("\n");
    }
    
    private static void createIndex(Connection con, String column, String indexType, boolean cluster) 
    		throws SQLException {
    	
    	con.createStatement().execute("CREATE INDEX \"publ_" + column + "_idx\" ON \"Publ\""
    			+ " USING " + indexType + "(\"pubID\");");
    	
    	if (cluster && indexType != "HASH") {
    		con.createStatement().execute("CLUSTER \"Publ\" USING \"publ_" + column + "_idx\";");
            con.createStatement().execute("ANALYZE \"Publ\";"); //ANALYZE after creating or  changing a table
        }
    }
    
    private static void testIndex(Connection con, String indexType, boolean cluster)
    		throws SQLException, IOException, FileNotFoundException {
    	
    	long startTime;
        long stopAt;
        long stopTime;
        float duration;
        int executedQueries;
        
        createandclean(con, true);
        
        if (firstTest) {
            fetchRandomValues(con);
            firstTest = false;
        }
        
        System.out.println("Starting " + (cluster ? "clustering" : (indexType != null ? "non-clustering" : ""))
                + " " + (indexType != null ? indexType : "table scan") + " index test:");
        
        System.out.print("\n");
        
//        System.out.println("--------------");  
//        System.out.println(randomList.getString(2));
//        System.out.println(randomList1.getString(2));
//        System.out.println(randomList2.getString(2));
//        //System.out.println(randomList.getString(2));         
//        System.out.println("--------------"); 
        
        //QUERY 1 Start
        executedQueries = 0;
        if (indexType != null) {
            createIndex(con, "PUBID", indexType, cluster);
        }
        
        System.out.print("---QUERY 1 START---");
        System.out.print("\n");
        System.out.print("Starting point query test (pubID = ...)... ");
        System.out.print("\n");
        System.out.print("\n");
        startTime = System.nanoTime();
        stopAt = startTime + ((long) 60 * 1000000000 ); // stop after one minute 
 
        System.out.println("--EXPLAIN ANALYZE QUERY 1--");
        
        j = 0; //for explain analyze
        while (System.nanoTime() < stopAt) {
        	randomPubids.absolute(RAND_GEN.nextInt(randomPubidsSize) + 1);
        	String qry1 = "SELECT * FROM \"Publ\" WHERE \"pubID\" = '"
        			+ randomPubids.getString(1) + "';";
        	explain_analyze(con, qry1);
        	executedQueries++;
        }
        
        stopTime = System.nanoTime();
        duration = ((float) (stopTime - startTime)) / 1000000000;
        System.out.println("\n Point query test ok! \n\t Elapsed time: " + duration + " seconds");
        System.out.println("\t Throughput [1/s]: " + (executedQueries / ((stopTime - startTime) / 1000 / 1000 / 1000)));
        System.out.print("---QUERY 1 END---");
        System.out.print("\n");
        System.out.print("\n");
        //QUERY 1 End
        
        
        //QUERY 2 Start
        executedQueries = 0;
        dropIndexes(con);
        if (indexType != null) {
            createIndex(con, "BOOKTITLE", indexType, cluster);
        }
        
        System.out.print("---QUERY 2 START---");
        System.out.print("\n");
        System.out.print("Starting low selectivity test (booktitle = ...)... ");
        System.out.print("\n");
        System.out.print("\n");
        startTime = System.nanoTime();
        stopAt = startTime + ((long) 60 * 1000000000); // stop after one minute 
        
        System.out.println("--EXPLAIN ANALYZE QUERY 2--");
        
        j = 0; //for explain analyze
        while (System.nanoTime() < stopAt) {
        	randomPubids.absolute(RAND_GEN.nextInt(randomBooktitlesSize) + 1);
            String qry2 = "SELECT * FROM \"Publ\" WHERE \"pubID\" = '"
            		+ randomBooktitles.getString(1) + "';";
            explain_analyze(con, qry2);
            executedQueries++;
        }
        
        stopTime = System.nanoTime();
        duration = ((float) (stopTime - startTime)) / 1000000000;
        System.out.println("\n Low selectivity test ok! \n\t Elapsed time: " + duration + "seconds");
        System.out.println("\t Throughput [1/s]: " + (executedQueries / ((stopTime - startTime) / 1000 / 1000 / 1000)));
        System.out.print("---QUERY 2 END---");
        System.out.print("\n");
        System.out.print("\n");
        //QUERY 2 End
        
        
        //QUERY 3 Start
        executedQueries = 0;
        dropIndexes(con);
        if (indexType != null) {
            createIndex(con, "LIST", indexType, cluster);
        }
        
        System.out.print("---QUERY 3 START---");
        System.out.print("\n");
        System.out.print("Starting low selectivity test with multipoint Query with IN predicate: IN List (...)...");
        System.out.print("\n");
        System.out.print("\n");
        startTime = System.nanoTime();
        stopAt = startTime + ((long) 60 * 1000000000); // stop after one minute 
        
        System.out.println("--EXPLAIN ANALYZE QUERY 3--");
        
        j = 0; //for explain analyze
        while (System.nanoTime() < stopAt) {
            randomPubids.absolute(RAND_GEN.nextInt(randomListSize) +  1);
            randomPubids.absolute(RAND_GEN.nextInt(randomListSize1) + 1);
            randomPubids.absolute(RAND_GEN.nextInt(randomListSize2) + 1);
            String qry3 = "SELECT * FROM \"Publ\" WHERE \"pubID\" IN " + "(" 
            	   + "'"  + randomList.getString(2)  + "'" + ", "  
                   + "'"  + randomList1.getString(2) + "'" + ", " 
                   + "'"  + randomList2.getString(2) + "'" + ")" + ";";
            explain_analyze(con, qry3);
            executedQueries++;
        }
        
        stopTime = System.nanoTime();
        duration = ((float) (stopTime - startTime)) / 1000000000;
        System.out.println("\n Low selectivity test with multipoint ok! \n\t Elapsed time: " + duration + "seconds");
        System.out.println("\t Throughput [1/s]: " + (executedQueries / ((stopTime - startTime) / 1000000000)));
        System.out.print("---QUERY 3 END---");
        System.out.print("\n");
        System.out.print("\n");
        //QUERY 3 End
        
        
        //QUERY 4 Start
        executedQueries = 0;
        dropIndexes(con);
        if (indexType != null) {
            createIndex(con, "YEAR", indexType, cluster);
        }
        
        System.out.print("---QUERY 4 START---");
        System.out.print("\n");
        System.out.print("Starting high selectivity test (year = ...)... ");
        System.out.print("\n");
        System.out.print("\n");
        startTime = System.nanoTime();
        stopAt = startTime + ((long) 60 * 1000000000); // stop after one minute 
        
        System.out.println("--EXPLAIN ANALYZE QUERY 4--");
        
        j = 0; //for explain analyze
        while (System.nanoTime() < stopAt) {
            randomPubids.absolute(RAND_GEN.nextInt(randomYearsSize) + 1);
            con.createStatement().execute("SELECT * FROM \"Publ\" WHERE \"pubID\" = '"
                    + randomYears.getString(1) + "';");
            String qry4 = "SELECT * FROM \"Publ\" WHERE \"pubID\" = '"
                    + randomYears.getString(1) + "';";
             explain_analyze(con, qry4);
            executedQueries++;
        }
        
        stopTime = System.nanoTime();
        duration = ((float) (stopTime - startTime)) / 1000000000;
        System.out.println("\n High selectivity test ok! \n\t Elapsed time: " + duration + "seconds");
        System.out.println("\t Throughput [1/s]: " + (executedQueries / ((stopTime - startTime) / 1000000000)));
        System.out.print("---QUERY 4 END---");
        System.out.print("\n");
        System.out.print("\n");
        //QUERY 4 End
        
        System.out.println("Tests ok!\n");
    }
    

    private static void dropIndexes(Connection con) throws SQLException {
        con.createStatement().execute("DROP INDEX IF EXISTS \"publ_pubid_idx\";");
        con.createStatement().execute("DROP INDEX IF EXISTS \"publ_booktitle_idx\";");
        con.createStatement().execute("DROP INDEX IF EXISTS \"publ_year_idx\";");
    }
    
    public static void explain_analyze(Connection con, String str) throws SQLException{
    	ResultSet rs = con.createStatement().executeQuery("explain analyze " + str );
    	int i = 0;
    	i = j;
    	while (i < 4 && rs.next())
    	{
    	   System.out.println("\t" + rs.getString(1));
    	   i++;
    	   j = i;
    	}
    }	
      
}