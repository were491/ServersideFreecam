package com.rha1117.serversidefreecam;

import net.minecraft.util.Identifier;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.GameMode;

public interface MixinInterface {
	void ssfc_savePlayerData(ServerPlayerEntity player, ServerWorld cxtWorld);
	double[] ssfc_getPosition();
	float[] ssfc_getAngle();
	float ssfc_getFallDistance();
	GameMode ssfc_getGamemode();
	Identifier ssfc_getWorld();
	void ssfc_resetPosition();
	float ssfc_getHealth();
	int ssfc_getFood();
	float ssfc_getSaturation();
	float ssfc_getExhaustion();
}