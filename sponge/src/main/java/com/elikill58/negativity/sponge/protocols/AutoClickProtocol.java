package com.elikill58.negativity.sponge.protocols;

import com.elikill58.negativity.sponge.NeedListener;
import com.elikill58.negativity.sponge.SpongeNegativity;
import com.elikill58.negativity.sponge.SpongeNegativityPlayer;
import com.elikill58.negativity.sponge.utils.Cheat;
import com.elikill58.negativity.sponge.utils.ReportType;
import com.elikill58.negativity.sponge.utils.Utils;
import com.elikill58.negativity.universal.ItemUseBypass;
import com.elikill58.negativity.universal.adapter.Adapter;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.type.HandTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.action.InteractEvent;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.enchantment.Enchantment;
import org.spongepowered.api.item.enchantment.EnchantmentTypes;
import org.spongepowered.api.item.inventory.ItemStack;

import java.util.Optional;

public class AutoClickProtocol implements NeedListener {

    public static final int CLICK_ALERT = Adapter.getAdapter().getIntegerInConfig("cheats.autoclick.click_alert");

    @Listener
	public void onPlayerInteract(InteractEvent e, @First Player p){
		SpongeNegativityPlayer np = SpongeNegativityPlayer.getNegativityPlayer(p);
		Optional<ItemType> item = e.getCause().first(ItemType.class);
		if(item.isPresent())
			if(item.get().equals(ItemTypes.REEDS))
				return;
		if(p.getItemInHand(HandTypes.MAIN_HAND).isPresent())
			if(ItemUseBypass.ITEM_BYPASS.containsKey(p.getItemInHand(HandTypes.MAIN_HAND).get().getType())) {
				ItemUseBypass ib = ItemUseBypass.ITEM_BYPASS.get(p.getItemInHand(HandTypes.MAIN_HAND).get().getType());
				if(ib.getWhen().isClick() && ib.isForThisCheat(Cheat.AUTOSTEAL))
					return;
			}
		np.ACTUAL_CLICK++;
		int ping = Utils.getPing(p), click = np.ACTUAL_CLICK - (ping / 9), efficiency = 0;
		if(p.getItemInHand(HandTypes.MAIN_HAND).isPresent()) {
			ItemStack hand = p.getItemInHand(HandTypes.MAIN_HAND).get();
			if(hand.get(Keys.ITEM_ENCHANTMENTS).isPresent())
				for(Enchantment en : hand.get(Keys.ITEM_ENCHANTMENTS).get())
					if(en.getType() == EnchantmentTypes.EFFICIENCY)
						efficiency = en.getLevel();
		}
		if (click > CLICK_ALERT) {
			boolean mayCancel = SpongeNegativity.alertMod(ReportType.WARNING, p, Cheat.AUTOCLICK, Utils.parseInPorcent(np.ACTUAL_CLICK * 2 - efficiency * 2),
					"Clicks in one second: " + np.ACTUAL_CLICK + "; Last second: " + np.LAST_CLICK
							+ "; Better click in one second: " + np.BETTER_CLICK + " Ping: " + ping, np.ACTUAL_CLICK + " clicks");
			if(Cheat.AUTOCLICK.isSetBack() && mayCancel)
				e.setCancelled(true);
		}
	}
}
