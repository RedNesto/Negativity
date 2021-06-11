package com.elikill58.negativity.api.commands;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.Collections;

import org.junit.jupiter.api.Test;

public class CommandTests {
	
	@Test
	public void testNoSuchCommand() {
		CommandManager manager = new CommandManager();
		manager.register(SimpleCommand.class);
		TestCommandSender sender = new TestCommandSender();
		manager.execute(sender, "unknown", new String[0]);
		assertIterableEquals(Collections.emptyList(), sender.messages);
	}

	@Test
	public void testSimpleCommand() {
		CommandManager manager = new CommandManager();
		manager.register(SimpleCommand.class);
		
		TestCommandSender sender1 = new TestCommandSender();
		manager.execute(sender1, "simple", new String[0]);
		assertIterableEquals(Collections.singletonList("Called simple command"), sender1.messages);
		
		TestCommandSender sender2 = new TestCommandSender();
		manager.execute(sender2, "simple2", new String[]{"somearg"});
		assertIterableEquals(Collections.singletonList("Argument: somearg"), sender2.messages);
		
		TestCommandSender sender3 = new TestCommandSender();
		manager.execute(sender3, "simple3", new String[]{"10"});
		assertIterableEquals(Collections.singletonList("Number: 10"), sender3.messages);
		
		TestCommandSender sender4 = new TestCommandSender();
		manager.execute(sender4, "simple3", new String[]{"invalid"});
		assertIterableEquals(Collections.singletonList("Failed to execute command: Not all arguments were consumed (got 1 but parsed 0)"), sender4.messages);
		
		TestCommandSender sender5 = new TestCommandSender();
		manager.execute(sender5, "vararginteger", new String[]{"1", "3", "-2", "100"});
		assertIterableEquals(Collections.singletonList("Numbers: [1, 3, -2, 100]"), sender5.messages);
	}
	
	private static class SimpleCommand {
		
		@Command
		public static void simple(CommandSender sender) {
			sender.sendMessage("Called simple command");
		}
		
		@Command
		public static void simple2(CommandSender sender, String argument) {
			sender.sendMessage("Argument: " + argument);
		}
		
		@Command
		public static void simple3(CommandSender sender, int number) {
			sender.sendMessage("Number: " + number);
		}
		
		@Command
		public static void vararginteger(CommandSender sender, Integer[] numbers) {
			sender.sendMessage("Numbers: " + Arrays.toString(numbers));
		}
	}
	
	@Test
	public void testRootCommand() {
		CommandManager manager = new CommandManager();
		manager.register(RootCommand.class);
		
		TestCommandSender sender1 = new TestCommandSender();
		manager.execute(sender1, "root", new String[]{"subcommand1"});
		assertIterableEquals(Collections.singletonList("Executed subcommand1"), sender1.messages);
		
		TestCommandSender sender2 = new TestCommandSender();
		manager.execute(sender2, "r", new String[]{"subcommand1"});
		assertIterableEquals(Collections.singletonList("Executed subcommand1"), sender2.messages);
		
		TestCommandSender sender3 = new TestCommandSender();
		manager.execute(sender3, "r", new String[]{"sub1"});
		assertIterableEquals(Collections.singletonList("Executed subcommand1"), sender3.messages);
		
		TestCommandSender sender4 = new TestCommandSender();
		manager.execute(sender4, "root", new String[]{"subcommand2", "myarg"});
		assertIterableEquals(Collections.singletonList("Subcommand2 argument: myarg"), sender4.messages);
	}
	
	@CommandRoot({"root", "r"})
	private static class RootCommand {
		
		@Command({"subcommand1", "sub1"})
		public static void subcommand1(CommandSender sender) {
			sender.sendMessage("Executed subcommand1");
		}
		
		@Command
		public static void subcommand2(CommandSender sender, String argument) {
			sender.sendMessage("Subcommand2 argument: " + argument);
		}
	}
}
