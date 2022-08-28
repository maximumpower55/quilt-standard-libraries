/*
 * Copyright 2016, 2017, 2018, 2019 FabricMC
 * Copyright 2022 QuiltMC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.quiltmc.qsl.screen.handler.api;

import org.jetbrains.annotations.Contract;
import org.quiltmc.qsl.networking.api.PacketByteBufs;
import org.quiltmc.qsl.networking.api.ServerPlayNetworking;

import net.minecraft.network.Packet;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.play.OpenScreenS2CPacket;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public interface QuiltExtendedScreenHandlerFactory extends NamedScreenHandlerFactory {
	Identifier EXTENDED_OPEN_SCREEN_PACKET = new Identifier("quilt", "extended_open_screen_packet");

	/**
	 * Write custom data to be sent when this screen handler gets opened. Will be read and deserialized on the client by
	 * {@link QuiltExtendedScreenHandlerType#readCustomScreenOpeningData(PacketByteBuf)}.
	 */
	@Contract(pure = true)
	default void writeCustomScreenOpeningData(ServerPlayerEntity player, PacketByteBuf buf) {}

	/**
	 * If true, a {@link CloseScreenS2CPacket} will be sent to the client closing this screen handler, resulting in the client's mouse cursor being re-centered.
	 * If false, a {@link CloseScreenS2CPacket} won't be sent to the client closing this screen handler, which stops the client's mouse cursor from being re-centered.
	 */
	default boolean shouldRecenterMouseOnClose() {
		return true;
	}

	/**
	 * Create an extended screen open packet to be sent to clients.
	 */
	@Contract(value = "_ -> new")
	default Packet<?> makeCustomScreenOpenPacket(ServerPlayerEntity player, ScreenHandler screenHandler) {
		PacketByteBuf buf = PacketByteBufs.create();
		new OpenScreenS2CPacket(screenHandler.syncId, screenHandler.getType(), getDisplayName()).write(buf);
		writeCustomScreenOpeningData(player, buf);
		return ServerPlayNetworking.createS2CPacket(EXTENDED_OPEN_SCREEN_PACKET, buf);
	}
}
