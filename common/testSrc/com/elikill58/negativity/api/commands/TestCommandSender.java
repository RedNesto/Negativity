package com.elikill58.negativity.api.commands;

import java.util.ArrayList;
import java.util.List;

class TestCommandSender implements CommandSender {
	
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
