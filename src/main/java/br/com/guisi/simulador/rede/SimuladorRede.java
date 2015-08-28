package br.com.guisi.simulador.rede;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.effect.BoxBlur;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import br.com.guisi.simulador.rede.constants.PreferenceKey;
import br.com.guisi.simulador.rede.enviroment.Environment;
import br.com.guisi.simulador.rede.util.PreferencesUtils;
import br.com.guisi.simulador.rede.view.Controller;

public class SimuladorRede extends Application {

	private static Stage primaryStage;
	private static Map<PreferenceKey, String> preferences;
	private static Map<String, Stage> openStages = new HashMap<String, Stage>();
	
	private static Environment environment;
	
	public static void main(String args[]) {
		launch(args);
	}

	@Override
	public void start(Stage stage) {
		SimuladorRede.primaryStage = stage;
		
		FXMLLoader loader = new FXMLLoader();
        try {
        	preferences = PreferencesUtils.loadPreferences();
        	
        	Pane node = loader.load(getClass().getResourceAsStream("/fxml/SimuladorRede.fxml"));
        	
			Scene scene = new Scene(node);
			primaryStage.setScene(scene);
			primaryStage.setTitle("Simulador");
			primaryStage.getIcons().add(new Image("/img/bolt.png"));
			scene.getStylesheets().add("/css/estilo.css");
			primaryStage.setMaximized(true);
			primaryStage.show();
			
        } catch (IOException e) {
        	throw new RuntimeException("Unable to load FXML file", e);
        }
	}
	
	public static void showModalScene(String title, String fxmlFile) {
    	showScene(title, fxmlFile, true);
    }
	
	public static void showUtilityScene(String title, String fxmlFile) {
    	showScene(title, fxmlFile, false);
    }
	
	public static void showScene(String title, String fxmlFile, boolean modal) {
		Stage stage = openStages.get(fxmlFile);
		
		if (stage == null) {
			FXMLLoader loader = new FXMLLoader();
			try {
				loader.load(SimuladorRede.class.getResourceAsStream(fxmlFile));
				Controller controller = (Controller) loader.getController();
				
				stage = new Stage(StageStyle.UTILITY);
		    	stage.initModality(modal ? Modality.APPLICATION_MODAL : Modality.NONE);
		    	stage.initOwner(primaryStage);
		    	stage.setTitle(title);
		    	final Stage finalStage = stage;
		    	stage.setOnCloseRequest((event) -> SimuladorRede.closeScene(finalStage));
		    	controller.setStage(stage);
		    	
		    	Pane myPane = (Pane) controller.getView();
    	    	if(stage.getScene() == null) {
    	    		Scene scene = new Scene(myPane, Color.TRANSPARENT);
    	    		scene.getStylesheets().add("/css/estilo.css");
    	    		scene.getStylesheets().add("/css/java-keywords.css");
    		    	
    	    		stage.setScene(scene);
    	    	} else {
    	    		stage.getScene().setRoot(myPane);
    	    	}
    	    	
    	    	openStages.put(fxmlFile, stage);
			} catch (IOException e) {
				throw new RuntimeException("Unable to load FXML file", e);
			}
		}
		
		stage.centerOnScreen();

		if (!stage.isShowing()) {
	    	if (modal) {
	    		primaryStage.getScene().getRoot().setEffect(new BoxBlur());
	    	}
	    	stage.show();
    	} else {
    		stage.requestFocus();
    	}
    }
	
	/**
     * Fecha cena
     */
    public static void closeScene(Stage stage){
    	if (stage.getModality().equals(Modality.APPLICATION_MODAL)) {
    		primaryStage.getScene().getRoot().setEffect(null);
    	}
		if (stage != null && stage.isShowing()) {
			stage.close();
		}
    }

	public static Stage getPrimaryStage() {
		return primaryStage;
	}

	public static Map<PreferenceKey, String> getPreferences() {
		return preferences;
	}

	public static Environment getEnvironment() {
		return environment;
	}

	public static void setEnvironment(Environment environment) {
		SimuladorRede.environment = environment;
	}
	
}