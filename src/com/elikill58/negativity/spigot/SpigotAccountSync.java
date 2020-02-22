package com.elikill58.negativity.spigot;

import java.io.IOException;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;

import org.bukkit.entity.Player;

import com.elikill58.negativity.universal.BaseAccountSync;
import com.elikill58.negativity.universal.NegativityAccount;
import com.elikill58.negativity.universal.pluginMessages.NegativityMessagesManager;
import com.elikill58.negativity.universal.pluginMessages.UpdateAccountMessage;
import com.elikill58.negativity.universal.pluginMessages.UpdateAccountRequestMessage;

/**
 * Account synchronization mechanism for the Spigot plugin.
 */
public class SpigotAccountSync {

	/**
	 * A map of all accounts waiting for and update to be received.
	 * Keys are the id of the account and values a set of the fields that have been
	 * changed since the update request.
	 */
	private static final Map<UUID, EnumSet<UpdateAccountMessage.AccountField>> PENDING_UPDATES = new HashMap<>();

	public static void requestAllFields(Player player) {
		requestUpdate(player, EnumSet.allOf(UpdateAccountMessage.AccountField.class));
	}

	/**
	 * Method to call when account fields have been updated on this side and the new values should be sent to the proxy.
	 *
	 * @param player the player owning the account
	 * @param account the updated account
	 * @param fields the fields to send to the proxy
	 */
	public static void sendFieldUpdate(Player player, NegativityAccount account, Set<UpdateAccountMessage.AccountField> fields) {
		EnumSet<UpdateAccountMessage.AccountField> modifiedFields = PENDING_UPDATES.get(player.getUniqueId());
		if (modifiedFields != null) {
			// An update request has already been sent
			modifiedFields.addAll(fields);
		} else {
			sendUpdate(player, account, fields);
		}
	}

	public static void applyUpdate(Player player, NegativityAccount account, NegativityAccount update, Set<UpdateAccountMessage.AccountField> fieldsToUpdate) {
		Set<UpdateAccountMessage.AccountField> outdatedFields = PENDING_UPDATES.remove(account.getPlayerId());
		if (outdatedFields == null) {
			outdatedFields = Collections.emptySet();
		}

		BaseAccountSync.applyUpdate(account, update, fieldsToUpdate, outdatedFields, false);

		if (outdatedFields != null && !outdatedFields.isEmpty()) {
			sendUpdate(player, account, outdatedFields);
		}
	}

	private static void requestUpdate(Player player, Set<UpdateAccountMessage.AccountField> fields) {
		PENDING_UPDATES.put(player.getUniqueId(), EnumSet.noneOf(UpdateAccountMessage.AccountField.class));
		try {
			byte[] message = NegativityMessagesManager.writeMessage(new UpdateAccountRequestMessage(player.getUniqueId(), fields));
			player.sendPluginMessage(SpigotNegativity.getInstance(), NegativityMessagesManager.CHANNEL_ID, message);
		} catch (IOException e) {
			SpigotNegativity.getInstance().getLogger().log(Level.SEVERE, "Could not send an account update request.", e);
		}
	}

	private static void sendUpdate(Player player, NegativityAccount account, Set<UpdateAccountMessage.AccountField> fields) {
		try {
			byte[] message = NegativityMessagesManager.writeMessage(new UpdateAccountMessage(account, fields));
			player.sendPluginMessage(SpigotNegativity.getInstance(), NegativityMessagesManager.CHANNEL_ID, message);
		} catch (IOException e) {
			SpigotNegativity.getInstance().getLogger().log(Level.SEVERE, "Could not send an account update.", e);
		}
	}
}
