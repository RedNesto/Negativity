package com.elikill58.negativity.api.commands;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.elikill58.negativity.api.entity.Player;

/**
 * Defines the annotated method as a command executor.
 * <p>
 * If the annotated method is in a class annotated with {@link CommandRoot} and the main alias matches the
 * root command's main alias then it will be used as root executor.
 * <p>
 * Command executors must have at least one parameter of type {@link CommandSender}
 * (or a subclass like {@link Player}) as first parameter.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Command {
	
	/**
	 * @return the aliases of this command. If empty (by default) uses the annotated method name
	 */
	String[] value() default {};
}
