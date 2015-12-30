package br.com.guisi.simulador.rede;

import java.util.Map;

import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.AbstractApplicationContext;

import br.com.guisi.simulador.rede.constants.PreferenceKey;
import br.com.guisi.simulador.rede.controller.Controller;
import br.com.guisi.simulador.rede.controller.main.SimuladorRedeController;
import br.com.guisi.simulador.rede.enviroment.Environment;
import br.com.guisi.simulador.rede.util.Matlab;
import br.com.guisi.simulador.rede.util.PreferencesUtils;

public class SimuladorRede extends Application {
	
	/*
	 * Contexto do Spring
	 */
	private static AbstractApplicationContext ctx;
	
	private static Stage primaryStage;
	private static Map<PreferenceKey, String> preferences;
	
	private static Environment environment;
	
	public static void main(String args[]) {
		launch(args);
	}

	/**
	 * Inicializa contexto do Spring
	 */
	private static void initializeSpring(){
		ctx = new AnnotationConfigApplicationContext("br.com.guisi.simulador.rede");
		ctx.registerShutdownHook();
	}
	
	@Override
	public void start(Stage stage) {
		initializeSpring();
		
		SimuladorRede.primaryStage = stage;
		
    	preferences = PreferencesUtils.loadPreferences();
    	
		Controller controller = ctx.getBean(SimuladorRedeController.class);
    	
		Scene scene = new Scene((Parent) controller.getView());
		primaryStage.setScene(scene);
		primaryStage.setTitle("Simulador");
		primaryStage.getIcons().add(new Image("/img/bolt.png"));
		scene.getStylesheets().add("/css/estilo.css");
		//TODO remover primaryStage.setMaximized(true);
		
		primaryStage.setOnCloseRequest((event) -> {
			try {
				Matlab.disconnectMatlabProxy();
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
		
		controller.initializeController();
		primaryStage.show();
	}
	
	public static void showModalScene(String title, Class<?> controllerClass, Object... data) {
    	showScene(title, controllerClass, true, data);
    }
	
	public static void showUtilityScene(String title, Class<?> controllerClass, Object... data) {
    	showScene(title, controllerClass, false, data);
    }
	
	public static void showScene(String title, Class<?> controllerClass, boolean modal, Object... data) {
		Controller controller = (Controller) ctx.getBean(controllerClass);
		Stage stage = controller.getStage();

		if (stage == null) {
			stage = new Stage(StageStyle.UTILITY);
	    	stage.initModality(modal ? Modality.APPLICATION_MODAL : Modality.NONE);
	    	stage.initOwner(primaryStage);
	    	stage.setTitle(title);
	    	stage.setOnCloseRequest((event) -> SimuladorRede.closeScene(controller));
	    	
	    	Pane myPane = (Pane) controller.getView();
	    	if(stage.getScene() == null) {
	    		Scene scene = new Scene(myPane, Color.TRANSPARENT);
	    		scene.getStylesheets().add("/css/estilo.css");
	    		scene.getStylesheets().add("/css/java-keywords.css");
		    	
	    		stage.setScene(scene);
	    	} else {
	    		stage.getScene().setRoot(myPane);
	    	}
	    	controller.setStage(stage);
	    	controller.initializeController();
		}
		controller.initializeControllerData(data);
		stage.centerOnScreen();

		if (!stage.isShowing()) {
	    	stage.show();
    	} else {
    		stage.requestFocus();
    	}
    }
	
	/**
     * Fecha cena
     */
    public static void closeScene(Controller controller) {
    	Stage stage = controller.getStage();
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

	public static AbstractApplicationContext getCtx() {
		return ctx;
	}
	
}