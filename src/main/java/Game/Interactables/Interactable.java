package Game.Interactables;

import Game.Player.PlayerInfo;

public interface Interactable {
    void interact();
    boolean canInteract(PlayerInfo info);
}
