package dev.rosewood.rosestacker.nms.v1_21_R3.event;

import dev.rosewood.rosestacker.event.AsyncEntityDeathEvent;
import java.lang.reflect.Field;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.damage.DamageSource;
import org.bukkit.damage.DamageType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class AsyncEntityDeathEventImpl extends EntityDeathEvent implements AsyncEntityDeathEvent {

    private static Field asyncField;
    static {
        try {
            asyncField = Event.class.getDeclaredField("async");
            asyncField.setAccessible(true);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
    }

    public AsyncEntityDeathEventImpl(@NotNull LivingEntity what, @NotNull List<ItemStack> drops, int droppedExp) {
        super(what, DamageSource.builder(DamageType.GENERIC_KILL).build(), drops, droppedExp);
        try {
            asyncField.set(this, !Bukkit.isPrimaryThread());
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

}
