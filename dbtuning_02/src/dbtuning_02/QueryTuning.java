package dbtuning_02;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Random;

import org.postgresql.copy.CopyManager;
import org.postgresql.core.BaseConnection;

public class QueryTuning {
	
    private static ArrayList<Techdept> departments;
    private static Random gen = new Random();
    
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
		String pwd = "Iechei5eexai";
		String user = "mmrazovic";*/

		
		//Localhost
		String host = "localhost";
		String port = "5432";
		String database = "postgres";
		String pwd = "lovenia";
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
        System.out.print("Cleaning db... \n");
        con.createStatement().execute("DROP TABLE IF EXISTS \"Employee\";");
        con.createStatement().execute("CREATE TABLE \"Employee\" (\n" +
                                      "	ssnum INT NOT NULL UNIQUE,\n" +
                                      "	name VARCHAR(40) NOT NULL UNIQUE,\n" +
                                      "	manager VARCHAR(40) NOT NULL,\n" +
                                      "	dept VARCHAR(40) NOT NULL,\n" +
                                      "	salary NUMERIC(8,2) NOT NULL,\n" +
                                      "	numfriends SMALLINT NOT NULL,\n" +
                                      " PRIMARY KEY (ssnum, name));");
        
        con.createStatement().execute("DROP TABLE IF EXISTS \"Student\";");
        con.createStatement().execute("CREATE TABLE \"Student\" (\n" +
                                      "	ssnum INT NOT NULL UNIQUE,\n" +
                                      "	name VARCHAR(40) NOT NULL UNIQUE,\n" +
                                      "	course VARCHAR(40) NOT NULL,\n" +
                                      "	grade SMALLINT NOT NULL,\n" +
                                      " PRIMARY KEY (ssnum, name));");
        
        con.createStatement().execute("DROP TABLE IF EXISTS \"Techdept\";");
        con.createStatement().execute("CREATE TABLE \"Techdept\" (\n" +
                                      "	dept VARCHAR(40) NOT NULL UNIQUE,\n" +
                                      "	manager VARCHAR(40) NOT NULL,\n" +
                                      "	location VARCHAR(40) NOT NULL,\n" +
                                      " PRIMARY KEY (dept));");
        
        
        con.createStatement().execute("CREATE UNIQUE INDEX employee_ssnum_unique_index ON \"Employee\" (ssnum);");
        con.createStatement().execute("CREATE UNIQUE INDEX employee_name_unique_index ON \"Employee\" (name);");
        con.createStatement().execute("CREATE INDEX employee_dept_index ON \"Employee\" (dept);");
        
        con.createStatement().execute("CREATE UNIQUE INDEX student_ssnum_unique_index  ON \"Student\" (ssnum);");
        con.createStatement().execute("CREATE UNIQUE INDEX student_name_unique_index ON \"Student\" (name);");
        
        con.createStatement().execute("CREATE UNIQUE INDEX techdept_dept_unique_index ON \"Techdept\" (dept);");
        System.out.println("ok");
    }
    
    //fills the tables with random data
    private static void filltableswithdata(Connection con) throws SQLException, IOException {
    	Random rand = new Random();
    	CopyManager cm = new CopyManager((BaseConnection) con);
    	FileOutputStream os = new FileOutputStream("Techdept.tsv");
        OutputStreamWriter wr = new OutputStreamWriter(os);
        int ssnum = 0;
        Techdept currdept;
    	
    	//Create 10 Techdepartments and save them in ArrayList
    	createDepartments();
    	
    	//Write departments to file - funktioniert 
    	for (Techdept dept : departments) {
            wr.write(dept.getDept() + "\t" + dept.getManager() + "\t" + dept.getLocation() + "\n");
        }
        wr.flush();
        cm.copyIn("COPY \"Techdept\" FROM stdin", new FileInputStream("Techdept.tsv"));
    	
    	//Create 100k of students with an random techdepart out of array
    	
    	//Create and write 100k of employees - funktioniert derzeit nicht
      /*  os = new FileOutputStream("Employee.tsv");
        wr = new OutputStreamWriter(os);
        
        for(int i = 1; i <= 99_990; i++) {
        	int x = rand.nextInt((10 - 1) + 1) + 1;
        	
        	if(x == 1)
        		wr.write(++ssnum + "\t" + randomString(10, 71) + "\t" + currdept.getManager() + "\t" + currdept.getDept());
        	
        }*/
    	
    }
    
    //kopiert von Martin, bin nicht sicher ob es alles funktioniert
    private static String randomString(int minLength, int bound) {
    	
    	int strLength = minLength + gen.nextInt(bound);
        StringBuilder str = new StringBuilder();
        
        for (int i = 1; i <= strLength; i++) {
            str.append('a' + gen.nextInt(26));
        }
        
        return str.toString();
    }
    
    
    //Create 10 Techdepartments and save them in ArrayList
    private static void createDepartments() {
    	
    	String[] managerPool = {"Mia Wagner", "David Gruber", "Maria Winkler", "Sophia Huber", "Helena Weber", "Emma Kocher", "Max Steiner", "Philipp Moser", "Fabian Mayer", "Franz Pichler"};
        String[] locationPool = {"Salzburg", "Wien", "Graz", "Klagenfurt", "Villach", "Innsbruck", "Linz", "Bregenz", "Eisenstadt", "Wels"};
         
        departments = new ArrayList<Techdept>(10);
         
        departments.add(new Techdept("Development", managerPool[gen.nextInt(10)], locationPool[gen.nextInt(10)]));
        departments.add(new Techdept("Systems Engineering", managerPool[gen.nextInt(10)], locationPool[gen.nextInt(10)]));
        departments.add(new Techdept("AI", managerPool[gen.nextInt(10)], locationPool[gen.nextInt(10)]));
        departments.add(new Techdept("Research", managerPool[gen.nextInt(10)], locationPool[gen.nextInt(10)]));
        departments.add(new Techdept("IT", managerPool[gen.nextInt(10)], locationPool[gen.nextInt(10)]));
        departments.add(new Techdept("Hardware Development", managerPool[gen.nextInt(10)], locationPool[gen.nextInt(10)]));              
        departments.add(new Techdept("Webdesign", managerPool[gen.nextInt(10)], locationPool[gen.nextInt(10)]));
        departments.add(new Techdept("Databases", managerPool[gen.nextInt(10)], locationPool[gen.nextInt(10)]));
        departments.add(new Techdept("IT Security", managerPool[gen.nextInt(10)], locationPool[gen.nextInt(10)]));
        departments.add(new Techdept("Software Testing", managerPool[gen.nextInt(10)], locationPool[gen.nextInt(10)]));
    }
    
private static class Techdept {
	    
    private String dept;
    private String manager;
    private String location;
        
    private Techdept(String dept, String manager, String location) {
    	this.dept = dept;
    	this.manager = manager;
    	this.location = location;
    	}
    
    private String getDept() {
    	return dept;
    	}
    
    private String getManager() {
    	return manager;
    	}
    
    private String getLocation() {
    	return location;
    	}
    
    }

}