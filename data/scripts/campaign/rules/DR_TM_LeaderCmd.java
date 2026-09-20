package data.scripts.campaign.rules;
// Package for rules command plugins.

import java.util.List;
// Needed for execute() signature.

import java.util.Map;
// Needed for execute() signature.

import com.fs.starfarer.api.Global;
// Used for global memory access and player fleet access.

import com.fs.starfarer.api.campaign.CampaignFleetAPI;
// Used to read player fleet FP and cargo.

import com.fs.starfarer.api.campaign.CargoAPI;
// Used to read/remove special items from player cargo.

import com.fs.starfarer.api.campaign.InteractionDialogAPI;
// The dialog we add options and text to.

import com.fs.starfarer.api.characters.PersonAPI;
// Used to identify the person you are speaking with.

import com.fs.starfarer.api.campaign.SpecialItemData;
// Used to reference special items like the Orbital Fusion Lamp.

import com.fs.starfarer.api.campaign.rules.MemoryAPI;
// Provided by rules engine; not heavily used here.

import com.fs.starfarer.api.impl.campaign.ids.Items;
// Contains special item id constants, including ORBITAL_FUSION_LAMP.

import com.fs.starfarer.api.impl.campaign.rulecmd.BaseCommandPlugin;
// Base class for rules.csv command plugins.

import com.fs.starfarer.api.util.Misc;
// Used for Token parsing in execute() signature.

import data.scripts.campaign.crisis.DR_TechMiningHostileActivityFactor;
// Used to permanently end the crisis.

public class DR_TM_LeaderCmd extends BaseCommandPlugin {
// Command plugin that injects options into the vanilla “talk to a person” dialog.

    public static final String LEADER_FLAG = "$DR_techMiningCrisisLeader";
    // Memory flag: whoever has this set to true is treated as “the leader”.

    public static final String GLOBAL_CAN_TALK_LEADER = "$DR_canTalkToCrisisLeader";
    // Global gate: player must have been told to speak to leadership (from blockade comms).

    public static final String OPT_TALK = "DR_TM_LEADER_TALK";
    // Option id for the basic conversation option.

    public static final String OPT_THREATEN = "DR_TM_LEADER_THREATEN";
    // Option id for intimidation resolution.

    public static final String OPT_LAMP = "DR_TM_LEADER_LAMP";
    // Option id for lamp trade resolution.

    public static final float INTIMIDATE_FP_REQ = 500f;
    // Fleet point threshold for intimidation.

    public static final String LAMP_ID = Items.ORBITAL_FUSION_LAMP;
    // Special item id for Orbital Fusion Lamp.

    @Override
    public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Misc.Token> params,
                           Map<String, MemoryAPI> memoryMap) {
        // Entry point invoked by rules.csv triggers.

        if (dialog == null || dialog.getInteractionTarget() == null) return false;
        // If there is no dialog or no target, nothing to do.

       
        if (params == null || params.isEmpty()) return false;
        String mode = params.get(0).toString();
        if (mode != null) mode = mode.replace("\"", "").trim();

        // Mode tells us whether we’re populating options or responding to selection.

        if ("populate".equals(mode)) {
            // Called when the dialog is building its options list.

            if (!canUseLeaderDialog(dialog)) return false;
            // Only add options if this is the correct leader and the crisis is active.

            if (!dialog.getOptionPanel().hasOption(OPT_TALK)) {
                // Avoid duplicate options.

                dialog.getOptionPanel().addOption("Discuss the tech-mining blockade", OPT_TALK);
                // Add the base “talk” option.
            }

            if (playerHasEnoughFP() && !dialog.getOptionPanel().hasOption(OPT_THREATEN)) {
                // Only show the threaten option if requirement is met.

                dialog.getOptionPanel().addOption("Threaten the leader into lifting the blockade", OPT_THREATEN);
                // Add the intimidation resolution option.
            }

            if (playerHasLamp() && !dialog.getOptionPanel().hasOption(OPT_LAMP)) {
                // Only show the lamp option if the player actually has a lamp.

                dialog.getOptionPanel().addOption("Offer an Orbital Fusion Lamp in exchange for lifting the blockade", OPT_LAMP);
                // Add the barter resolution option.
            }

            return true;
            // Indicate we added (or considered) options.
        }

        if ("selected".equals(mode)) {
            // Called when an option is clicked.

            if (!canUseLeaderDialog(dialog)) return false;
            // Make sure this selection is happening in the correct leader context.

            String selected = dialog.getInteractionTarget()
                    .getMemoryWithoutUpdate().getString("$option");
            // Read which option id the rules engine says was selected.

            if (OPT_TALK.equals(selected)) {
                // If the player chose the basic “talk” option...

                dialog.getTextPanel().addPara("");
                // Add spacing.

                dialog.getTextPanel().addPara("The leader studies you in silence.");
                // Placeholder narrative.

                dialog.getTextPanel().addPara("“TEST: Independent tech-mining has consequences. Speak plainly.”");
                // Placeholder leader line.

                return true;
                // We handled this selection.
            }

            if (OPT_THREATEN.equals(selected)) {
                // If the player chose the threaten option...

                if (!playerHasEnoughFP()) {
                    // Safety: in case FP changed after options were populated.

                    dialog.getTextPanel().addPara("");
                    // Add spacing.

                    dialog.getTextPanel().addPara("You are not currently in a position to make that threat stick.");
                    // Failure message.

                    return true;
                    // Handled the click.
                }

                dialog.getTextPanel().addPara("");
                // Add spacing.

                dialog.getTextPanel().addPara("“TEST: …Very well. The blockade will be lifted. This matter is concluded.”");
                // Placeholder success response.

                permanentlyEndCrisis();
                // Permanently end the crisis and stop blockade behavior.

                dialog.getTextPanel().addPara("Orders begin propagating through the blockade task forces.");
                // Wrap-up message.

                return true;
                // Handled the click.
            }

            if (OPT_LAMP.equals(selected)) {
                // If the player chose the lamp offer option...

                if (!playerHasLamp()) {
                    // Safety: in case the lamp was sold/used after options were populated.

                    dialog.getTextPanel().addPara("");
                    // Add spacing.

                    dialog.getTextPanel().addPara("You do not currently have an Orbital Fusion Lamp available.");
                    // Failure message.

                    return true;
                    // Handled the click.
                }

                removeOneLamp();
                // Remove one Orbital Fusion Lamp from player cargo.

                dialog.getTextPanel().addPara("");
                // Add spacing.

                dialog.getTextPanel().addPara("You present an Orbital Fusion Lamp.");
                // Placeholder narrative.

                dialog.getTextPanel().addPara("“TEST: Acceptable. The blockade will be lifted. Do not mistake this for weakness.”");
                // Placeholder success response.

                permanentlyEndCrisis();
                // Permanently end the crisis and stop blockade behavior.

                dialog.getTextPanel().addPara("The blockade begins to dissolve.");
                // Wrap-up message.

                return true;
                // Handled the click.
            }

            return false;
            // Not one of our options.
        }

        return false;
        // Unknown mode; do nothing.
    }

    private boolean canUseLeaderDialog(InteractionDialogAPI dialog) {
        // Checks whether the current dialog context is eligible for leader options.

        if (DR_TechMiningHostileActivityFactor.isDealtWith()) return false;
        // If crisis is already permanently ended, never show options.

        if (!Global.getSector().getMemoryWithoutUpdate().getBoolean(GLOBAL_CAN_TALK_LEADER)) return false;
        // Player must have been directed to leadership first (breadcrumb gate).

        PersonAPI p = getActivePerson(dialog);
        // Resolve who the dialog is actually “about”.

        if (p == null) return false;
        // If we can’t identify a person, we can’t determine leadership.

        return p.getMemoryWithoutUpdate().getBoolean(LEADER_FLAG);
        // Only show options if this person has been flagged as the crisis leader.
    }

    private PersonAPI getActivePerson(InteractionDialogAPI dialog) {
        // Attempts to find the “active person” associated with the current interaction.

        if (dialog.getInteractionTarget() != null && dialog.getInteractionTarget().getActivePerson() != null) {
            // Many person dialogs set activePerson on the interaction target.

            return dialog.getInteractionTarget().getActivePerson();
            // Return that person.
        }

        return null;
        // No active person found; treat as not a leader conversation.
    }

    private boolean playerHasEnoughFP() {
        // Checks if the player fleet meets the intimidation FP requirement.

        CampaignFleetAPI pf = Global.getSector().getPlayerFleet();
        // Get player fleet object.

        float fp = pf != null ? pf.getFleetPoints() : 0f;
        // Read player fleet points, or 0 if no fleet.

        return fp >= INTIMIDATE_FP_REQ;
        // Return true if the fleet is large enough.
    }

    private boolean playerHasLamp() {
        // Checks if the player has at least one Orbital Fusion Lamp in cargo.

        CampaignFleetAPI pf = Global.getSector().getPlayerFleet();
        // Get player fleet.

        if (pf == null || pf.getCargo() == null) return false;
        // If no fleet or cargo, cannot have lamp.

        CargoAPI cargo = pf.getCargo();
        // Get cargo reference.

        float qty = cargo.getQuantity(CargoAPI.CargoItemType.SPECIAL, new SpecialItemData(LAMP_ID, null));
        // Count special items matching Orbital Fusion Lamp id.

        return qty >= 1f;
        // True if at least one lamp exists.
    }

    private void removeOneLamp() {
        // Removes exactly one Orbital Fusion Lamp from player cargo.

        CampaignFleetAPI pf = Global.getSector().getPlayerFleet();
        // Get player fleet.

        if (pf == null || pf.getCargo() == null) return;
        // If missing, do nothing safely.

        pf.getCargo().removeItems(CargoAPI.CargoItemType.SPECIAL, new SpecialItemData(LAMP_ID, null), 1f);
        // Remove one special item matching Orbital Fusion Lamp id.
    }

    private void permanentlyEndCrisis() {
        // Ends the crisis permanently and cleans up gating flags.

        DR_TechMiningHostileActivityFactor.setDealtWith();
        // Set the permanent resolved flag used by the HA cause and blockade manager.

        Global.getSector().getMemoryWithoutUpdate().unset(GLOBAL_CAN_TALK_LEADER);
        // Remove the leader gate so the leader options disappear after resolution.
    }
}
