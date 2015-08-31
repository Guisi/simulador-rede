package br.com.guisi.simulador.rede.view;

import java.util.ArrayList;
import java.util.List;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.layout.VBox;
import br.com.guisi.simulador.rede.SimuladorRede;
import br.com.guisi.simulador.rede.functions.FunctionItem;

public class FunctionsController extends Controller {

	@FXML
	private VBox root;
	@FXML
	private VBox vBoxInternal;
	
	@FXML
	private TableView<FunctionItem> tvFunctions;
	@FXML
	private TableColumn<FunctionItem, String> tcFunctionName;
	@FXML
	private TableColumn<FunctionItem, String> tcFunctionExpression;
	
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
		
		tcFunctionName.setCellValueFactory(cellData -> cellData.getValue().getFunctionName());
		tcFunctionExpression.setCellValueFactory(cellData -> cellData.getValue().getFunctionExpression());
		
		List<FunctionItem> functions = new ArrayList<FunctionItem>();
		
		functions.add(new FunctionItem("FUNCAO_TESTE_1", new SimpleStringProperty("Função Teste"), new SimpleStringProperty("blablabla")));
		functions.add(new FunctionItem("FUNCAO_TESTE_2", new SimpleStringProperty("Função Teste 2"), new SimpleStringProperty("blablabla 2")));
		
		tvFunctions.setItems(FXCollections.observableArrayList());
		tvFunctions.getItems().addAll(functions);
	}
	
	private void editFunctionItem(FunctionItem functionItem) {
		SimuladorRede.showModalScene("Edit Function", "/fxml/FunctionEdit.fxml");
	}
	
	@Override
	public Node getView() {
		return root;
	}
	
}
