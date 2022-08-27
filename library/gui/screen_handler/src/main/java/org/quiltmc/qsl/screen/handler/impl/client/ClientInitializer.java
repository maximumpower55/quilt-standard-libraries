/*
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

package org.quiltmc.qsl.screen.handler.impl.client;

import org.jetbrains.annotations.ApiStatus;
import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.client.ClientModInitializer;
import org.quiltmc.qsl.networking.api.client.ClientPlayNetworking;
import org.quiltmc.qsl.screen.handler.api.QuiltExtendedScreenHandlerFactory;
import org.quiltmc.qsl.screen.handler.api.QuiltExtendedScreenHandlerType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.packet.s2c.play.OpenScreenS2CPacket;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.util.registry.Registry;

@ApiStatus.Internal
@Environment(EnvType.CLIENT)
public final class ClientInitializer implements ClientModInitializer {
	private static final Logger LOGGER = LoggerFactory.getLogger("quilt_screen_handler");

	@Override
	public void onInitializeClient(ModContainer mod) {
		ClientPlayNetworking.registerGlobalReceiver(QuiltExtendedScreenHandlerFactory.EXTENDED_OPEN_SCREEN_PACKET, (client, handler, buf, sender) -> {
			OpenScreenS2CPacket openScreenPacket = new OpenScreenS2CPacket(buf);
			buf.retain(); // Retain the buf so readCustomScreenOpeningData can use it
			client.execute(() -> {
				try {
					openScreenPacket.apply(handler); // Call vanilla screen opening logic
					ScreenHandlerType<?> type = openScreenPacket.getScreenHandlerType();

					if (type instanceof QuiltExtendedScreenHandlerType extendedType) {
						extendedType.readCustomScreenOpeningData(buf);
					} else {
						LOGGER.error("[Quilt] invalid screen handler type received for extended open screen packet; expected a QuiltExtendedScreenHandlerType, got: " +
							(type == null ? "null???" : Registry.SCREEN_HANDLER.getId(type)));
						return;
					}
				} finally {
					buf.release(); // Release the buf
				}
			});
		});
	}
}
