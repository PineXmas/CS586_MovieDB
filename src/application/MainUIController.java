package application;

import java.sql.ResultSet;
import java.util.ArrayList;

import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;

public class MainUIController {
	public Button btnTest;
	public TableView<ArrayList<String>> tableResult;
	
	public ToggleGroup groupQuestions;
	public RadioButton radioQuestion1;
	public RadioButton radioQuestion2;
	public RadioButton radioQuestion3;
	public RadioButton radioQuestion4;
	public RadioButton radioQuestion5;
	public RadioButton radioQuestion6;
	public RadioButton radioQuestion7;
	public RadioButton radioQuestion8;
	public RadioButton radioQuestion9;
	public RadioButton radioQuestion10;
	public RadioButton radioQuestion11;
	public RadioButton radioQuestion12;
	public RadioButton radioQuestion13;
	public RadioButton radioQuestion14;
	public RadioButton radioQuestion15;
	public RadioButton radioQuestion16;
	public RadioButton radioQuestion17;
	public RadioButton radioQuestion18;
	public RadioButton radioQuestion19;
	public RadioButton radioQuestion20;
	public TextField txtActor3;
	public TextField txtActor4;
	public TextField txtActor5;
	public TextField txtActor6;
	public TextField txtMovie1;
	public TextField txtMovie9;
	public TextField txtMovie19;
	public TextField txtYear8;
	public TextField txtYear16;
	public TextField txtDirector11;
	public TextField txtDirector12;
	public TextField txtGenre4;
	public TextField txtGenre12;
	public TextField txtGenre18;
	public TextField txtMin17;
	public TextField txtKeywords20;

	public void onBtnTest_Click(ActionEvent event) {

		try {
			RadioButton selectedBtn = (RadioButton)groupQuestions.getSelectedToggle();
			if (selectedBtn == null) {
				return;
			}
			
			String query = "";
			switch (selectedBtn.getId()) {
			case "radioQuestion1":
				query = Main.db.genQuery01(txtMovie1.getText());
				break;
			case "radioQuestion2":
				query = Main.db.genQuery02();
				break;
			case "radioQuestion3":
				query = Main.db.genQuery03(txtActor3.getText());
				break;
			case "radioQuestion4":
				query = Main.db.genQuery04(txtGenre4.getText(), txtActor4.getText());
				break;
			case "radioQuestion5":
				query = Main.db.genQuery05(txtActor5.getText());
				break;
			case "radioQuestion6":
				query = Main.db.genQuery06(txtActor6.getText());
				break;
			case "radioQuestion7":
				query = Main.db.genQuery07();
				break;
			case "radioQuestion8":
				query = Main.db.genQuery08(Integer.valueOf(txtYear8.getText()));
				break;
			case "radioQuestion9":
				query = Main.db.genQuery09(txtMovie9.getText());
				break;
			case "radioQuestion10":
				query = Main.db.genQuery10();
				break;
			case "radioQuestion11":
				query = Main.db.genQuery11(txtDirector11.getText());
				break;
			case "radioQuestion12":
				query = Main.db.genQuery12(txtGenre12.getText() ,txtDirector12.getText());
				break;
			case "radioQuestion13":
				query = Main.db.genQuery13();
				break;
			case "radioQuestion14":
				query = Main.db.genQuery14();
				break;
			case "radioQuestion15":
				query = Main.db.genQuery15();
				break;
			case "radioQuestion16":
				query = Main.db.genQuery16(Integer.valueOf(txtYear16.getText()));
				break;
			case "radioQuestion17":
				query = Main.db.genQuery17(Integer.valueOf(txtMin17.getText()));
				break;
			case "radioQuestion18":
				query = Main.db.genQuery18(txtGenre18.getText());
				break;
			case "radioQuestion19":
				query = Main.db.genQuery19(txtMovie19.getText());
				break;
			case "radioQuestion20":
				String keywords = txtKeywords20.getText();
				String[] arrKeywords = keywords.split(",");
				for (int i = 0; i < arrKeywords.length; i++) {
					arrKeywords[i] = arrKeywords[i].trim();
				}
				query = Main.db.genQuery20(arrKeywords);
				break;

			default:
				break;
			}
			
			if (query.isEmpty()) {
				return;
			}
			
			ResultSet result = Main.db.executeQuery(query);
			TableView<ArrayList<String>> tableTemp = Main.db.convertResult2Table(result);
			tableResult.getColumns().clear();
			tableResult.getColumns().addAll(tableTemp.getColumns());
			tableResult.getItems().clear();
			tableResult.getItems().addAll(tableTemp.getItems());
		} catch (NumberFormatException e) {
			e.printStackTrace();
			System.out.println("Error while querying");
		}
	}


}
