package com.elikill58.negativity.api.commands;

import org.checkerframework.checker.index.qual.GTENegativeOne;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.dataflow.qual.Pure;

public final class ParameterParser {
	
	private final String[] args;
	private @GTENegativeOne int cursor = -1;
	
	public ParameterParser(String[] args) {
		this.args = args;
	}
	
	public String next() {
		if (!hasNext()) {
			throw new ParameterException("Trying to read more parameters than provided (cursor: " + cursor + "; count: " + args.length + ")");
		}
		return args[++cursor];
	}
	
	public @Nullable String peek() {
		return peek(1);
	}
	
	public @Nullable String peek(int offset) {
		int i = cursor + offset;
		if (i >= args.length) {
			return null;
		}
		return args[i];
	}
	
	public boolean hasNext() {
		return cursor + 1 < args.length;
	}
	
	@Pure
	public @GTENegativeOne int getCursor() {
		return cursor;
	}
	
	public int nextInt() {
		String raw = next();
		try {
			return Integer.parseInt(raw);
		} catch (NumberFormatException ignore) {
			throw new ParameterException("Invalid int: " + raw);
		}
	}
}
