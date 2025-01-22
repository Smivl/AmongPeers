package Game.Interactables;

import Game.GameMap.GameMap;
import Game.Player.Player;
import Game.Player.PlayerInfo;
import Game.Player.PlayerView;
import Server.ClientUpdate;
import org.jspace.Space;

public class Meeting implements Interactable {
    public Space playerSpace;
    public String playerName;

    private final GameMap map;

    public Meeting(GameMap map){
        this.map = map;
    }

    @Override
    public void setPlayerName(String name) {
        this.playerName = name;
    }

    @Override
    public void setPlayerSpace(Space playerSpace) {
        this.playerSpace = playerSpace;
    }

    @Override
    public void interact(Player player) {
        try {
            playerSpace.put(ClientUpdate.MEETING);
            //playerSpace.put(ClientUpdate.MEETING, playerName);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void stopInteraction(Player player) {

    }

    @Override
    public boolean canInteract(PlayerInfo info) {
        return info.isAlive && !map.getSabotageActive();
    }

}
