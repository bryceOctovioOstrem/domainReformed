package data.scripts.quests.reactionaryai;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.impl.campaign.intel.bar.events.BarEventManager;
import com.fs.starfarer.api.impl.campaign.intel.bar.events.BaseBarEventWithPerson;
import com.fs.starfarer.api.util.Misc;

import java.util.Map;

public class DR_BarEvent extends BaseBarEventWithPerson {

    private enum OptionId {
        INIT,
        BUY,
        REFUSE,
        REPORT,
        LEAVE
    }

    @Override
    public boolean shouldShowAtMarket(MarketAPI market) {
        return super.shouldShowAtMarket(market) && DR_QuestCoordinator.shouldOfferQuest();
    }

    @Override
    public void addPromptAndOption(InteractionDialogAPI dialog, Map<String, MemoryAPI> memoryMap) {
        super.addPromptAndOption(dialog, memoryMap);
        regen(dialog.getInteractionTarget().getMarket());

        dialog.getTextPanel().addPara(
                "A nervous-looking spacer occupies a table near the back of the room, making a conspicuous effort not to look at anyone for too long. " +
                "When your eyes meet, the spacer gives you a small nod and taps two fingers against the tabletop."
        );
        dialog.getOptionPanel().addOption("See what the shady spacer wants", this);
    }

    @Override
    public void init(InteractionDialogAPI dialog, Map<String, MemoryAPI> memoryMap) {
        super.init(dialog, memoryMap);
        done = false;
        dialog.getVisualPanel().showPersonInfo(person, true);
        optionSelected(null, OptionId.INIT);
    }

    @Override
    public void optionSelected(String optionText, Object optionData) {
        if (!(optionData instanceof OptionId)) return;

        if (optionText != null) dialog.addOptionSelectedText(optionData);
        dialog.getOptionPanel().clearOptions();

        switch ((OptionId) optionData) {
            case INIT:
                dialog.getTextPanel().addPara(
                        "The spacer waits until you sit before leaning across the table. \"I've got something better than rumors, captain. " +
                        "A lead on an Alpha Core. Not a scavenger's story about a red light in some ruin — a real trail. Two hundred credits and I tell you where to start looking. " +
                        "After that, whatever you find is yours. I don't want to know what you do with it, and you don't need to know where I got the information.\""
                );

                dialog.getOptionPanel().addOption(
                        "Pay " + Misc.getDGSCredits(DR_QuestConfig.INFORMANT_PRICE) + " for the lead",
                        OptionId.BUY
                );
                if (Global.getSector().getPlayerFleet().getCargo().getCredits().get() < DR_QuestConfig.INFORMANT_PRICE) {
                    dialog.getOptionPanel().setEnabled(OptionId.BUY, false);
                    dialog.getOptionPanel().setTooltip(OptionId.BUY, "You do not have enough credits.");
                }

                dialog.getOptionPanel().addOption("Refuse, but leave the offer open", OptionId.REFUSE);
                dialog.getOptionPanel().addOption("Call the authorities and report the spacer", OptionId.REPORT);
                break;

            case BUY:
                MarketAPI market = dialog.getInteractionTarget().getMarket();
                if (!DR_QuestCoordinator.acceptQuest(market)) {
                    dialog.getTextPanel().addPara(
                            "The spacer checks the data again, frowns, and quietly withdraws the offer. Whatever trail existed has apparently gone cold."
                    );
                    dialog.getOptionPanel().addOption("Leave", OptionId.LEAVE);
                    break;
                }

                Global.getSector().getPlayerFleet().getCargo().getCredits().subtract(DR_QuestConfig.INFORMANT_PRICE);
                dialog.getTextPanel().addPara(
                        "The spacer palms your credits and slides a small data wafer across the table. The information is disappointingly incomplete: it does not contain the location of an Alpha Core. " +
                        "Instead, it identifies an abandoned courier whose navigation computer supposedly contains the last intact copy of a set of encrypted coordinates. If the lead is genuine, the courier is the next step."
                );
                dialog.getTextPanel().addPara(
                        "The location of the abandoned courier has been added to your Intel screen.",
                        Misc.getHighlightColor(), "Intel"
                );
                dialog.getOptionPanel().addOption("Leave", OptionId.LEAVE);
                break;

            case REFUSE:
                dialog.getTextPanel().addPara(
                        "The spacer shrugs and pockets the data wafer. \"Suit yourself. If I haven't found another buyer by the next time we cross paths, the offer stands.\""
                );
                dialog.getOptionPanel().addOption("Leave", OptionId.LEAVE);
                break;

            case REPORT:
                Global.getSector().getPlayerFleet().getCargo().getCredits().add(DR_QuestConfig.REPORT_REWARD);
                DR_QuestCoordinator.reportInformant();
                dialog.getTextPanel().addPara(
                        "You quietly flag down local security. The spacer notices the movement a few seconds too late and is dragged away protesting while an officer takes your statement. " +
                        "The authorities pay you " + Misc.getDGSCredits(DR_QuestConfig.REPORT_REWARD) + " for turning in a suspected trafficker in restricted technology. Whatever the spacer knew dies with the arrest."
                );
                dialog.getOptionPanel().addOption("Leave", OptionId.LEAVE);
                break;

            case LEAVE:
                noContinue = true;
                done = true;
                BarEventManager.getInstance().notifyWasInteractedWith(this);
                break;
        }
    }
}
