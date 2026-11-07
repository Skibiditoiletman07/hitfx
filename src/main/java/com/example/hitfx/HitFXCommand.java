package com.example.hitfx;

import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.minecraft.text.Text;

/** /hitfx theme <violet|gold>   and   /hitfx <feature> <on|off> */
public final class HitFXCommand {
    public static void register() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            var root = ClientCommandManager.literal("hitfx");

            var theme = ClientCommandManager.literal("theme");
            for (Theme t : Theme.values()) {
                theme.then(ClientCommandManager.literal(t.name().toLowerCase()).executes(c -> {
                    Config.setTheme(t);
                    c.getSource().sendFeedback(Text.literal("HitFX theme: " + t.name().toLowerCase()));
                    return 1;
                }));
            }
            root.then(theme);

            for (Config.Feature f : Config.Feature.values()) {
                String name = f.name().toLowerCase();
                root.then(ClientCommandManager.literal(name)
                        .then(ClientCommandManager.literal("on").executes(c -> {
                            Config.set(f, true);
                            c.getSource().sendFeedback(Text.literal(name + ": on"));
                            return 1;
                        }))
                        .then(ClientCommandManager.literal("off").executes(c -> {
                            Config.set(f, false);
                            c.getSource().sendFeedback(Text.literal(name + ": off"));
                            return 1;
                        })));
            }
            dispatcher.register(root);
        });
    }
}
