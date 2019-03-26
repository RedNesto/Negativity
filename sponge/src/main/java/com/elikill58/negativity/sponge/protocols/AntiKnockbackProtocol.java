package com.elikill58.negativity.sponge.protocols;

import com.elikill58.negativity.sponge.NeedListener;
import com.elikill58.negativity.sponge.SpongeNegativity;
import com.elikill58.negativity.sponge.SpongeNegativityPlayer;
import com.elikill58.negativity.sponge.utils.Cheat;
import com.elikill58.negativity.sponge.utils.ReportType;
import com.elikill58.negativity.sponge.utils.Utils;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.gamemode.GameModes;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.cause.entity.damage.source.DamageSources;
import org.spongepowered.api.event.entity.DamageEntityEvent;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.concurrent.TimeUnit;

public class AntiKnockbackProtocol implements NeedListener {

	@Listener
	public void onEntityDamageByEntity(DamageEntityEvent e, @First Player p) {
		SpongeNegativityPlayer np = SpongeNegativityPlayer.getNegativityPlayer(p);
		if (!np.hasDetectionActive(Cheat.ANTIKNOCKBACK))
			return;
		if (!p.gameMode().get().equals(GameModes.SURVIVAL) && !p.gameMode().get().equals(GameModes.ADVENTURE))
			return;
		Task.builder().delay(20, TimeUnit.MILLISECONDS).execute(new Runnable() {
			@Override
			public void run() {
				final Location<World> last = p.getLocation();
				p.damage(0D, DamageSources.MAGIC);
				Task.builder().delay(250, TimeUnit.MILLISECONDS).execute(new Runnable() {
					@Override
					public void run() {
						Location<World> actual = p.getLocation();
						double d = last.getPosition().distance(actual.getPosition());
						int ping = Utils.getPing(p), relia = Utils.parseInPorcent(100 - d);
						if (d < 0.1 && !actual.getBlock().getType().equals(BlockTypes.WEB) && !p.get(Keys.IS_SNEAKING).orElse(false)){
							np.addWarn(Cheat.ANTIKNOCKBACK);
							/*boolean mayCancel = */SpongeNegativity.alertMod(ReportType.WARNING, p, Cheat.ANTIKNOCKBACK, relia,
									"Distance after damage: " + d + "; Ping: " + ping, "Distance after damage: " + d);
						}
					}
				}).submit(SpongeNegativity.getInstance());
			}
		}).submit(SpongeNegativity.getInstance());
	}
}
