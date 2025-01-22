package Game.Interactables.Sabotage;

import Game.Interactables.Interactable;
import org.jspace.Space;

public abstract class Sabotage implements Interactable {

    protected Space sabotageSpace;

    public void setSabotageSpace(Space space){
        sabotageSpace = space;
    }
}
