package com.elikill58.negativity.universal;

public enum FlyingReason {
	POTION(Cheat.fromString("ANTIPOTION").get()), REGEN(Cheat.fromString("REGEN").get()), EAT(
			Cheat.fromString("FASTEAT").get()), BOW(Cheat.fromString("FASTBOW").get());

	private Cheat c;

	FlyingReason(Cheat c) {
		this.c = c;
	}

	public Cheat getCheat() {
		return c;
	}

}
