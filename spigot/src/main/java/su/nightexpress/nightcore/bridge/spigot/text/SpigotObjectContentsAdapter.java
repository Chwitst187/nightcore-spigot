package su.nightexpress.nightcore.bridge.spigot.text;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nightexpress.nightcore.bridge.text.adapter.ObjectContentsAdapter;
import su.nightexpress.nightcore.bridge.text.contents.NightObjectContents;
import su.nightexpress.nightcore.bridge.text.contents.NightPlayerHeadObjectContents;
import su.nightexpress.nightcore.bridge.text.contents.NightSpriteObjectContents;
import su.nightexpress.nightcore.util.Lists;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.util.List;

public class SpigotObjectContentsAdapter implements ObjectContentsAdapter<Object> {

    private static SpigotObjectContentsAdapter instance;

    @NotNull
    public static SpigotObjectContentsAdapter get() {
        if (instance == null) {
            instance = new SpigotObjectContentsAdapter();
        }
        return instance;
    }

    @Override
    @NotNull
    public Object adaptContents(@NotNull NightObjectContents contents) {
        return contents.adapt(this);
    }

    @Override
    @Nullable
    public Object adaptContents(@NotNull NightSpriteObjectContents contents) {
        return this.newSpriteObject(contents.atlas().asString(), contents.sprite().value());
    }

    @Override
    @Nullable
    public Object adaptContents(@NotNull NightPlayerHeadObjectContents contents) {
        Object profile = this.newProfile(contents.name(), contents.id(), Lists.modify(contents.profileProperties(), this::adaptProfileProperty));
        if (profile == null) return null;

        return this.newPlayerObject(profile, contents.hat());
    }

    @Nullable
    private Object adaptProfileProperty(@NotNull NightPlayerHeadObjectContents.NightProfileProperty property) {
        try {
            Class<?> propertyClass = Class.forName("net.md_5.bungee.api.chat.player.Property");
            return propertyClass.getConstructor(String.class, String.class, String.class).newInstance(property.name(), property.value(), property.signature());
        }
        catch (Exception exception) {
            return null;
        }
    }

    @Nullable
    private Object newSpriteObject(@NotNull String atlas, @NotNull String sprite) {
        try {
            Class<?> spriteObjectClass = Class.forName("net.md_5.bungee.api.chat.objects.SpriteObject");
            return spriteObjectClass.getConstructor(String.class, String.class).newInstance(atlas, sprite);
        }
        catch (Exception exception) {
            return null;
        }
    }

    @Nullable
    private Object newProfile(@Nullable String name, @Nullable java.util.UUID id, @NotNull List<Object> properties) {
        try {
            Class<?> profileClass = Class.forName("net.md_5.bungee.api.chat.player.Profile");
            Class<?> propertyClass = Class.forName("net.md_5.bungee.api.chat.player.Property");
            Object propertiesArray = Array.newInstance(propertyClass, properties.size());

            for (int i = 0; i < properties.size(); i++) {
                Array.set(propertiesArray, i, properties.get(i));
            }

            Constructor<?> constructor = profileClass.getConstructor(String.class, java.util.UUID.class, propertiesArray.getClass());
            return constructor.newInstance(name, id, propertiesArray);
        }
        catch (Exception exception) {
            return null;
        }
    }

    @Nullable
    private Object newPlayerObject(@NotNull Object profile, boolean hat) {
        try {
            Class<?> playerObjectClass = Class.forName("net.md_5.bungee.api.chat.objects.PlayerObject");
            return playerObjectClass.getConstructor(profile.getClass(), boolean.class).newInstance(profile, hat);
        }
        catch (Exception exception) {
            return null;
        }
    }
}
