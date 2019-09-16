package dev.esophose.rosestacker.stack;

import dev.esophose.rosestacker.RoseStacker;
import dev.esophose.rosestacker.manager.ConfigurationManager.Setting;
import dev.esophose.rosestacker.manager.LocaleManager.Locale;
import dev.esophose.rosestacker.manager.StackManager;
import dev.esophose.rosestacker.stack.settings.EntityStackSettings;
import dev.esophose.rosestacker.utils.EntitySerializer;
import dev.esophose.rosestacker.utils.StackerUtils;
import dev.esophose.rosestacker.utils.StringPlaceholders;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class StackedEntity extends Stack {

    private LivingEntity entity;
    private List<String> serializedStackedEntities;

    private EntityStackSettings stackSettings;

    public StackedEntity(int id, LivingEntity entity, List<String> serializedStackedEntities) {
        super(id);

        this.entity = entity;
        this.serializedStackedEntities = serializedStackedEntities;

        if (this.entity != null) {
            this.stackSettings = RoseStacker.getInstance().getStackSettingManager().getEntityStackSettings(this.entity);

            if (Bukkit.isPrimaryThread())
                this.updateDisplay();
        }
    }

    public StackedEntity(LivingEntity entity, List<String> serializedStackedEntities) {
        this(-1, entity, serializedStackedEntities);
    }

    public LivingEntity getEntity() {
        return this.entity;
    }

    public void increaseStackSize(LivingEntity entity) {
        if (Setting.ENTITY_STACK_TO_BOTTOM.getBoolean()) {
            this.serializedStackedEntities.add(EntitySerializer.toNBTString(entity));
        } else {
            this.serializedStackedEntities.add(0, EntitySerializer.toNBTString(entity));
        }
        this.updateDisplay();
    }

    public void increaseStackSize(List<String> entityNBTStrings) {
        if (Setting.ENTITY_STACK_TO_BOTTOM.getBoolean()) {
            this.serializedStackedEntities.addAll(entityNBTStrings);
        } else {
            this.serializedStackedEntities.addAll(0, entityNBTStrings);
        }
        this.updateDisplay();
    }

    public void decreaseStackSize() {
        LivingEntity oldEntity = this.entity;
        Location location = this.entity.getLocation();
        this.entity = null; // Null it first so the CreatureSpawnEvent doesn't conflict with this Stack
        this.entity = EntitySerializer.fromNBTString(this.serializedStackedEntities.remove(0), location);
        RoseStacker.getInstance().getStackManager().updateStackedEntityKey(oldEntity, this.entity);
        this.updateDisplay();
    }

    public List<String> getStackedEntityNBTStrings() {
        return Collections.unmodifiableList(this.serializedStackedEntities);
    }

    /**
     * Drops all loot for all internally-stacked entities.
     * Does not include loot for the current entity.
     */
    public void dropStackLoot(Collection<ItemStack> existingLoot) {
        Collection<ItemStack> loot = new ArrayList<>();
        if (existingLoot != null)
            loot.addAll(existingLoot);

        int fireTicks = this.entity.getFireTicks(); // Propagate fire ticks so meats cook as you would expect
        for (String entityNBT : this.serializedStackedEntities) {
            LivingEntity entity = EntitySerializer.getNBTStringAsEntity(this.entity.getType(), this.entity.getLocation(), entityNBT);
            if (entity != null) {
                entity.setFireTicks(fireTicks);
                loot.addAll(StackerUtils.getEntityLoot(entity, this.entity.getKiller(), this.entity.getLocation()));
            }
        }

        RoseStacker.getInstance().getStackManager().preStackItems(loot, this.entity.getLocation());
    }

    /**
     * @return true if this entity should stay stacked, otherwise false
     */
    public boolean shouldStayStacked() {
        if (this.serializedStackedEntities.isEmpty())
            return true;

        LivingEntity entity = EntitySerializer.getNBTStringAsEntity(this.entity.getType(), this.entity.getLocation(), this.serializedStackedEntities.get(0));
        StackedEntity stackedEntity = new StackedEntity(entity, Collections.emptyList());
        return this.stackSettings.canStackWith(this, stackedEntity, true);
    }

    public StackedEntity split() {
        if (this.serializedStackedEntities.isEmpty())
            throw new IllegalStateException();

        StackManager stackManager = RoseStacker.getInstance().getStackManager();

        LivingEntity oldEntity = this.entity;
        stackManager.setEntityStackingDisabled(true);
        this.entity = EntitySerializer.fromNBTString(this.serializedStackedEntities.remove(0), oldEntity.getLocation());
        stackManager.setEntityStackingDisabled(false);
        this.stackSettings.applyUnstackProperties(this.entity, oldEntity);
        stackManager.updateStackedEntityKey(oldEntity, this.entity);
        this.updateDisplay();
        return new StackedEntity(oldEntity, new LinkedList<>());
    }

    public EntityStackSettings getStackSettings() {
        return this.stackSettings;
    }

    @Override
    public int getStackSize() {
        return this.serializedStackedEntities.size() + 1;
    }

    @Override
    public Location getLocation() {
        return this.entity.getLocation();
    }

    @Override
    public void updateDisplay() {
        if (!Setting.ENTITY_DISPLAY_TAGS.getBoolean())
            return;

        if (this.getStackSize() > 1 || Setting.ENTITY_DISPLAY_TAGS_SINGLE.getBoolean()) {
            String displayString = ChatColor.translateAlternateColorCodes('&', StringPlaceholders.builder("amount", String.valueOf(this.getStackSize()))
                    .addPlaceholder("name", this.stackSettings.getDisplayName())
                    .apply(Locale.ENTITY_STACK_DISPLAY.get()));

            this.entity.setCustomNameVisible(!Setting.ENTITY_DISPLAY_TAGS_HOVER.getBoolean());
            this.entity.setCustomName(displayString);
        } else {
            this.entity.setCustomNameVisible(false);
            this.entity.setCustomName(null);
        }
    }

}