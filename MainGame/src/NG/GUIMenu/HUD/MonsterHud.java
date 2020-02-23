package NG.GUIMenu.HUD;

import NG.Camera.Camera;
import NG.Core.Game;
import NG.Entities.Entity;
import NG.Entities.MonsterEntity;
import NG.GUIMenu.Components.*;
import NG.GUIMenu.Rendering.BaseLF;
import NG.GUIMenu.Rendering.GUIPainter;
import NG.GUIMenu.Rendering.NGFonts;
import NG.GUIMenu.Rendering.SFrameLookAndFeel;
import NG.GameMap.GameMap;
import NG.InputHandling.MouseToolCallbacks;
import NG.InputHandling.MouseTools.CommandProvider;
import NG.InputHandling.MouseTools.EntitySelectedMouseTool;
import NG.InputHandling.MouseTools.MouseTool;
import NG.Living.MonsterSoul;
import NG.Living.Player;
import NG.Rendering.Textures.GenericTextures;
import org.joml.Vector2i;
import org.joml.Vector2ic;

import java.util.*;

/**
 * @author Geert van Ieperen created on 15-7-2019.
 */
public class MonsterHud extends SimpleHUD {
    private static final int UI_INFO_BAR_SIZE = 350;
    private static final int TEXT_BOX_HEIGHT = 200;

    private MiniMap minimap;
    private SComponentArea bottomBox;
    private SScrollableList teamSelection;
    private final Deque<SComponent> freeFloatingElements = new ArrayDeque<>();
    private SComponent modalComponent = null;

    private Map<MonsterSoul, SPanel> teamPanels = new HashMap<>();

    public MonsterHud() {
        super(new BaseLF());
    }

    @Override
    public void init(Game game) throws Exception {
        if (this.game != null) return;
        super.init(game);

        minimap = new MiniMap(game, UI_INFO_BAR_SIZE, UI_INFO_BAR_SIZE);

        bottomBox = new SComponentArea(0, TEXT_BOX_HEIGHT);
        bottomBox.setGrowthPolicy(true, false);
        teamSelection = new SScrollableList(5);
        teamSelection.setGrowthPolicy(false, true);

        display(
                SContainer.row(
                        SContainer.column(new SFiller(), bottomBox)
                                .setGrowthPolicy(true, true),
                        new SPanel(SContainer.column(
                                minimap, teamSelection
                        )).setGrowthPolicy(false, true)
                ).setGrowthPolicy(true, true)
        );
    }

    @Override
    public void draw(GUIPainter painter) {
        for (MonsterSoul monster : game.get(Player.class).getTeam()) {
            SPanel panel = teamPanels.get(monster);
            if (panel == null) {
                panel = getMonsterPanel(monster);
                teamSelection.add(panel, null);
                teamPanels.put(monster, panel);
            }
        }

        Vector2i focusCoord = game.get(GameMap.class).getCoordinate(game.get(Camera.class).getFocus());
        minimap.setFocus(focusCoord);

        super.draw(painter);

        Iterator<SComponent> itr = freeFloatingElements.descendingIterator();
        while (itr.hasNext()) {
            final SComponent f = itr.next();

            if (f.isVisible()) {
                f.validateLayout();
                f.draw(lookAndFeel, f.getPosition());
            }
        }

        if (modalComponent != null) modalComponent.draw(lookAndFeel, modalComponent.getScreenPosition());
    }

    @Override
    public void setModalListener(SComponent listener) {
        listener.setParent(null);
        modalComponent = listener;
    }

    @Override
    public void addElement(SComponent component) {
        int x = 50;
        int y = 50;

        // reposition frame not to overlap other frames (greedy)
        for (Iterator<SComponent> iterator = freeFloatingElements.descendingIterator(); iterator.hasNext(); ) {
            SComponent other = iterator.next();

            if (other.equals(component)) {
                iterator.remove();
                continue;
            }

            if (!other.isVisible()) continue;

            Vector2ic otherPos = other.getScreenPosition();
            if (otherPos.x() == x && otherPos.y() == y) {
                x += 20;
                y += 20; // MS windows-style
            }
        }

        component.setPosition(x, y);
        component.setParent(null);
        freeFloatingElements.offerFirst(component);
    }

    @Override
    public boolean removeElement(SComponent component) {
        return freeFloatingElements.remove(component);
    }

    @Override
    public SComponent getComponentAt(int xSc, int ySc) {
        if (modalComponent != null && modalComponent.contains(xSc, ySc)) return modalComponent;

        for (SComponent elt : freeFloatingElements) {
            if (elt.isVisible() && elt.contains(xSc, ySc)) {
                int xr = xSc - elt.getX();
                int yr = ySc - elt.getY();

                // focus
                if (!elt.equals(freeFloatingElements.peekFirst())) {
                    freeFloatingElements.remove(elt);
                    freeFloatingElements.addFirst(elt);
                }

                return elt.getComponentAt(xr, yr);
            }
        }

        return super.getComponentAt(xSc, ySc);
    }

    @Override
    public boolean checkMouseClick(MouseTool tool, int xSc, int ySc) {
        if (modalComponent != null) {
            if (modalComponent.contains(xSc, ySc)) {
                int xr = xSc - modalComponent.getX();
                int yr = ySc - modalComponent.getY();
                SComponent component = modalComponent.getComponentAt(xr, yr);

                if (component != null) {
                    tool.apply(component, xSc, ySc);
                }
            }

            modalComponent = null;
            return true;
        }

        for (SComponent elt : freeFloatingElements) {
            if (elt.isVisible() && elt.contains(xSc, ySc)) {
                int xr = xSc - elt.getX();
                int yr = ySc - elt.getY();
                SComponent component = elt.getComponentAt(xr, yr);

                if (component != null) {
                    tool.apply(component, xSc, ySc);
                    return true;
                }
            }
        }

        return super.checkMouseClick(tool, xSc, ySc);
    }

    @Override
    public boolean covers(int xSc, int ySc) {
        if (modalComponent != null && modalComponent.contains(xSc, ySc)) return true;

        for (SComponent elt : freeFloatingElements) {
            if (elt.isVisible() && elt.contains(xSc, ySc)) return true;
        }

        return super.covers(xSc, ySc);
    }

    @Override
    public void cleanup() {
        freeFloatingElements.clear();
        super.cleanup();
    }

    private SPanel getMonsterPanel(MonsterSoul soul) {
        SProgressBar healthBar = new SProgressBar(0, 20, () -> ((float) soul.getHitpoints() / soul.props.hitPoints));
        SExtendedTextArea name = new SExtendedTextArea(soul.toString(), 50, true);
        name.setClickListener(
                (button, x, y) -> game.get(MouseToolCallbacks.class)
                        .getDefaultMouseTool()
                        .apply(soul.entity(), x, y)
        );

        return new SPanel(SContainer.row(
                new STexturedPanel(GenericTextures.CHECKER, 100, 100),
                SContainer.column(
                        name,
                        healthBar
                )
        ));
    }

    /**
     * Sets the current selected entity to the given entity, which may be null in case of deselection
     */
    public void setSelectedEntity(
            Entity entity, EntitySelectedMouseTool mouseTool
    ) {
        if (entity instanceof MonsterEntity) {
            MonsterEntity monster = (MonsterEntity) entity;
            MonsterSoul soul = monster.getController();
            List<CommandProvider> acceptedCommands = soul.mind().getAcceptedCommands();
            String[] commandNames = acceptedCommands.stream()
                    .map(c -> c.name)
                    .toArray(String[]::new);

            int bbSize = bottomBox.getHeight() - 20;

            bottomBox.show(
                    new SPanel(SContainer.row(
                            // entity image
                            new STexturedPanel(GenericTextures.CHECKER, bbSize, bbSize),
                            SContainer.column(
                                    SContainer.row(
                                            new STextArea( // entity name
                                                    soul.toString(),
                                                    50, 0, true,
                                                    NGFonts.TextType.TITLE, SFrameLookAndFeel.Alignment.CENTER
                                            )
                                    ),
                                    SContainer.row( // health bar
                                            new SNamedValue("HP", soul::getHitpoints, 20)
                                                    .setGrowthPolicy(false, false),
                                            new SProgressBar(200, 20, () -> ((float) soul.getHitpoints() / soul.props.hitPoints))
                                                    .setGrowthPolicy(true, false)
                                    ),
                                    // commands
                                    new SExclusiveButtonRow(true, commandNames)
                                            .addSelectionListener((i) -> mouseTool.selectCommand(acceptedCommands.get(i)))
                                            .setGrowthPolicy(false, false)
                            ).setGrowthPolicy(true, true)
                    ))
            );

        } else if (entity != null) {
            bottomBox.show(
                    SContainer.column(
                            new STextArea(entity.toString(), 50)
                    ).setGrowthPolicy(true, true)
            );

        } else {
            bottomBox.hide();
        }
    }
}
