package dev.esophose.rosestacker.manager;

import co.aikar.commands.BukkitCommandCompletionContext;
import co.aikar.commands.CommandCompletions;
import co.aikar.commands.PaperCommandManager;
import co.aikar.locales.MessageKey;
import dev.esophose.rosestacker.RoseStacker;
import dev.esophose.rosestacker.command.RoseCommand;
import dev.esophose.rosestacker.manager.ConfigurationManager.Setting;
import dev.esophose.rosestacker.stack.settings.EntityStackSettings;
import dev.esophose.rosestacker.utils.StackerUtils;
import org.bukkit.ChatColor;
import org.bukkit.entity.EntityType;

import java.util.Arrays;
import java.util.Collections;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

public class CommandManager extends Manager {

    private boolean loaded;

    public CommandManager(RoseStacker roseStacker) {
        super(roseStacker);
        this.loaded = false;
    }

    @Override
    public void reload() {
        if (!this.loaded) {
            PaperCommandManager commandManager = new PaperCommandManager(this.roseStacker);
            commandManager.registerCommand(new RoseCommand(this.roseStacker));

            // Load custom message strings
            Map<String, String> acfCoreMessages = this.roseStacker.getLocaleManager().getAcfCoreMessages();
            for (String key : acfCoreMessages.keySet())
                commandManager.getLocales().addMessage(Locale.ENGLISH, MessageKey.of("acf-core." + key), LocaleManager.Locale.PREFIX.get() + acfCoreMessages.get(key));

            CommandCompletions<BukkitCommandCompletionContext> completions = commandManager.getCommandCompletions();
            completions.registerAsyncCompletion("amount", (ctx) -> Arrays.asList("5", "16", "64", "256", "<amount>"));
            completions.registerAsyncCompletion("stackableBlockMaterial", (ctx) -> this.roseStacker.getStackSettingManager().getStackableBlockTypes().stream().map(x -> x.name().toLowerCase()).collect(Collectors.toSet()));
            completions.registerAsyncCompletion("spawnableEntityType", (ctx) -> StackerUtils.getStackableEntityTypes().stream().map(x -> x.name().toLowerCase()).collect(Collectors.toSet()));
            completions.registerAsyncCompletion("blockStackAmounts", (ctx) -> {
                int maxStackAmount = Setting.BLOCK_MAX_STACK_SIZE.getInt();
                return Arrays.asList(String.valueOf(maxStackAmount), String.valueOf(maxStackAmount / 2), String.valueOf(maxStackAmount / 4), "<amount>");
            });
            completions.registerAsyncCompletion("spawnerStackAmounts", (ctx) -> {
                int maxStackAmount = Setting.SPAWNER_MAX_STACK_SIZE.getInt();
                return Arrays.asList(String.valueOf(maxStackAmount), String.valueOf(maxStackAmount / 2), String.valueOf(maxStackAmount / 4), "<amount>");
            });
            completions.registerAsyncCompletion("entityStackAmounts", (ctx) -> {
                EntityType entityType = ctx.getContextValue(EntityType.class);
                if (entityType != null) {
                    EntityStackSettings entityStackSettings = this.roseStacker.getStackSettingManager().getEntityStackSettings(entityType);
                    int maxStackAmount = entityStackSettings.getMaxStackSize();
                    return Arrays.asList(String.valueOf(maxStackAmount), String.valueOf(maxStackAmount / 2), String.valueOf(maxStackAmount / 4), "<amount>");
                }
                return Collections.emptySet();
            });

            this.loaded = true;
        }
    }

    @Override
    public void disable() {

    }

    private String parse(String string) {
        return ChatColor.translateAlternateColorCodes('&', string);
    }

}