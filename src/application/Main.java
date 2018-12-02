package application;
	
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;


public class Main extends Application {
	public static MovieDatabase db = new MovieDatabase();
	
	@Override
	public void start(Stage primaryStage) {
		startMainUI(primaryStage);
		db.connect();
	}

	@Override
	public void stop() throws Exception {
		db.disconnect();
		super.stop();
	}
	
	public void startMainUI(Stage primaryStage) {
		try {
			Parent root = FXMLLoader.load(getClass().getResource("mainUI.fxml"));
			
			Scene scene = new Scene(root);
			scene.getStylesheets().add(Main.class.getResource("dark_modena.css").toExternalForm());
			
			primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("popcorn.png")));
			primaryStage.setScene(scene);
			primaryStage.setTitle("MovieDB");
			primaryStage.show();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public void startPrototype(Stage primaryStage) {
		try {
			Parent root = FXMLLoader.load(getClass().getResource("prototype.fxml"));
			
			Scene scene = new Scene(root);
			
			primaryStage.setScene(scene);
			primaryStage.show();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		launch(args);
	}
}
