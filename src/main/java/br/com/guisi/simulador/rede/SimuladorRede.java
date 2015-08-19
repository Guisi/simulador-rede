package br.com.guisi.simulador.rede;

import java.io.IOException;
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
import br.com.guisi.simulador.rede.util.PreferencesUtils;
import br.com.guisi.simulador.rede.view.Controller;

public class SimuladorRede extends Application {

	private static Stage primaryStage;
	private static Stage modalStage;
	private static Map<PreferenceKey, String> preferences;
	
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
			primaryStage.getIcons().add(new Image("/img/favico.png"));
			scene.getStylesheets().add("/css/estilo.css");
			primaryStage.setMaximized(true);
			primaryStage.show();
			
        } catch (IOException e) {
        	throw new RuntimeException("Unable to load FXML file", e);
        }
	}
	
	public static void showModalScene(Controller controller) {
    	if (modalStage == null) {
    		modalStage = new Stage(StageStyle.TRANSPARENT);
	    	modalStage.initModality(Modality.WINDOW_MODAL);
	    	modalStage.initOwner(primaryStage);
    	}
	    
    	if (!modalStage.isShowing()) {
	    	Pane myPane = (Pane) controller.getView();
			    	
	    	if(modalStage.getScene() == null) {
	    		Scene scene = new Scene(myPane, Color.TRANSPARENT);
	    		scene.getStylesheets().add("/css/estilo.css");
		    	
	    		modalStage.setScene(scene);
	    	} else {
	    		modalStage.getScene().setRoot(myPane);
	    	}
	    	primaryStage.getScene().getRoot().setEffect(new BoxBlur());
	    	modalStage.centerOnScreen();
	    	modalStage.show();
    	}
    }
	
	/**
     * Fecha cena modal
     */
    public static void closeModalScene(){
    	primaryStage.getScene().getRoot().setEffect(null);
		if(modalStage != null && modalStage.isShowing()){
			modalStage.close();
		}
    }

	public static Stage getPrimaryStage() {
		return primaryStage;
	}

	public static Stage getModalStage() {
		return modalStage;
	}

	public static Map<PreferenceKey, String> getPreferences() {
		return preferences;
	}
	
}