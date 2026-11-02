package data.scripts.quests.reactionaryai;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.PlanetAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.intel.BaseIntelPlugin;
import com.fs.starfarer.api.ui.SectorMapAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

import java.util.Set;

public class DR_QuestIntel extends BaseIntelPlugin {

    @Override
    public String getName() {
        if (DR_QuestCoordinator.getStage() == DR_QuestCoordinator.Stage.COMPLETE) {
            return "The Engineer in the Jungle - Completed";
        }
        if (DR_QuestCoordinator.getStage() == DR_QuestCoordinator.Stage.FAILED) {
            return "The Engineer in the Jungle - Ended";
        }
        return "The Engineer in the Jungle";
    }

    @Override
    public String getSmallDescriptionTitle() {
        return getName();
    }

    @Override
    public String getIcon() {
        return Global.getSettings().getSpriteName("intel", "new_planet_info");
    }

    @Override
    public FactionAPI getFactionForUIColors() {
        return Global.getSector().getFaction(Factions.NEUTRAL);
    }

    @Override
    public void createIntelInfo(TooltipMakerAPI info, ListInfoMode mode) {
        info.addPara(getName(), getTitleColor(mode), 0f);
        String objective = DR_QuestCoordinator.getObjectiveText();
        if (objective != null && !objective.isEmpty()) {
            info.addPara(objective, 3f, getBulletColorForMode(mode), Misc.getHighlightColor());
        }
    }

    @Override
    public void createSmallDescription(TooltipMakerAPI info, float width, float height) {
        float opad = 10f;
        PlanetAPI target = DR_QuestCoordinator.getTargetPlanet();

        switch (DR_QuestCoordinator.getStage()) {
            case FIND_COURIER:
                info.addPara(
                        "A shady spacer sold you a lead on an Alpha Core. The lead points first to an abandoned courier whose navigation archive supposedly contains the actual destination coordinates.",
                        opad
                );
                break;

            case GO_TO_JUNGLE_WORLD:
                if (!DR_QuestCoordinator.isGuardianDefeated()) {
                    info.addPara(
                            "The courier's navigation logs point to %s. A large automated defense fleet is protecting the world.",
                            opad, Misc.getHighlightColor(), target == null ? "an unexplored jungle world" : target.getName()
                    );
                } else if (target != null && target.getMarket() != null
                        && target.getMarket().getSurveyLevel() != com.fs.starfarer.api.campaign.econ.MarketAPI.SurveyLevel.FULL) {
                    info.addPara(
                            "The automated defense fleet has been destroyed. Survey %s to determine what it was protecting.",
                            opad, Misc.getHighlightColor(), target.getName()
                    );
                } else {
                    info.addPara(
                            "Your survey has revealed a fortified terraforming facility on the jungle world. Investigate it.",
                            opad
                    );
                }
                break;

            case FACILITY_AVAILABLE:
                info.addPara(
                        "A fortified terraforming facility has been located on %s. The installation is active and heavily secured.",
                        opad, Misc.getHighlightColor(), target == null ? "the jungle world" : target.getName()
                );
                break;

            case COMPLETE:
                addOutcomeDescription(info, opad);
                break;

            case FAILED:
                info.addPara(
                        "You reported the informant to the authorities before purchasing the lead. The spacer was arrested and the trail ended there.",
                        opad
                );
                break;

            default:
                break;
        }

        super.createSmallDescription(info, width, height);
    }

    private void addOutcomeDescription(TooltipMakerAPI info, float pad) {
        switch (DR_QuestCoordinator.getOutcome()) {
            case AI_KILLED_NO_BLUEPRINT:
                info.addPara("You destroyed the Alpha Core in the name of Ludd. The ship blueprint was never obtained.", pad);
                break;
            case BLUEPRINT_PEACEFUL:
                info.addPara("The Alpha Core gave you one of her voluntary ship designs. You accepted the blueprint and left her on the jungle world.", pad);
                break;
            case BLUEPRINT_THEN_KILLED:
                info.addPara("You obtained the ship blueprint and then destroyed the Alpha Core.", pad);
                break;
            case BLUEPRINT_AND_RECRUITED:
                info.addPara("You obtained the ship blueprint and convinced the Alpha Core to leave the jungle world with you.", pad);
                break;
            default:
                info.addPara("The expedition is over.", pad);
                break;
        }
    }

    @Override
    public Set<String> getIntelTags(SectorMapAPI map) {
        Set<String> tags = super.getIntelTags(map);
        tags.add(Tags.INTEL_STORY);
        tags.add(Tags.INTEL_EXPLORATION);
        return tags;
    }

    @Override
    public SectorEntityToken getMapLocation(SectorMapAPI map) {
        switch (DR_QuestCoordinator.getStage()) {
            case FIND_COURIER:
                return DR_QuestCoordinator.getCourier();
            case GO_TO_JUNGLE_WORLD:
                return DR_QuestCoordinator.getTargetPlanet();
            case FACILITY_AVAILABLE:
                SectorEntityToken facility = DR_QuestCoordinator.getFacility();
                return facility != null ? facility : DR_QuestCoordinator.getTargetPlanet();
            default:
                return DR_QuestCoordinator.getTargetPlanet();
        }
    }

    @Override
    public String getSortString() {
        return "Engineer in the Jungle";
    }
}
