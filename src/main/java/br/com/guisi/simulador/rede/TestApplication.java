package br.com.guisi.simulador.rede;

import java.util.ArrayList;
import java.util.List;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class TestApplication extends Application {

	private Label statusLabel;
	private TableView<Linha> tv;
	
	public static void main(String[] args) throws Exception {
		launch(args);
	}

	public void start(final Stage stage) throws Exception {
		statusLabel = new Label("Status");
		
		final Button runButton = new Button("Run");

		tv = new TableView<TestApplication.Linha>();
		tv.setItems(FXCollections.observableArrayList());
		TableColumn<Linha, String> tc = new TableColumn<Linha, String>();
		tc.setCellValueFactory(cellData -> cellData.getValue().getProperty());
		tv.getColumns().add(tc);
		
		Obj obj = new Obj();
		obj.setIterations(new ArrayList<Integer>());
		
	    runButton.setOnAction(new EventHandler<ActionEvent>() {
	    	@Override public void handle(ActionEvent actionEvent) {
	    		
	    		Task<Integer> task = new Task<Integer>() {
	    			@Override protected Integer call() throws Exception {
	    				int iterations;
	    				for (iterations = 0; iterations < 1000000; iterations++) {
    						obj.getIterations().add(iterations);
	    				}
	    				
	    				Platform.runLater(new Runnable() {
    						@Override public void run() {
    							updateTable(obj);
    						}
    					});
	    				
	    				return iterations;
	    			}
	    		};
	    		
	    		runButton.disableProperty().bind(task.runningProperty());
	 	     
	    		/*task.valueProperty().addListener((observableValue, oldState, newState) -> {
					System.out.println(newState);
				});*/

	    		new Thread(task).start();
	    	}
	    });
	 
	    final VBox layout = new VBox();
	    layout.getChildren().add(statusLabel);
	    layout.getChildren().add(runButton);
	    layout.getChildren().add(tv);
		
		Scene scene = new Scene(layout);
	    stage.setScene(scene);
	    stage.show();
	}
	
	private void updateTable(Obj obj) {
		statusLabel.setText(String.valueOf(obj.getIterations().size()));
		/*for (Integer i : obj.getIterations()) {
			Linha l = new Linha();
			l.getProperty().setValue(String.valueOf(i));
			tv.getItems().add(l);
		}*/
	}
	
	class Obj {
		private List<Integer> iterations;

		public List<Integer> getIterations() {
			return iterations;
		}

		public void setIterations(List<Integer> iterations) {
			this.iterations = iterations;
		}
	}
	
	class Linha {
		private StringProperty property = new SimpleStringProperty();

		public StringProperty getProperty() {
			return property;
		}

		public void setProperty(StringProperty property) {
			this.property = property;
		}
	}
}
