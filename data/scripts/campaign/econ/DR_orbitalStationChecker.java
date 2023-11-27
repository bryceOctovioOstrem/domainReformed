package data.campaign.econ.impl;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.impl.campaign.econ.impl.OrbitalStation;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.RepLevel;
import com.fs.starfarer.api.campaign.SectorAPI;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.Industries;

public class DR_orbitalStationChecker extends OrbitalStation {
    
        public boolean hasPort = false;
        
        @Override
        public boolean isAvailableToBuild() {
            SectorAPI sector = Global.getSector();
        
            FactionAPI player = sector.getFaction(Factions.PLAYER);
            FactionAPI DR = sector.getFaction("DR");
        
            boolean canBuild = false;
            for (Industry ind : market.getIndustries()) {
            	if (ind == this) continue;
            	if (!ind.isFunctional()) continue;
            	if ((ind.getSpec().hasTag(Industries.TAG_SPACEPORT)) && (Global.getSector().getPlayerFaction().knowsIndustry(getId()))) {
			canBuild = true;
			break;
            	}
            }
            return canBuild;
                
            //if (!Global.getSector().getPlayerFaction().knowsIndustry(getId()) && (!hasPort) ) {
            //	return false;
            //}
            //return market.getPlanetEntity() != null;
        }

	@Override
	public boolean isFunctional() {
		return super.isFunctional();// && Global.getSector().getPlayerFaction().knowsIndustry(getId());
	}
        
	@Override
	public boolean showWhenUnavailable() {
		return Global.getSector().getPlayerFaction().knowsIndustry(getId());
	}

    @Override
    public String getUnavailableReason() {
        return "Station type unavailable.";
    }

}
