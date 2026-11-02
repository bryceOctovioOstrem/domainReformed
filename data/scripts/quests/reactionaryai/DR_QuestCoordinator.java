package data.scripts.quests.reactionaryai;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.FleetAssignment;
import com.fs.starfarer.api.campaign.PlanetAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.SpecialItemData;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.comm.IntelInfoPlugin;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI.SurveyLevel;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.fleet.FleetMemberType;
import com.fs.starfarer.api.impl.campaign.fleets.FleetFactoryV3;
import com.fs.starfarer.api.impl.campaign.fleets.FleetParamsV3;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.ids.Entities;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.FleetTypes;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Stateless coordinator. Persistent quest state is stored in Sector persistentData,
 * so save games remain the single source of truth.
 */
public final class DR_QuestCoordinator {

    public enum Stage {
        NOT_STARTED,
        FIND_COURIER,
        GO_TO_JUNGLE_WORLD,
        FACILITY_AVAILABLE,
        COMPLETE,
        FAILED
    }

    public enum Outcome {
        NONE,
        REPORTED_INFORMANT,
        AI_KILLED_NO_BLUEPRINT,
        BLUEPRINT_PEACEFUL,
        BLUEPRINT_THEN_KILLED,
        BLUEPRINT_AND_RECRUITED
    }

    private static final String KEY_STAGE = DR_QuestConfig.QUEST_ID + "_stage";
    private static final String KEY_OUTCOME = DR_QuestConfig.QUEST_ID + "_outcome";
    private static final String KEY_TARGET_PLANET = DR_QuestConfig.QUEST_ID + "_targetPlanet";
    private static final String KEY_START_MARKET = DR_QuestConfig.QUEST_ID + "_startMarket";
    private static final String KEY_GUARDIAN_DEFEATED = DR_QuestConfig.QUEST_ID + "_guardianDefeated";
    private static final String KEY_FACILITY_BREACHED = DR_QuestConfig.QUEST_ID + "_facilityBreached";
    private static final String KEY_BLUEPRINT_GIVEN = DR_QuestConfig.QUEST_ID + "_blueprintGiven";

    private DR_QuestCoordinator() {}

    private static Map<String, Object> data() {
        return Global.getSector().getPersistentData();
    }

    public static Stage getStage() {
        Object value = data().get(KEY_STAGE);
        if (value == null) return Stage.NOT_STARTED;
        try {
            return Stage.valueOf(String.valueOf(value));
        } catch (Exception ex) {
            return Stage.NOT_STARTED;
        }
    }

    private static void setStage(Stage stage) {
        data().put(KEY_STAGE, stage.name());
    }

    public static Outcome getOutcome() {
        Object value = data().get(KEY_OUTCOME);
        if (value == null) return Outcome.NONE;
        try {
            return Outcome.valueOf(String.valueOf(value));
        } catch (Exception ex) {
            return Outcome.NONE;
        }
    }

    private static void setOutcome(Outcome outcome) {
        data().put(KEY_OUTCOME, outcome.name());
    }

    public static boolean isStarted() {
        Stage stage = getStage();
        return stage != Stage.NOT_STARTED;
    }

    public static boolean isEnded() {
        Stage stage = getStage();
        return stage == Stage.COMPLETE || stage == Stage.FAILED;
    }

    public static boolean shouldOfferQuest() {
        return !isStarted() && findCandidateJunglePlanet(false) != null;
    }

    public static boolean acceptQuest(MarketAPI startMarket) {
        if (isStarted()) return false;

        PlanetAPI target = findCandidateJunglePlanet(true);
        if (target == null) return false;

        data().put(KEY_TARGET_PLANET, target.getId());
        if (startMarket != null) data().put(KEY_START_MARKET, startMarket.getId());
        data().put(KEY_GUARDIAN_DEFEATED, false);
        data().put(KEY_FACILITY_BREACHED, false);
        data().put(KEY_BLUEPRINT_GIVEN, false);
        setOutcome(Outcome.NONE);

        if (!spawnCourier(target)) return false;

        setStage(Stage.FIND_COURIER);

        if (!Global.getSector().getIntelManager().hasIntelOfClass(DR_QuestIntel.class)) {
            Global.getSector().getIntelManager().addIntel(new DR_QuestIntel(), false);
        }
        return true;
    }

    public static void reportInformant() {
        setOutcome(Outcome.REPORTED_INFORMANT);
        setStage(Stage.FAILED);
    }

    public static PlanetAPI getTargetPlanet() {
        Object id = data().get(KEY_TARGET_PLANET);
        if (id == null) return null;
        SectorEntityToken entity = Global.getSector().getEntityById(String.valueOf(id));
        return entity instanceof PlanetAPI ? (PlanetAPI) entity : null;
    }

    public static MarketAPI getStartMarket() {
        Object id = data().get(KEY_START_MARKET);
        if (id == null) return null;
        return Global.getSector().getEconomy().getMarket(String.valueOf(id));
    }

    public static SectorEntityToken getCourier() {
        return Global.getSector().getEntityById(DR_QuestConfig.COURIER_ENTITY_ID);
    }

    public static SectorEntityToken getFacility() {
        return Global.getSector().getEntityById(DR_QuestConfig.FACILITY_ENTITY_ID);
    }

    public static CampaignFleetAPI getGuardianFleet() {
        Object id = data().get(DR_QuestConfig.GUARDIAN_FLEET_ID_KEY);
        if (id == null) return null;
        SectorEntityToken entity = Global.getSector().getEntityById(String.valueOf(id));
        return entity instanceof CampaignFleetAPI ? (CampaignFleetAPI) entity : null;
    }

    public static boolean isGuardianDefeated() {
        Object value = data().get(KEY_GUARDIAN_DEFEATED);
        return value instanceof Boolean && (Boolean) value;
    }

    private static void setGuardianDefeated(boolean value) {
        data().put(KEY_GUARDIAN_DEFEATED, value);
    }

    public static boolean isFacilityBreached() {
        Object value = data().get(KEY_FACILITY_BREACHED);
        return value instanceof Boolean && (Boolean) value;
    }

    public static void setFacilityBreached(boolean value) {
        data().put(KEY_FACILITY_BREACHED, value);
    }

    public static boolean isBlueprintGiven() {
        Object value = data().get(KEY_BLUEPRINT_GIVEN);
        return value instanceof Boolean && (Boolean) value;
    }

    private static void setBlueprintGiven(boolean value) {
        data().put(KEY_BLUEPRINT_GIVEN, value);
    }

    public static void recoverCourierData() {
        if (getStage() != Stage.FIND_COURIER) return;
        setStage(Stage.GO_TO_JUNGLE_WORLD);
        spawnGuardianFleetIfNeeded();
    }

    public static void updateQuestWorldState() {
        Stage stage = getStage();
        if (stage != Stage.GO_TO_JUNGLE_WORLD && stage != Stage.FACILITY_AVAILABLE) return;

        if (stage == Stage.GO_TO_JUNGLE_WORLD) {
            spawnGuardianFleetIfNeeded();

            CampaignFleetAPI guardian = getGuardianFleet();
            if (!isGuardianDefeated()) {
                if (guardian == null || !guardian.isAlive() || guardian.isEmpty()) {
                    setGuardianDefeated(true);
                }
            }

            PlanetAPI target = getTargetPlanet();
            if (target != null && target.getMarket() != null && isGuardianDefeated()
                    && target.getMarket().getSurveyLevel() == SurveyLevel.FULL) {
                spawnFacilityIfNeeded();
                setStage(Stage.FACILITY_AVAILABLE);
            }
        } else {
            spawnFacilityIfNeeded();
        }
    }

    public static void giveBlueprint() {
        if (isBlueprintGiven()) return;

        String itemId = DR_QuestConfig.BLUEPRINT_ITEM_ID;
        if (itemId != null && !itemId.startsWith("PLACEHOLDER_")) {
            Global.getSector().getPlayerFleet().getCargo().addSpecial(
                    new SpecialItemData(itemId, null), 1f
            );
        }
        setBlueprintGiven(true);
    }

    public static void recruitAI() {
        // Mechanically this is a normal Alpha Core. If you later create a custom named
        // core item/officer, replace this one line with your custom reward logic.
        Global.getSector().getPlayerFleet().getCargo().addCommodity(Commodities.ALPHA_CORE, 1f);
        finish(Outcome.BLUEPRINT_AND_RECRUITED);
    }

    public static void finish(Outcome outcome) {
        setOutcome(outcome);
        setStage(Stage.COMPLETE);
    }

    public static String getObjectiveText() {
        Stage stage = getStage();
        switch (stage) {
            case FIND_COURIER:
                return "Locate the abandoned courier and recover its navigation logs.";
            case GO_TO_JUNGLE_WORLD:
                if (!isGuardianDefeated()) {
                    return "Travel to the jungle world and defeat the automated defense fleet.";
                }
                PlanetAPI target = getTargetPlanet();
                if (target != null && target.getMarket() != null
                        && target.getMarket().getSurveyLevel() != SurveyLevel.FULL) {
                    return "Survey the jungle world.";
                }
                return "Investigate the fortified facility revealed by the survey.";
            case FACILITY_AVAILABLE:
                return "Investigate the fortified terraforming facility.";
            case COMPLETE:
                return "Completed.";
            case FAILED:
                return "Ended.";
            default:
                return "";
        }
    }

    private static PlanetAPI findCandidateJunglePlanet(boolean randomize) {
        List<PlanetAPI> candidates = new ArrayList<PlanetAPI>();

        for (StarSystemAPI system : Global.getSector().getStarSystems()) {
            for (PlanetAPI planet : system.getPlanets()) {
                if (planet.getSpec() == null) continue;
                if (!"jungle".equals(planet.getSpec().getPlanetType())) continue;
                if (planet.getMarket() == null) continue;
                if (!planet.getMarket().isPlanetConditionMarketOnly()) continue;
                if (planet.getMarket().getSurveyLevel() == SurveyLevel.FULL) continue;
                candidates.add(planet);
            }
        }

        if (candidates.isEmpty()) return null;
        if (!randomize) return candidates.get(0);

        Collections.shuffle(candidates, new Random());
        return candidates.get(0);
    }

    private static boolean spawnCourier(PlanetAPI target) {
        if (getCourier() != null) return true;

        List<StarSystemAPI> systems = new ArrayList<StarSystemAPI>();
        for (StarSystemAPI system : Global.getSector().getStarSystems()) {
            if (system == target.getStarSystem()) continue;
            if (system.getStar() == null) continue;
            systems.add(system);
        }
        if (systems.isEmpty()) return false;

        Collections.shuffle(systems, new Random());
        StarSystemAPI system = systems.get(0);

        SectorEntityToken courier = system.addCustomEntity(
                DR_QuestConfig.COURIER_ENTITY_ID,
                "Abandoned Courier",
                Entities.DERELICT_SURVEY_SHIP,
                Factions.NEUTRAL
        );

        float angle = (float) (Math.random() * 360f);
        float radius = 4500f + (float) Math.random() * 3500f;
        float days = 180f + (float) Math.random() * 180f;
        courier.setCircularOrbitPointingDown(system.getStar(), angle, radius, days);
        courier.setDiscoverable(true);
        return true;
    }

    public static void spawnGuardianFleetIfNeeded() {
        if (isGuardianDefeated()) return;
        CampaignFleetAPI existing = getGuardianFleet();
        if (existing != null && existing.isAlive()) return;

        PlanetAPI target = getTargetPlanet();
        if (target == null) return;

        FleetParamsV3 params = new FleetParamsV3(
                null,
                target.getLocationInHyperspace(),
                DR_QuestConfig.FLEET_GENERATION_FACTION,
                1f,
                FleetTypes.PATROL_LARGE,
                DR_QuestConfig.GUARDIAN_COMBAT_FP,
                0f,
                0f,
                0f,
                0f,
                0f,
                0f
        );
        params.maxNumShips = 30;
        params.officerNumberMult = 1.25f;
        params.officerLevelBonus = 1;

        CampaignFleetAPI fleet = FleetFactoryV3.createFleet(params);
        if (fleet == null || fleet.isEmpty()) return;

        fleet.setName("Automated Defense Fleet");
        fleet.setNoAutoDespawn(true);
        fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_MAKE_HOSTILE, true);
        fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_MAKE_AGGRESSIVE, true);

        injectUniqueShip(fleet);
        fleet.forceSync();

        target.getContainingLocation().spawnFleet(target, 1200f, 0f, fleet);
        fleet.clearAssignments();
        fleet.addAssignment(FleetAssignment.DEFEND_LOCATION, target, 999999f, "guarding " + target.getName());

        data().put(DR_QuestConfig.GUARDIAN_FLEET_ID_KEY, fleet.getId());
    }

    private static void injectUniqueShip(CampaignFleetAPI fleet) {
        String variantId = DR_QuestConfig.UNIQUE_SHIP_VARIANT_ID;
        if (variantId == null || variantId.startsWith("PLACEHOLDER_")) return;

        try {
            // The normal defense fleet has already been generated at this point.
            // This adds exactly one copy of the quest ship without changing the
            // faction's ordinary fleet-composition rules.
            FleetMemberAPI member = Global.getFactory().createFleetMember(FleetMemberType.SHIP, variantId);
            if (member != null) {
                fleet.getFleetData().addFleetMember(member);
            }
        } catch (RuntimeException ignored) {
            // A missing/bad custom variant should not make the rest of the quest unplayable.
        }
    }

    private static void spawnFacilityIfNeeded() {
        if (getFacility() != null) return;

        PlanetAPI target = getTargetPlanet();
        if (target == null) return;

        SectorEntityToken facility = target.getContainingLocation().addCustomEntity(
                DR_QuestConfig.FACILITY_ENTITY_ID,
                "Fortified Terraforming Facility",
                Entities.STATION_RESEARCH_REMNANT,
                Factions.NEUTRAL
        );
        facility.setCircularOrbitPointingDown(target, 35f, target.getRadius() + 500f, 35f);
        facility.setDiscoverable(true);
    }

    public static DR_QuestIntel getIntel() {
        IntelInfoPlugin intel = Global.getSector().getIntelManager().getFirstIntel(DR_QuestIntel.class);
        return intel instanceof DR_QuestIntel ? (DR_QuestIntel) intel : null;
    }
}
