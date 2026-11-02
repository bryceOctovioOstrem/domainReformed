package data.scripts.quests.reactionaryai;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.util.IntervalUtil;

public class DR_QuestManager implements EveryFrameScript {

    private final IntervalUtil interval = new IntervalUtil(0.5f, 1f);

    @Override
    public void advance(float amount) {
        interval.advance(amount);
        if (interval.intervalElapsed()) {
            DR_QuestCoordinator.updateQuestWorldState();
        }
    }

    @Override
    public boolean isDone() {
        return false;
    }

    @Override
    public boolean runWhilePaused() {
        return false;
    }
}
