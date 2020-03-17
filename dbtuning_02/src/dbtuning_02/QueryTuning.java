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
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import org.postgresql.copy.CopyManager;
import org.postgresql.core.BaseConnection;

public class QueryTuning {
	
	//Global Variables
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
		orignalqueries(con);
		rewrittenqueries(con);
		System.out.println("DONE!");
		//----------------------Sequence Order END------------------------
    }
    
    //Creates the empty tables or cleans them if they existed befores
    private static void createandclean(Connection con) throws SQLException {
        System.out.print("Cleaning and Creating Tables... ");
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
    
    //Fills the tables with random data
    private static void filltableswithdata(Connection con) throws SQLException, IOException {	
    	CopyManager cm = new CopyManager((BaseConnection) con);
    	FileOutputStream os = new FileOutputStream("Techdept.tsv");
        OutputStreamWriter wr = new OutputStreamWriter(os);
        FileOutputStream os_student = new FileOutputStream("Student.tsv");
        OutputStreamWriter wr_student = new OutputStreamWriter(os_student);
        FileOutputStream os_employee = new FileOutputStream("Employee.tsv");
        OutputStreamWriter wr_employee = new OutputStreamWriter(os_employee);

        
      //--------Create 10 Techdepartments and save them in ArrayList--------
    	System.out.println("Creating Departments...");
    	createDepartments();
    	
    	//--------Create Departments--------
    	for (Techdept dept : departments) {
            wr.write(dept.getDept() + "\t" + dept.getManager() + "\t" + dept.getLocation() + "\n");
        }
        wr.flush();
        wr.close();
        cm.copyIn("COPY \"Techdept\" FROM stdin", new FileInputStream("Techdept.tsv"));
    	
    	//--------Create 100k students--------
        System.out.println("Creating Students...");
    	for(int i=0; i<100000; i++) {
    		//ssnum, name, course, grade //HIER VLT FÜR COURSE WAS SINNVOLLES NEHMEN
    		wr_student.write(i + "\t" + UUID.randomUUID().toString() + "\t" + UUID.randomUUID().toString() + "\t" + ThreadLocalRandom.current().nextInt(1,6) + "\n");
    	}
    	wr_student.flush();
    	wr_student.close();
    	cm.copyIn("COPY \"Student\" FROM stdin", new FileInputStream("Student.tsv"));
    	
    	//--------Create 100k employees--------
    	System.out.println("Creating Employees...");
    	for(int j=100000; j<200000; j++) {
    		String manager = null;
    		String dept = null;
    		//In ~10% der Fälle soll random ein manager und ein dept gesetzt werden
    		if(ThreadLocalRandom.current().nextInt(1,11) == 5) {
    			int random = ThreadLocalRandom.current().nextInt(0,10);
    			manager = departments.get(random).getManager();
    			dept = departments.get(random).getDept();
    		}
    		
    		//ssnum, name, manager, dept, salary, numfriends
    		wr_employee.write(j + "\t" + UUID.randomUUID().toString() + "\t" + manager + "\t" + dept + "\t" + ThreadLocalRandom.current().nextInt(2000,8000) + "\t" + ThreadLocalRandom.current().nextInt(2,12) +"\n");
    	}
    	wr_employee.flush();
    	wr_employee.close();
    	cm.copyIn("COPY \"Employee\" FROM stdin", new FileInputStream("Employee.tsv"));
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

    //Hier zwei Queries schreiben, Zeit messen -> Siehe Programm1 !
    public static void orignalqueries(Connection con) throws SQLException{
    	
    }
    
    //Hier die zwei Queries umschreiben, Zeit messen -> Siehe Programm1 !
    public static void rewrittenqueries(Connection con) throws SQLException{
    	
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

