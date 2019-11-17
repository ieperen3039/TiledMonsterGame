package NG.Mods;

import NG.Core.Game;

/**
 * A specialisation of Mods that are loaded upon starting the game. The {@link Mod#init(Game)} method will be called
 * before the main menu is shown to the player.
 * @author Geert van Ieperen created on 25-1-2019.
 */
public interface InitialisationMod extends Mod {
}
