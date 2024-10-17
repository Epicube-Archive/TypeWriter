package com.typewritermc.engine.paper.adapt;

import net.minestom.server.entity.Player;
import net.minestom.server.network.player.PlayerConnection;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class TypeWriterPlayer extends Player {
	private long playerTime;

	public TypeWriterPlayer(@NotNull UUID uuid, @NotNull String username, @NotNull PlayerConnection playerConnection) {
		super(uuid, username, playerConnection);
	}

	public long getPlayerTime() {
		return playerTime;
	}

	public void setPlayerTime(long playerTime) {
		this.playerTime = playerTime;
	}

	public void resetPlayerTime() {
		this.playerTime = 0;
	}
}
