package data.scripts.campaign.crisis; 
// Declares the package this class belongs to. 
// MUST match the folder path: data/scripts/campaign/crisis/

import java.util.ArrayList; 
// Used to store active blockade fleets.

import java.util.Iterator; 
// Used for safe removal from lists while iterating.

import java.util.List; 
// Interface for our fleet list (using RAW List to avoid Janino generic bugs).

import com.fs.starfarer.api.EveryFrameScript; 
// Interface that lets this script run every campaign frame.

import com.fs.starfarer.api.Global; 
// Gives access to the Sector and game-wide utilities.

import com.fs.starfarer.api.campaign.CampaignFleetAPI; 
// Represents campaign-layer fleets.

import com.fs.starfarer.api.campaign.LocationAPI; 
// Represents a campaign location (system or hyperspace).

import com.fs.starfarer.api.campaign.SectorEntityToken; 
// Base entity type for campaign objects.

import com.fs.starfarer.api.campaign.StarSystemAPI; 
// Represents a star system.

import com.fs.starfarer.api.impl.campaign.fleets.FleetFactoryV3; 
// Used to generate fleets from parameters.

import com.fs.starfarer.api.impl.campaign.fleets.FleetParamsV3; 
// Defines fleet composition parameters.

import com.fs.starfarer.api.impl.campaign.ids.Factions; 
// Contains vanilla faction IDs.

import com.fs.starfarer.api.impl.campaign.ids.FleetTypes; 
// Contains fleet type IDs like TASK_FORCE.

import com.fs.starfarer.api.impl.campaign.ids.MemFlags; 
// Memory flags for AI behavior (hostile, aggressive, etc.).

import com.fs.starfarer.api.util.Misc; 
// Utility methods (distance, randomness, etc.).

public class DR_TechMiningBlockadeManager implements EveryFrameScript {
// This class runs continuously and manages spawning and cleanup of blockade fleets.

    public static final String DR_MEMKEY_MANAGER_ADDED = "$DR_techMiningBlockadeManagerAdded";
    // Memory key used to ensure this manager is only added once.

    public static final String DR_FACTION_ID = Factions.HEGEMONY;
    // The faction spawning the blockade (placeholder for your future custom faction).

    public static final int DR_BLOCKADE_NUM_FLEETS = 5;
    // Number of blockade fleets active at once.

    public static final float DR_RESPAWN_CHECK_DAYS = 3f;
    // How often (in campaign days) we check whether to spawn more fleets.

    public static final int DR_START_PROGRESS_THRESHOLD = 140;
    // Minimum crisis pressure required before blockade begins.

    public static final float DR_FLEET_FP_MIN = 120f;
    // Minimum fleet points per blockade fleet.

    public static final float DR_FLEET_FP_MAX = 180f;
    // Maximum fleet points per blockade fleet.

    public static final String DR_MEMKEY_BLOCKADE_FLEET = "$DR_techMiningBlockadeFleet";
    // Memory key marking fleets as part of this blockade.

    private static final String DR_MEMKEY_STAGE = "$DR_TM_stage";
    // Memory key tracking whether fleet is traveling or in-system.

    private static final String DR_STAGE_HYPER_TRAVEL = "hyper_travel";
    // Fleet stage while traveling in hyperspace.

    private static final String DR_STAGE_IN_SYSTEM = "in_system";
    // Fleet stage once inside the target system.

    private static final float DR_ENTER_SYSTEM_DISTANCE = 1200f;
    // Distance to hyperspace anchor before entering the system.

    private static final float DR_SPAWN_DISTANCE_MIN = 4000f;
    // Minimum spawn distance from hyperspace anchor.

    private static final float DR_SPAWN_DISTANCE_MAX = 7000f;
    // Maximum spawn distance from hyperspace anchor.

    private float daysUntilCheck = 0f;
    // Countdown timer for respawn checks.

    private final List activeFleets = new ArrayList();
    // Raw list storing active blockade fleets (raw to avoid Janino generic issues).

    @Override
    public boolean isDone() {
        return false;
        // Script never self-terminates.
    }

    @Override
    public boolean runWhilePaused() {
        return false;
        // Script does not run while game is paused.
    }

    @Override
    public void advance(float amount) {
        // Called every campaign frame.

        if (Global.getSector() == null) return;
        // Safety check to avoid null pointer during load.

        if (DR_TechMiningHostileActivityFactor.isDealtWith()) {
            cleanup();
            return;
        }
        // If crisis permanently resolved, despawn fleets.

        if (DR_TechMiningLogic.countPlayerTechMining() <= 0) {
            cleanup();
            return;
        }
        // If player has no tech-mining anywhere, no blockade.

        float days = Global.getSector().getClock().convertToDays(amount);
        // Convert frame time to campaign days.

        advanceFleetStages();
        // Handle hyperspace-to-system transition logic.

        daysUntilCheck -= days;
        if (daysUntilCheck > 0f) return;
        // Only perform spawn logic every few days.

        daysUntilCheck = DR_RESPAWN_CHECK_DAYS;
        // Reset countdown.

        int pressure = DR_TechMiningLogic.getCurrentPressure();
        if (pressure < DR_START_PROGRESS_THRESHOLD) {
            cleanup();
            return;
        }
        // If crisis pressure not high enough, do nothing.

        StarSystemAPI targetSystem = pickTargetSystem();
        if (targetSystem == null) return;
        // Pick which system to blockade.

        pruneDeadFleets();
        // Remove invalid fleets from tracking.

        while (activeFleets.size() < DR_BLOCKADE_NUM_FLEETS) {
            CampaignFleetAPI fleet = spawnBlockadeFleetInHyperspace(targetSystem);
            if (fleet == null) break;
            activeFleets.add((Object) fleet);
        }
        // Spawn fleets until we reach desired count.
    }

    private void advanceFleetStages() {
        // Handles transition from hyperspace travel to in-system patrol.

        for (int i = 0; i < activeFleets.size(); i++) {
            CampaignFleetAPI fleet = (CampaignFleetAPI) activeFleets.get(i);
            if (fleet == null) continue;

            String stage = fleet.getMemoryWithoutUpdate().getString(DR_MEMKEY_STAGE);
            if (stage == null) continue;

            if (DR_STAGE_HYPER_TRAVEL.equals(stage)) {
                StarSystemAPI target =
                        (StarSystemAPI) fleet.getMemoryWithoutUpdate().get("$DR_TM_targetSystem");

                if (target == null) continue;

                SectorEntityToken hyperAnchor = target.getHyperspaceAnchor();
                if (hyperAnchor == null) continue;

                if (fleet.getContainingLocation() != Global.getSector().getHyperspace()) continue;

                float dist = Misc.getDistance(fleet.getLocation(), hyperAnchor.getLocation());
                // Measure distance to hyperspace anchor.

                if (dist <= DR_ENTER_SYSTEM_DISTANCE) {
                    moveFleetIntoSystemAndStartPatrol(fleet, target);
                    fleet.getMemoryWithoutUpdate().set(DR_MEMKEY_STAGE, DR_STAGE_IN_SYSTEM);
                }
            }
        }
    }

    private void moveFleetIntoSystemAndStartPatrol(CampaignFleetAPI fleet, StarSystemAPI system) {
        // Physically moves fleet entity from hyperspace into system.

        LocationAPI hyper = Global.getSector().getHyperspace();
        if (hyper != null) hyper.removeEntity(fleet);
        // Remove from hyperspace.

        system.addEntity(fleet);
        // Add to target system.

        SectorEntityToken anchor = system.getCenter();
        if (anchor == null) anchor = system.getStar();
        // Choose central reference point.

        if (anchor != null) {
            fleet.setLocation(
                    anchor.getLocation().x + Misc.random.nextFloat() * 300f - 150f,
                    anchor.getLocation().y + Misc.random.nextFloat() * 300f - 150f
            );
        }
        // Place fleet near system center.

        fleet.clearAssignments();
        // Remove old travel assignment.

        fleet.addAssignment(
                com.fs.starfarer.api.campaign.FleetAssignment.PATROL_SYSTEM,
                anchor,
                999999f,
                "Enforcing blockade"
        );
        // Begin infinite patrol.
    }

    private void pruneDeadFleets() {
        // Removes despawned or invalid fleets.

        for (Iterator it = activeFleets.iterator(); it.hasNext();) {
            Object obj = it.next();
            CampaignFleetAPI f = (CampaignFleetAPI) obj;

            if (f == null || f.isDespawning() || f.getContainingLocation() == null) {
                it.remove();
            }
        }
    }

    private void cleanup() {
        // Despawns all blockade fleets.

        for (int i = 0; i < activeFleets.size(); i++) {
            CampaignFleetAPI f = (CampaignFleetAPI) activeFleets.get(i);
            if (f != null && f.getContainingLocation() != null) {
                f.despawn();
            }
        }
        activeFleets.clear();
    }
}
