package Game.Interactables.Sabotage;

import Game.Interactables.Interactable;
import Game.Player.Player;
import Game.Player.PlayerInfo;
import org.jspace.ActualField;
import org.jspace.Space;

public class LightSabotage extends Sabotage {
    @Override
    public void setPlayerName(String name) {

    }

    @Override
    public void setPlayerSpace(Space playerSpace) {

    }

    @Override
    public void interact(Player player) {
        try {
            SabotageView sabotageView = new SabotageView("Fixing lights");
            sabotageView.addProgressBar(6, player::fixLights);
            player.getPlayerView().setCenter(sabotageView);
            player.setInputLocked(true);
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
