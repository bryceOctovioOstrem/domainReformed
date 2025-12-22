package data.scripts.campaign.econ.impl;

import com.fs.starfarer.api.impl.campaign.econ.impl.OrbitalStation;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.impl.campaign.ids.Industries;

public class DR_orbitalStationUpgradeChecker extends OrbitalStation {
    
        public boolean hasPort = false;
        
        @Override
        public boolean isAvailableToBuild() {
            if (market == null) {
                return false;
            }
        
            boolean canBuild = false;
            for (Industry ind : market.getIndustries()) {
                if (ind == this) continue;
		if (!ind.isFunctional()) continue;
		if (ind.getSpec().hasTag(Industries.TAG_SPACEPORT)) {
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
        
}
