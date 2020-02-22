package com.elikill58.negativity.universal.pluginMessages;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.EnumSet;
import java.util.Set;
import java.util.UUID;

public class UpdateAccountRequestMessage implements NegativityMessage {

	public static final byte MESSAGE_ID = 4;

	private UUID playerId;
	private Set<UpdateAccountMessage.AccountField> fields;

	public UpdateAccountRequestMessage() {
	}

	public UpdateAccountRequestMessage(UUID playerId) {
		this(playerId, EnumSet.allOf(UpdateAccountMessage.AccountField.class));
	}

	public UpdateAccountRequestMessage(UUID playerId, Set<UpdateAccountMessage.AccountField> fields) {
		this.playerId = playerId;
		this.fields = fields;
	}

	@Override
	public byte messageId() {
		return MESSAGE_ID;
	}

	@Override
	public void readFrom(DataInputStream input) throws IOException {
		playerId = new UUID(input.readLong(), input.readLong());
		fields = EnumSet.noneOf(UpdateAccountMessage.AccountField.class);

		int fieldsCount = input.readInt();
		for (int i = 0; i < fieldsCount; i++) {
			fields.add(UpdateAccountMessage.AccountField.valueOf(input.readUTF()));
		}
	}

	@Override
	public void writeTo(DataOutputStream output) throws IOException {
		output.writeLong(playerId.getMostSignificantBits());
		output.writeLong(playerId.getLeastSignificantBits());

		output.writeInt(fields.size());
		for (UpdateAccountMessage.AccountField field : fields) {
			output.writeUTF(field.name());
		}
	}

	public UUID getPlayerId() {
		return playerId;
	}
}
