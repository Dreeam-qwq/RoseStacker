package dev.rosewood.rosestacker.utils;

import dev.rosewood.rosestacker.config.SettingKey;
import dev.rosewood.rosestacker.manager.StackManager;
import dev.rosewood.rosestacker.nms.NMSAdapter;
import dev.rosewood.rosestacker.nms.NMSHandler;
import dev.rosewood.rosestacker.stack.StackedItem;
import org.bukkit.Location;
import org.bukkit.block.ShulkerBox;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;

import java.util.ArrayList;
import java.util.List;

public class ContainerUtil {

    public static boolean unpackShulkerBox(StackManager stackManager, Item item, double damage) {
        StackedItem stackedItem = stackManager.getStackedItem(item);
        if (stackedItem == null)
            return false;

        final int amount = stackedItem.getStackSize();

        if (amount > 1 && damage >= item.getHealth()) {
            final List<ItemStack> contents = getContents(item);
            final List<ItemStack> totalContents = new ArrayList<>();
            final Location location = item.getLocation();

            item.remove();

            for (int i = amount; i > 0; i--) {
                totalContents.addAll(contents);
            }

            final int maxStackSize = SettingKey.ITEM_MAX_STACK_SIZE.get();

            for (; ; ) {
                if (totalContents.size() > maxStackSize) {
                    List<ItemStack> stack = new ArrayList<>(totalContents.subList(0, maxStackSize));
                    totalContents.subList(0, maxStackSize).clear();
                    stackManager.preStackItems(stack, location);
                } else {
                    stackManager.preStackItems(totalContents, location);
                    break;
                }
            }

            return true;
        }

        return false;
    }

    public static List<ItemStack> getContents(Item item) {
        List<ItemStack> contents = new ArrayList<>();

        if (SettingKey.ITEM_UNPACK_BOX_LIKE_VANILLA.get()) {
            NMSHandler nmsHandler = NMSAdapter.getHandler();
            contents = nmsHandler.getBoxContents(item);
        } else {
            ItemStack itemStack = item.getItemStack();
            if (!(itemStack.getItemMeta() instanceof BlockStateMeta meta)) return contents;

            if (!(meta.getBlockState() instanceof ShulkerBox box)) return contents;

            for (ItemStack content : box.getInventory().getContents()) {
                if (content == null || content.getType().isAir()) continue;

                contents.add(content);
            }
        }

        return contents;
    }

    public static boolean isShulkerBox(Item item) {
        return item.getItemStack().getType().toString().contains("SHULKER_BOX");
    }

    // TODO
    public static boolean isContainer() {
        return false;
    }
}
