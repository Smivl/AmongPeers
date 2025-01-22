package Game.Interactables.Sabotage;

public enum SabotageType {
    NUCLEAR_MELTDOWN("Reactor Meltdown"),
    OXYGEN_DEPLETED("Oxygen Depleted"),
    LIGHTS("Fix Lights");

    private final String displayName;

    SabotageType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName(){
        return displayName;
    }
}
