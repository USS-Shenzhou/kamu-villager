package cn.ussshenzhou.villager.entity.fakeplayer.utils;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import net.minecraft.client.resources.DefaultPlayerSkin;

import java.util.UUID;

/**
 * @author USS_Shenzhou
 * <br/>This file is modified from <a href="https://github.com/HorseNuggets/TerminatorPlus">TerminatorPlus</a> under EPL-2.0 license, and can be distributed under EPL-2.0 license only.
 */
public class CustomGameProfile extends GameProfile {

    public CustomGameProfile(UUID uuid, String name) {
        super(uuid, name);
    }
}
