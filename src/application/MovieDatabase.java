package application;

import java.io.FileReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;

/*
 * https://howtodoinjava.com/json/json-simple-read-write-json-examples/
 * https://examples.javacodegeeks.com/core-java/sql/java-jdbc-postgresql-connection-example/
 */

public class MovieDatabase {
	
	static String hostURL = "DBCLASS.cs.pdx.edu";
	static String dbName = "f18wdb10";
	static String userName = "f18wdb10";
	static String password = "ekc2Dd#f8r";
	static String schema = "movie";
	
	public static void example02() {
		try  {
			//Connection connection = DriverManager.getConnection("jdbc:postgresql://"+"localhost:5432"+"/" + "example", "postgres", "postgres");
			Connection conn = DriverManager.getConnection("jdbc:postgresql://"+hostURL+"/" + dbName, userName, password);
			System.out.println("Connected to PostgreSQL database!");

			//perform a query
			Statement statement = conn.createStatement();
			ResultSet resultSet = statement.executeQuery("SELECT * FROM spy.mission;");
			
			//display queried results
			while (resultSet.next()) {
				System.out.printf("Mission ID = %s, Name = %s\n", resultSet.getString("mission_id"), resultSet.getString("name"));
			}

			conn.close();
		} /*catch (ClassNotFoundException e) {
			System.out.println("PostgreSQL JDBC driver not found.");
			e.printStackTrace();
		}*/ catch (SQLException e) {
			System.out.println("Connection failure.");
			e.printStackTrace();
		}
	}
	
	public static void main() {
		MovieDatabase db = new MovieDatabase();
		db.readJSONFile("E:\\Projects\\CS586_Movie_DB\\db\\info\\tt0042876.txt");

	}
	
	Connection conn;

	public MovieDatabase() {
		
	}
	
	public void connect() {
		try  {
			conn = DriverManager.getConnection("jdbc:postgresql://"+hostURL+"/" + dbName, userName, password);
			System.out.println("Connected to PostgreSQL database!");
		} catch (SQLException e) {
			conn = null;
			System.out.println("Error: connection failure.");
			e.printStackTrace();
		}
	}
	
	public void disconnect() {
		try {
			if (conn == null) {
				System.out.println("Connection is null. Skip closing connection.");
				return;
			}
			
			conn.close();
		} catch (SQLException e) {
			System.out.println("Error: could not close connection.");
			e.printStackTrace();
		}
	}
	
	public void readJSONFile(String filePath) {
		//JSON parser object to parse read file
        JSONParser jsonParser = new JSONParser();
         
        try (FileReader reader = new FileReader(filePath))
        {
            //Read JSON file
            Object obj = jsonParser.parse(reader);
 
            
            
            JSONArray employeeList = (JSONArray) obj;
            System.out.println(employeeList);
             
            //Iterate over employee array
            //employeeList.forEach( emp -> parseEmployeeObject( (JSONObject) emp ) );
 
        } catch (Exception e) {
            e.printStackTrace();
        }
	}
	
	//Populate database using data in the given directory. The directory should end with "/".
	public void populateDB(String dataDir) {
		/*
		 * From the data, collect
		 * - a list of actors
		 * - a list of writers
		 * - a list of directors
		 * - a list of genres
		 * 
		 * Then insert into db in this order:
		 * - table "Actor": list of actors
		 * - table "Writer": list of writers
		 * - table "Director": list of directors
		 * - table "Genre": list of genres
		 * - for each movies -> insert into table "Movie"
		 * 		- insert: title, story, runtime (total of mins), year, movie ID, poster URL
		 * 		- insert:
		 * 			table "ActorMovieRel"
		 * 			table "GenreMovieRel"
		 */
	}
}
