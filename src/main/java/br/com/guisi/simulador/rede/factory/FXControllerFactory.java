package br.com.guisi.simulador.rede.factory;

import java.io.IOException;

import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;

import br.com.guisi.simulador.rede.constants.EnvironmentKeyType;
import br.com.guisi.simulador.rede.controller.chart.LearningChartsPaneController;
import br.com.guisi.simulador.rede.controller.environment.ElementsDetailsPaneController;
import br.com.guisi.simulador.rede.controller.environment.FunctionsPaneController;
import br.com.guisi.simulador.rede.controller.environment.LabelAndMessagesPaneController;
import br.com.guisi.simulador.rede.controller.main.AgentInformationPaneController;
import br.com.guisi.simulador.rede.controller.main.ControlsPaneController;
import br.com.guisi.simulador.rede.controller.main.MenuPaneController;
import br.com.guisi.simulador.rede.controller.options.ExpressionEvaluatorController;
import br.com.guisi.simulador.rede.controller.options.FunctionEditController;
import br.com.guisi.simulador.rede.controller.options.FunctionsController;
import br.com.guisi.simulador.rede.controller.options.PriorityConfigController;

/**
 * Factory responsavel por registrar os controllers no contexto do Spring
 * @author eliomarcolino
 *
 */
@Configuration
public class FXControllerFactory{

	@Bean
	@Lazy
	public ExpressionEvaluatorController expressionEvaluatorController(){
		return (ExpressionEvaluatorController) loadController(ExpressionEvaluatorController.FXML_FILE);
	}
	
	@Bean
	@Lazy
	public FunctionEditController functionEditController(){
		return (FunctionEditController) loadController(FunctionEditController.FXML_FILE);
	}
	
	@Bean
	@Lazy
	public FunctionsController functionsController(){
		return (FunctionsController) loadController(FunctionsController.FXML_FILE);
	}
	
	@Bean
	@Lazy
	public PriorityConfigController priorityConfigController(){
		return (PriorityConfigController) loadController(PriorityConfigController.FXML_FILE);
	}
	
	@Bean
	@Lazy
	@Scope("prototype")
	public LabelAndMessagesPaneController labelAndMessagesPaneController(EnvironmentKeyType environmentKeyType) {
		LabelAndMessagesPaneController controller = (LabelAndMessagesPaneController) loadController(LabelAndMessagesPaneController.FXML_FILE);
		controller.setEnvironmentKeyType(environmentKeyType);
		return controller;
	}
	
	@Bean
	@Lazy
	@Scope("prototype")
	public FunctionsPaneController functionsPaneController(EnvironmentKeyType environmentKeyType) {
		FunctionsPaneController controller = (FunctionsPaneController) loadController(FunctionsPaneController.FXML_FILE);
		controller.setEnvironmentKeyType(environmentKeyType);
		return controller;
	}
	
	@Bean
	@Lazy
	@Scope("prototype")
	public ElementsDetailsPaneController elementsDetailsPaneController(EnvironmentKeyType environmentKeyType) {
		ElementsDetailsPaneController controller = (ElementsDetailsPaneController) loadController(ElementsDetailsPaneController.FXML_FILE);
		controller.setEnvironmentKeyType(environmentKeyType);
		return controller;
	}
	
	@Bean
	@Lazy
	public MenuPaneController menuPaneController(Stage stage){
		MenuPaneController controller = (MenuPaneController) loadController(MenuPaneController.FXML_FILE);
		controller.setStage(stage);
		return controller;
	}
	
	@Bean
	@Lazy
	public ControlsPaneController controlsPaneController(Stage stage){
		ControlsPaneController controller = (ControlsPaneController) loadController(ControlsPaneController.FXML_FILE);
		controller.setStage(stage);
		return controller;
	}
	
	@Bean
	@Lazy
	public LearningChartsPaneController learningChartsPaneController() {
		return (LearningChartsPaneController) loadController(LearningChartsPaneController.FXML_FILE);
	}
	
	@Bean
	@Lazy
	public AgentInformationPaneController agentInformationPaneController(Stage stage) {
		AgentInformationPaneController controller = (AgentInformationPaneController) loadController(AgentInformationPaneController.FXML_FILE);
		controller.setStage(stage);
		return controller;
	}
	
	/**
	 * Obtem instancia do controller pelo mecanismo do JavaFX
	 * @param fxmlFile
	 * @return
	 */
	private Object loadController(String fxmlFile) {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.load(getClass().getResourceAsStream(fxmlFile));            
            return loader.getController();
        } catch (IOException e) {
            throw new RuntimeException(String.format("Unable to load FXML file '%s'", fxmlFile), e);
        }
    }
}
