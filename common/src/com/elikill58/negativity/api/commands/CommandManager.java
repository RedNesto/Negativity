package com.elikill58.negativity.api.commands;

import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.checkerframework.checker.nullness.qual.Nullable;

import com.elikill58.negativity.api.entity.OfflinePlayer;
import com.elikill58.negativity.api.entity.Player;
import com.elikill58.negativity.api.events.EventListener;
import com.elikill58.negativity.api.events.Listeners;
import com.elikill58.negativity.api.events.others.CommandExecutionEvent;
import com.elikill58.negativity.api.events.others.TabExecutionEvent;
import com.elikill58.negativity.common.commands.NegativityCommand;
import com.elikill58.negativity.universal.Adapter;
import com.elikill58.negativity.universal.Cheat;

public class CommandManager implements Listeners {
	
	private final HashMap<String, CommandListeners> commands = new HashMap<>();
	private final HashMap<String, TabListeners> tabs = new HashMap<>();
	private final Map<String, CommandWrapper> cmds = new HashMap<>();
	private final Map<Class<?>, CommandParameter<?>> parameters;
	
	public CommandManager() {
		this.parameters = new LinkedHashMap<>();
		this.parameters.put(Player.class, new CommandParameter.OnlinePlayerParameter());
		this.parameters.put(OfflinePlayer.class, new CommandParameter.OfflinePlayerParameter());
		this.parameters.put(int.class, new CommandParameter.IntegerParameter());
		this.parameters.put(Integer.class, new CommandParameter.IntegerParameter());
		this.parameters.put(String.class, new CommandParameter.StringParameter());
		this.parameters.put(Cheat.class, new CommandParameter.CheatParameter());
		
		// TODO register commands outside of the manager!
		register(NegativityCommand.class);
		//NegativityCommand negativity = new NegativityCommand();
		//commands.put("negativity", negativity);
		//tabs.put("negativity", negativity);
		
		//Configuration conf = Adapter.getAdapter().getConfig();
		//if (conf.getBoolean("commands.kick")) {
		//	KickCommand kick = new KickCommand();
		//	commands.put("nkick", kick);
		//	tabs.put("nkick", kick);
		//}
		//if (conf.getBoolean("commands.lang")) {
		//	LangCommand lang = new LangCommand();
		//	commands.put("nlang", lang);
		//	tabs.put("nlang", lang);
		//}
		//if (conf.getBoolean("commands.mod")) {
		//	commands.put("nmod", new ModCommand());
		//}
		//if (conf.getBoolean("commands.report")) {
		//	ReportCommand report = new ReportCommand();
		//	commands.put("nreport", report);
		//	tabs.put("nreport", report);
		//}
		//
		//conf = BanManager.getBanConfig();
		//if (conf.getBoolean("commands.ban")) {
		//	BanCommand ban = new BanCommand();
		//	commands.put("nban", ban);
		//	tabs.put("nban", ban);
		//}
		//if (conf.getBoolean("commands.unban")) {
		//	UnbanCommand unban = new UnbanCommand();
		//	commands.put("nunban", unban);
		//	tabs.put("nunban", unban);
		//}
	}
	
	public static void main(String[] args) {
		CommandManager manager = new CommandManager();
		manager.register(NegativityCommand.class);
		ConsoleSender sender = new ConsoleSender();
		manager.execute(sender, "negativity", new String[]{"Player"});
		manager.execute(sender, "reload", new String[0]);
		manager.execute(sender, "negativity", new String[]{"reload"});
	}
	
	public void register(Class<?> commandClass) {
		CommandRoot commandRoot = commandClass.getAnnotation(CommandRoot.class);
		String @Nullable [] rootAliases = null;
		@Nullable String mainRootAlias = null;
		Map<String, CommandWrapper> rootChildren = new HashMap<>();
		@Nullable Method rootExecutor = null;
		if (commandRoot != null) {
			rootAliases = commandRoot.value();
			for (int i = 0, aliasesLength = rootAliases.length; i < aliasesLength; i++) {
				rootAliases[i] = checkAlias(rootAliases[i]);
			}
			if (rootAliases.length > 0) {
				mainRootAlias = rootAliases[0];
			}
		}
		for (Method method : commandClass.getMethods()) {
			if (!Modifier.isStatic(method.getModifiers())) {
				continue;
			}
			
			Command commandAnnotation = method.getAnnotation(Command.class);
			if (commandAnnotation == null) {
				continue;
			}
			
			String[] aliases = commandAnnotation.value();
			String mainAlias;
			if (aliases.length == 0) {
				mainAlias = checkAlias(method.getName());
				aliases = new String[]{mainAlias};
			} else {
				for (int i = 0, aliasesLength = aliases.length; i < aliasesLength; i++) {
					aliases[i] = checkAlias(aliases[i]);
				}
				mainAlias = aliases[0];
			}
			
			//Method executor = findSingleAnnotatedMethod(method, CommandExecutor.class);
			Class<?>[] executorParameterTypes = method.getParameterTypes();
			if (executorParameterTypes.length == 0 || !CommandSender.class.isAssignableFrom(executorParameterTypes[0])) {
				throw new InvalidCommandException("Executor of command " + mainAlias + " must accept at least one parameter of type CommandSender (or a subclass)");
			}
			
			//@Nullable Method helper = findSingleAnnotatedMethod(method, CommandHelp.class);
			//if (helper != null) {
			//	Class<?>[] helperParameterTypes = helper.getParameterTypes();
			//	if (helperParameterTypes.length == 0 || !helperParameterTypes[0].isAssignableFrom(CommandSender.class)) {
			//		throw new InvalidCommandException("Help of command " + alias + " must accept at least one parameter of type CommandSender (or a subclass)");
			//	}
			//}
			//
			//@Nullable Method condition = findSingleAnnotatedMethod(method, CommandCondition.class);
			//if (condition != null) {
			//	Class<?> conditionReturnType = condition.getReturnType();
			//	if (conditionReturnType != boolean.class) {
			//		throw new InvalidCommandException("Condition of command " + alias + " must return a boolean primitive");
			//	}
			//
			//	Class<?>[] conditionParameterTypes = condition.getParameterTypes();
			//	if (conditionParameterTypes.length == 0 || !conditionParameterTypes[0].isAssignableFrom(CommandSender.class)) {
			//		throw new InvalidCommandException("Condition of command " + alias + " must accept at least one parameter of type CommandSender (or a subclass)");
			//	}
			//}
			
			if (mainAlias.equals(mainRootAlias)) {
				rootExecutor = method;
			} else if (mainRootAlias != null) {
				for (String alias : aliases) {
					Map<String, CommandWrapper> children = new HashMap<>();
					rootChildren.put(alias, new CommandWrapper(children, method));
				}
			} else {
				Map<String, CommandWrapper> children = new HashMap<>();
				for (String alias : aliases) {
					this.cmds.put(alias, new CommandWrapper(children, method));
				}
			}
		}
		
		if (rootAliases != null) {
			CommandWrapper rootCommand = new CommandWrapper(rootChildren, rootExecutor);
			for (String rootAlias : rootAliases) {
				this.cmds.put(rootAlias, rootCommand);
			}
		}
	}
	
	private String checkAlias(String alias) {
		if (alias.isEmpty()) {
			throw new InvalidCommandException("Command alias cannot be empty or blank");
		}
		
		if (alias.contains(" ")) {
			throw new InvalidCommandException("Command alias cannot contain spaces");
		}
		
		return alias.toLowerCase(Locale.ROOT).trim();
	}
	
	private @Nullable Method findSingleAnnotatedMethod(Class<?> clazz, Class<? extends Annotation> annotationClass) {
		Method found = null;
		for (Method method : clazz.getDeclaredMethods()) {
			if (method.isAnnotationPresent(annotationClass)) {
				if (found == null) {
					found = method;
				} else {
					return null;
				}
			}
		}
		return found;
	}
	
	public void execute(CommandSender sender, String command, String[] args) {
		CommandWrapper cmd = cmds.get(command);
		if (cmd != null) {
			try {
				cmd.execute(sender, args);
			} catch (InvocationTargetException | IllegalAccessException e) {
				Adapter.getAdapter().getLogger().error("Failed to invoke command " + command + " " + String.join("", args));
				e.printStackTrace();
			} catch (InvalidCommandException e) {
				sender.sendMessage("Failed to execute command: " + e.getMessage());
			}
		} else {
			// TODO proper error reporting
			System.out.println("Unknown command " + command + " " + String.join(" ", args));
		}
	}
	
	public Collection<String> suggest(CommandSender sender, String command, String[] args) {
		CommandWrapper cmd = cmds.get(command);
		if (cmd != null) {
			return cmd.suggest(sender, args);
		}
		return Collections.emptyList();
	}
	
	@EventListener
	public void onCommand(CommandExecutionEvent e) {
		try {
			execute(e.getSender(), e.getCommand(), e.getArgument());
		} catch (ParameterException ex) {
			e.getSender().sendMessage("Invalid parameter: " + ex.getMessage());
		}
		//CommandListeners cmd = commands.get(e.getCommand().toLowerCase(Locale.ROOT));
		//if(cmd != null)
		//	e.setGoodResult(cmd.onCommand(e.getSender(), e.getArgument(), e.getPrefix()));
	}
	
	@EventListener
	public void onTab(TabExecutionEvent e) {
		e.setTabContent(new ArrayList<>(suggest(e.getSender(), e.getCommand(), e.getArgument())));
		//TabListeners cmd = tabs.get(e.getCommand().toLowerCase(Locale.ROOT));
		//if(cmd != null)
		//	e.setTabContent(cmd.onTabComplete(e.getSender(), e.getArgument(), e.getPrefix()));
	}
	
	private static class ConsoleSender implements CommandSender {
		
		@Override
		public Object getDefault() {
			throw new UnsupportedOperationException("To be implemented"); // TODO
		}
		
		@Override
		public void sendMessage(String msg) {
			System.out.println("Message received: " + msg);
		}
		
		@Override
		public String getName() {
			return "Console";
		}
	}
	
	private class CommandWrapper {
		
		private final Map<String, CommandWrapper> children;
		private final @Nullable Method executor;
		//private final @Nullable Method helper;
		//private final @Nullable Method condition;
		
		private CommandWrapper(Map<String, CommandWrapper> children,
							   @Nullable Method executor
							   //@Nullable Method helper,
							   //@Nullable Method condition
		) {
			this.children = children;
			this.executor = executor;
			//this.helper = helper;
			//this.condition = condition;
		}
		
		public void execute(CommandSender sender, String[] args) throws InvocationTargetException, IllegalAccessException {
			if (args.length > 0) {
				String firstArg = args[0];
				CommandWrapper child = this.children.get(firstArg);
				if (child != null) {
					String[] childArgs;
					if (args.length > 1) {
						childArgs = Arrays.copyOfRange(args, 1, args.length);
					} else {
						childArgs = new String[0];
					}
					child.execute(sender, childArgs);
					return;
				}
			}
			
			if (executor == null) {
				if (children.isEmpty() || args.length == 0) {
					throw new InvalidCommandException("This command can not be executed");
				} else {
					throw new InvalidCommandException("Unknown subcommand: " + args[0]);
				}
			}
			
			Class<?>[] parameterTypes = executor.getParameterTypes();
			if (args.length < parameterTypes.length - 1 && !parameterTypes[parameterTypes.length - 1].isArray()) {
				throw new InvalidCommandException("Not enough arguments (expected " + (parameterTypes.length - 1) + " but got " + args.length + ")");
			}
			
			List<Object> parameters = new ArrayList<>(parameterTypes.length);
			parameters.add(sender);
			
			ParameterParser parameterParser = new ParameterParser(args);
			for (int i = 1, parameterTypesLength = parameterTypes.length; i < parameterTypesLength; i++) {
				Class<?> parameterType = parameterTypes[i];
				if (parameterType.isArray()) {
					Class<?> componentType = parameterType.getComponentType();
					if (!parameterParser.hasNext()) {
						parameters.add(Array.newInstance(componentType, 0));
						break;
					}
					
					List<Object> values = new ArrayList<>();
					while (parameterParser.hasNext()) {
						values.add(parseParameter(parameterParser, componentType));
					}
					// Required to get an array with the correct component type, otherwise executor invocation fails with an IllegalArgumentException
					Object[] dummyArray = (Object[]) Array.newInstance(componentType, 0);
					parameters.add(values.toArray(dummyArray));
					break;
				} else {
					parameters.add(parseParameter(parameterParser, parameterType));
				}
			}
			
			if (parameterParser.hasNext()) {
				throw new InvalidCommandException("Not all arguments were consumed (got " + args.length + " but parsed " + parameterParser.getCursor() + ")");
			}
			
			if (parameters.size() != parameterTypes.length) {
				//if (parameters.size() + 1 == parameterTypes.length) {
				//	if (parameterTypes[parameterTypes.length - 1].isArray()) {
				//		// If we are only missing the last array argument
				//		parameters.add(Array.newInstance(parameterTypes[parameterTypes.length - 1].getComponentType(), 0));
				//	}
				//}
				throw new InvalidCommandException("Not enough arguments parsed (expected " + parameterTypes.length + " but got " + parameters.size() + ")");
			}
			
			executor.invoke(null, parameters.toArray());
		}
		
		@SuppressWarnings("unchecked")
		private <T> T parseParameter(ParameterParser parameterParser, Class<T> parameterType) {
			CommandParameter<T> parameter = (CommandParameter<T>) CommandManager.this.parameters.get(parameterType);
			if (parameter == null) {
				throw new ParameterException("No parameter parser supporting " + parameterType.getName());
			}
			
			T parseResult = parameter.parse(parameterParser);
			if (parseResult == null) {
				throw new ParameterException("Failed to parse parameter as " + parameterType.getSimpleName());
			}
			
			return parseResult;
		}
		
		public boolean canExecute(CommandSender sender) throws InvocationTargetException, IllegalAccessException {
			// TODO check permissions, custom conditions ?
			return true;
			//if (this.condition == null) {
			//	return true;
			//}
			//return (boolean) condition.invoke(null, sender);
		}
		
		public Collection<String> suggest(CommandSender sender, String[] args) {
			if (args.length == 0) {
				throw new IllegalArgumentException("Args array must not be empty");
			}
			
			List<String> suggestions = new ArrayList<>();
			if (!this.children.isEmpty()) {
				if (args.length == 1) {
					suggestions.addAll(this.children.keySet());
				} else {
					CommandWrapper child = this.children.get(args[0]);
					if (child == null) {
						return Collections.emptyList();
					}
					return child.suggest(sender, Arrays.copyOfRange(args, 1, args.length));
				}
			}
			
			if (this.executor != null) {
				Class<?>[] parameterTypes = this.executor.getParameterTypes();
				if (args.length < parameterTypes.length) {
					Class<?> parameterType = parameterTypes[args.length];
					if (parameterType.isArray()) {
						// TODO
					}
					CommandParameter<?> parameter = CommandManager.this.parameters.get(parameterType);
					ParameterParser parser = new ParameterParser(args);
					suggestions.addAll(parameter.suggest(parser));
				}
			}
			return suggestions;
		}
	}
}
