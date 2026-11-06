package data.scripts.campaign.crisis;
// Package declaration.

import java.awt.Color;
// Used for UI coloring.

import com.fs.starfarer.api.Global;
// Access to global memory and sector.

import com.fs.starfarer.api.impl.campaign.intel.events.BaseEventIntel;
// Base class for intel/event UI elements.

import com.fs.starfarer.api.impl.campaign.intel.events.BaseHostileActivityFactor;
// Base class for all hostile activity factors.

import com.fs.starfarer.api.impl.campaign.intel.events.HostileActivityEventIntel;
// The hostile activity manager.

import com.fs.starfarer.api.util.Misc;
// Utility functions (colors, formatting).

public class DR_TechMiningHostileActivityFactor extends BaseHostileActivityFactor {
// Represents one cause of hostile activity (our crisis).

    public static final String DR_MEMKEY_DEALT_WITH = "$DR_techMiningCrisis_dealtWith";
    // Global memory key that permanently marks the crisis as resolved.

    public DR_TechMiningHostileActivityFactor(HostileActivityEventIntel intel) {
        // Constructor called when the factor is registered.

        super(intel);
        // Pass intel reference to parent class.

        intel.addActivity(this, new DR_TechMiningCrisisCause(intel));
        // Attach our progress-calculating "cause" to this factor.
    }

    @Override
    public String getDesc(BaseEventIntel intel) {
        // Short description shown in the crisis UI.

        return "Tech-mining crackdown";
    }

    @Override
    public Color getDescColor(BaseEventIntel intel) {
        // Color used for the description text.

        return Misc.getHighlightColor();
    }

    public static boolean isDealtWith() {
        // Public helper: check whether the crisis has been permanently resolved.

        return Global.getSector() != null
                && Global.getSector().getMemoryWithoutUpdate()
                        .getBoolean(DR_MEMKEY_DEALT_WITH);
    }

    public static void setDealtWith() {
        // Public helper: permanently end the crisis.

        if (Global.getSector() == null) return;
        // Safety check.

        Global.getSector().getMemoryWithoutUpdate()
                .set(DR_MEMKEY_DEALT_WITH, true);
        // Set the permanent resolution flag.
    }
}

