package com.elikill58.negativity.api.commands;

import java.util.UUID;

import org.checkerframework.checker.nullness.qual.Nullable;

import com.elikill58.negativity.api.entity.OfflinePlayer;
import com.elikill58.negativity.api.entity.Player;
import com.elikill58.negativity.universal.Adapter;

public interface CommandParameter<T> {
	
	@Nullable T parse(String argument);
	
	final class StringParameter implements CommandParameter<String> {
		
		@Override
		public @Nullable String parse(String argument) {
			return argument;
		}
	}
	
	final class IntegerParameter implements CommandParameter<Integer> {
		
		@Override
		public @Nullable Integer parse(String argument) {
			try {
				return Integer.parseInt(argument);
			} catch (NumberFormatException ignore) {
			}
			return null;
		}
	}
	
	final class OnlinePlayerParameter implements CommandParameter<Player> {
		
		@Override
		public @Nullable Player parse(String argument) {
			try {
				UUID playerId = UUID.fromString(argument);
				return Adapter.getAdapter().getPlayer(playerId);
			} catch (IllegalArgumentException ignore) {
			}
			return Adapter.getAdapter().getPlayer(argument);
		}
	}
	
	final class OfflinePlayerParameter implements CommandParameter<OfflinePlayer> {
		
		@Override
		public @Nullable OfflinePlayer parse(String argument) {
			try {
				UUID playerId = UUID.fromString(argument);
				return Adapter.getAdapter().getOfflinePlayer(playerId);
			} catch (IllegalArgumentException ignore) {
			}
			return Adapter.getAdapter().getOfflinePlayer(argument);
		}
	}
}
