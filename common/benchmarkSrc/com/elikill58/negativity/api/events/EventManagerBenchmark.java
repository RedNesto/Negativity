package com.elikill58.negativity.api.events;

import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.infra.Blackhole;

public class EventManagerBenchmark {
	
	static {
		EventManager.registerEvent(new EmptyListeners1(), null);
		EventManager.registerEvent(new EmptyListeners2(), false);
		EventManager.registerEvent(new EmptyListeners3(), true);
	}

	@Benchmark
	@BenchmarkMode(Mode.Throughput)
	@OutputTimeUnit(TimeUnit.MICROSECONDS)
	public void eventDispatchOnGeneratedListener(Blackhole blackhole) {
		EventManager.callEvent(new EmptyEvent1(blackhole));
	}

	@Benchmark
	@BenchmarkMode(Mode.Throughput)
	@OutputTimeUnit(TimeUnit.MICROSECONDS)
	public void eventDispatchOnReflectionBasedListener(Blackhole blackhole) {
		EventManager.callEvent(new EmptyEvent2(blackhole));
	}

	@Benchmark
	@BenchmarkMode(Mode.Throughput)
	@OutputTimeUnit(TimeUnit.MICROSECONDS)
	public void eventDispatchOnHandleBasedListener(Blackhole blackhole) {
		EventManager.callEvent(new EmptyEvent3(blackhole));
	}
	
	private static class EmptyListeners1 implements Listeners {
		
		@EventListener
		public void testListener(EmptyEvent1 event) {
			event.blackhole.consume(event);
		}
	}
	
	private static class EmptyEvent1 implements Event {
		
		public final Blackhole blackhole;
		
		public EmptyEvent1(Blackhole blackhole) {
			this.blackhole = blackhole;
		}
	}
	
	private static class EmptyListeners2 implements Listeners {
		
		@EventListener
		public void testListener(EmptyEvent2 event) {
			event.blackhole.consume(event);
		}
	}
	
	private static class EmptyEvent2 implements Event {
		
		public final Blackhole blackhole;
		
		public EmptyEvent2(Blackhole blackhole) {
			this.blackhole = blackhole;
		}
	}
	
	private static class EmptyListeners3 implements Listeners {
		
		@EventListener
		public void testListener(EmptyEvent3 event) {
			event.blackhole.consume(event);
		}
	}
	
	private static class EmptyEvent3 implements Event {
		
		public final Blackhole blackhole;
		
		public EmptyEvent3(Blackhole blackhole) {
			this.blackhole = blackhole;
		}
	}
}
