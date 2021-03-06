package br.com.guisi.simulador.rede.util;

import java.lang.reflect.Field;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.control.Tooltip;
import javafx.util.Duration;

public class TooltipUtils {
	
	private TooltipUtils() {}

	public static Tooltip hackTooltipStartTiming(Tooltip tooltip) {
	    try {
	        Field fieldBehavior = tooltip.getClass().getDeclaredField("BEHAVIOR");
	        fieldBehavior.setAccessible(true);
	        Object objBehavior = fieldBehavior.get(tooltip);

	        Field fieldTimer = objBehavior.getClass().getDeclaredField("activationTimer");
	        fieldTimer.setAccessible(true);
	        Timeline objTimer = (Timeline) fieldTimer.get(objBehavior);

	        objTimer.getKeyFrames().clear();
	        objTimer.getKeyFrames().add(new KeyFrame(new Duration(250)));
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	    return tooltip;
	}
}
