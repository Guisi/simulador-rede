package br.com.guisi.simulador.rede;

import java.util.HashMap;
import java.util.Map;

import javafx.application.Application;
import javafx.collections.ObservableList;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import org.apache.commons.lang3.SerializationUtils;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.AbstractApplicationContext;

import br.com.guisi.simulador.rede.constants.Constants;
import br.com.guisi.simulador.rede.constants.EnvironmentKeyType;
import br.com.guisi.simulador.rede.constants.PropertyKey;
import br.com.guisi.simulador.rede.controller.Controller;
import br.com.guisi.simulador.rede.controller.main.SimuladorRedeController;
import br.com.guisi.simulador.rede.enviroment.Environment;
import br.com.guisi.simulador.rede.util.Matlab;
import br.com.guisi.simulador.rede.util.PropertiesUtils;

import com.sun.javafx.stage.StageHelper;

public class SimuladorRede extends Application {

	/*
	 * Contexto do Spring
	 */
	private static AbstractApplicationContext ctx;

	private static Stage primaryStage;
	
	private static Map<EnvironmentKeyType, Environment> environmentMap;
	
	public static void main(String args[]) {
		launch(args);
	}

	/**
	 * Inicializa contexto do Spring
	 */
	private static void initializeSpring() {
		ctx = new AnnotationConfigApplicationContext("br.com.guisi.simulador.rede");
		ctx.registerShutdownHook();
	}

	@Override
	public void start(Stage stage) {
		initializeSpring();

		SimuladorRede.primaryStage = stage;

		Controller controller = ctx.getBean(SimuladorRedeController.class, primaryStage);

		Scene scene = new Scene((Parent) controller.getView());
		primaryStage.setScene(scene);
		primaryStage.setTitle("Eletric Network Reconfiguration Simulator");
		primaryStage.getIcons().add(new Image("/img/bolt.png"));
		scene.getStylesheets().add("/css/estilo.css");
		primaryStage.setMaximized(true);
		
		primaryStage.setOnCloseRequest((event) -> {
			ObservableList<Stage> stages = StageHelper.getStages();
			stages.forEach(s -> saveStageSize(s));
			
			try {
				Matlab.disconnectMatlabProxy();
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
		
		primaryStage.show();
	}

	public static Controller showModalScene(String title, Class<?> controllerClass, boolean visible, Object... data) {
		Controller controller = (Controller) ctx.getBean(controllerClass);
		return showModalScene(title, controller, visible, data);
	}
	
	public static Controller showModalScene(String title, Controller controller, boolean visible, Object... data) {
		return showScene(title, controller, true, visible, false, data);
	}

	public static Controller showUtilityScene(String title, Class<?> controllerClass, boolean visible, boolean maximized, Object... data) {
		Controller controller = (Controller) ctx.getBean(controllerClass);
		return showUtilityScene(title, controller, visible, maximized, data);
	}
	
	public static Controller showUtilityScene(String title, Controller controller, boolean visible, boolean maximized, Object... data) {
		return showScene(title, controller, false, visible, maximized, data);
	}
	
	public static Controller showScene(String title, Controller controller, boolean modal, boolean visible, boolean maximized, Object... data) {
		Stage stage = controller.getStage();

		if (stage == null) {
			stage = new Stage(StageStyle.DECORATED);
			stage.initModality(modal ? Modality.APPLICATION_MODAL : Modality.NONE);
			stage.initOwner(primaryStage);
			stage.setTitle(title);
			stage.setOnCloseRequest((event) -> SimuladorRede.closeScene(controller));
			stage.setMaximized(maximized);
			stage.getProperties().put(Constants.CONTROLLER_KEY, controller.getControllerKey());

			Pane myPane = (Pane) controller.getView();
			if (stage.getScene() == null) {
				Scene scene = new Scene(myPane, Color.TRANSPARENT);
				scene.getStylesheets().add("/css/estilo.css");
				scene.getStylesheets().add("/css/java-keywords.css");

				stage.setScene(scene);
			} else {
				stage.getScene().setRoot(myPane);
			}
			controller.setStage(stage);
		}
		
		if (data != null) {
			controller.initializeControllerData(data);
		}
		
		String windowPositionX = PropertiesUtils.getProperty(PropertyKey.WINDOW_POSITION_X, controller.getControllerKey());
		if (windowPositionX != null) {
			stage.setX(Double.valueOf(windowPositionX));
			
			String windowPositionY = PropertiesUtils.getProperty(PropertyKey.WINDOW_POSITION_Y, controller.getControllerKey());
			if (windowPositionY != null) {
				stage.setY(Double.valueOf(windowPositionY));
			}
			
			String windowWidth = PropertiesUtils.getProperty(PropertyKey.WINDOW_WIDTH, controller.getControllerKey());
			if (windowWidth != null) {
				stage.setWidth(Double.valueOf(windowWidth));
			}
			
			String windowHeight = PropertiesUtils.getProperty(PropertyKey.WINDOW_HEIGHT, controller.getControllerKey());
			if (windowHeight != null) {
				stage.setHeight(Double.valueOf(windowHeight));
			}
		} else {
			stage.centerOnScreen();
		}

		if (!stage.isShowing()) {
			stage.show();
		} else {
			stage.requestFocus();
		}

		return controller;
	}

	/**
	 * Fecha cena
	 */
	public static void closeScene(Controller controller) {
		Stage stage = controller.getStage();
		if (stage != null && stage.isShowing()) {
			saveStageSize(stage);
			stage.close();
		}
	}
	
	private static void saveStageSize(Stage stage) {
		String controllerKey = (String) stage.getProperties().get(Constants.CONTROLLER_KEY);
		if (controllerKey != null) {
			PropertiesUtils.saveProperty(PropertyKey.WINDOW_POSITION_X, controllerKey, String.valueOf(stage.getX()));
			PropertiesUtils.saveProperty(PropertyKey.WINDOW_POSITION_Y, controllerKey, String.valueOf(stage.getY()));
			PropertiesUtils.saveProperty(PropertyKey.WINDOW_WIDTH, controllerKey, String.valueOf(stage.getWidth()));
			PropertiesUtils.saveProperty(PropertyKey.WINDOW_HEIGHT, controllerKey, String.valueOf(stage.getHeight()));
		}
	}

	public static Stage getPrimaryStage() {
		return primaryStage;
	}

	public static Environment getEnvironment(EnvironmentKeyType environmentKeyType) {
		return environmentMap != null ? environmentMap.get(environmentKeyType) : null;
	}
	
	public static void setEnvironment(Environment environment) {
		environmentMap = new HashMap<>();
		for (EnvironmentKeyType type : EnvironmentKeyType.values()) {
			environmentMap.put(type, SerializationUtils.clone(environment));
		}
	}

	public static AbstractApplicationContext getCtx() {
		return ctx;
	}

}