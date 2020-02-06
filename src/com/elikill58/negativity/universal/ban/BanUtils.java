package com.elikill58.negativity.universal.ban;

import java.util.concurrent.CompletableFuture;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import org.checkerframework.checker.nullness.qual.Nullable;

import com.elikill58.negativity.spigot.SpigotNegativity;
import com.elikill58.negativity.sponge.SpongeNegativity;
import com.elikill58.negativity.universal.Cheat;
import com.elikill58.negativity.universal.NegativityPlayer;
import com.elikill58.negativity.universal.adapter.Adapter;

public class BanUtils {

	public static int computeBanDuration(NegativityPlayer player, int reliability, Cheat cheat) {
		try {
			ScriptEngineManager factory = new ScriptEngineManager();
			ScriptEngine engine = factory.getEngineByName("JavaScript");
			String script = Adapter.getAdapter().getStringInConfig("ban.time.calculator")
					.replaceAll("%reliability%", String.valueOf(reliability))
					.replaceAll("%alert%", String.valueOf(player.getWarn(cheat)))
					.replaceAll("%all_alert%", String.valueOf(player.getAllWarn(cheat)));
			return Integer.parseInt(engine.eval(script).toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return -1;
	}

	public static boolean shouldBan(Cheat cheat, NegativityPlayer np, int relia) {
		if (!cheat.isActive() || !BanManager.banActive) {
			return false;
		}
		Adapter ada = Adapter.getAdapter();
		return ada.getIntegerInConfig("ban.reliability_need") >= relia && ada.getIntegerInConfig("ban.alert_need") >= np.getAllWarn(cheat);
	}

	/**
	 * Basically common code for {@link SpigotNegativity#alertMod} and {@link SpongeNegativity#alertMod}.
	 * @return see {@link BanManager#executeBan}, null if banning was not needed
	 */
	public static CompletableFuture<@Nullable ActiveBan> banIfNeeded(NegativityPlayer player, Cheat cheat, int reliability) {
		if (!shouldBan(cheat, player, reliability)) {
			return CompletableFuture.completedFuture(null);
		}

		return CompletableFuture.supplyAsync(() -> {
			String reason = player.getReason(cheat);
			int banDuration = -1;
			int banDefThreshold = Adapter.getAdapter().getIntegerInConfig("ban.def.ban_time");
			boolean isDefinitive = BanManager.getLoggedBans(player.getUUID()).join().size() >= banDefThreshold;
			if (!isDefinitive) {
				banDuration = BanUtils.computeBanDuration(player, reliability, cheat);
			}
			return BanManager.executeBan(new ActiveBan(player.getUUID(), "Cheat (" + reason + ")", "Negativity", BanType.MOD, banDuration, reason)).join();
		});
	}
}
