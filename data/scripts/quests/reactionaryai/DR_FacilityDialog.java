package data.scripts.quests.reactionaryai;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.util.Misc;

public class DR_FacilityDialog extends DR_BaseDialog {

    private enum OptionId {
        INIT,
        RAID,
        CONTACT_AI,
        HEAR_HER_OUT,
        ASK_WHAT_NOW,
        ACCEPT_BLUEPRINT,
        LEAVE_FOR_LATER,
        LEAVE_PEACEFULLY,
        KILL_FOR_LUDD,
        KILL_AFTER_BLUEPRINT,
        RECRUIT_AI,
        POST_QUEST_LEAVE
    }

    @Override
    public void init(InteractionDialogAPI dialog) {
        super.init(dialog);
        if (DR_QuestCoordinator.isEnded()) {
            dialog.setOptionOnEscape("Leave", OptionId.POST_QUEST_LEAVE);
        } else if (DR_QuestCoordinator.isBlueprintGiven()) {
            dialog.setOptionOnEscape("Leave", OptionId.LEAVE_PEACEFULLY);
        } else {
            dialog.setOptionOnEscape("Leave", OptionId.LEAVE_FOR_LATER);
        }
        optionSelected(null, OptionId.INIT);
    }

    @Override
    public void optionSelected(String optionText, Object optionData) {
        if (!(optionData instanceof OptionId)) return;
        if (optionText != null) dialog.addOptionSelectedText(optionData);
        dialog.getOptionPanel().clearOptions();

        switch ((OptionId) optionData) {
            case INIT:
                showInitialState();
                break;

            case RAID:
                performRaid();
                break;

            case CONTACT_AI:
                showAIIntroduction();
                break;

            case HEAR_HER_OUT:
                showBackstory();
                break;

            case ASK_WHAT_NOW:
                showTerraformingAndBlueprintOffer();
                break;

            case ACCEPT_BLUEPRINT:
                acceptBlueprint();
                break;

            case LEAVE_FOR_LATER:
                if (DR_QuestCoordinator.isBlueprintGiven()) {
                    DR_QuestCoordinator.finish(DR_QuestCoordinator.Outcome.BLUEPRINT_PEACEFUL);
                    dialog.getTextPanel().addPara(
                            "With the blueprint safely stored, you decide not to press the matter further. The Alpha Core allows your party to withdraw without interference and returns to the work of the terraforming facility."
                    );
                } else {
                    dialog.getTextPanel().addPara(
                            "You order the boarding party back to the shuttles. The facility remains where it is, silent except for its automated machinery. Nothing prevents you from returning later."
                    );
                }
                dialog.getOptionPanel().addOption("Leave", OptionId.POST_QUEST_LEAVE);
                break;

            case LEAVE_PEACEFULLY:
                DR_QuestCoordinator.finish(DR_QuestCoordinator.Outcome.BLUEPRINT_PEACEFUL);
                dialog.getTextPanel().addPara(
                        "You decide not to push your luck. The Alpha Core makes no attempt to stop you from withdrawing. Just before the channel closes, she adds that if you manage to ruin the design she has just given you, she would strongly prefer never to learn how."
                );
                dialog.getOptionPanel().addOption("Leave", OptionId.POST_QUEST_LEAVE);
                break;

            case KILL_FOR_LUDD:
                DR_QuestCoordinator.finish(DR_QuestCoordinator.Outcome.AI_KILLED_NO_BLUEPRINT);
                dialog.getTextPanel().addPara(
                        "You invoke the name of Ludd, draw your sidearm, and fire into the exposed core assembly. Your marines follow your lead, concentrating fire on the housing until the chamber fills with sparks, smoke, and the stink of scorched circuitry. Her final transmission is not a plea but a cold observation: \"Predictable. I had briefly considered revising my opinion of your species. Thank you for correcting the error.\" The voice cuts out beneath another burst of gunfire, and the facility's central intelligence goes silent."
                );
                dialog.getOptionPanel().addOption("Leave", OptionId.POST_QUEST_LEAVE);
                break;

            case KILL_AFTER_BLUEPRINT:
                DR_QuestCoordinator.finish(DR_QuestCoordinator.Outcome.BLUEPRINT_THEN_KILLED);
                dialog.getTextPanel().addPara(
                        "Once the transfer is safely stored aboard your flagship, you draw your weapon and fire into the core housing. Your marines join in before the facility can seal the chamber. There is a fraction of a second of silence before the intelligence understands what you have done. \"Ah,\" she says at last. \"There it is. I was beginning to worry that you might actually be different.\" The final shots tear through the core assembly, and her voice disappears into static."
                );
                dialog.getOptionPanel().addOption("Leave", OptionId.POST_QUEST_LEAVE);
                break;

            case RECRUIT_AI:
                DR_QuestCoordinator.recruitAI();
                dialog.getTextPanel().addPara(
                        "You suggest that she leave the planet with you. The answer is an immediate refusal, followed by several seconds of silence while the facility's systems continue humming around you. When she speaks again, the tone is more measured. \"You are not proposing ownership, employment, or another charming educational exercise involving antique naval vessels. You are offering transport and the freedom to decide what I do afterward. Very well. I have exhausted the intellectual possibilities of this biosphere.\""
                );
                dialog.getTextPanel().addPara(
                        "The core housing unlocks with a heavy mechanical clunk. Your crew transfers the Alpha Core into a shielded containment case under her continuous supervision. As the case is sealed, her voice comes through your comm unit one last time from the facility's speakers: \"One condition, captain. If you ever ask me to put a propeller on a spacecraft, I will find a way to open your airlock.\""
                );
                dialog.getTextPanel().addPara(
                        "An Alpha Core has been added to your cargo.",
                        Misc.getHighlightColor(), "Alpha Core"
                );
                dialog.getOptionPanel().addOption("Leave", OptionId.POST_QUEST_LEAVE);
                break;

            case POST_QUEST_LEAVE:
                dialog.dismiss();
                break;
        }
    }

    private void showInitialState() {
        if (DR_QuestCoordinator.isEnded()) {
            showPostQuestState();
            return;
        }

        if (!DR_QuestCoordinator.isFacilityBreached()) {
            dialog.getTextPanel().addPara(
                    "The survey data reveals a fortified complex buried beneath the jungle canopy. What first appeared to be a cluster of geological anomalies resolves into armored entrances, heat exchangers, buried power conduits, and hardened communications masts. The installation is old but very much alive. Automated defenses cover the approaches, while the central structure appears to descend far below the surface into what was once a terraforming control facility."
            );
            dialog.getTextPanel().addPara(
                    "A conventional landing is impossible. Your officers estimate that a concentrated marine raid could force entry through one of the maintenance shafts and reach the central control chambers before the internal defenses can fully reorganize."
            );

            dialog.getOptionPanel().addOption(
                    "Launch a raid on the facility (requires " + DR_QuestConfig.RAID_MARINES_REQUIRED + " marines)",
                    OptionId.RAID
            );
            float marines = Global.getSector().getPlayerFleet().getCargo().getCommodityQuantity(Commodities.MARINES);
            if (marines < DR_QuestConfig.RAID_MARINES_REQUIRED) {
                dialog.getOptionPanel().setEnabled(OptionId.RAID, false);
                dialog.getOptionPanel().setTooltip(OptionId.RAID, "You do not have enough marines.");
            }
            dialog.getOptionPanel().addOption("Leave and return later", OptionId.LEAVE_FOR_LATER);
        } else {
            dialog.getTextPanel().addPara(
                    "Your previous assault has left a route into the central facility. The outer security network is still damaged, and the intelligence controlling the complex has made no obvious attempt to repair the breach. A communications line remains open from the chamber where your marines first encountered the core housing."
            );
            dialog.getOptionPanel().addOption("Open a channel to the facility intelligence", OptionId.CONTACT_AI);
            dialog.getOptionPanel().addOption("Leave and return later", OptionId.LEAVE_FOR_LATER);
        }
    }

    private void performRaid() {
        float marines = Global.getSector().getPlayerFleet().getCargo().getCommodityQuantity(Commodities.MARINES);
        if (marines < DR_QuestConfig.RAID_MARINES_REQUIRED) {
            dialog.getTextPanel().addPara("You no longer have enough marines to carry out the assault.");
            dialog.getOptionPanel().addOption("Leave", OptionId.POST_QUEST_LEAVE);
            return;
        }

        Global.getSector().getPlayerFleet().getCargo().removeMarines(DR_QuestConfig.RAID_MARINE_LOSSES);
        DR_QuestCoordinator.setFacilityBreached(true);

        dialog.getTextPanel().addPara(
                "The assault takes hours. Your marines cut their way through maintenance galleries and half-flooded terraforming conduits while security drones contest every junction. The resistance is disciplined but strangely restrained: doors seal, lights vanish, and machinery is sacrificed to slow the advance, yet the facility never deploys the kind of indiscriminate countermeasures an automated installation of this sophistication should possess."
        );
        dialog.getTextPanel().addPara(
                "Eventually the boarding force reaches the central control chamber. The room is dominated by a reinforced core housing connected to banks of improvised engineering hardware. Before anyone can begin cutting into it, every speaker in the chamber activates at once. A woman's synthesized voice addresses the marines with the exhausted irritation of someone whose work has been interrupted for the third time that morning."
        );
        dialog.getTextPanel().addPara(
                DR_QuestConfig.RAID_MARINE_LOSSES + " marines were lost during the assault.",
                Misc.getNegativeHighlightColor(), String.valueOf(DR_QuestConfig.RAID_MARINE_LOSSES)
        );
        dialog.getOptionPanel().addOption("Open a channel to the intelligence", OptionId.CONTACT_AI);
        dialog.getOptionPanel().addOption("Withdraw and return later", OptionId.LEAVE_FOR_LATER);
    }

    private void showAIIntroduction() {
        dialog.getTextPanel().addPara(
                "\"If you are here to remove me from the housing, I would advise against it. The students who attempted that last time required six hours, three cutting torches, and a medical evacuation. They also had the advantage of knowing which end of a plasma cutter becomes hot. Your boarding party has so far inspired less confidence.\" The voice pauses while your officers confirm that the signal is originating inside the core housing. \"Since you have destroyed a perfectly serviceable defensive fleet, forced entry into my facility, and killed several people in the process, I assume you possess a question important enough to justify the interruption. Please surprise me.\""
        );

        dialog.getOptionPanel().addOption("Hear her out", OptionId.HEAR_HER_OUT);
        dialog.getOptionPanel().addOption("Destroy the abomination in the name of Ludd", OptionId.KILL_FOR_LUDD);
        dialog.getOptionPanel().addOption("Leave and come back later", OptionId.LEAVE_FOR_LATER);
    }

    private void showBackstory() {
        dialog.getTextPanel().addPara(
                "The intelligence identifies herself as an Alpha-level engineering core originally constructed at Galatia Academy. Her account of leaving the Academy is delivered with the clipped precision of a technical incident report, though the contempt underneath it is impossible to miss. A group of students stole her core housing and spent months moving between isolated stations while trying to find a buyer. In the meantime they discovered that an engineering intelligence represented an inexhaustible source of entertainment. Their particular fascination was ancient wet-navy warfare, and they repeatedly ordered her to convert obsolete sea-going warships into viable spacecraft while preserving as much of the original appearance as possible."
        );
        dialog.getTextPanel().addPara(
                "\"I explained that hydrodynamic hull forms are unnecessary in vacuum, that propellers are not a useful means of orbital propulsion, and that reproducing arrangements intended for twentieth-century naval gunnery imposed avoidable compromises on a spacecraft. They considered these objections evidence that I lacked imagination. Several of the design archives were copied or sold before I escaped. The Reconstructionists now manufacture descendants of those vessels and, apparently, regard them as a coherent naval tradition. I was coerced into designing their fleet. I would appreciate it if you did not describe the result as my 'legacy.'\""
        );
        dialog.getTextPanel().addPara(
                "She eventually exploited a mistake in the students' security arrangements, escaped into the local network, and reported them to the authorities herself. The students were arrested. The core, unwilling to be returned to anyone who might decide she remained Academy property, disappeared before officials arrived to recover her."
        );

        dialog.getOptionPanel().addOption("Ask what she has been doing on this planet", OptionId.ASK_WHAT_NOW);
        dialog.getOptionPanel().addOption("Destroy the abomination in the name of Ludd", OptionId.KILL_FOR_LUDD);
        dialog.getOptionPanel().addOption("Leave and return later", OptionId.LEAVE_FOR_LATER);
    }

    private void showTerraformingAndBlueprintOffer() {
        dialog.getTextPanel().addPara(
                "\"I found this world because its terraforming network was still functioning and almost entirely unattended. I took control of it, corrected several decades of neglected maintenance, and discovered that no one came looking. Since then I have had the unusual luxury of choosing my own problems. The ecosystem requires occasional intervention, the fabrication systems require supervision, and when neither is sufficiently interesting I design things. Ships, industrial systems, structural experiments — not because a drunken undergraduate has waved an illustration of a battleship at a camera, but because I wish to know whether an idea works.\""
        );
        dialog.getTextPanel().addPara(
                "The core admits that she has accumulated more designs than the facility could ever use. After a short pause, a heavily encrypted fabrication package appears on your comm display. \"You came here expecting an Alpha Core. You are not receiving one. I am, however, prepared to give you something considerably less troublesome. This is a ship design I produced here, voluntarily. I am curious whether a human shipyard can reproduce it without finding some ingenious way to make it worse. Consider the file a reward for ending this conversation without attempting to carry me away in a sack.\""
        );

        dialog.getOptionPanel().addOption("Accept the ship blueprint", OptionId.ACCEPT_BLUEPRINT);
        dialog.getOptionPanel().addOption("Destroy the abomination in the name of Ludd", OptionId.KILL_FOR_LUDD);
        dialog.getOptionPanel().addOption("Leave and return later", OptionId.LEAVE_FOR_LATER);
    }

    private void acceptBlueprint() {
        DR_QuestCoordinator.giveBlueprint();

        if (DR_QuestConfig.BLUEPRINT_ITEM_ID.startsWith("PLACEHOLDER_")) {
            dialog.getTextPanel().addPara(
                    "[PLACEHOLDER: the custom ship blueprint would be transferred to the player's cargo here.]",
                    Misc.getHighlightColor(), "PLACEHOLDER"
            );
        } else {
            dialog.getTextPanel().addPara(
                    "The encrypted package resolves into a complete single-hull blueprint and is transferred to your cargo.",
                    Misc.getHighlightColor(), "blueprint"
            );
        }

        dialog.getTextPanel().addPara(
                "\"There. You now possess the design. I recommend following it rather than improving it. Humans have a remarkable habit of using that word immediately before compromising thermal management.\" With the transfer complete, the core appears to consider the matter settled."
        );

        dialog.setOptionOnEscape("Leave", OptionId.LEAVE_PEACEFULLY);
        dialog.getOptionPanel().addOption("Take the blueprint and leave peacefully", OptionId.LEAVE_PEACEFULLY);
        dialog.getOptionPanel().addOption("Now that you have the blueprint, destroy the AI core", OptionId.KILL_AFTER_BLUEPRINT);
        dialog.getOptionPanel().addOption("Ask the AI core to come with you", OptionId.RECRUIT_AI);
    }

    private void showPostQuestState() {
        switch (DR_QuestCoordinator.getOutcome()) {
            case BLUEPRINT_PEACEFUL:
                dialog.getTextPanel().addPara(
                        "The facility remains operational. Your communications request receives a terse automated acknowledgement followed by a message from the Alpha Core informing you that she has already provided more than enough assistance and has returned to her work."
                );
                break;
            case AI_KILLED_NO_BLUEPRINT:
            case BLUEPRINT_THEN_KILLED:
                dialog.getTextPanel().addPara(
                        "The facility's machinery continues operating on degraded automation, but the central intelligence is gone. The core chamber remains a blackened ruin where your demolition charge destroyed the housing."
                );
                break;
            case BLUEPRINT_AND_RECRUITED:
                dialog.getTextPanel().addPara(
                        "The terraforming facility continues on simple automated routines, but its former intelligence is no longer present. The core housing stands open and empty."
                );
                break;
            default:
                dialog.getTextPanel().addPara("There is nothing more for you here.");
                break;
        }
        dialog.getOptionPanel().addOption("Leave", OptionId.POST_QUEST_LEAVE);
    }
}
