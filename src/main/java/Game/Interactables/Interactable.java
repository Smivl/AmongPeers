package Game.Interactables;

import Game.Player.PlayerInfo;
import org.jspace.Space;

public interface Interactable {

    void setPlayerName(String name);
    void setPlayerSpace(Space playerSpace);
    void interact();
    boolean canInteract(PlayerInfo info);
}
