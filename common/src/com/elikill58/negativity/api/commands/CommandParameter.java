package com.elikill58.negativity.api.commands;

import java.util.UUID;

import org.checkerframework.checker.nullness.qual.Nullable;

import com.elikill58.negativity.api.entity.OfflinePlayer;
import com.elikill58.negativity.api.entity.Player;
import com.elikill58.negativity.universal.Adapter;
import com.elikill58.negativity.universal.Cheat;

public interface CommandParameter<T> {
	
	@Nullable T parse(ParameterParser parser);
	
	final class StringParameter implements CommandParameter<String> {
		
		@Override
		public @Nullable String parse(ParameterParser parser) {
			return parser.next();
		}
	}
	
	final class IntegerParameter implements CommandParameter<Integer> {
		
		@Override
		public @Nullable Integer parse(ParameterParser parser) {
			return parser.nextInt();
		}
	}
	
	final class OnlinePlayerParameter implements CommandParameter<Player> {
		
		@Override
		public @Nullable Player parse(ParameterParser parser) {
			String raw = parser.next();
			try {
				UUID playerId = UUID.fromString(raw);
				return Adapter.getAdapter().getPlayer(playerId);
			} catch (IllegalArgumentException ignore) {
			}
			return Adapter.getAdapter().getPlayer(raw);
		}
	}
	
	final class OfflinePlayerParameter implements CommandParameter<OfflinePlayer> {
		
		@Override
		public @Nullable OfflinePlayer parse(ParameterParser parser) {
			String raw = parser.next();
			try {
				UUID playerId = UUID.fromString(raw);
				return Adapter.getAdapter().getOfflinePlayer(playerId);
			} catch (IllegalArgumentException ignore) {
			}
			return Adapter.getAdapter().getOfflinePlayer(raw);
		}
	}
	
	final class CheatParameter implements CommandParameter<Cheat> {
		
		@Override
		public @Nullable Cheat parse(ParameterParser parser) {
			return Cheat.fromString(parser.next());
		}
	}
}
