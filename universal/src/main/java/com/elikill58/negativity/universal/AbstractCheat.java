package com.elikill58.negativity.universal;

public interface AbstractCheat {

    String[] aliases_forcefield = {"ff"},
			aliases_fastplace = { "fp" },
			aliases_speedhack = { "speed" },
			aliases_autoclick = {"fastclick"},
			aliases_fly = {"fly"},
			aliases_antipotion = {"potion", "popo"},
			aliases_autoeat = {"eat"},
			aliases_autoregen = {"regen"},
			aliases_antiknockback = {"kb", "antikb"},
			aliases_jesus = {"water"},
			aliases_nofall = {"fall"},
			aliases_blink = {},
			aliases_spider = {"wallhack"},
			aliases_sneak = {"sneack"},
			aliases_fastbow = {"bow"},
			aliases_scaffold = {},
			aliases_step = {},
			aliases_noslowdown = {},
			aliases_fastladders = {"ladders"},
			aliases_phase = {},
			aliases_autosteal = {},
			aliases_edited_client = {"hacked client", "edited client"},
			aliases_all = {};

    String name();

    String getName();

    boolean isActive();

    boolean needPacket();

    boolean isAutoVerif();

    int getReliabilityAlert();

    boolean isSetBack();

    int getAlertToKick();

    boolean allowKick();

    int getMaxAlertPing();

    String[] getAliases();

}
