package NG.InputHandling.MouseTools;

import NG.Actions.Commands.Command;
import NG.Actions.EntityAction;
import NG.Core.Game;
import NG.Living.Living;
import org.joml.Vector2ic;
import org.joml.Vector3fc;

/**
 * @author Geert van Ieperen created on 19-2-2020.
 */
public abstract class CommandProvider {
    public final String name;

    public CommandProvider(String name) {
        this.name = name;
    }

    public abstract Command create(Living receiver, Vector2ic target);

    /**
     * @return an action command. Takes a lambda function for creating an action, and generates commands that try to
     * execute the given action exactly once..
     */
    public static CommandProvider actionCommand(String name, ActionCreator action) {
        return new CommandProvider(name) {
            @Override
            public Command create(Living receiver, Vector2ic target) {
                return new Command(receiver) {
                    boolean hasFired = false;

                    @Override
                    public EntityAction getAction(Game game, Vector3fc startPosition, float gameTime) {
                        if (hasFired) return null;
                        hasFired = true;

                        return action.create(game, startPosition, target);
                    }

                    @Override
                    public String toString() {
                        return "Command " + name;
                    }
                };
            }
        };
    }

    public interface ActionCreator {
        EntityAction create(Game game, Vector3fc startPosition, Vector2ic target);
    }
}
