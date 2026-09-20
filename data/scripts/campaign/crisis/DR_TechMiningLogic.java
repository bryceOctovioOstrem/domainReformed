package data.scripts.campaign.crisis;
// Declares package for this helper.

import java.util.List;
// Needed to iterate over markets list.

import com.fs.starfarer.api.Global;
// Access to Sector and economy.

import com.fs.starfarer.api.campaign.econ.Industry;
// Industry API for checking tech-mining.

import com.fs.starfarer.api.campaign.econ.MarketAPI;
// Market API for colonies.

import com.fs.starfarer.api.impl.campaign.ids.Conditions;
// Vanilla condition IDs for ruins.

import com.fs.starfarer.api.impl.campaign.ids.Industries;
// Vanilla industry IDs for tech-mining.

public class DR_TechMiningLogic {
// Utility functions used by both the HA cause and the blockade manager.

    public static final String DR_RUINS_DEPLETION_MEMKEY = "$ruinsDepletion";
    // Vanilla memory key that tracks ruin depletion; 1.0 means untouched.

    public static int countPlayerTechMining() {
        // Counts how many player colonies have functional tech-mining.

        int count = 0;
        // Initialize count.

        for (MarketAPI m : Global.getSector().getEconomy().getMarketsCopy()) {
            // Iterate over all markets in the economy.

            if (m == null || !m.isPlayerOwned()) continue;
            // Only count player-owned markets.

            if (m.isHidden()) continue;
            // Skip hidden/internal markets.

            if (marketHasFunctionalTechMining(m)) count++;
            // Increment count if this market has functional tech-mining.
        }

        return count;
        // Return the total number of functional tech-mining industries.
    }

    public static boolean marketHasFunctionalTechMining(MarketAPI m) {
        // Checks whether a single market has functional tech-mining.

        Industry ind = m.getIndustry(Industries.TECHMINING);
        // Get the tech-mining industry from the market.

        return ind != null && ind.isFunctional();
        // Return true only if it exists and is functional.
    }

    public static int countPlayerUntouchedRuins() {
        // Counts player colonies that have ruins that are still 100% intact.

        int count = 0;
        // Initialize count.

        for (MarketAPI m : Global.getSector().getEconomy().getMarketsCopy()) {
            // Iterate over all markets.

            if (m == null || !m.isPlayerOwned()) continue;
            // Only player-owned markets.

            if (m.isHidden()) continue;
            // Skip hidden markets.

            if (!hasAnyRuinsCondition(m)) continue;
            // Must have at least one ruins condition.

            float depletion = 1.0f;
            // Assume 100% intact unless we find a stored depletion value.

            if (m.getMemoryWithoutUpdate().contains(DR_RUINS_DEPLETION_MEMKEY)) {
                // If the market memory explicitly stores depletion...

                depletion = m.getMemoryWithoutUpdate().getFloat(DR_RUINS_DEPLETION_MEMKEY);
                // Read the float value from memory.
            }

            if (depletion >= 0.999f) count++;
            // Treat 0.999+ as “100% intact” to avoid float precision problems.
        }

        return count;
        // Return number of untouched ruins.
    }

    public static int getCurrentPressure() {
        // Computes the same “net pressure” value used by the HA cause.

        if (DR_TechMiningHostileActivityFactor.isDealtWith()) return 0;
        // If crisis is resolved, pressure is always zero.

        int techMiningCount = countPlayerTechMining();
        // Count player tech-mining industries.

        if (techMiningCount <= 0) return 0;
        // Hard rule: no tech-mining means no pressure.

        int untouched = countPlayerUntouchedRuins();
        // Count untouched ruins (countermeasure).

        int positive = 0;
        // Positive pressure accumulator.

        for (int i = 0; i < techMiningCount; i++) {
            // For each tech-mining, add base plus a scaling kicker.

            positive += DR_TechMiningCrisisCause.DR_BASE_PROGRESS_PER_TECHMINING
                    + i * DR_TechMiningCrisisCause.DR_EXTRA_PROGRESS_PER_TECHMINING;
            // Sum up the scaling pressure, matching the cause’s formula.
        }

        int net = positive - untouched * DR_TechMiningCrisisCause.DR_UNTOUCHED_RUINS_REDUCTION;
        // Subtract countermeasure value (-20 per untouched ruin).

        return Math.max(0, net);
        // Clamp at zero and return.
    }

    private static boolean hasAnyRuinsCondition(MarketAPI m) {
        // Checks whether market has any ruins condition.

        return m.hasCondition(Conditions.RUINS_SCATTERED)
                // True if scattered ruins.

                || m.hasCondition(Conditions.RUINS_WIDESPREAD)
                // True if widespread ruins.

                || m.hasCondition(Conditions.RUINS_EXTENSIVE)
                // True if extensive ruins.

                || m.hasCondition(Conditions.RUINS_VAST);
                // True if vast ruins.
    }
}
