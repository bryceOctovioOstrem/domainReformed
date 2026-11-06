package data.scripts.quests.reactionaryai;

/**
 * All values you are most likely to tune for the quest are kept here.
 */
public final class DR_QuestConfig {

    private DR_QuestConfig() {}

    public static final String QUEST_ID = "reactionary_engineer_ai_quest";

    // Replace this when your custom quest ship is ready.
    // This is the exact .variant id injected into the guardian fleet.
    public static final String UNIQUE_SHIP_VARIANT_ID = "DR_effulgence_prime";

    // You said you will create the blueprint item yourself. Put the id from
    // data/campaign/special_items.csv here. The quest awards one copy of it.
    public static final String BLUEPRINT_ITEM_ID = "EFF_package";

    // The large defense fleet is generated normally from this faction's .faction file,
    // and then the single custom ship above is injected afterward.
    public static final String FLEET_GENERATION_FACTION = "remnant";
    public static final float GUARDIAN_COMBAT_FP = 450f;

    public static final int INFORMANT_PRICE = 200;
    public static final int REPORT_REWARD = 500;

    // This is intentionally a simple quest raid rather than a full vanilla raid screen.
    public static final int RAID_MARINES_REQUIRED = 100;
    public static final int RAID_MARINE_LOSSES = 15;

    // Fixed quest entity ids; the quest is one-time, so they are safe and convenient.
    public static final String COURIER_ENTITY_ID = QUEST_ID + "_courier";
    public static final String GUARDIAN_FLEET_ID_KEY = QUEST_ID + "_guardianFleetId";
    public static final String FACILITY_ENTITY_ID = QUEST_ID + "_facility";
}
