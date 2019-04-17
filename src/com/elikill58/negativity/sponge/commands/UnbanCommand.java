package com.elikill58.negativity.sponge.commands;

import static org.spongepowered.api.command.args.GenericArguments.onlyOne;
import static org.spongepowered.api.command.args.GenericArguments.user;

import org.spongepowered.api.command.CommandCallable;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.text.Text;

import com.elikill58.negativity.sponge.Messages;
import com.elikill58.negativity.sponge.utils.NegativityCmdWrapper;
import com.elikill58.negativity.universal.ban.BanManager;

public class UnbanCommand implements CommandExecutor {

	@Override
	public CommandResult execute(CommandSource src, CommandContext args) {
		User target = args.requireOne("target");

		if (BanManager.revokeBan(target.getUniqueId()) == null) {
			Messages.sendMessage(src, "unban.not_banned", "%name%", target.getName());
			return CommandResult.empty();
		} else {
			Messages.sendMessage(src, "unban.well_unban", "%name%", target.getName());
			return CommandResult.success();
		}
	}

	public static CommandCallable create() {
		CommandSpec command = CommandSpec.builder()
				.executor(new UnbanCommand())
				.arguments(onlyOne(user(Text.of("target"))))
				.build();
		return new NegativityCmdWrapper(command, false, "unban");
	}
}
