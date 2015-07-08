package br.com.guisi.simulador.rede;

import java.io.IOException;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

public class SimuladorRede extends Application {

	public static void main(String args[]) {
		launch(args);
	}

	@Override
	public void start(Stage stage) {
		
		FXMLLoader loader = new FXMLLoader();
        try {
        	Pane node = loader.load(getClass().getResourceAsStream("/fxml/SimuladorRede.fxml"));
			Scene scene = new Scene(node);
			stage.setScene(scene);
			stage.show();
			
			loader.getController();
        } catch (IOException e) {
        	throw new RuntimeException("Unable to load FXML file", e);
        }
	}
	
}