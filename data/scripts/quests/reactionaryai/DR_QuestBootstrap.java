package data.scripts.quests.reactionaryai;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.impl.campaign.intel.bar.events.BarEventManager;

/** Call register() from your existing BaseModPlugin.onGameLoad(). */
public final class DR_QuestBootstrap {

    private DR_QuestBootstrap() {}

    public static void register() {
        if (Global.getSector() == null) return;

        BarEventManager bar = BarEventManager.getInstance();
        if (!bar.hasEventCreator(DR_BarEventCreator.class)) {
            bar.addEventCreator(new DR_BarEventCreator());
        }

        Global.getSector().registerPlugin(new DR_CampaignPlugin());

        if (!Global.getSector().hasTransientScript(DR_QuestManager.class)) {
            Global.getSector().addTransientScript(new DR_QuestManager());
        }
    }
}
