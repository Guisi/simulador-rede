package br.com.guisi.simulador.rede.controller.modal;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
import br.com.guisi.simulador.rede.constants.FunctionType;
import br.com.guisi.simulador.rede.controller.Controller;
import br.com.guisi.simulador.rede.events.EventType;
import br.com.guisi.simulador.rede.functions.FunctionItem;
import br.com.guisi.simulador.rede.util.FunctionsUtils;

public class FunctionsController extends Controller {

	public static final String FXML_FILE = "/fxml/modal/Functions.fxml";
	
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
	public void initializeController() {
		this.initializeTable();
		this.listenToEvent(EventType.FUNCTION_UPDATE);
	}
	
	@Override
	public void initializeControllerData(Object... data) {
	}
	
	@Override
	public void onEvent(EventType eventType, Object data) {
		switch (eventType) {
			case FUNCTION_UPDATE: updateList((FunctionItem) data); break;
			default: break;
		}
	}
	
	private void initializeTable() {
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
			this.functions = new ArrayList<>();
			Map<FunctionType, List<FunctionItem>> functionsMap = FunctionsUtils.loadProperties();
			functionsMap.forEach( (key, value) -> functions.addAll(value));
			
		} catch (IOException e) {
			e.printStackTrace();
			Alert alert = new Alert(AlertType.ERROR, e.getMessage());
			alert.showAndWait();
			SimuladorRede.closeScene(this);
			return;
		}
		
		tcFunctionName.setCellValueFactory(cellData -> cellData.getValue().getFunctionName());
		tcFunctionType.setCellValueFactory(cellData -> cellData.getValue().getFunctionType());
		
		tvFunctions.setItems(FXCollections.observableArrayList());
		tvFunctions.getItems().addAll(functions);
		
		this.getStage().setOnCloseRequest((event) -> {
			this.fireEvent(EventType.FUNCTIONS_UPDATED, null);
			SimuladorRede.closeScene(this);
		});
	}
	
	private void editFunctionItem(FunctionItem functionItem) {
		SimuladorRede.showModalScene("Edit Function", FunctionEditController.class, true, functionItem);
	}
	
	public void newFunction() {
		SimuladorRede.showModalScene("Edit Function", FunctionEditController.class, true, new FunctionItem());
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
			e.printStackTrace();
			Alert alert = new Alert(AlertType.ERROR, e.getMessage());
			alert.showAndWait();
		}
	}
	
	@Override
	public Node getView() {
		return root;
	}
}
