package dev.rosewood.rosestacker.nms.v1_21_R5.storage;

import dev.rosewood.rosestacker.nms.NMSAdapter;
import dev.rosewood.rosestacker.nms.NMSHandler;
import dev.rosewood.rosestacker.nms.storage.AbstractSimpleStackedEntityDataStorage;
import dev.rosewood.rosestacker.nms.v1_21_R5.NMSHandlerImpl;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.storage.TagValueOutput;
import org.bukkit.craftbukkit.v1_21_R5.entity.CraftLivingEntity;
import org.bukkit.entity.LivingEntity;

public class SimpleStackedEntityDataStorage extends AbstractSimpleStackedEntityDataStorage {

    public SimpleStackedEntityDataStorage(LivingEntity livingEntity) {
        super(livingEntity);
    }

    public SimpleStackedEntityDataStorage(LivingEntity livingEntity, byte[] data) {
        super(livingEntity, data);
    }

    @Override
    protected NBTEntityDataEntry copy() {
        LivingEntity entity = this.entity.get();
        if (entity == null)
            return new NBTEntityDataEntry(new CompoundTag());

        TagValueOutput valueOutput = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, ((CraftLivingEntity) entity).getHandle().registryAccess());
        ((NMSHandlerImpl) NMSAdapter.getHandler()).saveEntityToTag(entity, valueOutput);

        CompoundTag tag = valueOutput.buildResult();

        this.stripUnneeded(tag);
        return new NBTEntityDataEntry(tag);
    }

    private void stripUnneeded(CompoundTag compoundTag) {
        NMSHandler.REMOVABLE_NBT_KEYS.forEach(compoundTag::remove);
        CompoundTag bukkitValues = compoundTag.getCompoundOrEmpty("BukkitValues");
        bukkitValues.remove("rosestacker:stacked_entity_data");
        NMSHandler.UNSAFE_NBT_KEYS.forEach(compoundTag::remove);
    }

}
