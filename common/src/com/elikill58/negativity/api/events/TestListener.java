package com.elikill58.negativity.api.events;

import java.util.function.BiConsumer;

class TestListener implements Listeners, BakedListeners {
	
	@EventListener
	public void test(TestEvent event) {
		System.out.println("test " + event);
	}
	
	@EventListener
	public void test2(TestEvent event) {
		System.out.println("test2 " + event);
	}
	
	@Override
	public void bakeListeners(BiConsumer<Class<? extends Event>, Listener<?>> registrator) {
		registrator.accept(TestEvent.class, (Listener<TestEvent>) this::test);
		registrator.accept(TestEvent.class, (Listener<TestEvent>) this::test2);
		registrator.accept(TestEvent.class, (Listener<TestEvent>) this::test2);
	}
}
