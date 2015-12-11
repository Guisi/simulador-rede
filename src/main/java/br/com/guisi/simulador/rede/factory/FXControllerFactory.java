package br.com.guisi.simulador.rede.factory;

import java.io.IOException;

import javafx.fxml.FXMLLoader;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

import br.com.guisi.simulador.rede.view.ExpressionEvaluatorController;
import br.com.guisi.simulador.rede.view.FunctionEditController;
import br.com.guisi.simulador.rede.view.FunctionsController;
import br.com.guisi.simulador.rede.view.PriorityConfigController;
import br.com.guisi.simulador.rede.view.SimuladorRedeController;

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
