package me.giiena.config.client.screen;

import me.giiena.config.ConfigCommon;
import me.giiena.config.api.Config;
import me.giiena.config.client.screen.widget.ConfigEditBox;
import me.giiena.config.platform.Services;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.OptionsSubScreen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextColor;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public class ConfigEditScreen extends OptionsSubScreen {
    public static final Component RESET =
            Component.translatable(ConfigCommon.langKey("config", "gui", "reset"));

    private final ResetManager resetManager = new ResetManager();
    private final Config config;
    private Button reset;
    private Button done;
    private boolean changed = false;

    public ConfigEditScreen(String modName, Screen previous, Config config) {
        super(
                previous,
                Minecraft.getInstance().options,
                Component.translatable(ConfigCommon.langKey(
                        "config." + config.getType().suffix(),
                        "gui",
                        "title"), modName));
        this.config = config;
    }

    @Override
    public void init() {
        this.changed = false;
        this.resetManager.clear();
        this.createResetButton();
        this.createDoneButton();
        super.init();
    }

    @Override
    protected void addOptions() {
        this.rebuild();
    }

    @Override
    protected void addFooter() {
        if (this.reset != null || this.done != null) {
            LinearLayout layout = this.layout.addToFooter(LinearLayout.horizontal().spacing(8));
            if (this.reset != null) {
                layout.addChild(this.reset);
            }
            if (this.done != null) {
                layout.addChild(this.done);
            }
        } else {
            super.addFooter();
        }
    }

    @SuppressWarnings("UnusedReturnValue")
    protected ConfigEditScreen rebuild() {
        if (this.list != null) {
            this.list.clearEntries();

            for (Object entry : this.buildEntries()) {
                String labelKey;
                Component label;
                final AbstractWidget valueWidget;
                if (entry instanceof String section) {
                    labelKey = ConfigCommon.langKey(
                            this.config.getModID(),
                            "config." + this.config.getType().suffix(),
                            "gui",
                            section);
                    label = Component.translatable(labelKey).withStyle(ChatFormatting.UNDERLINE);
                    AbstractWidget labelWidget = new StringWidget(
                            ConfigListScreen.BIG_BUTTON_WIDTH,
                            Button.DEFAULT_HEIGHT,
                            label,
                            this.font);

                    this.list.addSmall(labelWidget, null);
                } else if (entry instanceof Config.Value<?> value) {
                    labelKey = ConfigCommon.langKey(
                            this.config.getModID(),
                            "config." + this.config.getType().suffix(),
                            "gui",
                            value.path());
                    label = Component.translatable(labelKey);
                    AbstractWidget labelWidget = new StringWidget(
                            ConfigListScreen.BIG_BUTTON_WIDTH,
                            Button.DEFAULT_HEIGHT,
                            label,
                            this.font);

                    Object defaultVal = value.getDefault();
                    MutableComponent tooltip = Component.empty();
                    if (defaultVal instanceof String) {
                        valueWidget =
                                this.createStringValue(value.path(), label, (String) value.get());
                    } else if (defaultVal instanceof Integer) {
                        valueWidget =
                                this.createIntValue(value.path(), label, (Integer) value.get());
                    } else if (defaultVal instanceof Boolean) {
                        valueWidget =
                                this.createBooleanValue(value.path(), label, (Boolean) value.get());
                    } else if (value.get() != null) {
                        ConfigEditBox box = new ConfigEditBox(
                                this.font,
                                Button.DEFAULT_WIDTH,
                                Button.DEFAULT_HEIGHT,
                                label);
                        box.setEditable(false);
                        box.setValue(value.get().toString());
                        valueWidget = box;
                        tooltip.append(Component.translatable(ConfigCommon.langKey(
                                        "config.tooltip",
                                        "gui",
                                        "unsupported"))
                                .withColor(TextColor.RED));
                    } else {
                        valueWidget = null;
                    }
                    this.config.comment(value.path()).ifPresent(c -> {
                        if (valueWidget == null) return;
                        if (!tooltip.getSiblings().isEmpty()) {
                            tooltip.append(CommonComponents.NEW_LINE);
                            tooltip.append(CommonComponents.NEW_LINE);
                        }
                        tooltip.append(c);
                        valueWidget.setTooltip(Tooltip.create(tooltip));
                    });

                    this.list.addSmall(labelWidget, valueWidget);
                }
            }
        }
        return this;
    }

    protected void createResetButton() {
        this.reset = Button.builder(RESET, _ -> {
                    this.resetManager.reset();
                    this.setResetButtonState(false);
                })
                .width(Button.SMALL_WIDTH).build();
        this.reset.active = false;
    }

    protected void setResetButtonState(boolean state) {
        if (this.reset != null) {
            this.reset.active = state;
        }
    }

    protected void createDoneButton() {
        this.done = Button.builder(CommonComponents.GUI_DONE, _ -> {
                    this.resetManager.run();
                    if (this.changed) {
                        this.config.save();
                        Services.PLATFORM.sendReloadPacket(this.config.getModID());
                    }
                    this.onClose();
                })
                .width(Button.SMALL_WIDTH).build();
        this.done.active = true;
    }

    protected void onChanged() {
        this.changed = true;
        this.setResetButtonState(true);
    }

    private List<Object> buildEntries() {
        List<Object> entries = new ArrayList<>();
        Set<String> seen = new LinkedHashSet<>();

        for (Config.Value<?> value : this.config.values()) {
            String path = value.path();
            List<String> parts = List.of(path.split("\\."));

            StringBuilder prefix = new StringBuilder();
            for (String part : parts) {
                if (!prefix.isEmpty()) prefix.append(".");
                prefix.append(part);
                String fullPath = prefix.toString();
                if (config.get(fullPath) == null && seen.add(fullPath)) {
                    entries.add(fullPath);
                }
            }

            if (seen.add(path)) {
                entries.add(value);
            }
        }

        return entries;
    }

    protected AbstractWidget createStringValue(String path, Component label, String oldVal) {
        ConfigEditBox box = new ConfigEditBox(
                this.font,
                Button.DEFAULT_WIDTH,
                Button.DEFAULT_HEIGHT,
                label);
        box.setEditable(true);
        box.setResponder(resp -> {
            if (!resp.equals(oldVal)) {
                this.onChanged();
                this.resetManager.remove(path);
                this.resetManager.add(
                        path,
                        v -> this.config.set(path, v),
                        resp,
                        v -> {
                            box.setValue(v);
                            this.config.set(path, v);
                        },
                        oldVal);
            } else {
                this.resetManager.remove(path);
            }
        });
        box.setValue(oldVal);
        return box;
    }

    protected AbstractWidget createIntValue(String path, Component label, Integer oldVal) {
        ConfigEditBox box = new ConfigEditBox(
                this.font,
                Button.DEFAULT_WIDTH,
                Button.DEFAULT_HEIGHT,
                label);
        box.setEditable(true);
        box.setFilter(i -> {
            if (i.isEmpty()) return true;
            try {
                Integer.parseInt(i);
                return true;
            } catch (NumberFormatException e) {
                return false;
            }
        });
        box.setResponder(resp -> {
            if (oldVal.toString().equals(resp)) {
                this.resetManager.remove(path);
                return;
            }

            int newVal;
            try {
                newVal = Integer.parseInt(resp);
            } catch (NumberFormatException e) {
                return;
            }

            this.onChanged();
            this.resetManager.remove(path);
            this.resetManager.add(
                    path,
                    v -> this.config.set(path, v),
                    newVal,
                    v -> {
                        box.setValue(v.toString());
                        this.config.set(path, v);
                    },
                    oldVal);
        });
        box.setValue(oldVal.toString());
        return box;
    }

    protected AbstractWidget createBooleanValue(
            String path,
            Component ignoredLabel,
            Boolean oldVal) {
        AtomicBoolean currentVal = new AtomicBoolean(oldVal);

        return Button.builder(
                        this.resolveBooleanValue(oldVal),
                        b -> {
                            boolean newVal = !currentVal.get();
                            currentVal.set(newVal);
                            b.setMessage(this.resolveBooleanValue(newVal));

                            if (oldVal.equals(newVal)) {
                                this.resetManager.remove(path);
                                return;
                            }

                            this.onChanged();
                            this.resetManager.remove(path);
                            this.resetManager.add(
                                    path,
                                    v -> this.config.set(path, v),
                                    newVal,
                                    v -> {
                                        currentVal.set(v);
                                        b.setMessage(this.resolveBooleanValue(v));
                                        this.config.set(path, v);
                                    },
                                    oldVal);
                        })
                .size(Button.DEFAULT_WIDTH, Button.DEFAULT_HEIGHT)
                .build();
    }

    private Component resolveBooleanValue(boolean value) {
        return value ? CommonComponents.GUI_YES : CommonComponents.GUI_NO;
    }

    public static class ResetManager {
        private final Map<String, Entry<?>> entries = new LinkedHashMap<>();

        public <T> void add(
                String path,
                Consumer<T> run,
                T newValue,
                Consumer<T> reset,
                T oldValue) {
            this.entries.put(path, new Entry<>(run, newValue, reset, oldValue));
        }

        public void remove(String path) {
            this.entries.remove(path);
        }

        public void run() {
            this.consumeEntries().forEach(Entry::runEntry);
        }

        public void reset() {
            this.consumeEntries().forEach(Entry::resetEntry);
        }

        public void clear() {
            this.entries.clear();
        }

        protected List<Entry<?>> consumeEntries() {
            List<Entry<?>> list = new ArrayList<>(this.entries.values());
            this.clear();
            return list;
        }

        public record Entry<T>(
                Consumer<T> run,
                T newValue,
                Consumer<T> reset,
                T oldValue) {
            public void runEntry() {
                this.run.accept(this.newValue);
            }

            public void resetEntry() {
                this.reset.accept(this.oldValue);
            }
        }
    }
}
