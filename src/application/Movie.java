package application;

import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class Movie {
	public String movieID;
	public String title;
	public String story;
	public String posterURL;
	public String[] arrGenres;
	public String[] arrActors;
	public String[] arrDirectors;
	public String[] arrWriters;
	public int year;
	public int runtime;
	public float rating;
	
	//mark whether this movie should be included in the db
	public boolean isBadMovie;
	
	/**
	 * Construct the movie using a JSON file
	 * @param jsonFilePath
	 */
	public Movie(String jsonFilePath, String movieID) {
		//JSON parser object to parse read file
        JSONParser jsonParser = new JSONParser();
        isBadMovie = false;
         
        try
        {
            //Read JSON file
        	FileReader reader = new FileReader(jsonFilePath);
            JSONObject obj = (JSONObject) jsonParser.parse(reader);
            Object temp;
            
            //parsing into attributes
            
            //movieID
            this.movieID = movieID;
            
            //title
            title = (String)obj.get("title");
            title = removeWeirdChar(title);
            
            //year
            year = Integer.valueOf((String)obj.get("year"));
            
            //rating
            temp = obj.get("rating");
            if (((String)temp).isEmpty()) {
            	temp = "0";
            }
            rating = Float.valueOf((String)temp);
            
            //runtime
            runtime = parseRuntime((String)obj.get("runtime"));
            if (runtime == -1) {
            	isBadMovie = true;
            	return;
            }
            
            //poster
            posterURL = (String)obj.get("poster");
            
            //story
            story = (String)obj.get("story");
            
            //genre
            arrGenres = parseJSONStringArray((JSONArray)obj.get("genre"));
            for (String aGenre : arrGenres) {
				if (aGenre.isEmpty()) {
					isBadMovie = true;
					return;
				}
			}
            
            //director
            temp = obj.get("director");
            if (temp == null) {
            	temp = obj.get("directors");
			}
            arrDirectors = parseJSONStringArray((JSONArray)temp);
            
            //writer
            temp = obj.get("writer");
            if (temp == null) {
            	temp = obj.get("writers");
			}
            arrWriters = parseJSONStringArray((JSONArray)temp);
            
            //actor
            arrActors = parseActors((JSONArray)obj.get("cast"));
            
            //** POST PROCESS
            
            title = postProcessTitle();
            
            //escape single-quote-characters
            arrGenres = escapeSingleQuote(arrGenres);
            arrActors = escapeSingleQuote(arrActors);
            arrDirectors = escapeSingleQuote(arrDirectors);
            arrWriters = escapeSingleQuote(arrWriters);
            title = escapeSingleQuote(title);
            story = escapeSingleQuote(story);
            posterURL = escapeSingleQuote(posterURL);
            
        } catch (Exception e) {
            //System.out.println(e.toString());
            isBadMovie = true;
        }
	}

	/**
	 * Remove year info out of the given title. For example: "Deadpool 2 (2018)" will become "Deadpool 2"
	 */
	public String postProcessTitle() {
		String wannaFind = "(" + year + ")";
		int pos = title.indexOf(wannaFind);
		return title.substring(0, pos).trim();
	}
	
	/**
	 * Return a string ready to be used in the INSERT query, following this order
	 * 				movie_ID varchar
					title varchar
					story varchar
					poster_URL varchar
					year integer
					runtime integer
					rating real
	 * @return
	 */
	public String genInsertQuery() {
		return "("
				+ "'" + movieID + "',"
				+ "'" + title + "',"
				+ "'" + story + "',"
				+ "'" + posterURL + "',"
				+ year + ","
				+ runtime + ","
				+ rating
				+ ")";
	}
	
	/**
	 * Return tuples of form (movie_id, value) to be ready for insert into relational-table for the given attribute.
	 * @param arrValues
	 * @return
	 */
	public String[] genInsertQuery_MovieRel(String[] arrValues) {
		String[] queries = new String[arrValues.length];
		
		for (int i = 0; i < queries.length; i++) {
			queries[i] = "('" + movieID + "', '" + arrValues[i] + "')";
		}
		
		return queries;
	}
	
	public static void collectSets(ArrayList<Movie> listMovies, ArrayList<String> listActors, ArrayList<String> listWriters, ArrayList<String> listGenres, ArrayList<String> listDirectors) {
		for (Movie movie : listMovies) {
			listActors.addAll(Arrays.asList(movie.arrActors));
			listDirectors.addAll(Arrays.asList(movie.arrDirectors));
			listWriters.addAll(Arrays.asList(movie.arrWriters));
			listGenres.addAll(Arrays.asList(movie.arrGenres));
		}
		
		//remove duplicate values using sets
		Set<String> setActors = new HashSet<>(listActors);
		Set<String> setDirectors = new HashSet<>(listDirectors);
		Set<String> setWriters = new HashSet<>(listWriters);
		Set<String> setGenres = new HashSet<>(listGenres);
		
		//assign back to lists
		listActors.clear();
		listActors.addAll(setActors);
		listDirectors.clear();
		listDirectors.addAll(setDirectors);
		listWriters.clear();
		listWriters.addAll(setWriters);
		listGenres.clear();
		listGenres.addAll(setGenres);
	}
	
	public static String escapeSingleQuote(String s) {
		if (!s.contains("'")) {
			return s;
		}
		
		return s.replace("'", "''");
	}
	
	public static String[] escapeSingleQuote(String[] arr) {
		for (int i = 0; i < arr.length; i++) {
			
			arr[i] = escapeSingleQuote(arr[i]);
		}
		
		return arr;
	}
	
	public static String[] parseJSONStringArray(JSONArray arrJSON) {
		String[] arrStrings = new String[arrJSON.size()];
		for (int i = 0; i < arrJSON.size(); i++) {
			arrStrings[i] = (String)arrJSON.get(i);
			arrStrings[i] = removeWeirdChar(arrStrings[i]);
			arrStrings[i] = arrStrings[i].trim();
		}
		
		return arrStrings;
	}
	
	public static String[] parseActors(JSONArray arrJSON) {
		String[] arrStrings = new String[arrJSON.size()];
		for (int i = 0; i < arrJSON.size(); i++) {
			arrStrings[i] = (String)   ((JSONObject)arrJSON.get(i)).get("name");
			arrStrings[i] = removeWeirdChar(arrStrings[i]);
			arrStrings[i] = arrStrings[i].trim();
		}
		
		return arrStrings;
	}
	
	public static int parseRuntime(String runtime) {
		if (runtime.isEmpty()) {
			return -1;
		}
		
		try {
			
			String sHour = "0";
			int pos;
			if ((pos = runtime.indexOf('h')) != -1) {
				 sHour = runtime.substring(0, pos);
				 runtime = runtime.substring(pos+1);
			}
			
			String sMin = "0";
			if ((pos = runtime.indexOf('h')) != -1) {
				sMin = runtime.substring(0, pos);
			}
			
			return Integer.valueOf(sHour) * 60 + Integer.valueOf(sMin);
		} catch (Exception e) {
			return -1;
		}
	}
	
	public static String removeWeirdChar(String source) {
		char[] arrWeirdChars = {194, 160};
		String s = "";
		char replaced;
		for (int i = 0; i < source.length(); i++) {
			replaced = source.charAt(i);
			
			//replace all weird chars by SPACE chars
			for (int j = 0; j < arrWeirdChars.length; j++) {
				if (replaced == arrWeirdChars[j]) {
					replaced = ' ';
					break;
				}
			}
			
			s = s + replaced;
		}
		
		return s;
	}
}
