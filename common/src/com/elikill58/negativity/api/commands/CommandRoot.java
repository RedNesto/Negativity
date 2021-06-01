package com.elikill58.negativity.api.commands;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a command class containing subcommands.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface CommandRoot {
	
	/**
	 * @return the root command's aliases.
	 */
	String[] value();
}
