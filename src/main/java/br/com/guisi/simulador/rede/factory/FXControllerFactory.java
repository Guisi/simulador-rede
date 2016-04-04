package br.com.guisi.simulador.rede.factory;

import java.io.IOException;

import javafx.fxml.FXMLLoader;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

import br.com.guisi.simulador.rede.controller.chart.EnvironmentChartsPaneController;
import br.com.guisi.simulador.rede.controller.chart.LearningChartsPaneController;
import br.com.guisi.simulador.rede.controller.main.ControlsPaneController;
import br.com.guisi.simulador.rede.controller.main.ElementsDetailsPaneController;
import br.com.guisi.simulador.rede.controller.main.FunctionsPaneController;
import br.com.guisi.simulador.rede.controller.main.LabelAndMessagesPaneController;
import br.com.guisi.simulador.rede.controller.main.MenuPaneController;
import br.com.guisi.simulador.rede.controller.main.SimuladorRedeController;
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
	public SimuladorRedeController simuladorRedeController(){
		return (SimuladorRedeController) loadController(SimuladorRedeController.FXML_FILE);
	}
	
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
	public LabelAndMessagesPaneController labelAndMessagesPaneController(){
		return (LabelAndMessagesPaneController) loadController(LabelAndMessagesPaneController.FXML_FILE);
	}
	
	@Bean
	@Lazy
	public FunctionsPaneController functionsPaneController(){
		return (FunctionsPaneController) loadController(FunctionsPaneController.FXML_FILE);
	}
	
	@Bean
	@Lazy
	public ElementsDetailsPaneController elementsDetailsPaneController(){
		return (ElementsDetailsPaneController) loadController(ElementsDetailsPaneController.FXML_FILE);
	}
	
	@Bean
	@Lazy
	public MenuPaneController menuPaneController(){
		return (MenuPaneController) loadController(MenuPaneController.FXML_FILE);
	}
	
	@Bean
	@Lazy
	public ControlsPaneController controlsPaneController(){
		return (ControlsPaneController) loadController(ControlsPaneController.FXML_FILE);
	}
	
	@Bean
	@Lazy
	public EnvironmentChartsPaneController environmentChartsPaneController() {
		return (EnvironmentChartsPaneController) loadController(EnvironmentChartsPaneController.FXML_FILE);
	}
	
	@Bean
	@Lazy
	public LearningChartsPaneController learningChartsPaneController() {
		return (LearningChartsPaneController) loadController(LearningChartsPaneController.FXML_FILE);
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
