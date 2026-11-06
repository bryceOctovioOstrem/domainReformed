package data.scripts.campaign.econ.impl;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.SectorAPI;
import com.fs.starfarer.api.campaign.econ.MarketImmigrationModifier;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.impl.campaign.DebugFlags;
import com.fs.starfarer.api.impl.campaign.econ.impl.Cryosanctum;

import static com.fs.starfarer.api.campaign.RepLevel.*;
import static com.fs.starfarer.api.campaign.rules.MemKeys.FACTION;
import static com.fs.starfarer.api.impl.campaign.econ.impl.BaseIndustry.getDeficitText;
import com.fs.starfarer.api.impl.campaign.econ.impl.MilitaryBase;

import static com.fs.starfarer.api.impl.campaign.econ.impl.GroundDefenses.DEFENSE_BONUS_BASE;
import static com.fs.starfarer.api.impl.campaign.econ.impl.MilitaryBase.DEFENSE_BONUS_MILITARY;
import static com.fs.starfarer.api.impl.campaign.econ.impl.MilitaryBase.DEFENSE_BONUS_PATROL;
import static com.fs.starfarer.api.impl.campaign.econ.impl.MilitaryBase.createPatrol;
import static com.fs.starfarer.api.impl.campaign.fleets.FleetFactory.PatrolType.FAST;
import static com.fs.starfarer.api.impl.campaign.fleets.FleetFactory.PatrolType.HEAVY;
import static com.fs.starfarer.api.impl.campaign.fleets.FleetFactory.PatrolType.COMBAT;

import com.fs.starfarer.api.impl.campaign.fleets.FleetFactory;
import com.fs.starfarer.api.impl.campaign.fleets.PatrolAssignmentAIV4;
import com.fs.starfarer.api.impl.campaign.fleets.RouteManager;
import com.fs.starfarer.api.impl.campaign.ids.*;
import com.fs.starfarer.api.util.Misc;
import data.scripts.campaign.econ.DR_industries;

import java.util.Random;

// TODO: Contains stuff to be checked after a Starsector update is released
public class WhiteskyePlayer extends MilitaryBase{
	
	
	
// Same as vanilla except fixes http://fractalsoftworks.com/forum/index.php?topic=8558.msg258499#msg258499
    @Override
    public CampaignFleetAPI spawnFleet(RouteManager.RouteData route)
    {
        PatrolFleetData custom = (PatrolFleetData) route.getCustom();
        FleetFactory.PatrolType type = custom.type;

        Random random = route.getRandom();

        CampaignFleetAPI fleet = createPatrol(HEAVY, "WhiteskyPlayer", route, market, null, random);// generates the fleet

        if (fleet == null || fleet.isEmpty())
        {
            return null;
        }


        fleet.addEventListener(this);
	
	

        market.getContainingLocation().addEntity(fleet);
        fleet.setFacing((float) Math.random() * 360f);
        // This will get overridden by the patrol assignment AI, depending on route-time elapsed etc
        fleet.setLocation(market.getPrimaryEntity().getLocation().x, market.getPrimaryEntity().getLocation().y);

        fleet.addScript(new PatrolAssignmentAIV4(fleet, route));

        //market.getContainingLocation().addEntity(fleet);
        //fleet.setLocation(market.getPrimaryEntity().getLocation().x, market.getPrimaryEntity().getLocation().y);
        if (custom.spawnFP <= 0)
        {
            custom.spawnFP = fleet.getFleetPoints();
        }

        return fleet;
    }
	@Override
	public boolean isAvailableToBuild() {
		return false;
	}

	public boolean showWhenUnavailable() {
		return false;
	}
		public static float DEFENSE_BONUS = .2f;
		public static float IMPROVE_STABILITY_BONUS = 1f; 
		 public void apply() {
		
		 int size = market.getSize();// gets market size 
		 demand(Commodities.SHIPS, size-1);//increases ore demand
		 demand(Commodities.FUEL, size-1);// increases organics demand
		demand(Commodities.HAND_WEAPONS, size-1);// increases organics demand
		 supply(Commodities.CREW, size-2);// increases marines production
         	supply(Commodities.MARINES, size-2);// increases marine demand
		applyIncomeAndUpkeep(3);
		float bonus = DEFENSE_BONUS;
		market.getStats().getDynamic().getMod(Stats.GROUND_DEFENSES_MOD).modifyMult(getModId(), 1f + bonus, getNameForModifier());
		market.getStability().modifyFlat("Whitesky Base", IMPROVE_STABILITY_BONUS, getNameForModifier() );
	 }
	 
	 
	// public static void syncMercRelationshipsToPlayer()
	// {
		// FactionAPI factionId = sector.getFaction("Whitesky"); // assigns a faction variable
		// if (factionId.equals(Factions.PLAYER)) return;
		
		// SectorAPI sector = Global.getSector();	
		// FactionAPI playerFaction = sector.getPlayerFaction();
		// FactionAPI faction = sector.getFaction(factionId);
		// //if (NexConfig.getFactionConfig(factionId).noSyncRelations)
			// //return;
		
		// for (FactionAPI otherFaction: sector.getAllFactions())
		// {
			// if (otherFaction != playerFaction && otherFaction != faction)
			// {
				// syncPlayerRelationshipToFaction(factionId, otherFaction.getId());
			// }
		// }
		
		// //syncFactionRelationshipsToPlayer(ExerelinConstants.PLAYER_NPC_ID);
		// //SectorManager.checkForVictory(); // already done in syncFactionRelationshipsToPlayer
	// }
}