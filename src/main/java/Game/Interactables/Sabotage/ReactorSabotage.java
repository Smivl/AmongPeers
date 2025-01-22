package Game.Interactables.Sabotage;

import Game.Interactables.Interactable;
import Game.Player.Player;
import Game.Player.PlayerInfo;
import org.jspace.ActualField;
import org.jspace.Space;

import java.util.Arrays;

public class ReactorSabotage extends Sabotage {

    private String reactorNumber;

    public ReactorSabotage(String reactor){
        this.reactorNumber = reactor;
    }

    @Override
    public void setPlayerName(String name) {

    }

    @Override
    public void setPlayerSpace(Space playerSpace) {

    }

    @Override
    public void interact(Player player) {
        try {
            sabotageSpace.get(new ActualField("lock"));
            sabotageSpace.put(reactorNumber);
            sabotageSpace.put("lock");
            player.getPlayerView().setCenter(new SabotageView("Fixing reactor (needs 2 players)"));
            player.setInputLocked(true);
        }catch (Exception e){
            System.out.println(Arrays.toString(e.getStackTrace()));
        }
    }

    @Override
    public void stopInteraction(Player player) {
        try {
            sabotageSpace.get(new ActualField("lock"));
            sabotageSpace.getp(new ActualField(reactorNumber));
            sabotageSpace.put("lock");
            player.getPlayerView().setCenter(null);
            player.setInputLocked(false);
        }catch (Exception e){
            System.out.println(e.getStackTrace());
        }
    }

    @Override
    public boolean canInteract(PlayerInfo info) {
        return info.isAlive;
    }

}
