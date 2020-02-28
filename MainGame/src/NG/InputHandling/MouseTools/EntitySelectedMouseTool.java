package NG.InputHandling.MouseTools;

import NG.Actions.Commands.Command;
import NG.Core.Game;
import NG.Core.GameTimer;
import NG.Entities.Entity;
import NG.Entities.MonsterEntity;
import NG.GUIMenu.HUD.MonsterHud;
import NG.GameMap.GameMap;
import NG.Living.MonsterSoul;
import NG.Rendering.Pointer;
import NG.Tools.Logger;
import NG.Tools.Vectors;
import org.joml.Vector2i;
import org.joml.Vector2ic;
import org.joml.Vector3fc;
import org.lwjgl.glfw.GLFW;

/**
 * @author Geert van Ieperen created on 19-2-2020.
 */
public class EntitySelectedMouseTool extends DefaultMouseTool {
    private final MonsterSoul receiver;
    private CommandProvider selectedCommand;

    public EntitySelectedMouseTool(Game game, MonsterEntity entity) {
        super(game);
        Logger.DEBUG.print("Selected " + entity);
        this.receiver = entity.getController();

        MonsterHud hud = game.get(MonsterHud.class);
        hud.setSelectedEntity(entity, this);

        entity.markAs(MonsterEntity.Mark.SELECTED);
    }

    @Override
    public void apply(Entity entity, int xSc, int ySc) {
        float now = game.get(GameTimer.class).getGametime();
        apply(entity.getPositionAt(now), xSc, ySc);
    }

    @Override
    public void apply(Vector3fc position, int xSc, int ySc) {
        if (getButton() == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
            deselect();
            return;
        }

        Logger.DEBUG.print("Clicked at " + Vectors.toString(position) + " with " + receiver + " selected");

        Vector2i coordinate = game.get(GameMap.class).getCoordinate(position);
        accept(selectedCommand, coordinate);
    }

    private void deselect() {
        game.get(Pointer.class).setSelection(null);
        game.get(MouseToolCallbacks.class).setMouseTool(null);
        receiver.entity().markAs(MonsterEntity.Mark.OWNED);
    }

    private void accept(CommandProvider provider, Vector2ic targetPosition) {
        Command command = provider.create(receiver, targetPosition);
        if (command == null) return;

        receiver.mind().queueCommand(game, command);
    }

    public void selectCommand(CommandProvider command) {
        selectedCommand = command;
    }
}
