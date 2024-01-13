package cn.ussshenzhou.villager.entity.fakeplayer.utils;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import net.minecraft.client.resources.DefaultPlayerSkin;

import java.util.UUID;

public class CustomGameProfile extends GameProfile {

    public CustomGameProfile(UUID uuid, String name) {
        super(uuid, name);
    }
}
