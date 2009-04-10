package com.netifera.platform.host.internal.terminal.ui;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.tm.internal.terminal.provisional.api.ISettingsStore;

public class ReadOnlySettingsStore implements ISettingsStore {
	private Map<String, String> fSettings = new HashMap<String, String>();

	public ReadOnlySettingsStore(String[] settings) {
		for (int i = 0; i < settings.length; i+=2)
			fSettings.put(settings[i], settings[i + 1]);
	}

	public String get(String key) {
		return fSettings.get(key);
	}

	public String get(String key, String defaultValue) {
		String r = fSettings.get(key);
		return r != null ? r : null;
	}

	public void put(String key, String value) {
		throw new IllegalArgumentException("Read-only settings");
	}
}