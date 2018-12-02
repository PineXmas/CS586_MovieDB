package application;

import java.util.ArrayList;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.control.TableView;

public class MainUIController {
	public Button btnTest;
	public TableView<ObservableList<String>> tableResult;

	public void onBtnTest_Click(ActionEvent event) {
		
		//prepare data
		ArrayList<ArrayList<String>> data = new ArrayList<>();
		ArrayList<String> row1 = new ArrayList<>();
		ArrayList<String> row2 = new ArrayList<>();

		row1.add("01");
		row1.add("thong");

		row2.add("02");
		row2.add("pine");

		data.add(row1);
		data.add(row2);

		//bind data to table 
		ObservableList<ObservableList<String>> cvsData = FXCollections.observableArrayList();

		for (ArrayList<String> dataList : data) {
			ObservableList<String> row = FXCollections.observableArrayList();
			for (String rowData : dataList) {
				row.add(rowData);
			}
			cvsData.add(row);
		}

		tableResult.setItems(cvsData);
	}
}
