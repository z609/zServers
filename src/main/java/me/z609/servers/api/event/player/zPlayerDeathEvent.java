package me.z609.servers.api.event.player;

import me.z609.servers.api.zServersPlayerEvent;
import me.z609.servers.server.zServer;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class zPlayerDeathEvent extends zServersPlayerEvent {
    private List<ItemStack> drops;
    private int dropExp;
    private double reviveHealth;
    private boolean shouldPlayDeathSound;
    private Sound deathSound;
    private SoundCategory deathSoundCategory;
    private float deathSoundVolume;
    private float deathSoundPitch;
    private int newExp;
    private String deathMessage;
    private int newLevel;
    private int newTotalExp;
    private boolean keepLevel;
    private boolean keepInventory;

    public zPlayerDeathEvent(zServer server, Player player, List<ItemStack> drops, int dropExp, double reviveHealth, boolean shouldPlayDeathSound, Sound deathSound, SoundCategory deathSoundCategory, float deathSoundVolume, float deathSoundPitch, int newExp, String deathMessage, int newLevel, int newTotalExp, boolean keepLevel, boolean keepInventory) {
        super(server, player);
        this.drops = drops;
        this.dropExp = dropExp;
        this.reviveHealth = reviveHealth;
        this.shouldPlayDeathSound = shouldPlayDeathSound;
        this.deathSound = deathSound;
        this.deathSoundCategory = deathSoundCategory;
        this.deathSoundVolume = deathSoundVolume;
        this.deathSoundPitch = deathSoundPitch;
        this.newExp = newExp;
        this.deathMessage = deathMessage;
        this.newLevel = newLevel;
        this.newTotalExp = newTotalExp;
        this.keepLevel = keepLevel;
        this.keepInventory = keepInventory;
    }

    public List<ItemStack> getDrops() {
        return drops;
    }

    public void setDrops(List<ItemStack> drops) {
        this.drops = drops;
    }

    public int getDropExp() {
        return dropExp;
    }

    public void setDropExp(int dropExp) {
        this.dropExp = dropExp;
    }

    public double getReviveHealth() {
        return reviveHealth;
    }

    public void setReviveHealth(double reviveHealth) {
        this.reviveHealth = reviveHealth;
    }

    public boolean isShouldPlayDeathSound() {
        return shouldPlayDeathSound;
    }

    public void setShouldPlayDeathSound(boolean shouldPlayDeathSound) {
        this.shouldPlayDeathSound = shouldPlayDeathSound;
    }

    public Sound getDeathSound() {
        return deathSound;
    }

    public void setDeathSound(Sound deathSound) {
        this.deathSound = deathSound;
    }

    public SoundCategory getDeathSoundCategory() {
        return deathSoundCategory;
    }

    public void setDeathSoundCategory(SoundCategory deathSoundCategory) {
        this.deathSoundCategory = deathSoundCategory;
    }

    public float getDeathSoundVolume() {
        return deathSoundVolume;
    }

    public void setDeathSoundVolume(float deathSoundVolume) {
        this.deathSoundVolume = deathSoundVolume;
    }

    public float getDeathSoundPitch() {
        return deathSoundPitch;
    }

    public void setDeathSoundPitch(float deathSoundPitch) {
        this.deathSoundPitch = deathSoundPitch;
    }

    public int getNewExp() {
        return newExp;
    }

    public void setNewExp(int newExp) {
        this.newExp = newExp;
    }

    public String getDeathMessage() {
        return deathMessage;
    }

    public void setDeathMessage(String deathMessage) {
        this.deathMessage = deathMessage;
    }

    public int getNewLevel() {
        return newLevel;
    }

    public void setNewLevel(int newLevel) {
        this.newLevel = newLevel;
    }

    public int getNewTotalExp() {
        return newTotalExp;
    }

    public void setNewTotalExp(int newTotalExp) {
        this.newTotalExp = newTotalExp;
    }

    public boolean isKeepLevel() {
        return keepLevel;
    }

    public void setKeepLevel(boolean keepLevel) {
        this.keepLevel = keepLevel;
    }

    public boolean isKeepInventory() {
        return keepInventory;
    }

    public void setKeepInventory(boolean keepInventory) {
        this.keepInventory = keepInventory;
    }
}
