package com.elikill58.negativity.universal;

import java.util.Map;
import java.util.Set;

import com.elikill58.negativity.universal.pluginMessages.UpdateAccountMessage;

public class BaseAccountSync {

	public static void applyUpdate(NegativityAccount account,
								   NegativityAccount update,
								   Set<UpdateAccountMessage.AccountField> fieldsToUpdate,
								   Set<UpdateAccountMessage.AccountField> outdatedFields,
								   boolean alwaysOverwrite) {
		if (fieldsToUpdate.contains(UpdateAccountMessage.AccountField.LANGUAGE)
				&& (alwaysOverwrite || !outdatedFields.contains(UpdateAccountMessage.AccountField.LANGUAGE))) {
			// Only update the language if the value is not outdated
			account.setLang(update.getLang());
		}

		if (fieldsToUpdate.contains(UpdateAccountMessage.AccountField.MINERATE)) {
			Minerate minerate = account.getMinerate();
			Minerate updatedMinerate = update.getMinerate();
			for (Minerate.MinerateType type : Minerate.MinerateType.values()) {
				// Add the values because the local value may have changed since the request has been sent
				int value = updatedMinerate.getMinerateType(type);
				if (!alwaysOverwrite) {
					value += minerate.getMinerateType(type);
				}
				minerate.setMine(type, value);
			}
		}

		if (fieldsToUpdate.contains(UpdateAccountMessage.AccountField.MOST_CLICKS_PER_SECOND)) {
			int value = update.getMostClicksPerSecond();
			if (!alwaysOverwrite) {
				value += account.getMostClicksPerSecond();
			}
			account.setMostClicksPerSecond(value);
		}

		if (fieldsToUpdate.contains(UpdateAccountMessage.AccountField.WARNS)) {
			Map<String, Integer> allWarns = account.getAllWarns();
			Map<String, Integer> updatedWarns = update.getAllWarns();
			for (Map.Entry<String, Integer> entry : updatedWarns.entrySet()) {
				String key = entry.getKey();
				int value = entry.getValue();
				if (!alwaysOverwrite) {
					value += allWarns.getOrDefault(key, 0);
				}
				account.setWarnCount(key, value);
			}
			// Reset the warn count for cheats that are not contained in the up-to-date value
			allWarns.forEach((cheatKey, value) -> {
				if (!updatedWarns.containsKey(cheatKey)) {
					account.setWarnCount(cheatKey, 0);
				}
			});
		}
	}
}
