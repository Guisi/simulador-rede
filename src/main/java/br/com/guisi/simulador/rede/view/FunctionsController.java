package br.com.guisi.simulador.rede.view;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.layout.VBox;
import br.com.guisi.simulador.rede.SimuladorRede;
import br.com.guisi.simulador.rede.functions.FunctionItem;
import br.com.guisi.simulador.rede.util.FunctionsUtils;

public class FunctionsController extends Controller {

	public static final String FXML_FILE = "/fxml/Functions.fxml";
	
	@FXML
	private VBox root;
	@FXML
	private VBox vBoxInternal;
	
	@FXML
	private TableView<FunctionItem> tvFunctions;
	@FXML
	private TableColumn<FunctionItem, String> tcFunctionName;
	@FXML
	private TableColumn<FunctionItem, String> tcFunctionType;
	
	private List<FunctionItem> functions;
	
	@Override
	public void initializeController(Object... data) {
		tvFunctions.setRowFactory( tv -> {
		    TableRow<FunctionItem> row = new TableRow<>();
		    row.setOnMouseClicked(event -> {
		        if (event.getClickCount() == 2 && (! row.isEmpty()) ) {
		        	 editFunctionItem(row.getItem());
		        }
		    });
		    return row ;
		});
		
		try {
			this.functions = FunctionsUtils.loadProperties();
		} catch (IOException e) {
			Alert alert = new Alert(AlertType.ERROR, e.getMessage());
			alert.showAndWait();
			SimuladorRede.closeScene(this);
			return;
		}
		
		tcFunctionName.setCellValueFactory(cellData -> cellData.getValue().getFunctionName());
		tcFunctionType.setCellValueFactory(cellData -> cellData.getValue().getFunctionType());
		
		tvFunctions.setItems(FXCollections.observableArrayList());
		tvFunctions.getItems().addAll(functions);
		
		SimuladorRedeController controller = (SimuladorRedeController) data[0];
		this.getStage().setOnCloseRequest((event) -> {
			controller.updateFunctionsTables();
			SimuladorRede.closeScene(this);
		});
	}
	
	private void editFunctionItem(FunctionItem functionItem) {
		SimuladorRede.showModalScene("Edit Function", FunctionEditController.class, functionItem, this);
	}
	
	public void newFunction() {
		SimuladorRede.showModalScene("Edit Function", FunctionEditController.class, new FunctionItem(), this);
	}
	
	public void removeFunction() {
		FunctionItem fi = tvFunctions.getSelectionModel().getSelectedItem();
		if (fi == null) {
			Alert alert = new Alert(AlertType.ERROR, "Selecione um item para remover!");
			alert.showAndWait();
		} else {
			Alert alert = new Alert(AlertType.CONFIRMATION);
			alert.setTitle("Confirmação");
			alert.setContentText("Are you sure??");

			Optional<ButtonType> result = alert.showAndWait();
			if (result.get() == ButtonType.OK){
			    this.removeFunctionItem(fi);
				alert.close();
			} else {
				alert.close();
			}
			
		}
	}
	
	private void removeFunctionItem(FunctionItem functionItem) {
		this.functions.remove(functionItem);
		this.saveList();
	}
	
	public void updateList(FunctionItem functionItem) {
		if (this.functions.contains(functionItem)) {
			this.functions.set(functions.indexOf(functionItem), functionItem);
		} else {
			functionItem.setFunctionIndex(functions.size());
			this.functions.add(functionItem);
		}
		this.saveList();
	}
	
	private void saveList() {
		tvFunctions.getItems().clear();
		tvFunctions.getItems().addAll(functions);
		
		try {
			FunctionsUtils.saveProperties(this.functions);
		} catch (IOException e) {
			Alert alert = new Alert(AlertType.ERROR, e.getMessage());
			alert.showAndWait();
		}
	}
	
	@Override
	public Node getView() {
		return root;
	}
}
