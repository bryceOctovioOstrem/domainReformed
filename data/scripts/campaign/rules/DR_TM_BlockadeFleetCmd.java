package data.scripts.campaign.rules;

import java.util.List;
import java.util.Map;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.impl.campaign.rulecmd.BaseCommandPlugin;
import com.fs.starfarer.api.util.Misc;

import data.scripts.campaign.crisis.DR_TechMiningBlockadeManager;

public class DR_TM_BlockadeFleetCmd extends BaseCommandPlugin {

    public static final String OPT_ID = "DR_TM_BLOCKADE_COMMS";
    public static final String GLOBAL_CAN_TALK_LEADER = "$DR_canTalkToCrisisLeader";

    @Override
    public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Misc.Token> params,
                           Map<String, MemoryAPI> memoryMap) {

        if (dialog == null || dialog.getInteractionTarget() == null) return false;


        if (params == null || params.isEmpty()) return false;
        String mode = params.get(0).toString();
        if (mode != null) mode = mode.replace("\"", "").trim();


        // Only applies in fleet encounter dialogs
        if (!(dialog.getInteractionTarget() instanceof CampaignFleetAPI)) return false;

        CampaignFleetAPI fleet = (CampaignFleetAPI) dialog.getInteractionTarget();

        if ("populate".equals(mode)) {
            // Only add this option for our blockade fleets
            if (!fleet.getMemoryWithoutUpdate().getBoolean(DR_TechMiningBlockadeManager.DR_MEMKEY_BLOCKADE_FLEET)) {
                return false;
            }

            // Add comms option into the vanilla fleet-intercept UI
            if (!dialog.getOptionPanel().hasOption(OPT_ID)) {
                dialog.getOptionPanel().addOption("Open a comm link (blockade task force)", OPT_ID);
            }
            return true;
        }

        if ("selected".equals(mode)) {
            MemoryAPI local = dialog.getInteractionTarget().getMemoryWithoutUpdate();
            String selected = local.getString("$option");
            if (!OPT_ID.equals(selected)) return false;

            dialog.getTextPanel().addPara("");
            dialog.getTextPanel().addPara("You open a channel to the blockade flagship.");
            dialog.getTextPanel().addPara("A fleet admiral appears on the screen.");
            dialog.getTextPanel().addPara(
                    "“TEST: This task force is acting under direct orders. We do not have authority to negotiate.”",
                    Misc.getHighlightColor()
            );
            dialog.getTextPanel().addPara(
                    "“TEST: If you want the blockade lifted, speak to our leadership.”",
                    Misc.getHighlightColor()
            );

            // Unlock leader conversation (global breadcrumb)
            Global.getSector().getMemoryWithoutUpdate().set(GLOBAL_CAN_TALK_LEADER, true);

            return true;
        }

        return false;
    }
}
