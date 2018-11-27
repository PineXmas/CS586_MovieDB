package application;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.control.TableView;

public class PrototypeController {
	static String hostURL = "DBCLASS.cs.pdx.edu";
	static String dbName = "f18wdb10";
	static String userName = "f18wdb10";
	static String password = "ekc2Dd#f8r";
	static String schema = "movie";
	
	public Button btnGet;
	public TableView<String> tableGenre;
	
	public void onBtnGet_Click(ActionEvent event) {
		try  {
			Connection conn = DriverManager.getConnection("jdbc:postgresql://"+hostURL+"/" + dbName, userName, password);
			System.out.println("Connected to PostgreSQL database!");

			//perform a query
			Statement statement = conn.createStatement();
			ResultSet resultSet = statement.executeQuery("SELECT * FROM movieDB.Genre;");
			
			//display queried results
			while (resultSet.next()) {
				System.out.printf("Name = %s, Desc. = %s\n", resultSet.getString("genre_name"), resultSet.getString("description"));
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
}
