package me.giiena.config.client.screen;

import me.giiena.config.ConfigCommon;
import me.giiena.config.api.Config;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.OptionsSubScreen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextColor;

import java.util.EnumMap;
import java.util.Map;

public class ConfigListScreen extends OptionsSubScreen {
    public static final int BIG_BUTTON_WIDTH = 310;

    public static final String TITLE = ConfigCommon.langKey("config", "gui", "title");
    public static final MutableComponent COMMON_CONFIG =
            Component.translatable(ConfigCommon.langKey("config", "gui", "common"));
    public static final MutableComponent SERVER_CONFIG =
            Component.translatable(ConfigCommon.langKey("config", "gui", "server"));
    public static final MutableComponent CLIENT_CONFIG =
            Component.translatable(ConfigCommon.langKey("config", "gui", "client"));
    public static final Component TOOLTIP_CANNOT_EDIT_ONLINE = Component.translatable(
            ConfigCommon.langKey("config.tooltip", "gui", "disabled.online"));

    private final String modName;
    private final EnumMap<Config.Type, Config> configs;

    public ConfigListScreen(
            String modName,
            Screen previous,
            final EnumMap<Config.Type, Config> configs) {
        super(
                previous,
                Minecraft.getInstance().options,
                Component.translatable(TITLE, modName));
        this.modName = modName;
        this.configs = configs;
    }

    @Override
    protected void addOptions() {
        if (this.list == null) return;

        for (Map.Entry<Config.Type, Config> entry : configs.entrySet()) {
            MutableComponent section;
            switch (entry.getKey()) {
                case COMMON -> section = COMMON_CONFIG;
                case SERVER -> section = SERVER_CONFIG;
                case CLIENT -> section = CLIENT_CONFIG;
                default -> section = Component.literal("UNKNOWN CONFIG TYPE");
            }

            Button btn = Button.builder(
                            section.copy(),
                            _ -> this.minecraft.gui.setScreen(new ConfigEditScreen(
                                    this.modName,
                                    this,
                                    entry.getValue())))
                    .width(BIG_BUTTON_WIDTH)
                    .build();
            MutableComponent tooltip = Component.empty();
            //noinspection DataFlowIssue
            if (entry.getValue().getType() == Config.Type.COMMON &&
                    this.minecraft.getCurrentServer() != null &&
                    (!this.minecraft.hasSingleplayerServer() ||
                            !this.minecraft.getSingleplayerServer().isPublished())) {
                btn.active = false;
                tooltip.append(TOOLTIP_CANNOT_EDIT_ONLINE.copy().withColor(TextColor.RED));
                tooltip.append(CommonComponents.NEW_LINE);
                tooltip.append(CommonComponents.NEW_LINE);
            }

            tooltip.append(Component.translatable(ConfigCommon.langKey("config", "gui", "file"),
                            entry.getValue().getFilePath().getFileName().toString())
                    .withColor(TextColor.GRAY));
            btn.setTooltip(Tooltip.create(tooltip));

            this.list.addSmall(new StringWidget(
                    BIG_BUTTON_WIDTH,
                    Button.DEFAULT_HEIGHT,
                    section.copy().withStyle(ChatFormatting.UNDERLINE),
                    this.font), null);
            this.list.addBig(btn);
        }
    }
}
