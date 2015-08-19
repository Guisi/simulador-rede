package br.com.guisi.simulador.rede.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import br.com.guisi.simulador.rede.constants.PreferenceKey;

public class PreferencesUtils {

	private PreferencesUtils() {}
	
	public static Map<PreferenceKey, StringProperty> loadPreferences() {
		Map<PreferenceKey, StringProperty> map = new HashMap<PreferenceKey, StringProperty>();
		Preferences prefs = Preferences.userRoot().node(PreferencesUtils.class.getName());
		
		for (PreferenceKey preferenceKey : PreferenceKey.values()) {
			String value = prefs.get(preferenceKey.name(), preferenceKey.getDefaultValue());
			map.put(preferenceKey, new SimpleStringProperty(value));
		}
		
		return map;
	}
	
	public static void savePreferences(Map<PreferenceKey, StringProperty> map) throws BackingStoreException {
		Preferences prefs = Preferences.userRoot().node(PreferencesUtils.class.getName());
		for (Entry<PreferenceKey, StringProperty> entry : map.entrySet()) {
			prefs.put(entry.getKey().name(), entry.getValue().get());
		}
		prefs.flush();
	}
}
