package com.elikill58.negativity.universal;

import com.elikill58.negativity.universal.adapter.Adapter;

import java.util.ArrayList;
import java.util.List;

public class SuspectManager {

	public static boolean ENABLED = Adapter.getAdapter().getBooleanInConfig("suspect.enabled"),
			ENABLED_CMD = Adapter.getAdapter().getBooleanInConfig("suspect_command"),
			CHAT = Adapter.getAdapter().getBooleanInConfig("suspect.chat"),
			WITH_REPORT = Adapter.getAdapter().getBooleanInConfig("suspect.with_report_cmd");

	public static void init() {
		ENABLED = Adapter.getAdapter().getBooleanInConfig("suspect.enabled");
		ENABLED_CMD = Adapter.getAdapter().getBooleanInConfig("suspect_command");
		CHAT = Adapter.getAdapter().getBooleanInConfig("suspect.chat");
		WITH_REPORT = Adapter.getAdapter().getBooleanInConfig("suspect.with_report_cmd");
	}

	public static void analyzeText(NegativityPlayer np, String text) {
		String[] content = text.split(" ");
		List<AbstractCheat> cheats = new ArrayList<>();
		for(String s : content) {
			for(AbstractCheat c : Adapter.getAdapter().getAbstractCheats())
				for(String alias : c.getAliases())
					if(alias.equalsIgnoreCase(s) || alias.contains(s) || alias.startsWith(s))
						cheats.add(c);
		}
	}

	public static void analyzeText(NegativityPlayer np, List<AbstractCheat> cheats) {
		for(AbstractCheat ac : cheats) {
			np.startAnalyze(ac);
		}
	}
}
