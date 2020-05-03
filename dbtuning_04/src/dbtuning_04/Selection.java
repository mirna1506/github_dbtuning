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
    private static int randomPubidsSize;
    private static int randomBooktitlesSize;
    private static int randomYearsSize;
    private static int randomListSize;
    private static boolean firstTest;
    
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
            
            testIndex(con, "BTREE", true);
            testIndex(con, "BTREE", false);
            testIndex(con, "HASH", false);
            testIndex(con, null, false);
        } else {
            System.err.println("Can't work with a null value connection.");
            return;
        }
    }
    
    private static void testIndex(Connection con, String indexType, boolean cluster)
            throws SQLException, IOException, FileNotFoundException {
        long startTs;
        long stopAt;
        long stopTs;
        float duration;
        int executedQueries;
        
        cleanAndInitializeDB(con, true);
        
        if (firstTest) {
            fetchRandomValues(con);
            firstTest = false;
        }
        
        System.out.println("Starting " + (cluster ? "clustering" : (indexType != null ? "non-clustering" : ""))
                + " " + (indexType != null ? indexType : "table scan") + " index test...");
        
        executedQueries = 0;
        if (indexType != null) {
            createIndex(con, "PUBID", indexType, cluster);
        }
        System.out.print("\tStarting point query test (pubID = ...)... ");
        startTs = System.nanoTime();
        stopAt = startTs + ((long) 60 * 1000 * 1000 * 1000); // stop after one minute 
        while (System.nanoTime() < stopAt) {
            randomPubids.absolute(RAND_GEN.nextInt(randomPubidsSize) + 1);
            con.createStatement().execute("SELECT * FROM \"Publ\" WHERE \"pubID\" = '"
                    + randomPubids.getString(1) + "';");
            executedQueries++;
        }
        stopTs = System.nanoTime();
        duration = ((float) (stopTs - startTs)) / 1000 / 1000 / 1000;
        System.out.println("ok\n\tStopped after: " + duration + "s");
        System.out.println("\tThroughput [1/s]: " + (executedQueries / ((stopTs - startTs) / 1000 / 1000 / 1000)));
        
        executedQueries = 0;
        dropIndexes(con);
        if (indexType != null) {
            createIndex(con, "BOOKTITLE", indexType, cluster);
        }
        System.out.print("\n\tStarting low selectivity test (booktitle = ...)... ");
        startTs = System.nanoTime();
        stopAt = startTs + ((long) 60 * 1000 * 1000 * 1000); // stop after one minute 
        while (System.nanoTime() < stopAt) {
            randomPubids.absolute(RAND_GEN.nextInt(randomBooktitlesSize) + 1);
            con.createStatement().execute("SELECT * FROM \"Publ\" WHERE \"pubID\" = '"
                    + randomBooktitles.getString(1) + "';");
            executedQueries++;
        }
        stopTs = System.nanoTime();
        duration = ((float) (stopTs - startTs)) / 1000 / 1000 / 1000;
        System.out.println("ok\n\tStopped after: " + duration + "s");
        System.out.println("\tThroughput [1/s]: " + (executedQueries / ((stopTs - startTs) / 1000 / 1000 / 1000)));
        
        // noch zu umbauen beginn
//        executedQueries = 0;
//        dropIndexes(con);
//        if (indexType != null) {
//            createIndex(con, "LIST", indexType, cluster);
//        }
//        System.out.print("\n\tStarting low selectivity test with multipoint Query with IN predicate ");
//        startTs = System.nanoTime();
//        stopAt = startTs + ((long) 60 * 1000 * 1000 * 1000); // stop after one minute 
//        while (System.nanoTime() < stopAt) {
//            randomPubids.absolute(RAND_GEN.nextInt(randomListSize) + 1);
//            con.createStatement().execute("SELECT * FROM \"Publ\" WHERE \"pubID\" IN '"
//                    + randomYears.getString(1) + "';");
//            executedQueries++;
//        }
//        stopTs = System.nanoTime();
//        duration = ((float) (stopTs - startTs)) / 1000 / 1000 / 1000;
//        System.out.println("ok\n\tStopped after: " + duration + "s");
//        System.out.println("\tThroughput [1/s]: " + (executedQueries / ((stopTs - startTs) / 1000 / 1000 / 1000)));
//        System.out.println("ok\n");
    	// noch zu umbauen ende
        
        executedQueries = 0;
        dropIndexes(con);
        if (indexType != null) {
            createIndex(con, "YEAR", indexType, cluster);
        }
        System.out.print("\n\tStarting high selectivity test (year = ...)... ");
        startTs = System.nanoTime();
        stopAt = startTs + ((long) 60 * 1000 * 1000 * 1000); // stop after one minute 
        while (System.nanoTime() < stopAt) {
            randomPubids.absolute(RAND_GEN.nextInt(randomYearsSize) + 1);
            con.createStatement().execute("SELECT * FROM \"Publ\" WHERE \"pubID\" = '"
                    + randomYears.getString(1) + "';");
            executedQueries++;
        }
        stopTs = System.nanoTime();
        duration = ((float) (stopTs - startTs)) / 1000 / 1000 / 1000;
        System.out.println("ok\n\tStopped after: " + duration + "s");
        System.out.println("\tThroughput [1/s]: " + (executedQueries / ((stopTs - startTs) / 1000 / 1000 / 1000)));
        System.out.println("ok\n");
    }
    
    private static void cleanAndInitializeDB(Connection con, boolean initialize) 
    		throws SQLException, IOException, FileNotFoundException {
		
    	CopyManager cm = new CopyManager((BaseConnection) con);
		
		System.out.print("Cleaning db... ");
		con.createStatement().execute("DROP TABLE IF EXISTS \"Auth\";");
		con.createStatement().execute("CREATE TABLE \"Auth\" (\n" +
		                              " \"name\"    varchar(49),\n" +
		                              " \"pubID\"   varchar(129)\n" +
		                              ");");
		con.createStatement().execute("DROP TABLE IF EXISTS \"Publ\";");
		con.createStatement().execute("CREATE TABLE \"Publ\" (\n" +
		                              " \"pubID\"   varchar(129),\n" +
		                              " \"type\"    varchar(13),\n" +
		                              " \"title\"   varchar(700),\n" +
		                              " \"booktitle\"   varchar(132),\n" +
		                              " \"year\"    varchar(4),\n" +
		                              " \"publisher\"   varchar(196),\n" +
		                              " PRIMARY KEY(\"pubID\")" +
		                              ");");
		System.out.println("ok");
		
		if (initialize) {
		    System.out.print("Initializing tables... ");
		    
		    // use copymanager to handle the stdin side
		    cm.copyIn("COPY \"Auth\" FROM stdin", new FileInputStream("auth_small.tsv")); 
		    cm.copyIn("COPY \"Publ\" FROM stdin", new FileInputStream("publ_small.tsv")); 
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
        randomYears = con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY)
                .executeQuery("SELECT \"year\" FROM \"Publ\" TABLESAMPLE BERNOULLI(5);");
        randomYears.last();
        randomYearsSize = randomYears.getRow();
        System.out.println("ok");
    }
    
    private static void createIndex(Connection con, String column, String indexType, boolean cluster) 
    		throws SQLException {
        con.createStatement().execute("CREATE INDEX \"publ_" + column + "_idx\" ON \"Publ\""
                + " USING " + indexType + "(\"pubID\");");
        if (cluster && indexType != "HASH") {
            con.createStatement().execute("CLUSTER \"Publ\" USING \"publ_" + column + "_idx\";");
            con.createStatement().execute("ANALYZE \"Publ\";");
        }
    }
    
    private static void dropIndexes(Connection con) throws SQLException {
        con.createStatement().execute("DROP INDEX IF EXISTS \"publ_pubid_idx\";");
        con.createStatement().execute("DROP INDEX IF EXISTS \"publ_booktitle_idx\";");
        con.createStatement().execute("DROP INDEX IF EXISTS \"publ_year_idx\";");
    }
    
}