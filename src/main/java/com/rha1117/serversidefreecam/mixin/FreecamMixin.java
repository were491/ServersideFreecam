package com.rha1117.serversidefreecam.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.authlib.GameProfile;
import com.rha1117.serversidefreecam.MixinInterface;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameMode;
import net.minecraft.world.World;

@Mixin(ServerPlayerEntity.class)
public abstract class FreecamMixin extends PlayerEntity implements MixinInterface {
	public FreecamMixin(World world, BlockPos pos, float yaw, GameProfile profile) {
		super(world, pos, yaw, profile);
	}

	// After accidentally creating a conflict with float fallDistance, I am overcautious about my naming.
	// TODO: Add/set "Previous" variables such as exhaustion and such. Maybe I can do this without adding yet more fields, since I can just gradually move data and let vanilla save it?
	private double[] ssfc_position;
	private float[] ssfc_angle = new float[2];
	private float ssfc_fallDistance;
	private GameMode ssfc_gamemode = GameMode.DEFAULT;
	private Identifier ssfc_world = new Identifier("minecraft", "overworld");
	private float ssfc_health;
	private int ssfc_food;
	private float ssfc_saturation;
	private float ssfc_exhaustion;
	
	public void ssfc_savePlayerData(ServerPlayerEntity player, ServerWorld cxtWorld) {
		ssfc_position = new double[3];
		ssfc_position[0] = player.getX();
		ssfc_position[1] = player.getY();
		ssfc_position[2] = player.getZ();
		
		ssfc_angle[0] = player.getYaw();
		ssfc_angle[1] = player.getPitch();
		
		ssfc_fallDistance = player.fallDistance;
		ssfc_gamemode = player.interactionManager.getGameMode();
		
		ssfc_world = cxtWorld.getRegistryKey().getValue();
		
		ssfc_health = player.getHealth();
		ssfc_food = player.getHungerManager().getFoodLevel();
		ssfc_saturation = player.getHungerManager().getSaturationLevel();
		ssfc_exhaustion = player.getHungerManager().getExhaustion();
	}
	
	public double[] ssfc_getPosition() {
		return ssfc_position;
	}
	
	public float[] ssfc_getAngle() {
		return ssfc_angle;
	}
	
	public float ssfc_getFallDistance() {
		return ssfc_fallDistance;
	}
	
	public GameMode ssfc_getGamemode() {
		return ssfc_gamemode;
	}
	
	public Identifier ssfc_getWorld() {
		return ssfc_world;
	}
	
	public void ssfc_resetPosition() {
		ssfc_position = null;
	}
	
	public float ssfc_getHealth() {
		return ssfc_health;
	}
	
	public int ssfc_getFood() {
		return ssfc_food;
	}
	
	public float ssfc_getSaturation() {
		return ssfc_saturation;
	}
	
	public float ssfc_getExhaustion() {
		return ssfc_exhaustion;
	}
	
	//@Inject (method = "writeCustomDataToNbt/readCustomDataFromNbt", at = @At("HEAD","RETURN"))
	@Inject(method = "writeCustomDataToNbt", at = @At("RETURN"))
	public void writeCustomDataToNbt (NbtCompound tag, CallbackInfo info) {
		if (ssfc_position == null) {
			tag.putDouble("ssfc_positionX", 2000000000.0);
			tag.putDouble("ssfc_positionY", 0.0);
			tag.putDouble("ssfc_positionZ", 0.0);
		} else {
			tag.putDouble("ssfc_positionX", ssfc_position[0]);
			tag.putDouble("ssfc_positionY", ssfc_position[1]);
			tag.putDouble("ssfc_positionZ", ssfc_position[2]);
		}
		
		tag.putFloat("ssfc_angleYaw", ssfc_angle[0]);
		tag.putFloat("ssfc_anglePitch", ssfc_angle[1]);
		tag.putFloat("ssfc_fallDistance", ssfc_fallDistance);
		
		// Thanks mojang for making me implement an awful hack...
		byte ssfc_gm;
		switch(ssfc_gamemode) {
		case SURVIVAL:
			ssfc_gm = 0;
			break;
		case CREATIVE:
			ssfc_gm = 1;
			break;
		case ADVENTURE:
			ssfc_gm = 2;
			break;
		case SPECTATOR:
			ssfc_gm = 3;
			break;
		default:
			ssfc_gm = 4; // Means GameMode.DEFAULT
		}
		tag.putByte("ssfc_gm", ssfc_gm);
		
		tag.putString("ssfc_world", ssfc_world.toString());
		
		tag.putFloat("ssfc_health", ssfc_health);
		tag.putInt("ssfc_food", ssfc_food);
		tag.putFloat("ssfc_saturation", ssfc_saturation);
		tag.putFloat("ssfc_exhaustion", ssfc_exhaustion);
	}
	
	@Inject(method = "readCustomDataFromNbt", at = @At("RETURN"))
	public void readCustomDataFromNbt(NbtCompound tag, CallbackInfo info) {
		ssfc_position = new double[3];
		ssfc_position[0] = tag.getDouble("ssfc_positionX");
		ssfc_position[1] = tag.getDouble("ssfc_positionY");
		ssfc_position[2] = tag.getDouble("ssfc_positionZ");
		if (ssfc_position[0] == 2000000000.0) {
			ssfc_position = null;
		}
		
		ssfc_angle[0] = tag.getFloat("ssfc_angleYaw");
		ssfc_angle[1] = tag.getFloat("ssfc_anglePitch");
		
		ssfc_fallDistance = tag.getFloat("ssfc_fallDistance");
		
		byte ssfc_gm = tag.getByte("ssfc_gm");
		switch(ssfc_gm) {
		case 0:
			ssfc_gamemode = GameMode.SURVIVAL;
			break;
		case 1:
			ssfc_gamemode = GameMode.CREATIVE;
			break;
		case 2:
			ssfc_gamemode = GameMode.ADVENTURE;
			break;
		case 3:
			ssfc_gamemode = GameMode.SPECTATOR;
			break;
		case 4:
			ssfc_gamemode = GameMode.DEFAULT;
			break;
		default:
			System.out.println("Something went SERIOUSLY wrong when loading data...");
		}

		ssfc_world = Identifier.tryParse(tag.getString("ssfc_world"));
		
		ssfc_health = tag.getFloat("ssfc_health");
		ssfc_food = tag.getInt("ssfc_food");
		ssfc_saturation = tag.getFloat("ssfc_saturation");
		ssfc_exhaustion = tag.getFloat("ssfc_exhaustion");
	}
}