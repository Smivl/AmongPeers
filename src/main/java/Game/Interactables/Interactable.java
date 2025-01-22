package Game.Interactables;

import Game.Player.Player;
import Game.Player.PlayerInfo;
import Game.Player.PlayerView;
import org.jspace.Space;

public interface Interactable {

    void setPlayerName(String name);
    void setPlayerSpace(Space playerSpace);
    void interact(Player player);
    void stopInteraction(Player player);
    boolean canInteract(PlayerInfo info);
}
