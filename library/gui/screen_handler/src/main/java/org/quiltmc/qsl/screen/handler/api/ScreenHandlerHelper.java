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

package org.quiltmc.qsl.screen.handler.api;

import java.util.function.Consumer;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.slot.Slot;

public final class ScreenHandlerHelper {
	private ScreenHandlerHelper() {
		throw new UnsupportedOperationException("ScreenHandlerHelper only contains static definitions.");
	}

	/**
	 * Add the slots from an player's inventory and hotbar to a screen handler.
	 */
	@Contract(mutates = "this")
	public static void addPlayerInventorySlots(@NotNull Consumer<Slot> slotAdder, @NotNull PlayerInventory playerInventory) {
		int y, x;

		for (y = 0; y < 3; ++y) {
			for (x = 0; x < 9; ++x) {
				slotAdder.accept(new Slot(playerInventory, x + (y + 1) * 9, 8 + x * 18, 84 + y * 18));
			}
		}

		for (y = 0; y < 9; ++y) {
			slotAdder.accept(new Slot(playerInventory, y, 8 + y * 18, 142));
		}
	}
}
