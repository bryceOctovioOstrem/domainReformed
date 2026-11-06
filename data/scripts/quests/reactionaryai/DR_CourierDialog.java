package data.scripts.quests.reactionaryai;

import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.PlanetAPI;
import com.fs.starfarer.api.util.Misc;

public class DR_CourierDialog extends DR_BaseDialog {

    private enum OptionId {
        INIT,
        RECOVER_LOGS,
        LEAVE
    }

    @Override
    public void init(InteractionDialogAPI dialog) {
        super.init(dialog);
        dialog.setOptionOnEscape("Leave", OptionId.LEAVE);
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
                        "The coordinates lead to a dead vessel drifting in a slow orbit. Most of the courier has been stripped by scavengers or opened to vacuum by old impacts, but a hardened navigation compartment remains intact behind several layers of emergency bulkhead. " +
                        "A boarding team reports that the computer is badly corrupted, though its last-route archive appears recoverable. Several empty bottles and scraps of hand-painted naval diagrams litter the surviving crew compartment, suggesting that whoever flew the ship was either very young, very bored, or both."
                );
                dialog.getOptionPanel().addOption("Recover the courier's navigation logs", OptionId.RECOVER_LOGS);
                dialog.getOptionPanel().addOption("Leave", OptionId.LEAVE);
                break;

            case RECOVER_LOGS:
                DR_QuestCoordinator.recoverCourierData();
                PlanetAPI target = DR_QuestCoordinator.getTargetPlanet();
                String planetName = target == null ? "an uncharted jungle world" : target.getName();
                String systemName = target == null || target.getStarSystem() == null
                        ? "a remote system"
                        : target.getStarSystem().getNameWithLowercaseTypeShort();

                dialog.getTextPanel().addPara(
                        "Your crew eventually reconstructs enough of the archive to identify a repeated destination. The surviving entries are years apart, but each route terminates at the same otherwise unremarkable body: " +
                        planetName + " in " + systemName + ". The files contain no explanation for the repeated trips, only old authentication strings, terraforming telemetry, and references to an automated installation on the surface."
                );
                dialog.getTextPanel().addPara(
                        "The jungle world's coordinates have been added to your Intel screen.",
                        Misc.getHighlightColor(), "Intel"
                );
                dialog.getOptionPanel().addOption("Leave", OptionId.LEAVE);
                break;

            case LEAVE:
                dialog.dismiss();
                break;
        }
    }
}
