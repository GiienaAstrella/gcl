package me.giiena.config.impl.client.screen;

import me.giiena.config.impl.ConfigCommon;
import me.giiena.config.api.Config;
import me.giiena.config.impl.TranslationChecker;
import me.giiena.config.impl.client.screen.component.ConfigEditBox;
import me.giiena.config.impl.network.ConfigReloadPayload;
import me.giiena.config.impl.platform.Services;
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
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Supplier;

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

    @Override
    public void onClose() {
        TranslationChecker.done();
        super.onClose();
    }

    @SuppressWarnings({"UnusedReturnValue", "unchecked"})
    protected ConfigEditScreen rebuild() {
        if (this.list != null) {
            this.list.clearEntries();

            final Map<String, Entry> entries = new LinkedHashMap<>();

            for (Config.Value<?> value : this.config.values()) {
                String path = value.path();
                List<String> paths = ConfigCommon.DOT_SPLITTER.splitToList(path);
                Map<String, Entry> current = entries;

                for (int i = 0; i < paths.size() - 1; i++) {
                    String sectPath = ConfigCommon.DOT_JOINER.join(paths.subList(0, i + 1));

                    Section section;
                    if (current.get(paths.get(i)) instanceof Section exist) {
                        section = exist;
                    } else {
                        section = new Section(this.getTranslationComponent(sectPath),
                                this.getTooltipComponent(sectPath));
                        current.put(paths.get(i), section);
                    }
                    current = section.entries;
                }

                String leaf = paths.getLast();
                Object defaultVal = value.getDefault();

                switch (defaultVal) {
                    case String _ -> {
                        Config.Value<String> typed = (Config.Value<String>) value;
                        current.put(leaf, this.createStringElement(path, typed, typed::set));
                    }
                    case Integer _ -> {
                        Config.Value<Integer> typed = (Config.Value<Integer>) value;
                        current.put(leaf, this.createIntElement(path, typed, typed::set));
                    }
                    case Boolean _ -> {
                        Config.Value<Boolean> typed = (Config.Value<Boolean>) value;
                        current.put(leaf, this.createBooleanElement(path, typed, typed::set));
                    }
                    default -> {
                    }
                }
            }

            this.addEntries(entries);
        }
        return this;
    }

    protected void addEntries(Map<String, Entry> entries) {
        if (this.list == null) return;

        for (Entry entry : entries.values()) {
            StringWidget label = new StringWidget(ConfigListScreen.BIG_BUTTON_WIDTH,
                    Button.DEFAULT_HEIGHT,
                    entry.name(),
                    this.font);
            Component tooltip = entry.tooltip();
            if (tooltip != null) {
                label.setTooltip(Tooltip.create(tooltip));
            }

            if (entry instanceof Section section) {
                label.setMessage(section.name().copy().withStyle(ChatFormatting.UNDERLINE));
                this.list.addBig(label);
                this.addEntries(section.entries());
            } else if (entry instanceof Element element) {
                this.list.addSmall(label, element.widget());
            }
        }
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
                        Services.PLATFORM.sendPacketToServer(
                                new ConfigReloadPayload(this.config.getModID()));
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

    private Component resolveBooleanValue(boolean value) {
        return value ? CommonComponents.GUI_YES : CommonComponents.GUI_NO;
    }

    protected Element createStringElement(final String path,
                                          final Supplier<String> source,
                                          final Consumer<String> target) {
        ConfigEditBox box = new ConfigEditBox(this.font,
                Button.DEFAULT_WIDTH,
                Button.DEFAULT_HEIGHT,
                this.getTranslationComponent(path));
        box.setEditable(true);
        box.setResponder(resp -> {
            if (!resp.equals(source.get())) {
                this.onChanged();
                this.resetManager.remove(path);
                this.resetManager.add(path,
                        target,
                        resp,
                        v -> {
                            box.setValue(v);
                            target.accept(v);
                        },
                        source.get());
            } else {
                this.resetManager.remove(path);
            }
        });
        box.setValue(source.get());
        return new Element(this.getTranslationComponent(path),
                this.getTooltipComponent(path),
                box);
    }

    protected Element createIntElement(final String path,
                                       final Supplier<Integer> source,
                                       final Consumer<Integer> target) {
        ConfigEditBox box = new ConfigEditBox(this.font,
                Button.DEFAULT_WIDTH,
                Button.DEFAULT_HEIGHT,
                this.getTranslationComponent(path));
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
            if (resp.equals(source.get().toString())) {
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
            this.resetManager.add(path,
                    target,
                    newVal,
                    v -> {
                        box.setValue(v.toString());
                        target.accept(v);
                    },
                    source.get());
        });
        box.setValue(source.get().toString());
        return new Element(this.getTranslationComponent(path),
                this.getTooltipComponent(path),
                box);
    }

    protected Element createBooleanElement(final String path,
                                           final Supplier<Boolean> source,
                                           final Consumer<Boolean> target) {
        AtomicBoolean current = new AtomicBoolean(source.get());

        Button btn = Button.builder(this.resolveBooleanValue(source.get()),
                b -> {
                    boolean newVal = !current.get();
                    current.set(newVal);
                    b.setMessage(this.resolveBooleanValue(!source.get()));

                    if (newVal == source.get()) {
                        this.resetManager.remove(path);
                        return;
                    }

                    this.onChanged();
                    this.resetManager.remove(path);
                    this.resetManager.add(path,
                            target,
                            !source.get(),
                            v -> {
                                current.set(v);
                                b.setMessage(this.resolveBooleanValue(v));
                                target.accept(v);
                            },
                            source.get());
                })
                .size(Button.DEFAULT_WIDTH, Button.DEFAULT_HEIGHT)
                .build();
        return new Element(this.getTranslationComponent(path),
                this.getTooltipComponent(path),
                btn);
    }

    protected Component getTranslationComponent(final String path) {
        String translation = this.translationKey(path);
        if (TranslationChecker.has(this.config, translation)) {
            return Component.translatable(translation);
        } else {
            return Component.literal(path);
        }
    }

    @Nullable
    protected Component getTooltipComponent(final String path) {
        String translation = this.translationKey(path) + ".tooltip";
        if (TranslationChecker.has(this.config, translation)) {
            return Component.translatable(translation);
        } else {
            return this.config.getComment(path).map(Component::literal).orElse(null);
        }
    }

    protected String translationKey(final String path) {
        List<String> parts = List.of(this.config.getModID(), this.config.getType().suffix(), path);
        return String.join(".", parts);
    }

    protected interface Entry {
        Component name();

        @Nullable
        Component tooltip();
    }

    protected record Section(
            Component name,
            @Nullable Component tooltip,
            Map<String, Entry> entries
    ) implements Entry {
        public Section(Component name, @Nullable Component tooltip) {
            this(name, tooltip, new LinkedHashMap<>());
        }
    }

    protected record Element(
            Component name,
            @Nullable Component tooltip,
            AbstractWidget widget
    ) implements Entry {}

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
