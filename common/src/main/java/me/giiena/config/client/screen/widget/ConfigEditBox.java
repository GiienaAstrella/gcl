package me.giiena.config.client.screen.widget;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.NonNull;

import java.util.function.Predicate;

public class ConfigEditBox extends EditBox {
    private Predicate<String> filter;

    @SuppressWarnings("unused")
    public ConfigEditBox(Font font, Component narration) {
        this(font, 250, 20, narration);
    }

    public ConfigEditBox(Font font, int width, int height, Component narration) {
        super(font, 0, 0, width, height, narration);
        this.filter = _ -> true;
    }

    public void setFilter(Predicate<String> filter) {
        this.filter = filter;
    }

    @Override
    public void insertText(@NonNull String input) {
        if (!this.filter.test(input)) {
            return;
        }
        super.insertText(input);
    }
}
