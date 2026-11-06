package data.scripts.quests.reactionaryai;

import com.fs.starfarer.api.impl.campaign.intel.bar.PortsideBarEvent;
import com.fs.starfarer.api.impl.campaign.intel.bar.events.BaseBarEventCreator;

public class DR_BarEventCreator extends BaseBarEventCreator {
    @Override
    public PortsideBarEvent createBarEvent() {
        return new DR_BarEvent();
    }
}
