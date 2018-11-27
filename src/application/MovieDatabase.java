package application;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.InputStreamReader;
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
		//db.connect();
		db.populateDB("E:\\Projects\\CS586_Movie_DB\\db\\", "E:\\Projects\\CS586_Movie_DB\\db\\movie_id_list.txt");
		//db.disconnect();
	}
	
	Connection conn = null;
	ArrayList<Movie> listMovies = new ArrayList<>();
	ArrayList<String> listActors = new ArrayList<>();
	ArrayList<String> listDirectors = new ArrayList<>();
	ArrayList<String> listWriters = new ArrayList<>();
	ArrayList<String> listGenres = new ArrayList<>();

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
	
	//Populate database using data in the given directory. The directory should end with "/".
	public void populateDB(String dataDirPath, String movieListFilePath) {
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
		
		try {
			
			//read all movies
			System.out.println("Reading movie info files: ");
			FileReader fileReader = new FileReader(movieListFilePath);
			BufferedReader reader = new BufferedReader(fileReader);
			String movieID;
			int nMovies = 0;
			int nBads = 0;
			listMovies.clear();
			while ((movieID = reader.readLine()) != null) {
				Movie movie = new Movie(dataDirPath+"info/"+movieID+".txt", movieID);
				
				if (!movie.isBadMovie) {
					listMovies.add(movie);
				} else {
					nBads++;
				}
				
				nMovies++;
				System.out.println("  " + nMovies + ": " + movieID);
			}
			reader.close();
			fileReader.close();
			System.out.println(" done");
			System.out.println("Bad movies = " + nBads);
			
			//collect sets of: actors, directors, genres, writers
			System.out.print("Collecting sets... ");
			listActors.clear();
			listDirectors.clear();
			listWriters.clear();
			listGenres.clear();
			Movie.collectSets(listMovies, listActors, listWriters, listGenres, listDirectors);
			System.out.println("done");
			
			//check connection to continue
			if (conn==null) {
				System.out.println("DB connection is not established. Stop populating.");
				return;
			}
			if (conn.isClosed()) {
				System.out.println("DB connection is closed. Stop populating.");
				return;
			}
			
			//drop all tables before inserting new instances
			
			//insert table "genre"
			
			//insert table "actor"
			
			//insert table "director"
			
			//insert table "writer"
			
			//insert table "movie"
			
			//insert table "MovieGenreRel"
			
			//insert table "MovieActorRel"
			
			//insert table "MovieDirectorRel"
			
			//insert table "MovieWriterRel"
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
