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
        private StarSystemAPI pickTargetSystem() {
        // Chooses which star system the blockade should target.

        Object bestObj = null;
        // Will store the best MarketAPI candidate found so far (kept as Object to avoid Janino generic issues).

        List markets = Global.getSector().getEconomy().getMarketsCopy();
        // Get a copy of all markets in the economy (raw List to avoid foreach+generics issues).

        for (int i = 0; i < markets.size(); i++) {
            // Iterate by index to avoid enhanced-for generic type problems in Janino.

            Object obj = markets.get(i);
            // Get the current list element.

            if (!(obj instanceof com.fs.starfarer.api.campaign.econ.MarketAPI)) continue;
            // If this element is not a MarketAPI, skip it.

            com.fs.starfarer.api.campaign.econ.MarketAPI m =
                    (com.fs.starfarer.api.campaign.econ.MarketAPI) obj;
            // Cast the element to MarketAPI.

            if (m == null || !m.isPlayerOwned()) continue;
            // Only player-owned colonies can be targeted.

            if (m.isHidden()) continue;
            // Skip hidden/internal markets.

            if (m.getPrimaryEntity() == null) continue;
            // Must have a primary entity to be a real colony.

            if (m.getStarSystem() == null) continue;
            // Must be inside a star system.

            if (!DR_TechMiningLogic.marketHasFunctionalTechMining(m)) continue;
            // Only markets with functional tech-mining qualify.

            if (bestObj == null) {
                // If we have no best candidate yet...

                bestObj = m;
                // Set this market as the best candidate.
            } else {
                // Otherwise compare against the current best.

                com.fs.starfarer.api.campaign.econ.MarketAPI best =
                        (com.fs.starfarer.api.campaign.econ.MarketAPI) bestObj;
                // Cast bestObj back to MarketAPI.

                if (m.getSize() > best.getSize()) bestObj = m;
                // Prefer the largest tech-mining market (simple “most important” rule).
            }
        }

        if (bestObj == null) return null;
        // If we found no valid candidate, return null.

        com.fs.starfarer.api.campaign.econ.MarketAPI best =
                (com.fs.starfarer.api.campaign.econ.MarketAPI) bestObj;
        // Cast the best candidate back to MarketAPI.

        return best.getStarSystem();
        // Return the star system that contains the chosen market.
    }

    private CampaignFleetAPI spawnBlockadeFleetInHyperspace(StarSystemAPI targetSystem) {
        // Spawns a blockade fleet in hyperspace and assigns it to travel toward the target system.

        SectorEntityToken hyperAnchor = targetSystem.getHyperspaceAnchor();
        // The hyperspace anchor is the hyperspace-side “entrance” reference for the system.

        if (hyperAnchor == null) return null;
        // If the system has no hyperspace anchor (rare), we cannot do hyperspace travel.

        float fp = Misc.random.nextFloat() * (DR_FLEET_FP_MAX - DR_FLEET_FP_MIN) + DR_FLEET_FP_MIN;
        // Choose a random fleet strength between min and max.

        FleetParamsV3 params = new FleetParamsV3(
                null,
                // No source market; this is a generic task force spawn.

                hyperAnchor.getLocation(),
                // Use the anchor location as the fleet “spawn context” location.

                DR_FACTION_ID,
                // Faction that owns this blockade fleet.

                null,
                // Optional quality override; null lets vanilla choose defaults.

                FleetTypes.TASK_FORCE,
                // Fleet type; task force is a good “blockade patrol” style fleet.

                fp, 0f, 0f, 0f, 0f, 0f, 0f
                // Combat points + six more buckets; your Starsector version expects 7 floats total.
        );
        // Construct the fleet generation parameters.

        CampaignFleetAPI fleet = FleetFactoryV3.createFleet(params);
        // Actually create the fleet using vanilla fleet generator.

        if (fleet == null) return null;
        // If fleet generation failed, abort.

        LocationAPI hyper = Global.getSector().getHyperspace();
        // Get hyperspace location object.

        hyper.addEntity(fleet);
        // Add the fleet entity to hyperspace.

        float dist = DR_SPAWN_DISTANCE_MIN +
                Misc.random.nextFloat() * (DR_SPAWN_DISTANCE_MAX - DR_SPAWN_DISTANCE_MIN);
        // Choose a spawn distance away from the hyperspace anchor.

        float angle = Misc.random.nextFloat() * 360f;
        // Choose a random angle around the anchor to spawn from.

        float x = hyperAnchor.getLocation().x + (float) Math.cos(Math.toRadians(angle)) * dist;
        // Compute spawn x coordinate.

        float y = hyperAnchor.getLocation().y + (float) Math.sin(Math.toRadians(angle)) * dist;
        // Compute spawn y coordinate.

        fleet.setLocation(x, y);
        // Place the fleet in hyperspace at the computed position.

        fleet.getMemoryWithoutUpdate().set(DR_MEMKEY_BLOCKADE_FLEET, true);
        // Mark it as our blockade fleet for rules.csv and filtering.

        fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_MAKE_HOSTILE, true);
        // Make fleet hostile to the player.

        fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_MAKE_AGGRESSIVE, true);
        // Make fleet more likely to chase/engage.

        fleet.getMemoryWithoutUpdate().set("$DR_TM_targetSystem", targetSystem);
        // Store a reference to the target system so we can “enter” later.

        fleet.getMemoryWithoutUpdate().set(DR_MEMKEY_STAGE, DR_STAGE_HYPER_TRAVEL);
        // Set stage to hyperspace travel.

        fleet.clearAssignments();
        // Clear any default assignments the fleet generator added.

        fleet.addAssignment(
                com.fs.starfarer.api.campaign.FleetAssignment.GO_TO_LOCATION,
                // Use a vanilla assignment to fly to a location.

                hyperAnchor,
                // Travel toward the hyperspace anchor for the target system.

                999999f,
                // Very long duration; effectively “until it gets there”.

                "En route to blockade"
                // UI text describing what the fleet is doing.
        );
        // Add the travel assignment.

        return fleet;
        // Return the fleet so the manager can track it.
    }

}
