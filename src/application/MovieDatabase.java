package application;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;

import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;

/*
 * https://howtodoinjava.com/json/json-simple-read-write-json-examples/
 * https://examples.javacodegeeks.com/core-java/sql/java-jdbc-postgresql-connection-example/
 */

public class MovieDatabase {
	
	/***************************************************************************
	 * STATIC
	 ***************************************************************************/
	
	static String hostURL = "DBCLASS.cs.pdx.edu";
	static String dbName = "f18wdb10";
	static String userName = "f18wdb10";
	static String password = "ekc2Dd#f8r";
	static String schema = "movie";
	static String dbDir = "G:\\ThongDoan\\Projects\\CS586_MovieDB\\db\\";
	static String creationQueryFile = "createQueries_UTF8.txt";
	
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

	/***************************************************************************
	 * MEMBERS
	 ***************************************************************************/
	
	public static void main(String[] args) {
		MovieDatabase db = new MovieDatabase();
		db.connect();
		String creationQueries = db.populateDB(dbDir, dbDir + "movie_id_list.txt");
		db.disconnect();
		
		//write create queries to file
		try {
			System.out.print("Write creation-queries to file ...");
			//FileWriter fileWriter = new FileWriter(creationQueryFile);
			OutputStreamWriter fileWriter =
		             new OutputStreamWriter(new FileOutputStream(creationQueryFile), StandardCharsets.UTF_8);
		    
			fileWriter.write(creationQueries);
			fileWriter.close();
			System.out.println("done");
		} catch (Exception e) {
			System.out.println("Error while writing creation-queries to file.");
		}
	}
	
	Connection conn = null;
	ArrayList<Movie> listMovies = new ArrayList<>();
	ArrayList<String> listActors = new ArrayList<>();
	ArrayList<String> listDirectors = new ArrayList<>();
	ArrayList<String> listWriters = new ArrayList<>();
	ArrayList<String> listGenres = new ArrayList<>();

	public MovieDatabase() {
		
	}
	
	public ResultSet executeQuery(String query) {
		if (conn == null) {
			System.out.println("Connection is null. Skip running query.");
			return null;
		}
		
		try {
			Statement stmn = conn.createStatement();
			return stmn.executeQuery(query);
		} catch (SQLException e) {
			System.out.println("Error run query: " + e.getMessage());
			System.out.println(query);
		}
		
		return null;
	}
	
	public int executeUpdate(String query) {
		if (conn == null) {
			System.out.println("Connection is null. Skip running query.");
			return -1;
		}
		
		try {
			Statement stmn = conn.createStatement();
			return stmn.executeUpdate(query);
		} catch (SQLException e) {
			System.out.println("Error run query: " + e.getMessage());
			System.out.println(query);
		}
		
		return -1;
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
			System.out.println("Disconnected from PostgreSQL database!");
		} catch (SQLException e) {
			System.out.println("Error: could not close connection.");
			e.printStackTrace();
		}
	}
	
	/**
	 * Populate database using data in the given directory. The directory should end with "/".
	 * @param dataDirPath
	 * @param movieListFilePath
	 * @return the query using to populate the database
	 */
	public String populateDB(String dataDirPath, String movieListFilePath) {
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
		 * 			table "DirectorMovieRel"
		 * 			table "WriterMovieRel"
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
			
			//drop all tables before inserting new instances
			String query;
			String allQueries = "";
			query = ""
					+ "DROP TABLE IF EXISTS movie.genre;\n"
					+ "DROP TABLE IF EXISTS movie.actor;\n"
					+ "DROP TABLE IF EXISTS movie.director;\n"
					+ "DROP TABLE IF EXISTS movie.writer;\n"
					+ "DROP TABLE IF EXISTS movie.movie;\n"
					+ "DROP TABLE IF EXISTS movie.movieGenreRel;\n"
					+ "DROP TABLE IF EXISTS movie.movieActorRel;\n"
					+ "DROP TABLE IF EXISTS movie.movieDirectorRel;\n"
					+ "DROP TABLE IF EXISTS movie.movieWriterRel;\n";
			System.out.print("Dropping tables...");
			executeUpdate(query);
			allQueries += query;
			System.out.println("done");
			
			//create all 9 tables
			String[] queryCreateTables = new String[9];
			Arrays.fill(queryCreateTables, "");
			queryCreateTables[0] = "CREATE TABLE movie.genre ("
					+ "genre_name varchar PRIMARY KEY,"
					+ "description varchar"
					+ ");";
			queryCreateTables[1] = "CREATE TABLE movie.actor ("
					+ "actor_name varchar PRIMARY KEY"
					+ ");";
			queryCreateTables[2] = "CREATE TABLE movie.director ("
					+ "director_name varchar PRIMARY KEY"
					+ ");";
			queryCreateTables[3] = "CREATE TABLE movie.writer ("
					+ "writer_name varchar PRIMARY KEY"
					+ ");";
			queryCreateTables[4] = "CREATE TABLE movie.movie ("
					+ "movie_ID varchar PRIMARY KEY,"
					+ "title varchar,"
					+ "story varchar,"
					+ "poster_URL varchar,"
					+ "year integer,"
					+ "runtime integer,"
					+ "rating real"
					+ ");";
			queryCreateTables[5] = "CREATE TABLE movie.movieGenreRel ("
					+ "movie_ID varchar,"
					+ "genre_name varchar"
					+ ");";
			queryCreateTables[6] = "CREATE TABLE movie.movieActorRel ("
					+ "movie_ID varchar,"
					+ "actor_name varchar"
					+ ");";
			queryCreateTables[7] = "CREATE TABLE movie.movieDirectorRel ("
					+ "movie_ID varchar,"
					+ "director_name varchar"
					+ ");";
			queryCreateTables[8] = "CREATE TABLE movie.movieWriterRel ("
					+ "movie_ID varchar,"
					+ "writer_name varchar"
					+ ");";
			query = "";
			for (String q : queryCreateTables) {
				query += q + "\n";
			}
			
			System.out.print("Creating tables...");
			executeUpdate(query);
			allQueries += query;
			System.out.println("done");
			
			//insert table "genre"
			query = genInsertQuery("genre", "genre_name", listGenres);
			System.out.print("Insert into table 'genre'...");
			executeUpdate(query);
			allQueries += query;
			System.out.println("done");			
			
			//insert table "actor"
			query = genInsertQuery("actor", "actor_name", listActors);
			System.out.print("Insert into table 'actor'...");
			executeUpdate(query);
			allQueries += query;
			System.out.println("done");
			
			//insert table "director"
			query = genInsertQuery("director", "director_name", listDirectors);
			System.out.print("Insert into table 'director'...");
			executeUpdate(query);
			allQueries += query;
			System.out.println("done");
			
			//insert table "writer"
			query = genInsertQuery("writer", "writer_name", listWriters);
			System.out.print("Insert into table 'writer'...");
			executeUpdate(query);
			allQueries += query;
			System.out.println("done");
			
			//insert table "movie"
			query = genInsertQuery(listMovies);
			System.out.print("Insert into table 'movie'...");
			executeUpdate(query);
			allQueries += query;
			System.out.println("done");
			
			//insert table "MovieGenreRel"
			query = genInsertQuery_MovieGenreRel(listMovies);
			System.out.print("Insert into table 'movieGenreRel'...");
			executeUpdate(query);
			allQueries += query;
			System.out.println("done");
			
			//insert table "MovieActorRel"
			query = genInsertQuery_MovieActorRel(listMovies);
			System.out.print("Insert into table 'movieActorRel'...");
			executeUpdate(query);
			allQueries += query;
			System.out.println("done");
			
			//insert table "MovieDirectorRel"
			query = genInsertQuery_MovieDirectorRel(listMovies);
			System.out.print("Insert into table 'movieDirectorRel'...");
			executeUpdate(query);
			allQueries += query;
			System.out.println("done");
			
			//insert table "MovieWriterRel"
			query = genInsertQuery_MovieWriterRel(listMovies);
			System.out.print("Insert into table 'movieMovieRel'...");
			executeUpdate(query);
			allQueries += query;
			System.out.println("done");
			
			
			return allQueries;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public String genInsertQuery_MovieGenreRel(ArrayList<Movie> listMovies) {
		String query = "INSERT INTO movie.movieGenreRel (movie_id, genre_name) VALUES";
		for (Movie movie : listMovies) {
			String[] arr =movie.genInsertQuery_MovieRel(movie.arrGenres);
			for (String valueTuple : arr) {
				query += "    " + valueTuple + ",\n";
			}
		}
		
		query = query.substring(0, query.lastIndexOf(","));
		query += ";\n";
		return query;
	}

	public String genInsertQuery_MovieActorRel(ArrayList<Movie> listMovies) {
		String query = "INSERT INTO movie.movieActorRel (movie_id, actor_name) VALUES";
		for (Movie movie : listMovies) {
			String[] arr =movie.genInsertQuery_MovieRel(movie.arrActors);
			for (String valueTuple : arr) {
				query += "    " + valueTuple + ",\n";
			}
		}
		
		query = query.substring(0, query.lastIndexOf(","));
		query += ";\n";
		return query;
	}
	
	public String genInsertQuery_MovieDirectorRel(ArrayList<Movie> listMovies) {
		String query = "INSERT INTO movie.movieDirectorRel (movie_id, director_name) VALUES";
		for (Movie movie : listMovies) {
			String[] arr =movie.genInsertQuery_MovieRel(movie.arrDirectors);
			for (String valueTuple : arr) {
				query += "    " + valueTuple + ",\n";
			}
		}
		
		query = query.substring(0, query.lastIndexOf(","));
		query += ";\n";
		return query;
	}
	
	public String genInsertQuery_MovieWriterRel(ArrayList<Movie> listMovies) {
		String query = "INSERT INTO movie.movieWriterRel (movie_id, writer_name) VALUES";
		for (Movie movie : listMovies) {
			String[] arr =movie.genInsertQuery_MovieRel(movie.arrWriters);
			for (String valueTuple : arr) {
				query += "    " + valueTuple + ",\n";
			}
		}
		
		query = query.substring(0, query.lastIndexOf(","));
		query += ";\n";
		return query;
	}
	
	public String genInsertQuery(String tableName, String attributeName, ArrayList<String> listValues) {
		String query;
		query = "INSERT INTO movie." + tableName + " (" + attributeName + ")"
				+ "\nVALUES\n";
		for (String value : listValues) {
			query += "    ('" + value + "'),\n";
		}
		query = query.substring(0, query.lastIndexOf(","));
		query += ";\n";
		return query;
	}

	public String genInsertQuery(ArrayList<Movie> listMovies) {
		String query = "INSERT INTO movie.movie (movie_id, title, story, poster_url, year, runtime, rating) VALUES";
		for (Movie movie : listMovies) {
			query += "    " + movie.genInsertQuery() + ",\n";
		}
		
		query = query.substring(0, query.lastIndexOf(","));
		query += ";\n";
		return query;
	}
}
