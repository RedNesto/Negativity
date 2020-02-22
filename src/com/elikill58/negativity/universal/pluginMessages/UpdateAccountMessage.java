package com.elikill58.negativity.universal.pluginMessages;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import com.elikill58.negativity.universal.Minerate;
import com.elikill58.negativity.universal.NegativityAccount;

public class UpdateAccountMessage implements NegativityMessage {

	public static final byte MESSAGE_ID = 5;

	private NegativityAccount account;
	private Set<AccountField> affectedFields;

	public UpdateAccountMessage() {
	}

	public UpdateAccountMessage(NegativityAccount account) {
		this(account, EnumSet.allOf(AccountField.class));
	}

	public UpdateAccountMessage(NegativityAccount account, AccountField... fieldsToUpdate) {
		this.account = account;
		EnumSet<AccountField> fields = EnumSet.noneOf(AccountField.class);
		Collections.addAll(fields, fieldsToUpdate);
		this.affectedFields = Collections.unmodifiableSet(fields);
	}

	public UpdateAccountMessage(NegativityAccount account, Set<AccountField> fieldsToUpdate) {
		this.account = account;
		this.affectedFields = Collections.unmodifiableSet(fieldsToUpdate);
	}

	@Override
	public byte messageId() {
		return MESSAGE_ID;
	}

	@Override
	public void readFrom(DataInputStream input) throws IOException {
		UUID playerId = new UUID(input.readLong(), input.readLong());
		account = new NegativityAccount(playerId);

		EnumSet<AccountField> fields = EnumSet.noneOf(AccountField.class);
		this.affectedFields = Collections.unmodifiableSet(fields);
		int affectedFieldsCount = input.readInt();
		for (int i = 0; i < affectedFieldsCount; i++) {
			fields.add(AccountField.valueOf(input.readUTF()));
		}

		if (affectedFields.contains(AccountField.LANGUAGE)) {
			account.setLang(input.readUTF());
		}

		if (affectedFields.contains(AccountField.MINERATE)) {
			Minerate minerate = account.getMinerate();
			int minerateEntriesCount = input.readInt();
			for (int i = 0; i < minerateEntriesCount; i++) {
				Minerate.MinerateType type = Minerate.MinerateType.getMinerateType(input.readUTF());
				if (type != null) {
					int value = input.readInt();
					minerate.setMine(type, value);
				}
			}
		}

		if (affectedFields.contains(AccountField.MOST_CLICKS_PER_SECOND)) {
			account.setMostClicksPerSecond(input.readInt());
		}

		if (affectedFields.contains(AccountField.WARNS)) {
			int warnsEntriesCount = input.readInt();
			for (int i = 0; i < warnsEntriesCount; i++) {
				account.setWarnCount(input.readUTF(), input.readInt());
			}
		}
	}

	@Override
	public void writeTo(DataOutputStream output) throws IOException {
		UUID playerId = account.getPlayerId();
		output.writeLong(playerId.getMostSignificantBits());
		output.writeLong(playerId.getLeastSignificantBits());

		output.writeInt(affectedFields.size());
		for (AccountField field : affectedFields) {
			output.writeUTF(field.name());
		}

		if (affectedFields.contains(AccountField.LANGUAGE)) {
			output.writeUTF(account.getLang());
		}

		if (affectedFields.contains(AccountField.MINERATE)) {
			Minerate minerate = account.getMinerate();
			output.writeInt(Minerate.MinerateType.values().length);
			for (Minerate.MinerateType type : Minerate.MinerateType.values()) {
				output.writeUTF(type.getName());
				output.writeInt(minerate.getMinerateType(type));
			}
		}

		if (affectedFields.contains(AccountField.MOST_CLICKS_PER_SECOND)) {
			output.writeInt(account.getMostClicksPerSecond());
		}

		if (affectedFields.contains(AccountField.WARNS)) {
			Map<String, Integer> allWarns = account.getAllWarns();
			output.writeInt(allWarns.size());
			for (Map.Entry<String, Integer> entry : allWarns.entrySet()) {
				output.writeUTF(entry.getKey());
				output.writeInt(entry.getValue());
			}
		}
	}

	public Set<AccountField> getAffectedFields() {
		return affectedFields;
	}

	public NegativityAccount getAccount() {
		return account;
	}

	public UUID getPlayerId() {
		return account.getPlayerId();
	}

	public enum AccountField {
		LANGUAGE,
		MINERATE,
		MOST_CLICKS_PER_SECOND,
		WARNS
	}
}
