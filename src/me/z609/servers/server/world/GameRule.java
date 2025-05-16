package me.z609.servers.server.world;

import org.bukkit.World;

public enum GameRule {
    ANNOUNCE_ADVANCEMENTS("announceAdvancements", false),
    BLOCK_EXPLOSION_DROP_DECAY("blockExplosionDropDecay", true),
    COMMAND_BLOCK_OUTPUT("commandBlockOutput", true),
    COMMAND_MODIFICATION_BLOCK_LIMIT("commandModificationBlockLimit", 32768),
    DISABLE_ELYTRA_MOVEMENT_CHECK("disableElytraMovementCheck", false),
    DISABLE_PLAYER_MOVEMENT_CHECK("disablePlayerMovementCheck", false),
    DISABLE_RAIDS("disableRaids", false),
    DO_DAYLIGHT_CYCLE("doDaylightCycle", true),
    DO_ENTITY_DROPS("doEntityDrops", true),
    DO_FIRE_TICK("doFireTick", true),
    DO_INSOMNIA("doInsomnia", true),
    DO_IMMEDIATE_RESPAWN("doImmediateRespawn", false),
    DO_LIMITED_CRAFTING("doLimitedCrafting", false),
    DO_MOB_LOOT("doMobLoot", true),
    DO_MOB_SPAWNING("doMobSpawning", true),
    DO_PATROL_SPAWNING("doPatrolSpawning", true),
    DO_TILE_DROPS("doTileDrops", true),
    DO_TRADER_SPAWNING("doTraderSpawning", true),
    DO_VINES_SPREAD("doVinesSpread", true),
    DO_WEATHER_CYCLE("doWeatherCycle", true),
    DO_WARDEN_SPAWNING("doWardenSpawning", true),
    DROWNING_DAMAGE("drowningDamage", true),
    ENDER_PEARLS_VANISH_ON_DEATH("enderPealsVanishOnDeath", true),
    FALL_DAMAGE("fallDamage", true),
    FIRE_DAMAGE("fireDamage", true),
    FORGIVE_DEAD_PLAYERS("forgiveDeadPlayers", true),
    FREEZE_DAMAGE("freezeDamage", true),
    GLOBAL_SOUND_EFFECTS("globalSoundEffects", true),
    KEEP_INVENTORY("keepInventory", false),
    LAVA_SOURCE_CONVERSION("lavaSourceConversion", false),
    LOG_ADMIN_COMMANDS("logAdminCommands", true),
    MAX_COMMAND_CHAIN_LENGTH("maxCommandChainLength", 65536),
    MAX_COMMAND_FORK_COUNT("maxCommandForkCount", 65536),
    MAX_ENTITY_CRAMMING("maxEntityCramming", 24),
    MINECART_MAX_SPEED("minecartMaxSpeed", 8),
    MOB_EXPLOSION_DROP_DECAY("mobExplosionDropDecay", true), 
    MOB_GRIEFING("mobGriefing", true),
    NATURAL_REGENERATION("naturalRegeneration", true),
    PLAYERS_NETHER_PORTAL_CREATIVE_DELAY("playersNetherPortalCreativeDelay", 1),
    PLAYERS_NETHER_PORTAL_DEFAULT_DELAY("playersNetherPortalDefaultDelay", 80),
    PLAYERS_SLEEPING_PERCENTAGE("playersSleepingPercentage", 100),
    PROJECTILES_CAN_BREAK_BLOCKS("projectilesCanBreakBlocks", true),
    RANDOM_TICK_SPEED("randomTickSpeed", 3),
    REDUCED_DEBUG_INFO("reducedDebugInfo", false),
    SEND_COMMAND_FEEDBACK("sendCommandFeedback", true),
    SHOW_DEATH_MESSAGES("showDeathMessages", true),
    SNOW_ACCUMULATE_HEIGHT("snowAccumulateHeight", 1),
    SPAWN_CHUNK_RADIUS("spawnChunkRadius", 2),
    SPAWN_RADIUS("spawnRadius", 10),
    SPECTATORS_GENERATE_CHUNKS("spectatorsGenerateChunks", true),
    TNT_EXPLOSION_DROP_DECAY("tntExplosionDropDecay", false),
    UNIVERSAL_ANGER("universalAnger", false),
    WATER_SOURCE_CONVERSION("waterSourceConversion", true);

    private final String ruleName;
    private final Class<?> ruleType;
    private final Object ruleDefault;

    GameRule(String ruleName, Object ruleDefault) {
        this.ruleName = ruleName;
        this.ruleType = ruleDefault.getClass();
        this.ruleDefault = ruleDefault;
    }

    public String getRuleName() {
        return ruleName;
    }

    public Class<?> getRuleType() {
        return ruleType;
    }

    @SuppressWarnings("unchecked")
    public <T> T getRuleDefault() {
        return (T) ruleDefault;
    }

    @SuppressWarnings("unchecked")
    public <T> T get(World world) {
        String raw = world.getGameRuleValue(ruleName);
        if (raw == null) return null;

        try {
            if (ruleType == Boolean.class) {
                return (T) Boolean.valueOf(raw);
            } else if (ruleType == Integer.class) {
                return (T) Integer.valueOf(raw);
            } else {
                return (T) raw;
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to convert gamerule '" + ruleName + "' to " + ruleType.getSimpleName(), e);
        }
    }

    public void set(World world, Object value) {
        if(value == null)
            throw new IllegalArgumentException("Value of " + name() + " cannot be NULL");
        if(!ruleType.isInstance(value))
            throw new IllegalArgumentException("Value of " + name() + " must be in the type of " + ruleType.getSimpleName() + " - got " + value.getClass().getSimpleName() + " instead");
        world.setGameRuleValue(ruleName, String.valueOf(value));
    }

    public static void applyDefaults(World world){
        for(GameRule rule : values())
            world.setGameRuleValue(rule.ruleName, String.valueOf(rule.ruleDefault));
    }
}
