package br.com.guisi.simulador.rede.view;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.layout.VBox;

import org.apache.commons.lang3.StringUtils;

import br.com.guisi.simulador.rede.SimuladorRede;
import br.com.guisi.simulador.rede.functions.FunctionItem;

public class FunctionsController extends Controller {

	public static final String FXML_FILE = "/fxml/Functions.fxml";
	private static final String FUNCTIONS_PROPERTIES = "functions.properties";
	
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
			this.functions = this.loadProperties();
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
	}
	
	private void editFunctionItem(FunctionItem functionItem) {
		SimuladorRede.showModalScene("Edit Function", FunctionEditController.FXML_FILE, functionItem, this);
	}
	
	public void newFunction() {
		SimuladorRede.showModalScene("Edit Function", FunctionEditController.FXML_FILE, new FunctionItem(), this);
	}
	
	public void updateList(FunctionItem functionItem) {
		if (this.functions.contains(functionItem)) {
			this.functions.set(functions.indexOf(functionItem), functionItem);
		} else {
			functionItem.setFunctionKey(functions.size());
			this.functions.add(functionItem);
		}
		
		tvFunctions.getItems().clear();
		tvFunctions.getItems().addAll(functions);
		
		this.saveProperties();
	}
	
	private List<FunctionItem> loadProperties() throws IOException {
		List<FunctionItem> functions = new ArrayList<>();
		if (Files.exists(Paths.get(FUNCTIONS_PROPERTIES))) {
			Properties prop = new Properties();
			prop.load(new FileInputStream(FUNCTIONS_PROPERTIES));
			
			for (Entry<Object, Object> entry : prop.entrySet()) {
				String key = (String) entry.getKey();
				String value = (String) entry.getValue();
				if (StringUtils.isNotBlank(value)) {
					String[] arr = value.split("@");
					if (arr.length == 3) {
						FunctionItem fi = new FunctionItem(key, arr[0], arr[1], arr[2]);
						functions.add(fi);
					}
				}
			}
		}
		Collections.sort(functions);
		return functions;
	}
	
	private void saveProperties() {
		Properties prop = new Properties();
		for (FunctionItem function : this.functions) {
			String value = function.getFunctionName().get() + "@" + function.getFunctionType().get() + "@" + function.getFunctionExpression();
			prop.put(function.getFunctionKey(), value);
		}
		
		try {
			prop.store(new FileOutputStream(FUNCTIONS_PROPERTIES), null);
		} catch (IOException e) {
			Alert alert = new Alert(AlertType.ERROR, e.getMessage());
			alert.showAndWait();
		}
	}
	
	@Override
	public Node getView() {
		return root;
	}
	
	@Override
	public String getFxmlFile() {
		return FXML_FILE;
	}
}
