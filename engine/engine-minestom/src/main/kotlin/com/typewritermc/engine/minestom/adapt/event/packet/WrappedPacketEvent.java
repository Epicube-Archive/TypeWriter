package com.typewritermc.engine.minestom.adapt.event.packet;

import net.minestom.server.event.player.PlayerPacketEvent;
import net.minestom.server.event.player.PlayerPacketOutEvent;
import net.minestom.server.event.trait.CancellableEvent;

@SuppressWarnings("UnstableApiUsage")
public class WrappedPacketEvent implements CancellableEvent {
	private final CancellableEvent event;

	public WrappedPacketEvent(CancellableEvent event) {
		this.event = event;
		if(!isClient() && !isServer()) {
			throw new IllegalArgumentException("event must be of type PlayerPacketEvent or PlayerPacketOutEvent");
		}
	}

	public Object packet() {
		if(isClient()) {
			return asClient().getPacket();
		}
		return asServer().getPacket();
	}

	public Object event() {
		return event;
	}

	public Type type() {
		return isClient() ? Type.CLIENT : Type.SERVER;
	}

	public boolean isClient() {
		return event instanceof PlayerPacketEvent;
	}

	public boolean isServer() {
		return event instanceof PlayerPacketOutEvent;
	}

	public PlayerPacketEvent asClient() {
		if(!isClient()) {
			throw new ClassCastException("event is not a PlayerPacketEvent");
		}
		return (PlayerPacketEvent) event;
	}

	public PlayerPacketOutEvent asServer() {
		if(!isServer()) {
			throw new ClassCastException("event is not a PlayerPacketOutEvent");
		}
		return (PlayerPacketOutEvent) event;
	}

	@Override
	public boolean isCancelled() {
		return event.isCancelled();
	}

	@Override
	public void setCancelled(boolean cancelled) {
		event.setCancelled(cancelled);
	}

	public enum Type {
		CLIENT,
		SERVER;
	}
}
