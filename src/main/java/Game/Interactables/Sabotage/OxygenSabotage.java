package Game.Interactables.Sabotage;

import Game.Interactables.Interactable;
import Game.Player.Player;
import Game.Player.PlayerInfo;
import org.jspace.ActualField;
import org.jspace.Space;

import java.util.Arrays;

public class OxygenSabotage extends Sabotage {

    private String pNumber;

    public OxygenSabotage(String pNumber){
        this.pNumber = pNumber;
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
            SabotageView sabotageView = new SabotageView("Entering code...");
            sabotageView.addProgressBar(4, () -> finishInteraction(player));
            player.getPlayerView().setCenter(sabotageView);
            player.setInputLocked(true);
        }catch (Exception e){
            System.out.println(Arrays.toString(e.getStackTrace()));
        }
    }

    public void finishInteraction(Player player){
        try {
            sabotageSpace.put(pNumber);
            player.getPlayerView().setCenter(null);
            player.setInputLocked(false);
        }catch (Exception e){
            System.out.println(e.getStackTrace());
        }
    }

    @Override
    public void stopInteraction(Player player) {
    }


    @Override
    public boolean canInteract(PlayerInfo info) {
        return info.isAlive;
    }

}
