package NG.GUIMenu.Menu;

import NG.Camera.Camera;
import NG.Core.Game;
import NG.DataStructures.Generic.PairList;
import NG.GUIMenu.Components.*;
import NG.GUIMenu.HUD.HUDManager;
import NG.GameMap.GameMap;
import NG.GameMap.MapGeneratorMod;
import NG.GameMap.SimpleMapGenerator;
import NG.GameMap.TileThemeSet;
import NG.Mods.Mod;
import NG.Mods.ModLoader;
import NG.Rendering.Pointer;
import NG.Tools.Toolbox;
import org.joml.Vector2i;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Geert van Ieperen. Created on 21-11-2018.
 */
public class NewGameFrame extends SFrame {
    private final SDropDown xSizeSelector;
    private final SDropDown ySizeSelector;
    private final PairList<SToggleButton, Mod> toggleList;
    private final ModLoader modLoader;
    private final Game game;
    private final STextArea notice;

    public NewGameFrame(final Game game, final ModLoader loader) {
        super("New Game Frame");
        modLoader = loader;
        List<Mod> modList = modLoader.allMods();
        int nOfMods = modList.size();
        toggleList = new PairList<>(nOfMods);

        final int ROWS = 10;
        final int COLS = 3;
        Vector2i mpos = new Vector2i(1, 0);

        SPanel mainPanel = new SPanel(COLS, ROWS);
        mainPanel.add(new SFiller(20, 20), new Vector2i(0, 0));
        mainPanel.add(new SFiller(20, 20), new Vector2i(COLS - 1, ROWS - 1));

        // message
        notice = new STextArea("Select which mods to load", 50);
        mainPanel.add(notice, mpos.add(0, 1));

        // size selection
        SPanel sizeSelection = new SPanel(0, 0, 4, 1, false, false);
        sizeSelection.add(new STextArea("Size", 0), new Vector2i(0, 0));
        this.game = game;
        xSizeSelector = new SDropDown(game.get(HUDManager.class), 100, 60, 1, "100", "200", "500", "1000");
        sizeSelection.add(xSizeSelector, new Vector2i(1, 0));
        sizeSelection.add(new STextArea("X", 0), new Vector2i(2, 0));
        ySizeSelector = new SDropDown(game.get(HUDManager.class), 100, 60, 1, "100", "200", "500", "1000");
        sizeSelection.add(ySizeSelector, new Vector2i(3, 0));
        mainPanel.add(sizeSelection, mpos.add(0, 1));

        if (nOfMods > 0) {
            // add mod buttons
            SContainer modPanel = new SPanel(1, nOfMods);
            Vector2i pos = new Vector2i(0, -1);
            for (Mod mod : modList) {
                if (mod instanceof MapGeneratorMod) continue;
                SToggleButton button = new SToggleButton(mod.getModName());
                button.setGrowthPolicy(true, false);
                toggleList.add(button, mod);
                modPanel.add(button, pos.add(0, 1));
            }
            mainPanel.add(modPanel, mpos.add(0, 1));
        }

        // generate button
        mainPanel.add(new SFiller(0, 50), mpos.add(0, 1));
        SButton generate = new SButton("Generate");
        mainPanel.add(generate, mpos.add(0, 1));

        setMainPanel(mainPanel);
        pack();

        // start game action
        generate.addLeftClickListener(this::generate);
    }

    private void generate() {
        int seed = Math.abs(Toolbox.random.nextInt());
        notice.setText("Generating new terrain with seed " + seed);

        SimpleMapGenerator generator = new SimpleMapGenerator(seed);

        // initialize generator
        generator.init(game);
        int xSize = Integer.parseInt(xSizeSelector.getSelected());
        int ySize = Integer.parseInt(ySizeSelector.getSelected());
        generator.setXSize(xSize);
        generator.setYSize(ySize);

        // install selected mods
        List<Mod> targets = new ArrayList<>();
        for (int i = 0; i < toggleList.size(); i++) {
            if (toggleList.left(i).getState()) {
                Mod mod = toggleList.right(i);
                targets.add(mod);
            }
        }
        modLoader.initMods(targets);

        // generate map
        TileThemeSet.BASE.load();
        GameMap map = game.get(GameMap.class);
        map.generateNew(generator);

        // set visual elements
        MainMenu.centerCamera(game.get(Camera.class), map);
        game.get(Pointer.class).setVisible(true);

        // start game
        modLoader.startGame();
        this.setVisible(false);
    }
}
