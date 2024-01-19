package cn.ussshenzhou.villager.entity.fakeplayer.utils;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * @author USS_Shenzhou
 * <br/>This file is modified from <a href="https://github.com/HorseNuggets/TerminatorPlus">TerminatorPlus</a> under EPL-2.0 license, and can be distributed under EPL-2.0 license only.
 */
public class MojangAPI {

    private static final boolean CACHE_ENABLED = false;

    private static final Map<String, String[]> CACHE = new HashMap<>();

    public static String[] getSkin(String name) {
        if (CACHE_ENABLED && CACHE.containsKey(name)) {
            return CACHE.get(name);
        }

        String[] values = pullFromAPI(name);
        CACHE.put(name, values);
        return values;
    }

    // CATCHING NULL ILLEGALSTATEEXCEPTION BAD!!!! eventually fix from the getAsJsonObject thingy
    public static String[] pullFromAPI(String name) {
        try {
            String uuid = new JsonParser().parse(new InputStreamReader(new URL("https://api.mojang.com/users/profiles/minecraft/" + name)
                    .openStream())).getAsJsonObject().get("id").getAsString();
            JsonObject property = new JsonParser()
                    .parse(new InputStreamReader(new URL("https://sessionserver.mojang.com/session/minecraft/profile/" + uuid + "?unsigned=false")
                            .openStream())).getAsJsonObject().get("properties").getAsJsonArray().get(0).getAsJsonObject();
            return new String[] {property.get("value").getAsString(), property.get("signature").getAsString()};
        } catch (IOException | IllegalStateException e) {
            return null;
        }
    }
}
