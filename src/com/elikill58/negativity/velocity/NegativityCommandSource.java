package com.elikill58.negativity.velocity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.permission.Tristate;

import net.kyori.text.Component;
import net.kyori.text.serializer.plain.PlainComponentSerializer;

public class NegativityCommandSource implements CommandSource {

	private static final Logger LOGGER = LoggerFactory.getLogger(NegativityCommandSource.class);

	public static final NegativityCommandSource INSTANCE = new NegativityCommandSource();

	@Override
	public void sendMessage(Component component) {
		String plainMessage = PlainComponentSerializer.INSTANCE.serialize(component);
		LOGGER.info(plainMessage);
	}

	@Override
	public Tristate getPermissionValue(String permission) {
		return Tristate.TRUE;
	}
}
