package com.elikill58.negativity.universal;

import com.elikill58.negativity.universal.ban.storage.FileLoggedBanStorage;

public class NegativityConfig {

	public FileLoggedBanStorage.Config fileLoggedBanStorageConfig;

	public NegativityConfig(FileLoggedBanStorage.Config fileLoggedBanStorageConfig) {
		this.fileLoggedBanStorageConfig = fileLoggedBanStorageConfig;
	}
}
