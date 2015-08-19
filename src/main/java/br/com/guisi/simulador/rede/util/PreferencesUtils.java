package br.com.guisi.simulador.rede.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import br.com.guisi.simulador.rede.constants.PreferenceKey;

public class PreferencesUtils {

	private PreferencesUtils() {}
	
	public static Map<PreferenceKey, String> loadPreferences() {
		Map<PreferenceKey, String> map = new HashMap<PreferenceKey, String>();
		Preferences prefs = Preferences.userRoot().node(PreferencesUtils.class.getName());
		
		for (PreferenceKey preferenceKey : PreferenceKey.values()) {
			String value = prefs.get(preferenceKey.name(), preferenceKey.getDefaultValue());
			map.put(preferenceKey, value);
		}
		
		return map;
	}
	
	public static void savePreferences(Map<PreferenceKey, String> map) throws BackingStoreException {
		Preferences prefs = Preferences.userRoot().node(PreferencesUtils.class.getName());
		for (Entry<PreferenceKey, String> entry : map.entrySet()) {
			prefs.put(entry.getKey().name(), entry.getValue());
		}
		prefs.flush();
	}
}
