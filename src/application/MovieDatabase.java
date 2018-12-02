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

		/**
		 * POPULATE
		 */
		
		//TODO (note) do this once & comment out
		
//		//populate & write queries to file for backup
//		String creationQueries = db.populateDB(dbDir, dbDir + "movie_id_list.txt");
//		write2File(creationQueries);
		
		/**
		 * TEST 20 QUERIES
		 */
		
		
		db.disconnect();
		

	}

	public static void write2File(String creationQueries) {
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


	/************************************************************************************************
	 * THE 20 QUERIES
	 ************************************************************************************************/

	/*
	 * NOTES:
	 * %M: movie title
	 * %A: actor name
	 * %D: director name
	 * %G: genre name
	 * XXXX: year
	 */
	
	/**
1/
SELECT actor_name
FROM movie.movie NATURAL JOIN movie.movieactorrel
WHERE UPPER(title) = UPPER('$M')

	 */
	public String genQuery01(String movieTitle) {
		return "SELECT actor_name\r\n" + 
				"FROM movie.movie NATURAL JOIN movie.movieactorrel\r\n" + 
				"WHERE UPPER(title) = UPPER('" + movieTitle + "')";
	}
	
	/**
2/
SELECT actor_name, COUNT(*) as movie_count
FROM movie.movie NATURAL JOIN movie.movieactorrel
GROUP BY actor_name
HAVING COUNT(*) >= ALL
(
SELECT COUNT(*)
FROM movie.movie NATURAL JOIN movie.movieactorrel
GROUP BY actor_name
)

	 */
	public String genQuery02() {
		return "SELECT actor_name, COUNT(*) as movie_count\r\n" + 
				"FROM movie.movie NATURAL JOIN movie.movieactorrel\r\n" + 
				"GROUP BY actor_name\r\n" + 
				"HAVING COUNT(*) >= ALL\r\n" + 
				"(\r\n" + 
				"SELECT COUNT(*)\r\n" + 
				"FROM movie.movie NATURAL JOIN movie.movieactorrel\r\n" + 
				"GROUP BY actor_name\r\n" + 
				")";
	}

	/**
3/
SELECT title, year
FROM movie.movie NATURAL JOIN movie.movieactorrel
WHERE UPPER(actor_name) = UPPER('$A')

	 */
	public String genQuery03(String actorName) {
		return "SELECT title, year\r\n" + 
				"FROM movie.movie NATURAL JOIN movie.movieactorrel\r\n" + 
				"WHERE UPPER(actor_name) = UPPER('" + actorName + "')";
	}
	
	/**
4/
SELECT title, year, genre_name, rating
FROM (movie.movie NATURAL JOIN movie.moviegenrerel) NATURAL JOIN movie.movieactorrel
WHERE UPPER(genre_name) = UPPER('$G') AND UPPER(actor_name) = UPPER('$A') AND rating > 8
ORDER BY rating DESC

	 */
	public String genQuery04(String genreName, String actorName) {
		return "SELECT title, year, genre_name, rating\r\n" + 
				"FROM (movie.movie NATURAL JOIN movie.moviegenrerel) NATURAL JOIN movie.movieactorrel\r\n" + 
				"WHERE UPPER(genre_name) = UPPER('" + genreName + "') AND UPPER(actor_name) = UPPER('" + actorName +  "') AND rating > 8\r\n" + 
				"ORDER BY rating DESC";
	}
	
	/**
5/
SELECT title, rating
FROM movie.movie NATURAL JOIN movie.movieactorrel
WHERE UPPER(actor_name) = UPPER('$A')
AND rating = 
(
SELECT MAX(rating)
FROM movie.movie NATURAL JOIN movie.movieactorrel
WHERE UPPER(actor_name) = UPPER('$A')
)

	 */
	public String genQuery05(String actorName) {
		return "SELECT title, rating\r\n" + 
				"FROM movie.movie NATURAL JOIN movie.movieactorrel\r\n" + 
				"WHERE UPPER(actor_name) = UPPER('" + actorName + "')\r\n" + 
				"AND rating = \r\n" + 
				"(\r\n" + 
				"SELECT MAX(rating)\r\n" + 
				"FROM movie.movie NATURAL JOIN movie.movieactorrel\r\n" + 
				"WHERE UPPER(actor_name) = UPPER('" + actorName + "')\r\n" + 
				")";
	}
	
	/**
6/
SELECT B.actor_name, COUNT(DISTINCT A.movie_id) as times_of_coplaying
FROM movie.movieactorrel A, movie.movieactorrel B
WHERE A.movie_id = B.movie_id AND UPPER(A.actor_name) = UPPER('$A') AND UPPER(B.actor_name) <> UPPER('$A')
GROUP BY (A.actor_name, B.actor_name)
HAVING COUNT(DISTINCT A.movie_id) >= ALL
(
SELECT COUNT(DISTINCT A.movie_id) as times_of_coplaying
FROM movie.movieactorrel A, movie.movieactorrel B
WHERE A.movie_id = B.movie_id AND UPPER(A.actor_name) = UPPER('$A') AND UPPER(B.actor_name) <> UPPER('$A')
GROUP BY (A.actor_name, B.actor_name)
)


	 */
	public String genQuery06(String actorName) {
		return "SELECT B.actor_name, COUNT(DISTINCT A.movie_id) as times_of_coplaying\r\n" + 
				"FROM movie.movieactorrel A, movie.movieactorrel B\r\n" + 
				"WHERE A.movie_id = B.movie_id AND UPPER(A.actor_name) = UPPER('"  +actorName + "') AND UPPER(B.actor_name) <> UPPER('" + actorName + "')\r\n" + 
				"GROUP BY (A.actor_name, B.actor_name)\r\n" + 
				"HAVING COUNT(DISTINCT A.movie_id) >= ALL\r\n" + 
				"(\r\n" + 
				"SELECT COUNT(DISTINCT A.movie_id) as times_of_coplaying\r\n" + 
				"FROM movie.movieactorrel A, movie.movieactorrel B\r\n" + 
				"WHERE A.movie_id = B.movie_id AND UPPER(A.actor_name) = UPPER('" + actorName +  "') AND UPPER(B.actor_name) <> UPPER('" + actorName + "')\r\n" + 
				"GROUP BY (A.actor_name, B.actor_name)\r\n" + 
				")";
	}
	
	/**
7/ Who usually appears in good movies? (at least 7 average rating with and play at least 8 movies)

SELECT actor_name, AVG(rating) as average_movie_rating, COUNT(movie_id) as movie_counts
FROM movie.movieactorrel NATURAL JOIN movie.movie
GROUP BY actor_name
HAVING COUNT(movie_id) >= 8 AND AVG(rating) > 7
ORDER BY AVG(rating) DESC


	 */
	public String genQuery07() {
		return "SELECT actor_name, AVG(rating) as average_movie_rating, COUNT(movie_id) as movie_counts\r\n" + 
				"FROM movie.movieactorrel NATURAL JOIN movie.movie\r\n" + 
				"GROUP BY actor_name\r\n" + 
				"HAVING COUNT(movie_id) >= 8 AND AVG(rating) > 7\r\n" + 
				"ORDER BY AVG(rating) DESC";
	}
	
	/**
8/ 
SELECT actor_name, COUNT(movie_id)
FROM movie.movie NATURAL JOIN movie.movieactorrel
WHERE year = XXXX
GROUP BY actor_name
HAVING COUNT(movie_id) >= ALL
(
SELECT COUNT(movie_id)
FROM movie.movie NATURAL JOIN movie.movieactorrel
WHERE year = XXXX
GROUP BY actor_name
)


	 */
	public String genQuery08(int year) {
		return "SELECT actor_name, COUNT(movie_id)\r\n" + 
				"FROM movie.movie NATURAL JOIN movie.movieactorrel\r\n" + 
				"WHERE year = "+ year +  "\r\n" + 
				"GROUP BY actor_name\r\n" + 
				"HAVING COUNT(movie_id) >= ALL\r\n" + 
				"(\r\n" + 
				"SELECT COUNT(movie_id)\r\n" + 
				"FROM movie.movie NATURAL JOIN movie.movieactorrel\r\n" + 
				"WHERE year = " + year +  "\r\n" + 
				"GROUP BY actor_name\r\n" + 
				")";
	}
	
	/**
9/
SELECT director_name
FROM movie.movie NATURAL JOIN movie.moviedirectorrel
WHERE UPPER(title) = UPPER('$M')


	 */
	public String genQuery09(String movieName) {
		return "SELECT director_name\r\n" + 
				"FROM movie.movie NATURAL JOIN movie.moviedirectorrel\r\n" + 
				"WHERE UPPER(title) = UPPER('" +movieName+ "')";
	}
	
	/**
10/
SELECT director_name, COUNT(movie_id)
FROM movie.movie NATURAL JOIN movie.moviedirectorrel
GROUP BY director_name
HAVING COUNT(movie_id) >= ALL
(
SELECT COUNT(movie_id)
FROM movie.movie NATURAL JOIN movie.moviedirectorrel
GROUP BY director_name
)


	 */
	public String genQuery10() {
		return "SELECT director_name, COUNT(movie_id)\r\n" + 
				"FROM movie.movie NATURAL JOIN movie.moviedirectorrel\r\n" + 
				"GROUP BY director_name\r\n" + 
				"HAVING COUNT(movie_id) >= ALL\r\n" + 
				"(\r\n" + 
				"SELECT COUNT(movie_id)\r\n" + 
				"FROM movie.movie NATURAL JOIN movie.moviedirectorrel\r\n" + 
				"GROUP BY director_name\r\n" + 
				")";
	}

	/**
11/
SELECT title, year
FROM movie.movie NATURAL JOIN movie.moviedirectorrel
WHERE UPPER(director_name) = UPPER('$D')


	 */	
	public String genQuery11(String directorName) {
		return "SELECT title, year\r\n" + 
				"FROM movie.movie NATURAL JOIN movie.moviedirectorrel\r\n" + 
				"WHERE UPPER(director_name) = UPPER('" +directorName+  "')";
	}

	/**
12/
SELECT title, year, genre_name, rating
FROM (movie.movie NATURAL JOIN movie.moviegenrerel) NATURAL JOIN movie.moviedirectorrel
WHERE UPPER(genre_name) = UPPER('$G') AND UPPER(director_name) = UPPER('$D') AND rating > 8
ORDER BY rating DESC


	 */
	public String genQuery12(String genreName, String directorName) {
		return "SELECT title, year, genre_name, rating\r\n" + 
				"FROM (movie.movie NATURAL JOIN movie.moviegenrerel) NATURAL JOIN movie.moviedirectorrel\r\n" + 
				"WHERE UPPER(genre_name) = UPPER('" + genreName +  "') AND UPPER(director_name) = UPPER('" +directorName +"') AND rating > 8\r\n" + 
				"ORDER BY rating DESC";
	}
	
	/**
13/
SELECT director_name, COUNT(movie_id), avg(rating)
FROM movie.movie NATURAL JOIN movie.moviedirectorrel
GROUP BY director_name
HAVING COUNT(movie_id) >= 5 AND AVG(rating) >= ALL
(
SELECT AVG(rating)
FROM movie.movie NATURAL JOIN movie.moviedirectorrel
GROUP BY director_name
HAVING COUNT(movie_id) >= 5
)


	 */
	public String genQuery13() {
		return "SELECT director_name, COUNT(movie_id), avg(rating)\r\n" + 
				"FROM movie.movie NATURAL JOIN movie.moviedirectorrel\r\n" + 
				"GROUP BY director_name\r\n" + 
				"HAVING COUNT(movie_id) >= 5 AND AVG(rating) >= ALL\r\n" + 
				"(\r\n" + 
				"SELECT AVG(rating)\r\n" + 
				"FROM movie.movie NATURAL JOIN movie.moviedirectorrel\r\n" + 
				"GROUP BY director_name\r\n" + 
				"HAVING COUNT(movie_id) >= 5\r\n" + 
				")";
	}
	
	/**
14/
SELECT writer_name, COUNT(movie_id), avg(rating)
FROM movie.movie NATURAL JOIN movie.moviewriterrel
GROUP BY writer_name
HAVING COUNT(movie_id) >= 5 AND AVG(rating) >= ALL
(
SELECT AVG(rating)
FROM movie.movie NATURAL JOIN movie.moviewriterrel
GROUP BY writer_name
HAVING COUNT(movie_id) >= 5
)


	 */
	public String genQuery14() {
		return "SELECT writer_name, COUNT(movie_id), avg(rating)\r\n" + 
				"FROM movie.movie NATURAL JOIN movie.moviewriterrel\r\n" + 
				"GROUP BY writer_name\r\n" + 
				"HAVING COUNT(movie_id) >= 5 AND AVG(rating) >= ALL\r\n" + 
				"(\r\n" + 
				"SELECT AVG(rating)\r\n" + 
				"FROM movie.movie NATURAL JOIN movie.moviewriterrel\r\n" + 
				"GROUP BY writer_name\r\n" + 
				"HAVING COUNT(movie_id) >= 5\r\n" + 
				")";
	}
	
	/**
15/
SELECT title, year, rating
FROM movie.movie
WHERE rating >= ALL
(
SELECT rating
FROM movie.movie
)


	 */
	public String genQuery15() {
		return "SELECT title, year, rating\r\n" + 
				"FROM movie.movie\r\n" + 
				"WHERE rating >= ALL\r\n" + 
				"(\r\n" + 
				"SELECT rating\r\n" + 
				"FROM movie.movie\r\n" + 
				")";
	}
	
	/**
16/
SELECT title, year, rating
FROM movie.movie
WHERE year = XXXX AND rating >= ALL
(
SELECT rating
FROM movie.movie
WHERE year = XXXX
)


	 */
	public String genQuery16(int year) {
		return "SELECT title, year, rating\r\n" + 
				"FROM movie.movie\r\n" + 
				"WHERE year = " + year + " AND rating >= ALL\r\n" + 
				"(\r\n" + 
				"SELECT rating\r\n" + 
				"FROM movie.movie\r\n" + 
				"WHERE year = " +year +"\r\n" + 
				")";
	}
	
	/**
17/
SELECT title, year, rating, runtime
FROM movie.movie
WHERE runtime <= X AND rating >= 8


	 */
	public String genQuery17(String mins) {
		return "SELECT title, year, rating, runtime\r\n" + 
				"FROM movie.movie\r\n" + 
				"WHERE runtime <= " + mins +" AND rating >= 8";
	}
	
	/**
18/
SELECT title, year, rating, genre_name
FROM movie.movie NATURAL JOIN movie.moviegenrerel
WHERE UPPER(genre_name) = UPPER('$G') AND rating >= 8


	 */
	public String genQuery18(String genreName) {
		return "SELECT title, year, rating, genre_name\r\n" + 
				"FROM movie.movie NATURAL JOIN movie.moviegenrerel\r\n" + 
				"WHERE UPPER(genre_name) = UPPER('" +genreName + "') AND rating >= 8";
	}
	
	/**
19/
SELECT runtime
FROM movie.movie
WHERE UPPER(title) = UPPER('$M')


	 */
	public String genQuery19(String movieName) {
		return "SELECT runtime\r\n" + 
				"FROM movie.movie\r\n" + 
				"WHERE UPPER(title) = UPPER('" + movieName + "')";
	}
	
	/**
20/
SELECT *
FROM movie.movie
WHERE
UPPER(story) LIKE UPPER('%X%') AND
...
UPPER(story) LIKE UPPER('%X%')


	 */
	public String genQuery20(String[] arrKeywords) {
		if (arrKeywords.length < 1) {
			return "";
		}
		
		String s= "SELECT *\r\n" + 
				"FROM movie.movie\r\n" + 
				"WHERE\r\n" + 
				"UPPER(story) LIKE UPPER(' " + arrKeywords[0] + "')\r\n";
		for (int i = 1; i < arrKeywords.length; i++) {
			s += "AND UPPER(story) LIKE UPPER('" + arrKeywords[i] + "')\r\n";
		}
		
		return s;
	}
}
