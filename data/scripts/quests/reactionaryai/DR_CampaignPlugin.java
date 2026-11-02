package data.scripts.quests.reactionaryai;

import com.fs.starfarer.api.PluginPick;
import com.fs.starfarer.api.campaign.BaseCampaignPlugin;
import com.fs.starfarer.api.campaign.CampaignPlugin.PickPriority;
import com.fs.starfarer.api.campaign.InteractionDialogPlugin;
import com.fs.starfarer.api.campaign.SectorEntityToken;

public class DR_CampaignPlugin extends BaseCampaignPlugin {

    @Override
    public String getId() {
        return DR_QuestConfig.QUEST_ID + "_campaignPlugin";
    }

    @Override
    public boolean isTransient() {
        return true;
    }

    @Override
    public PluginPick<InteractionDialogPlugin> pickInteractionDialogPlugin(SectorEntityToken target) {
        if (target == null || target.getId() == null) return null;

        if (DR_QuestConfig.COURIER_ENTITY_ID.equals(target.getId())
                && DR_QuestCoordinator.getStage() == DR_QuestCoordinator.Stage.FIND_COURIER) {
            return new PluginPick<InteractionDialogPlugin>(new DR_CourierDialog(), PickPriority.MOD_SPECIFIC);
        }

        if (DR_QuestConfig.FACILITY_ENTITY_ID.equals(target.getId())) {
            return new PluginPick<InteractionDialogPlugin>(new DR_FacilityDialog(), PickPriority.MOD_SPECIFIC);
        }

        return null;
    }
}
