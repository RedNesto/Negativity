package com.elikill58.negativity.api.commands;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;

public class CommandTests {

	@Test
	public void testSimpleCommand() {
		CommandManager manager = new CommandManager();
		manager.register(SimpleCommand.class);
		TestCommandSender sender = new TestCommandSender();
		manager.execute(sender, "simple", new String[0]);
		assertIterableEquals(Collections.singletonList("Called simple command"), sender.messages);
	}
	
	private static class TestCommandSender implements CommandSender {
		
		public final List<String> messages = new ArrayList<>();
		
		@Override
		public Object getDefault() {
			throw new UnsupportedOperationException();
		}
		
		@Override
		public void sendMessage(String msg) {
			messages.add(msg);
		}
		
		@Override
		public String getName() {
			return "TestSender";
		}
	}
	
	private static class SimpleCommand {
		
		@Command
		public static void simple(CommandSender sender) {
			sender.sendMessage("Called simple command");
		}
	}
}
