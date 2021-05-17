package com.elikill58.negativity.universal.ban;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.elikill58.negativity.universal.Adapter;

public abstract class BaseBanProcessorTests {
	
	@Test
	public void successfulBan() {
	
	}
	
	@BeforeAll
	public void setUp() {
		Adapter.setAdapter();
	}
}
