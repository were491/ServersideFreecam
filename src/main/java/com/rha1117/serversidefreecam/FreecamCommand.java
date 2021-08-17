package com.rha1117.serversidefreecam;

import static net.minecraft.server.command.CommandManager.literal;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.GameMode;

public class FreecamCommand implements ModInitializer {
	@Override
	public void onInitialize() {
		CommandRegistrationCallback.EVENT.register((cmdDispatcher, isDedicatedServer) -> {
			cmdDispatcher.register(literal("freecam").executes(context -> onExecute(context)));
		});
	}

	int onExecute(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
		// TODO: We should catch an exception and return -2 if the executor is not a player.
		// I have NO IDEA how to do this but it seems to silently fail if it doesn't work
		// (maybe since gamemode is never spectator? xD) so i can just uhh ignore it for now.
		// I think it's null when it is not a player.
		ServerPlayerEntity player = context.getSource().getPlayer();

		/*
		NOTE: This causes the old freecam data to be thrown out if one enters freecam then exits spectator mode
		(and does not re-enter it). This is because not re-entering spectator mode assumes the operator
		forgot they ever entered the mode, and it would be confusing if they randomly were teleported
		to a place they haven't been in for a long time.
		*/
		if ((player.interactionManager.getGameMode() == GameMode.SPECTATOR) && (((MixinInterface) player).ssfc_getPosition() != null)) {
			player.teleport(
					context.getSource().getServer().getWorld(RegistryKey.of(Registry.WORLD_KEY, ((MixinInterface) player).ssfc_getWorld())),
					((MixinInterface) player).ssfc_getPosition()[0], ((MixinInterface) player).ssfc_getPosition()[1], ((MixinInterface) player).ssfc_getPosition()[2],
					((MixinInterface) player).ssfc_getAngle()[0], ((MixinInterface) player).ssfc_getAngle()[1]
                    );
			player.changeGameMode(((MixinInterface) player).ssfc_getGamemode());
			player.fallDistance = ((MixinInterface) player).ssfc_getFallDistance();
			
			player.setHealth(((MixinInterface) player).ssfc_getHealth());
			player.getHungerManager().setFoodLevel(((MixinInterface) player).ssfc_getFood());
			player.getHungerManager().setSaturationLevel(((MixinInterface) player).ssfc_getSaturation());
			player.getHungerManager().setExhaustion(((MixinInterface) player).ssfc_getExhaustion());
			
			((MixinInterface) player).ssfc_resetPosition();
		} else {
			((MixinInterface) player).ssfc_savePlayerData(player, context.getSource().getWorld());
			player.changeGameMode(GameMode.SPECTATOR);
		}
		return 69420;
	}
}