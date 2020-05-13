package dbtuning_05;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.postgresql.copy.CopyManager;
import org.postgresql.core.BaseConnection;

public class JoinTuning {
	
    private static enum tables {
        PUBL {
            public String toString() {
                return "Publ";
            }
        },
        AUTH {
            public String toString() {
                return "Auth";
            }
        }
    }
    
    private static enum joins {
        HASH {
            public String toString() {
                return "hashjoin";
            }
        },
        MERGE {
            public String toString() {
                return "mergejoin";
            }
        },
        NESTED_LOOP {
            public String toString() {
                return "nestloop";
            }
        }
    }

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
//		String host = "biber.cosy.sbg.ac.at";
//		String port = "5432";
//		String database = "dbtuning_ss2020";
//		String pwd = "Iechei5eexai";
//		String user = "mmrazovic";

    	
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
		
        task1(con);
        task2(con);
        task3(con);
        task4(con);
        
        // reenable all join strategies or else it will be forgotten...
        setJoinStrategies(con, new joins[]{joins.HASH, joins.MERGE, joins.NESTED_LOOP}, null);
    }
    
    private static void setJoinStrategies(Connection con, joins[] enable, joins[] disable) 
    		throws SQLException {
    	if (enable != null) {
            for (joins join : enable) {
                con.createStatement().execute("SET enable_" + join + " TO true;");
            }    
    	}
        if (disable != null) {
            for (joins join : disable) {
                con.createStatement().execute("SET enable_" + join + " TO false;");
            }
        }
    }
    
    private static void task1(Connection con) throws FileNotFoundException, SQLException, IOException {
        ResultSet explainAnalyze;
        
        setJoinStrategies(con, new joins[]{joins.NESTED_LOOP, joins.HASH, joins.MERGE}, null);
        
        System.out.println("1) Testing proposed join strategies: \n");
        
        // 1a
        System.out.println("\t1.a) Testing without indexes: \n");
        createandclean(con, true, false, false);
        explainAnalyze = firstJoin(con);
        while (explainAnalyze.next()) {
            System.out.println("\t\t" + explainAnalyze.getString(1));
        }
        explainAnalyze = secondJoin(con);
        while (explainAnalyze.next()) {
            System.out.println("\t\t" + explainAnalyze.getString(1));
        }        
        System.out.println("\t Task 1a ok. \n");
        
        // 1b
        System.out.println("\t1.b) Testing with non-clustering index on Publ.pubID: \n");
        createandclean(con, true, false, true);
        explainAnalyze = firstJoin(con);
        while (explainAnalyze.next()) {
            System.out.println("\t\t" + explainAnalyze.getString(1));
        }
        explainAnalyze = secondJoin(con);
        while (explainAnalyze.next()) {
            System.out.println("\t\t" + explainAnalyze.getString(1));
        }        
        System.out.println("\t Task 1b ok. \n");
        
        // 1c
        System.out.println("\t1.c) Testing with two clustering indexes: \n");
        createandclean(con, true, false, false);
        createIndex(con, tables.AUTH, true);
        createIndex(con, tables.PUBL, true);
        explainAnalyze = firstJoin(con);
        while (explainAnalyze.next()) {
            System.out.println("\t\t" + explainAnalyze.getString(1));
        }
        explainAnalyze = secondJoin(con);
        while (explainAnalyze.next()) {
            System.out.println("\t\t" + explainAnalyze.getString(1));
        }        
        System.out.println("\t Task 1c ok. \n");
        
        System.out.println("Task 1 ok. \n");
    }
    
    private static void task2(Connection con) throws SQLException, FileNotFoundException, IOException {
        ResultSet explainAnalyze;
        
        setJoinStrategies(con, new joins[]{joins.NESTED_LOOP}, new joins[]{joins.HASH, joins.MERGE});
        
        System.out.println("2.) Testing indexed nested loop join: \n");
        
        System.out.println("\t2.a) Testing with index on Publ.pubID: \n");
        createandclean(con, true, false, false);
        createIndex(con, tables.PUBL, false);
        explainAnalyze = firstJoin(con);
        while (explainAnalyze.next()) {
            System.out.println("\t\t" + explainAnalyze.getString(1));
        }
        explainAnalyze = secondJoin(con);
        while (explainAnalyze.next()) {
            System.out.println("\t\t" + explainAnalyze.getString(1));
        } 
        System.out.println("\t Task 2a ok. \n");
        
        System.out.println("\t2.b) Testing with index on Auth.pubID: \n");
        dropIndexes(con);
        createIndex(con, tables.AUTH, false);
        explainAnalyze = firstJoin(con);
        while (explainAnalyze.next()) {
            System.out.println("\t\t" + explainAnalyze.getString(1));
        }
        explainAnalyze = secondJoin(con);
        while (explainAnalyze.next()) {
            System.out.println("\t\t" + explainAnalyze.getString(1));
        } 
        System.out.println("\t Task 2b ok. \n");
        
        System.out.println("\t2.c) Testing with index on Publ.pubID and Auth.pubID: \n");
        dropIndexes(con);
        createIndex(con, tables.PUBL, false);
        createIndex(con, tables.AUTH, false);
        explainAnalyze = firstJoin(con);
        while (explainAnalyze.next()) {
            System.out.println("\t\t" + explainAnalyze.getString(1));
        }
        explainAnalyze = secondJoin(con);
        while (explainAnalyze.next()) {
            System.out.println("\t\t" + explainAnalyze.getString(1));
        } 
        System.out.println("\t Task 2c ok. \n");
        
        System.out.println(" Task 2 ok. \n");
    }
    
    private static void task3(Connection con) throws SQLException {
        ResultSet explainAnalyze;
        
        setJoinStrategies(con, new joins[]{joins.MERGE}, new joins[]{joins.HASH, joins.NESTED_LOOP});
        
        System.out.println("3.) Testing sort-merge join: \n");
        
        System.out.println("\t3.a) Testing without index: \n");
        dropIndexes(con);
        explainAnalyze = firstJoin(con);
        while (explainAnalyze.next()) {
            System.out.println("\t\t" + explainAnalyze.getString(1));
        }
        explainAnalyze = secondJoin(con);
        while (explainAnalyze.next()) {
            System.out.println("\t\t" + explainAnalyze.getString(1));
        } 
        System.out.println("\t Task 3a ok. \n");
        
        System.out.println("\t3.b) Testing with two non-clustering indexes: \n");
        createIndex(con, tables.PUBL, false);
        createIndex(con, tables.AUTH, false);        
        explainAnalyze = firstJoin(con);
        while (explainAnalyze.next()) {
            System.out.println("\t\t" + explainAnalyze.getString(1));
        }
        explainAnalyze = secondJoin(con);
        while (explainAnalyze.next()) {
            System.out.println("\t\t" + explainAnalyze.getString(1));
        } 
        System.out.println("\t Task 3b ok. \n");
        
        System.out.println("\t3.c) Testing with two clustering indexes: \n");
        dropIndexes(con);
        createIndex(con, tables.PUBL, true);
        createIndex(con, tables.AUTH, true);
        explainAnalyze = firstJoin(con);
        while (explainAnalyze.next()) {
            System.out.println("\t\t" + explainAnalyze.getString(1));
        }
        explainAnalyze = secondJoin(con);
        while (explainAnalyze.next()) {
            System.out.println("\t\t" + explainAnalyze.getString(1));
        } 
        System.out.println("\t Task 3c ok. \n");
        
        System.out.println(" Task 3 ok. \n");
    }
    
    private static void task4(Connection con) throws SQLException, FileNotFoundException, IOException {
        ResultSet explainAnalyze;
        
        setJoinStrategies(con, new joins[]{joins.HASH}, new joins[]{joins.MERGE, joins.NESTED_LOOP});
        
        System.out.println("4.) Testing hash join without index: \n");
        
        createandclean(con, true, false, false);
        explainAnalyze = firstJoin(con);
        while (explainAnalyze.next()) {
            System.out.println("\t\t" + explainAnalyze.getString(1));
        }
        explainAnalyze = secondJoin(con);
        while (explainAnalyze.next()) {
            System.out.println("\t\t" + explainAnalyze.getString(1));
        } 
        System.out.println("\t Task 4 ok. \n");
        
    }
    
    //for first query
    private static ResultSet firstJoin(Connection con) throws SQLException {
        System.out.println("\t\t--EXECUTING FIRST JOIN-- ");
        return con.createStatement().executeQuery("EXPLAIN ANALYZE SELECT name, title\n" +
                                                  "FROM \"Auth\", \"Publ\"\n" +
                                                  "WHERE \"Auth\".\"pubID\" = \"Publ\".\"pubID\";");
    }
    
    //for second query
    private static ResultSet secondJoin(Connection con) throws SQLException {
        System.out.println("\n\t\t --EXECUTING SECOND JOIN-- ");
        return con.createStatement().executeQuery("EXPLAIN ANALYZE SELECT title\n" +
                                                  "FROM \"Auth\", \"Publ\"\n" +
                                                  "WHERE \"Auth\".\"pubID\" = \"Publ\".\"pubID\" AND \"Auth\".name = 'Divesh Srivastava';");
    }
    
    private static void createandclean(Connection con, boolean initialize, boolean authIndex, boolean pubIndex)
            throws SQLException, IOException, FileNotFoundException {
        CopyManager cm = new CopyManager((BaseConnection) con);

        System.out.print("\t\tCleaning db...");
        con.createStatement().execute("DROP TABLE IF EXISTS \"Auth\";");
        con.createStatement().execute("CREATE TABLE \"Auth\" (\n" +
                                      " \"name\"    varchar(49),\n" +
                                      " \"pubID\"   varchar(129)" + (authIndex ? " UNIQUE\n" : "\n") +
                                      ");");
        
        con.createStatement().execute("DROP TABLE IF EXISTS \"Publ\";");
        con.createStatement().execute("CREATE TABLE \"Publ\" (\n" +
                                      " \"pubID\"   varchar(129)" + (pubIndex ? " UNIQUE,\n" : ",\n") +
                                      " \"type\"    varchar(13),\n" +
                                      " \"title\"   varchar(700),\n" +
                                      " \"booktitle\"   varchar(132),\n" +
                                      " \"year\"    varchar(4),\n" +
                                      " \"publisher\"   varchar(196)\n" +
                                      ");");
        System.out.println("ok");

        if (initialize) {
            System.out.print("\t\tInitializing tables...");

            // use copymanager to handle the stdin side
            cm.copyIn("COPY \"Auth\" FROM stdin", new FileInputStream("auth.tsv")); 
            cm.copyIn("COPY \"Publ\" FROM stdin", new FileInputStream("publ.tsv")); 
            System.out.println("ok");
        }
    }
    
    private static void dropIndexes(Connection con) throws SQLException {
        con.createStatement().execute("DROP INDEX IF EXISTS \"publ_pubid_idx\";");
        con.createStatement().execute("DROP INDEX IF EXISTS \"auth_pubid_idx\";");
        con.createStatement().execute("ANALYZE \"Publ\";");
        con.createStatement().execute("ANALYZE \"Auth\";");
    }
    
    private static void createIndex(Connection con, tables table, boolean cluster) throws SQLException {
        con.createStatement().execute("CREATE INDEX " + table.toString().toLowerCase() 
        		+ "_pubid_idx ON \"" + table + "\" (\"pubID\");");
        if (cluster) {
            con.createStatement().execute("CLUSTER \"" + table + "\" USING " 
            		+ table.toString().toLowerCase() + "_pubid_idx;");
        }
        con.createStatement().execute("ANALYZE \"" + table + "\";");
    }
}
