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

package org.quiltmc.qsl.screen.handler.mixin;

import java.util.OptionalInt;

import org.quiltmc.qsl.screen.handler.api.QuiltExtendedScreenHandlerFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import net.minecraft.network.Packet;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin {
	@Shadow
	abstract void closeHandledScreen();

	@Shadow
	abstract void closeScreenHandler();

	@Unique
	private final ThreadLocal<ScreenHandler> quilt$openHandledScreen$screenHandler = new ThreadLocal<>();

	@Redirect(
		method = "openHandledScreen(Lnet/minecraft/screen/NamedScreenHandlerFactory;)Ljava/util/OptionalInt;",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/server/network/ServerPlayerEntity;closeHandledScreen()V"
		)
	)
	private void closeCurrentScreenIfAllowed(ServerPlayerEntity player, NamedScreenHandlerFactory factory) {
		if (factory instanceof SimpleNamedScreenHandlerFactory simpleFactory && simpleFactory.baseFactory instanceof QuiltExtendedScreenHandlerFactory extendedFactory) {
			factory = extendedFactory;
		}

		if (factory instanceof QuiltExtendedScreenHandlerFactory extendedFactory && extendedFactory.shouldCloseCurrentScreen()) {
			closeHandledScreen();
		} else {
			closeScreenHandler();
		}
	}

	@Inject(
		method = "openHandledScreen(Lnet/minecraft/screen/NamedScreenHandlerFactory;)Ljava/util/OptionalInt;",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/server/network/ServerPlayNetworkHandler;sendPacket(Lnet/minecraft/network/Packet;)V"
		),
		locals = LocalCapture.CAPTURE_FAILHARD
	)
	private void storeOpenedScreenHandler(NamedScreenHandlerFactory factory, CallbackInfoReturnable<OptionalInt> cir, ScreenHandler handler) {
		if (factory instanceof QuiltExtendedScreenHandlerFactory ||
			(factory instanceof SimpleNamedScreenHandlerFactory simpleFactory && simpleFactory.baseFactory instanceof QuiltExtendedScreenHandlerFactory)) {
				quilt$openHandledScreen$screenHandler.set(handler);
		} else if (handler.getType() instanceof QuiltExtendedScreenHandlerFactory) {
			Identifier id = Registry.SCREEN_HANDLER.getId(handler.getType());
			throw new IllegalArgumentException("[Quilt] extended screen handler " + id + " must be opened with an QuiltExtendedScreenHandlerFactory!");
		}
	}

	@Redirect(
		method = "openHandledScreen(Lnet/minecraft/screen/NamedScreenHandlerFactory;)Ljava/util/OptionalInt;",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/server/network/ServerPlayNetworkHandler;sendPacket(Lnet/minecraft/network/Packet;)V"
		)
	)
	private void replaceVanillaScreenOpenPacket(ServerPlayNetworkHandler networkHandler, Packet<?> screenOpenPacket, NamedScreenHandlerFactory factory) {
		if (factory instanceof SimpleNamedScreenHandlerFactory simpleFactory && simpleFactory.baseFactory instanceof QuiltExtendedScreenHandlerFactory extendedFactory) {
			factory = extendedFactory;
		}

		if (factory instanceof QuiltExtendedScreenHandlerFactory extendedFactory) {
			networkHandler.sendPacket(extendedFactory.makeCustomScreenOpenPacket((ServerPlayerEntity) (Object) this, quilt$openHandledScreen$screenHandler.get()));
		} else {
			networkHandler.sendPacket(screenOpenPacket);
		}

		quilt$openHandledScreen$screenHandler.remove();
	}
}
