package data.scripts.campaign.crisis;
// Package declaration.

import java.awt.Color;
// Used for UI colors.

import java.util.List;
// Used to iterate over markets.

import com.fs.starfarer.api.Global;
// Access to economy and sector.

import com.fs.starfarer.api.campaign.StarSystemAPI;
// Used for system weighting.

import com.fs.starfarer.api.campaign.econ.Industry;
// Represents an industry on a market.

import com.fs.starfarer.api.campaign.econ.MarketAPI;
// Represents a colony/market.

import com.fs.starfarer.api.impl.campaign.ids.Conditions;
// Vanilla condition IDs (ruins).

import com.fs.starfarer.api.impl.campaign.ids.Industries;
// Vanilla industry IDs (tech-mining).

import com.fs.starfarer.api.impl.campaign.intel.events.BaseEventIntel;
// Base event class.

import com.fs.starfarer.api.impl.campaign.intel.events.BaseHostileActivityCause2;
// Base class for hostile activity causes.

import com.fs.starfarer.api.impl.campaign.intel.events.HostileActivityEventIntel;
// Hostile activity manager.

import com.fs.starfarer.api.ui.TooltipMakerAPI;
// Used to build UI tooltips.

import com.fs.starfarer.api.ui.TooltipMakerAPI.TooltipCreator;
// Interface for tooltip generators.

import com.fs.starfarer.api.util.Misc;
// Utility helpers.

public class DR_TechMiningCrisisCause extends BaseHostileActivityCause2 {
// Calculates how much the crisis bar fills.

    public static final int DR_BASE_PROGRESS_PER_TECHMINING = 60;
    // Base pressure from the first tech-mining industry.

    public static final int DR_EXTRA_PROGRESS_PER_TECHMINING = 10;
    // Additional pressure added per extra tech-mining industry.

    public static final int DR_UNTOUCHED_RUINS_REDUCTION = 20;
    // Pressure reduction per untouched ruin.

    public static final String DR_RUINS_DEPLETION_MEMKEY = "$ruinsDepletion";
    // Vanilla memory key used to track ruin depletion.

    public DR_TechMiningCrisisCause(HostileActivityEventIntel intel) {
        // Constructor.

        super(intel);
        // Pass intel reference to parent class.
    }

    @Override
    public String getDesc() {
        // Short description shown in the crisis UI.

        return "Tech-mining provocation";
    }

    @Override
    public Color getDescColor(BaseEventIntel intel) {
        // Color for description text.

        return Misc.getHighlightColor();
    }

@Override
public boolean shouldShow() {
    // Do not show or conceptually exist until the player owns at least one colony
    if (Global.getSector() == null) return false;

    boolean hasPlayerColony = false;
    for (MarketAPI m : Global.getSector().getEconomy().getMarketsCopy()) {
        if (m != null && m.isPlayerOwned() && !m.isHidden()) {
            hasPlayerColony = true;
            break;
        }
    }
    if (!hasPlayerColony) return false;

    return getProgress() > 0 || DR_TechMiningHostileActivityFactor.isDealtWith();
}


    @Override
    public String getProgressStr() {
        // Text shown next to the progress bar.

        if (DR_TechMiningHostileActivityFactor.isDealtWith())
            return "Dealt with";

        return super.getProgressStr();
    }

    @Override
    public int getProgress() {
        // Main logic: compute crisis pressure.

        if (DR_TechMiningHostileActivityFactor.isDealtWith())
            return 0;
        // If resolved, always return zero.

        int techMiningCount = countPlayerTechMining();
        // Count how many tech-mining industries the player has.

        if (techMiningCount <= 0)
            return 0;
        // Hard rule: no tech-mining = no crisis.

        int untouchedRuins = countPlayerUntouchedRuins();
        // Count ruins that are still 100% intact.

        int positive = 0;
        // Accumulate positive pressure.

        for (int i = 0; i < techMiningCount; i++) {
            // Each tech-mining adds more pressure than the last.

            positive += DR_BASE_PROGRESS_PER_TECHMINING
                    + i * DR_EXTRA_PROGRESS_PER_TECHMINING;
        }

        int net = positive - untouchedRuins * DR_UNTOUCHED_RUINS_REDUCTION;
        // Subtract countermeasures.

        if (net < 0) net = 0;
        // Never allow negative pressure.

        return net;
    }

    private int countPlayerTechMining() {
        // Counts functional tech-mining industries on player colonies.

        int count = 0;

        List<MarketAPI> markets =
                Global.getSector().getEconomy().getMarketsCopy();

        for (MarketAPI m : markets) {
            // Iterate over all markets.

            if (m == null || !m.isPlayerOwned()) continue;
            // Skip non-player markets.

            if (m.isHidden()) continue;
            // Skip hidden/internal markets.

            Industry ind = m.getIndustry(Industries.TECHMINING);
            // Fetch tech-mining industry.

            if (ind != null && ind.isFunctional()) count++;
            // Count only functional industries.
        }

        return count;
    }

    private int countPlayerUntouchedRuins() {
        // Counts player colonies with ruins at 100% depletion.

        int count = 0;

        List<MarketAPI> markets =
                Global.getSector().getEconomy().getMarketsCopy();

        for (MarketAPI m : markets) {
            // Iterate over markets.

            if (m == null || !m.isPlayerOwned()) continue;
            if (m.isHidden()) continue;

            if (!hasAnyRuinsCondition(m)) continue;
            // Skip markets without ruins.

            float depletion = 1.0f;
            // Default to fully intact.

            if (m.getMemoryWithoutUpdate().contains(DR_RUINS_DEPLETION_MEMKEY)) {
                depletion = m.getMemoryWithoutUpdate()
                        .getFloat(DR_RUINS_DEPLETION_MEMKEY);
            }

            if (depletion >= 0.999f) count++;
            // Count only fully untouched ruins.
        }

        return count;
    }

    private boolean hasAnyRuinsCondition(MarketAPI m) {
        // Checks for any type of ruins condition.

        return m.hasCondition(Conditions.RUINS_SCATTERED)
                || m.hasCondition(Conditions.RUINS_WIDESPREAD)
                || m.hasCondition(Conditions.RUINS_EXTENSIVE)
                || m.hasCondition(Conditions.RUINS_VAST);
    }

    @Override
    public float getMagnitudeContribution(StarSystemAPI system) {
        // Light system weighting for UI visuals.

        return getProgress() > 0 ? 1f : 0f;
    }
}
