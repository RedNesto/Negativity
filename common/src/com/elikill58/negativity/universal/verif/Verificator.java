package com.elikill58.negativity.universal.verif;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.StringJoiner;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import com.elikill58.negativity.api.NegativityPlayer;
import com.elikill58.negativity.universal.Adapter;
import com.elikill58.negativity.universal.Cheat;
import com.elikill58.negativity.universal.Version;
import com.elikill58.negativity.universal.verif.storage.VerificationStorage;

public class Verificator {
	
	public static final int VERIFICATION_VERSION = 0;
	private static final Collector<Cheat, ?, Map<Cheat, VerifData>> COLLECTOR = Collectors.toMap(Function.identity(), t -> new VerifData());
	
	private final Map<Cheat, VerifData> cheats;
	private final NegativityPlayer np;
	private final String asker;
	private final List<String> messages;
	private final int version;
	private final Version playerVersion;
	
	/**
	 * Create a new verificator for all cheats
	 * Cheat are filtered after, specially for those which don't have verif feature
	 * 
	 * @param np the negativity player of the verified player
	 * @param asker the name of the player which ask for verif
	 */
	public Verificator(NegativityPlayer np, String asker) {
		this(np, asker, new HashSet<>(Cheat.CHEATS));
	}

	/**
	 * Create a new verificator for given cheats
	 * 
	 * @param np the negativity player of the verified player
	 * @param asker the name of the player which ask for verif
	 * @param list all cheat which have to be verified, filtered if they have verification available
	 */
	public Verificator(NegativityPlayer np, String asker, Set<Cheat> list) {
		this(np, asker, list.stream().filter(Cheat::hasVerif).collect(COLLECTOR), new ArrayList<>(), VERIFICATION_VERSION, np.getPlayer().getPlayerVersion());
	}
	
	/**
	 * Create a new verificator
	 * 
	 * @param np the negativity player of the verified player
	 * @param asker the name of the player which ask for verif
	 * @param cheats all detected cheat with a new {@link VerifData} (or empty if it's a saved one)
	 * @param messages all previous generated message is it's saved (or empty)
	 * @param version the version of verification
	 * @param playerVersion the player version
	 */
	public Verificator(NegativityPlayer np, String asker, Map<Cheat, VerifData> cheats, List<String> messages, int version, Version playerVersion) {
		this.np = np;
		this.asker = asker;
		this.cheats = cheats;
		this.messages = messages;
		this.version = version;
		this.playerVersion = playerVersion;
	}

	/**
	 * Get the NegativityPlayer of the verified player
	 * 
	 * @return the verified negativity player
	 */
	public NegativityPlayer getNegativityPlayer() {
		return np;
	}
	
	/**
	 * Get the UUID of the verified player
	 * 
	 * @return the verified player UUID
	 */
	public UUID getPlayerId() {
		return np.getUUID();
	}
	
	/**
	 * Get asker name
	 * 
	 * @return the name of the verif asker
	 */
	public String getAsker() {
		return asker;
	}

	/**
	 * Get all cheats with their verif data
	 * 
	 * @return cheat and verif data
	 */
	public Map<Cheat, VerifData> getCheats() {
		return cheats;
	}
	
	/**
	 * Get the verif data of a cheat
	 * (Can be null)
	 * 
	 * @param c the cheat which we are looking for verif data
	 * @return the optional verif data
	 */
	public Optional<VerifData> getVerifData(Cheat c) {
		return Optional.ofNullable(cheats.get(c));
	}
	
	/**
	 * Get all generated message
	 * 
	 * @return messages summary
	 */
	public List<String> getMessages(){
		return messages;
	}

	/**
	 * Get version of the verif
	 * 
	 * @return the version ID
	 */
	public int getVersion() {
		return version;
	}

	/**
	 * Get player version
	 * 
	 * @return the player version
	 */
	public Version getPlayerVersion() {
		return playerVersion;
	}

	/**
	 * Generate un-translated message of all cheat which was in detection
	 * The summary is available thanks to {@link #getMessages()}
	 */
	public void generateMessage() {
		StringJoiner messageCheatNothing = new StringJoiner(", ");
		for(Entry<Cheat, VerifData> currentCheat : cheats.entrySet()) {
			Cheat c = currentCheat.getKey();
			VerifData data = currentCheat.getValue();
			if(data.hasSomething()) {
				String name = c.makeVerificationSummary(data, np);
				if(name != null) {
					messages.add("&6" + c.getName() + "&8: &7" + name);
					continue;
				}
			} else if(c.hasVerif())
				messageCheatNothing.add(c.getName());
		}
		if (messageCheatNothing.length() > 0)
			messages.add("Nothing detected: " + messageCheatNothing);
	}
	
	/**
	 * Save the message
	 * If {@link #getMessages()} is empty, a new summary will be made before saving
	 */
	public void save() {
		if(messages.isEmpty())
			generateMessage();
		VerificationStorage.getStorage().saveVerification(this).exceptionally(t -> {
		    Adapter.getAdapter().getLogger().error("Error occurred while saving verification results");
		    t.printStackTrace();
		    return null;
		});
	}
}
