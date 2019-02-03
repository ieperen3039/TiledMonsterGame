package NG.ScreenOverlay.Menu;

import NG.DataStructures.Generic.PairList;
import NG.Engine.Game;
import NG.Engine.ModLoader;
import NG.GameState.MapGeneratorMod;
import NG.Mods.Mod;
import NG.ScreenOverlay.Frames.Components.*;
import NG.Tools.Logger;
import org.joml.Vector2i;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Geert van Ieperen. Created on 21-11-2018.
 */
public class NewGameFrame extends SFrame implements Runnable {
    private final SDropDown generatorSelector;
    private final List<Mod> modList;
    private final SDropDown xSizeSelector;
    private final SDropDown ySizeSelector;
    private final PairList<SToggleButton, Mod> toggleList;
    private final ModLoader modLoader;
    private final Game game;
    private final STextArea notice;

    public NewGameFrame(final Game game, final ModLoader loader) {
        super("New Game Frame");
        modLoader = loader;
        modList = modLoader.allMods();
        int nOfMods = modList.size();
        toggleList = new PairList<>(nOfMods);

        final int ROWS = 10;
        final int COLS = 3;
        Vector2i mpos = new Vector2i(1, 0);

        SPanel mainPanel = new SPanel(COLS, ROWS);
        mainPanel.add(new SFiller(100, 100), new Vector2i(0, 0));
        mainPanel.add(new SFiller(100, 100), new Vector2i(COLS - 1, ROWS - 1));

        // message
        notice = new STextArea("Select which mods to load", 50, false);
        mainPanel.add(notice, mpos.add(0, 1));

        // size selection
        SPanel sizeSelection = new SPanel(0, 0, 4, 1, false, false);
        sizeSelection.add(new STextArea("Size", 0, true), new Vector2i(0, 0));
        this.game = game;
        xSizeSelector = new SDropDown(this.game, 100, 60, 1, "100", "200", "500", "1000");
        sizeSelection.add(xSizeSelector, new Vector2i(1, 0));
        sizeSelection.add(new STextArea("X", 0, true), new Vector2i(2, 0));
        ySizeSelector = new SDropDown(this.game, 100, 60, 1, "100", "200", "500", "1000");
        sizeSelection.add(ySizeSelector, new Vector2i(3, 0));
        mainPanel.add(sizeSelection, mpos.add(0, 1));

        // add mod buttons
        SContainer modPanel = new SPanel(1, nOfMods);
        Vector2i pos = new Vector2i(0, -1);
        for (Mod mod : modList) {
            if (mod instanceof MapGeneratorMod) continue;
            SToggleButton button = new SToggleButton(mod.getModName(), MainMenu.BUTTON_MIN_WIDTH, MainMenu.BUTTON_MIN_HEIGHT);
            button.setGrowthPolicy(true, false);
            toggleList.add(button, mod);
            modPanel.add(button, pos.add(0, 1));
        }
        mainPanel.add(modPanel, mpos.add(0, 1));

        // generator selection
        List<String> generatorNames = modList.stream()
                .filter(m -> m instanceof MapGeneratorMod)
                .map(Mod::getModName)
                .collect(Collectors.toList());
        generatorSelector = new SDropDown(this.game, generatorNames);
        mainPanel.add(generatorSelector, mpos.add(0, 1));

        // generate button
        mainPanel.add(new SFiller(0, 50), mpos.add(0, 1));
        SButton generate = new SButton("Generate", MainMenu.BUTTON_MIN_WIDTH, MainMenu.BUTTON_MIN_HEIGHT);
        mainPanel.add(generate, mpos.add(0, 1));

        setMainPanel(mainPanel);
        pack();

        // start game action
        generate.addLeftClickListener(this);
    }

    public void run() {
        try {
            // get and install map generator
            int selected = generatorSelector.getSelectedIndex();
            MapGeneratorMod generatorMod = (MapGeneratorMod) modList.get(selected);

            int xSize = Integer.parseInt(xSizeSelector.getSelected());
            int ySize = Integer.parseInt(ySizeSelector.getSelected());
            generatorMod.setXSize(xSize);
            generatorMod.setYSize(ySize);

            // install selected mods
            List<Mod> targets = new ArrayList<>();
            for (int i = 0; i < toggleList.size(); i++) {
                if (toggleList.left(i).getState()) {
                    Mod mod = toggleList.right(i);

                    if (mod instanceof MapGeneratorMod) {
                        Logger.ASSERT.print("map generator mod found in modlist");

                    } else {
                        targets.add(mod);
                    }
                }
            }

            modLoader.initMods(targets);

            if (targets.isEmpty()) throw new ModLoader.IllegalNumberOfModulesException("No mods selected");
            this.game.map().generateNew(generatorMod);

            // set camera to middle of map
            Vector3f cameraFocus = new Vector3f(xSize / 2f, ySize / 2f, 0);
            Vector3f cameraEye = cameraFocus.add(10, 10, 10, new Vector3f());
            this.game.camera().set(cameraFocus, cameraEye);

            // start
            modLoader.startGame();
            this.setVisible(false);

        } catch (ModLoader.IllegalNumberOfModulesException e) {
            notice.setText(e.getMessage());
            Logger.WARN.print(e);
        }
    }
}
