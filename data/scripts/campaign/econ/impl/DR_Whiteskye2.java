package data.campaign.econ.industries;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.SectorAPI;
import com.fs.starfarer.api.campaign.econ.MarketImmigrationModifier;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.impl.campaign.DebugFlags;

import static com.fs.starfarer.api.campaign.RepLevel.*;
import static com.fs.starfarer.api.campaign.rules.MemKeys.FACTION;
import static com.fs.starfarer.api.impl.campaign.econ.impl.BaseIndustry.getDeficitText;
import com.fs.starfarer.api.impl.campaign.econ.impl.MilitaryBase;

import static com.fs.starfarer.api.impl.campaign.econ.impl.GroundDefenses.DEFENSE_BONUS_BASE;
import static com.fs.starfarer.api.impl.campaign.econ.impl.MilitaryBase.DEFENSE_BONUS_MILITARY;
import static com.fs.starfarer.api.impl.campaign.econ.impl.MilitaryBase.DEFENSE_BONUS_PATROL;
import static com.fs.starfarer.api.impl.campaign.econ.impl.MilitaryBase.createPatrol;
import static com.fs.starfarer.api.impl.campaign.fleets.FleetFactory.PatrolType.FAST;

import com.fs.starfarer.api.impl.campaign.fleets.FleetFactory;
import com.fs.starfarer.api.impl.campaign.fleets.PatrolAssignmentAIV4;
import com.fs.starfarer.api.impl.campaign.fleets.RouteManager;
import com.fs.starfarer.api.impl.campaign.ids.*;
import com.fs.starfarer.api.util.Misc;
import data.campaign.econ.DR_industries;

import java.util.Random;

// TODO: Contains stuff to be checked after a Starsector update is released
public class DR_Whiteskye extends MilitaryBase{
    /*// Copy of code from MilitaryBase.java; validate values on Starsector update
    protected static int[] getNumPatrolsForBase(int size, boolean patrol,
            boolean militaryBase, boolean command)
    {
        int light = 1, medium = 1, heavy = 2;

        if (patrol)
        {
            light = 2;
            medium = 0;
            heavy = 0;
        }
        else
        {
            if (size <= 3)
            {
                light = 2;
                medium = 0;
                heavy = 0;
            }
            else if (size == 4)
            {
                light = 3;
                medium = 0;
                heavy = 0;
            }
            else if (size == 5)
            {
                light = 3;
                medium = 0;
                heavy = 0;
            }
            else if (size == 6)
            {
                light = 3;
                medium = 1;
                heavy = 0;
            }
            else if (size == 7)
            {
                light = 4;
                medium = 1;
                heavy = 0;
            }
            else if (size == 8)
            {
                light = 4;
                medium = 2;
                heavy = 0;
            }
            else if (size >= 9)
            {
                light = 5;
                medium = 2;
                heavy = 0;
            }
        }

        if (militaryBase || command)
        {
            // Light++;
            medium = Math.max(medium + 1, size / 2 - 1);
            heavy = Math.max(heavy, medium - 1);
        }

        if (command)
        {
            medium++;
            heavy++;
        }

        return new int[] // creates array 
        {
            light, medium, heavy
        };
    }

    @Override
    public void apply()
    {
        updateSupplyAndDemandModifiers();

        updateIncomeAndUpkeep();

        applyAICoreModifiers();

        if (this instanceof MarketImmigrationModifier) // adds a modifier if large number of imagrents
        {
            market.addTransientImmigrationModifier((MarketImmigrationModifier) this);
        }

        int size = market.getSize();

        int extraDemand = 1;

        // Number of patrols
        int light = 1, medium = 1, heavy = 2;
        if (size <= 3)
        {
            light = 2;
            medium = 0;
            heavy = 0;
        }
        else if (size == 4)
        {
            light = 3;
            medium = 0;
            heavy = 0;
        }
        else if (size == 5)
        {
            light = 3;
            medium = 0;
            heavy = 0;
        }
        else if (size == 6)
        {
            light = 3;
            medium = 1;
            heavy = 0;
        }
        else if (size == 7)
        {
            light = 4;
            medium = 1;
            heavy = 0;
        }
        else if (size == 8)
        {
            light = 4;
            medium = 2;
            heavy = 0;
        }
        else if (size >= 9)
        {
            light = 5;
            medium = 2;
            heavy = 0;
        }

        medium = Math.max(medium + 1, size / 2 - 1);
        heavy = Math.max(heavy, medium - 1);

        // Subtract patrols if other structures are already generating them

        //market.getStats().getDynamic().getMod(Stats.PATROL_NUM_LIGHT_MOD).modifyFlat(getModId(), light);
        //market.getStats().getDynamic().getMod(Stats.PATROL_NUM_MEDIUM_MOD).modifyFlat(getModId(), medium);
        //market.getStats().getDynamic().getMod(Stats.PATROL_NUM_HEAVY_MOD).modifyFlat(getModId(), heavy);

        demand(Commodities.SUPPLIES, size - 1 + extraDemand);// modifies demand of goods
        demand(Commodities.FUEL, size - 1 + extraDemand);
        demand(Commodities.SHIPS, size - 1 + extraDemand);
        demand(Commodities.CREW, size - 1 + extraDemand);
        demand(Commodities.MARINES, size - 2 + extraDemand);
        demand(Commodities.HAND_WEAPONS, size - 2 + extraDemand);

        modifyStabilityWithBaseMod();

        float mult = getDeficitMult(Commodities.SUPPLIES);
        String extra = "";
        if (mult != 1)
        {
            String com = getMaxDeficit(Commodities.SUPPLIES).one;
            extra = " (" + getDeficitText(com).toLowerCase() + ")";
        }

        // Ground defense mod
        float bonus = DEFENSE_BONUS_BASE;

        if (bonus > 0)
        {
            market.getStats().getDynamic().getMod(Stats.GROUND_DEFENSES_MOD)
                    .modifyMult(getModId(), 1f + bonus * mult, getNameForModifier() + extra);
        }
        else
        {
            market.getStats().getDynamic().getMod(Stats.GROUND_DEFENSES_MOD).unmodify(getModId());
        }

        MemoryAPI memory = market.getMemoryWithoutUpdate();
        Misc.setFlagWithReason(memory, MemFlags.MARKET_PATROL, getModId(), true, -1);
        Misc.setFlagWithReason(memory, MemFlags.MARKET_MILITARY, getModId(), true, -1);

        if (!isFunctional())
        {
            supply.clear();
            unapply();
        }
    }

    // Same as vanilla except fixes http://fractalsoftworks.com/forum/index.php?topic=8558.msg258499#msg258499
    @Override
    public CampaignFleetAPI spawnFleet(RouteManager.RouteData route)
    {
        PatrolFleetData custom = (PatrolFleetData) route.getCustom();
        FleetFactory.PatrolType type = custom.type;

        Random random = route.getRandom();

        //CampaignFleetAPI fleet = createPatrol(FAST, Factions.Whitesky, route, market, null, random);// generates the fleet

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
    public boolean isAvailableToBuild() { // determines if building is avaliable to be built
        boolean hasCOND = false;

        //if (market.getPlanetEntity() != null && (market.hasIndustry(GMDA_industries.SEAGOON))) { // checks if mority defence is loaded
            //hasCOND = true;
        //}

        if (hasCOND) {
            return false;
        } else if (!hasCOND) {
            return true;
        }

        return false;
    }

    @Override
    public String getUnavailableReason() { // determines if another mercenary post is already built . depricate in future
        if (!super.isAvailableToBuild()) return super.getUnavailableReason();
        return "Another Mercenary outpost is already present.";
    }

    @Override
    public boolean showWhenUnavailable()// shows is unavaliable  
    {
        return Global.getSector().getPlayerFaction().knowsIndustry(getId());
    }

    @Override
    public String getCurrentImage()
    {
        return getSpec().getImageName();
    }*/
}