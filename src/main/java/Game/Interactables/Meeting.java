package Game.Interactables;

import Game.Player.PlayerInfo;

public class Meeting implements Interactable {
    @Override
    public void interact() {
        System.out.println("Meeting Called!");
    }

    @Override
    public boolean canInteract(PlayerInfo info) {
        return info.isAlive;
    }
}
