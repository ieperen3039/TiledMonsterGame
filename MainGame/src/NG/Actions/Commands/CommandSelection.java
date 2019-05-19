package NG.Actions.Commands;

import NG.GUIMenu.Components.SButton;
import NG.GUIMenu.Components.SComponent;
import NG.GUIMenu.Components.SPanel;
import NG.Living.Living;
import org.joml.Vector2i;
import org.joml.Vector2ic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Geert van Ieperen created on 10-5-2019.
 */
public class CommandSelection {
    private Vector2ic position;
    private Living source;

    private List<CommandProvider> providers = new ArrayList<>();
    private Living controller;

    /**
     * @param position   position where the command is targeted to.
     * @param source     the Living that is planning to create a command (the player)
     * @param controller the living that is going to receive this command
     * @param providers  the possible commands that may be chosen
     */
    public CommandSelection(Vector2ic position, Living source, Living controller, CommandProvider... providers) {
        this.position = position;
        this.source = source;
        this.controller = controller;
        this.providers.addAll(Arrays.asList(providers));
    }

    /**
     * adds another possible command to this selection
     */
    public void addProvider(CommandProvider elt) {
        providers.add(elt);
    }

    /**
     * adds another command to this selection. Accepts lambda functions.
     */
    public void addProvider(String name, CommandCreator elt) {
        providers.add(new CommandProvider(name) {
            @Override
            public Command create(Living source, Living receiver, Vector2ic target) {
                return elt.create(source, receiver, target);
            }
        });
    }

    /**
     * @return a list of the names of commands that can be chosen. Only these can be accepted by {@link
     * #activate(String)}
     */
    public List<String> commands() {
        List<String> list = new ArrayList<>(providers.size());
        for (CommandProvider cp : providers) {
            list.add(cp.name);
        }
        return list;
    }

    /**
     * activates a command by its name field.
     * @param target one of the names returned by {@link #commands()}
     * @return true iff the commands was found and is executed
     */
    public boolean activate(String target) {
        for (CommandProvider provider : providers) {
            if (provider.name.equals(target)) {
                accept(provider);
                return true;
            }
        }

        return false;
    }

    public SComponent asComponent() {
        SPanel panel = new SPanel(1, providers.size());

        Vector2i pos = new Vector2i(0, -1);
        for (CommandProvider provider : providers) {
            panel.add(
                    new SButton(provider.name, () -> accept(provider), 200, 60),
                    pos.add(0, 1)
            );
        }

        return panel;
    }

    private void accept(CommandProvider provider) {
        Command command = provider.create(source, controller, position);
        if (command == null) return;
        controller.accept(command);
    }

    public static abstract class CommandProvider implements CommandCreator {
        public final String name;

        public CommandProvider(String name) {
            this.name = name;
        }
    }

    private interface CommandCreator {
        Command create(Living source, Living receiver, Vector2ic target);
    }
}
