package data.scripts;
import java.awt.Color;
import java.util.List;


import com.fs.starfarer.api.campaign.*;
//import com.fs.starfarer.api.impl.campaign.procgen.NebulaEditor;
//import com.fs.starfarer.api.impl.campaign.procgen.themes.SalvageSpecialAssigner;
//import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.BaseModPlugin;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.econ.Industry;
//import com.fs.starfarer.api.campaign.CustomCampaignEntityAPI;
import com.fs.starfarer.api.campaign.PlanetAPI;
import com.fs.starfarer.api.campaign.SectorAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
// import com.fs.starfarer.api.impl.campaign.econ.impl.HeavyIndustry;
// import com.fs.starfarer.api.impl.campaign.econ.impl.InstallableItemEffect;
// import com.fs.starfarer.api.impl.campaign.econ.impl.ItemEffectsRepo;
import com.fs.starfarer.api.campaign.econ.EconomyAPI;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
// import com.fs.starfarer.api.campaign.econ.MarketConditionAPI;
// import com.fs.starfarer.api.impl.campaign.procgen.StarAge;
// import com.fs.starfarer.api.impl.campaign.procgen.StarSystemGenerator;
// import com.fs.starfarer.api.util.Misc;
// import com.fs.starfarer.api.campaign.SpecialItemData;
// import com.fs.starfarer.api.campaign.econ.EconomyAPI;
import com.fs.starfarer.api.impl.campaign.econ.impl.InstallableItemEffect;
import com.fs.starfarer.api.impl.campaign.econ.impl.ItemEffectsRepo;
import com.fs.starfarer.api.impl.campaign.ids.*;
import com.fs.starfarer.api.impl.campaign.ids.Items;
import com.fs.starfarer.api.campaign.SectorAPI;
//import com.fs.starfarer.api.impl.campaign.ids.Factions;
//import data.strings.descriptions;
import data.scripts.relations.DR_Relations;

public class DR_ModPlugin extends BaseModPlugin {

    @Override

    public void onNewGame() {
        newTerraGen(); // capital world
        MaistreGen();// prison world
        Ilyin();// industrial world
        Burke(); // farm world
        factionRelations(); // sets faction relations 

    }
    private void newTerraGen() {
        StarSystemAPI system = Global.getSector().createStarSystem("Metternich");

        PlanetAPI star = system.initStar("Metternich", "star_yellow", 1000, -7150, -14500, 250);

        PlanetAPI planet = system.addPlanet("newTerra", star, "New Terra", "terran", -10, 180, 4000, 120);

        planet.setFaction("DR");
        MarketAPI market = Global.getFactory().createMarket(
                "newTerra_market", //market id
                planet.getName(), //market display name, usually the planet's name
                2 //market size
        );

        planet.setMarket(market);
        planet.setCustomDescriptionId("DR_planet_new_terra");
        //Market global property settings
        market.setPrimaryEntity(planet);
        market.setSurveyLevel(MarketAPI.SurveyLevel.FULL);
        market.getTariff().modifyFlat("generator", 0.3f);
        //Planet surface/market conditions
        market.setPlanetConditionMarketOnly(false);
        market.addCondition(Conditions.HABITABLE);
        market.addCondition(Conditions.POPULATION_2);
        market.addCondition(Conditions.RUINS_VAST);
        //market.addCondition(Conditions.PATHER_CELLS);
        market.addCondition(Conditions.MILD_CLIMATE);
        // BREAK
        market.addCondition(Conditions.ORGANICS_PLENTIFUL);
        market.addCondition(Conditions.FARMLAND_BOUNTIFUL);
        market.addCondition(Conditions.RARE_ORE_MODERATE);
        market.addCondition(Conditions.URBANIZED_POLITY);
        market.addCondition(Conditions.REGIONAL_CAPITAL);
        market.addCondition(Conditions.HOT);
        market.addCondition(Conditions.SOLAR_ARRAY);
        market.setFactionId("DR");
        market.addIndustry(Industries.POPULATION);
        market.addIndustry(Industries.SPACEPORT);
        market.addIndustry(Industries.HIGHCOMMAND);
        market.addIndustry(Industries.STARFORTRESS);
        market.addIndustry(Industries.PATROLHQ);
        market.addIndustry(Industries.HEAVYBATTERIES);

        Industry newTerraPATROLHQ = market.getIndustry(Industries.HIGHCOMMAND);// grabs the orbital 
        newTerraPATROLHQ.setSpecialItem(new SpecialItemData(Items.CRYOARITHMETIC_ENGINE , null));// adds a cryo engine
        Industry newTerraHEAVYBATTERIES = market.getIndustry(Industries.HEAVYBATTERIES);// grabs the heavy batteries
        newTerraHEAVYBATTERIES.setSpecialItem(new SpecialItemData(Items.DRONE_REPLICATOR , null));// adds a drone replicator


        //MY INDUSTRIES
        //market.addIndustry(Industries.TECHMINING);

        //Planet sub-markets
        market.addSubmarket(Submarkets.SUBMARKET_STORAGE);
        market.addSubmarket(Submarkets.SUBMARKET_BLACK);
        market.addSubmarket(Submarkets.SUBMARKET_OPEN);




        //The market needs to be added to the global economy after adding sub-markets and industries.
        //If you don't do this, at best all commodities will be $1 because the colony has no idea how to price them.
        //At worst, the game will crash.
        EconomyAPI globalEconomy = Global.getSector().getEconomy();
        globalEconomy.addMarket(
                market, //The market to add obviously!
                false //The "withJunkAndChatter" flag. It will add space debris in orbit and radio chatter sound effects.*
        );

        //make asteroid belt surround it
        system.addAsteroidBelt(star, 800, 6000f, 150f, 180, 365, Terrain.ASTEROID_BELT, "");
        system.autogenerateHyperspaceJumpPoints(true, true);

        
        SectorEntityToken relay = system.addCustomEntity("Metternich_relay",null, "comm_relay_makeshift","DR");
        relay.setCircularOrbitPointingDown(star, 230, 6500, 265f);

    }
    private void MaistreGen() {
        StarSystemAPI system = Global.getSector().getStarSystem("Metternich");
        SectorEntityToken star = system.getStar();
        PlanetAPI planet = system.addPlanet("maistre", star, "Maistre", "jungle", 90, 175, 4001, 120);
        MarketAPI market = Global.getFactory().createMarket(
                "Maistre_market", //market id
                planet.getName(), //market display name, usually the planet's name
                2 //market size
        );


        planet.setMarket(market);
        //Market global property settings
        market.setPrimaryEntity(planet);

        market.setSurveyLevel(MarketAPI.SurveyLevel.FULL);

        planet.setFaction("DR");
        //planet.setCustomDescriptionId("DR_planet_new_terra");
        market.setPlanetConditionMarketOnly(false); //We are going to turn this into a proper colony and not just a "surface only".
        market.setFactionId("DR");
        market.addIndustry(Industries.POPULATION);
        market.addCondition(Conditions.POPULATION_3);
        market.setSize(3);
        market.addSubmarket(Submarkets.SUBMARKET_STORAGE);
        market.addSubmarket(Submarkets.SUBMARKET_BLACK);
        market.addSubmarket(Submarkets.SUBMARKET_OPEN);
        market.addCondition(Conditions.ORGANICS_PLENTIFUL);
        market.addCondition(Conditions.HOT);

        market.addCondition(Conditions.RUINS_SCATTERED);
        market.addCondition(Conditions.ORE_ULTRARICH);
        market.addCondition(Conditions.RARE_ORE_ULTRARICH);

        market.addCondition(Conditions.VOLATILES_ABUNDANT);
        market.addCondition(Conditions.INIMICAL_BIOSPHERE);
        market.addCondition(Conditions.FRONTIER);
        market.addCondition(Conditions.LOW_GRAVITY);
        
        

        // industries
        market.addIndustry(Industries.SPACEPORT);
        market.addIndustry(Industries.STARFORTRESS);
        market.addIndustry(Industries.PATROLHQ);
        market.addIndustry(Industries.HEAVYBATTERIES);
        market.addIndustry(Industries.MINING);
        market.addIndustry(Industries.HEAVYINDUSTRY);


        Industry MaistrePATROLHQ = market.getIndustry(Industries.PATROLHQ);// grabs the orbital 
        MaistrePATROLHQ.setSpecialItem(new SpecialItemData(Items.CRYOARITHMETIC_ENGINE , null));// adds a cryo engine
        Industry MaistreHEAVYBATTERIES = market.getIndustry(Industries.HEAVYBATTERIES);// grabs the heavy batteries
        MaistreHEAVYBATTERIES.setSpecialItem(new SpecialItemData(Items.DRONE_REPLICATOR , null));// adds a drone replicator

        Industry MaistreWorks = market.getIndustry(Industries.HEAVYINDUSTRY);// grabs the orbital 
        MaistreWorks.setSpecialItem(new SpecialItemData(Items.CORRUPTED_NANOFORGE, null));// adds a corrupted nano forge

        //Those rascally Tritachyon have set their tariffs to 15%!
        market.getTariff().modifyFlat("generator", 0.15f);
        //planet.setMarket(market);
        EconomyAPI globalEconomy = Global.getSector().getEconomy();
        globalEconomy.addMarket(
                market, //The market to add obviously!
                false //The "withJunkAndChatter" flag. It will add space debris in orbit and radio chatter sound effects.*
        );
        //Those rascally Tritachyon have set their tariffs to 15%!
        market.getTariff().modifyFlat("generator", 0.15f);
        planet.setCustomDescriptionId("DR_planet_Maistre"); // adds planet description
        //system.updateAllOrbits();
    }

    private void Ilyin() {
        StarSystemAPI system = Global.getSector().getStarSystem("Metternich");
        SectorEntityToken star = system.getStar();
        PlanetAPI planet = system.addPlanet("ilyin", star, "Ilyin", "tundra", 180, 180, 4002, 120);
        MarketAPI market = Global.getFactory().createMarket(
                "ilyin_market", //market id
                planet.getName(), //market display name, usually the planet's name
                5 //market size
        );


        planet.setMarket(market);
        //Market global property settings
        market.setPrimaryEntity(planet);

        market.setSurveyLevel(MarketAPI.SurveyLevel.FULL);

        planet.setFaction("DR");

        market.setPlanetConditionMarketOnly(false); //We are going to turn this into a proper colony and not just a "surface only".
        market.setFactionId("DR");
        market.addIndustry(Industries.POPULATION);
        market.addCondition(Conditions.POPULATION_3);
        market.setSize(5);
        market.addSubmarket(Submarkets.SUBMARKET_STORAGE);
        market.addSubmarket(Submarkets.SUBMARKET_BLACK);
        market.addSubmarket(Submarkets.SUBMARKET_OPEN);
        market.addCondition(Conditions.LARGE_REFUGEE_POPULATION);
        market.addCondition(Conditions.HABITABLE);
        market.addCondition(Conditions.COLD);
        market.addCondition(Conditions.RUINS_SCATTERED);
        market.addCondition(Conditions.ORE_SPARSE);

        market.addCondition(Conditions.LARGE_REFUGEE_POPULATION);
        market.addCondition(Conditions.INDUSTRIAL_POLITY);
        //market.addCondition(Conditions.LOW_GRAVITY);
        market.addCondition(Conditions.ORGANIZED_CRIME);
        market.addCondition(Conditions.POLLUTION);
        market.addCondition(Conditions.INDUSTRIAL_POLITY);

        // industries
        market.addIndustry(Industries.MEGAPORT);
        market.addIndustry(Industries.STARFORTRESS);
        market.addIndustry(Industries.PATROLHQ);
        market.addIndustry(Industries.HEAVYBATTERIES);
        market.addIndustry(Industries.ORBITALWORKS);
        market.addIndustry(Industries.REFINING);
        market.addIndustry(Industries.WAYSTATION);
        market.addIndustry(Industries.FUELPROD);
        //market.addIndustry(Industries.COMMERCE);

        // //add special items to industries WIP

        Industry ilynOrbitalWorks = market.getIndustry(Industries.ORBITALWORKS);// grabs the orbital 
        ilynOrbitalWorks.setSpecialItem(new SpecialItemData(Items.CORRUPTED_NANOFORGE, null));// adds a corrupted nano forge

        Industry ilynHEAVYBATTERIES = market.getIndustry(Industries.HEAVYBATTERIES);// grabs the heavy batteries
        ilynHEAVYBATTERIES.setSpecialItem(new SpecialItemData(Items.DRONE_REPLICATOR , null));// adds a drone replicator

        //Those rascally Tritachyon have set their tariffs to 15%!
        market.getTariff().modifyFlat("generator", 0.15f);
        //planet.setMarket(market);
        EconomyAPI globalEconomy = Global.getSector().getEconomy();
        globalEconomy.addMarket(
                market, //The market to add obviously!
                false //The "withJunkAndChatter" flag. It will add space debris in orbit and radio chatter sound effects.*
        );
        //Those rascally Tritachyon have set their tariffs to 15%!
        market.getTariff().modifyFlat("generator", 0.15f);
        planet.setCustomDescriptionId("DR_planet_Ilyin"); // adds planet description
        system.updateAllOrbits();

    }
    private void Burke() {
        StarSystemAPI system = Global.getSector().getStarSystem("Metternich");
        SectorEntityToken star = system.getStar();
        PlanetAPI planet = system.addPlanet("burke", star, "Burke","arid", 270, 180, 4004, 120);
        MarketAPI market = Global.getFactory().createMarket(
                "Burke_market", //market id
                planet.getName(), //market display name, usually the planet's name
                4 //market size
        );


        planet.setMarket(market);
        //Market global property settings
        market.setPrimaryEntity(planet);

        market.setSurveyLevel(MarketAPI.SurveyLevel.FULL);

        planet.setFaction("DR");

        market.setPlanetConditionMarketOnly(false); //We are going to turn this into a proper colony and not just a "surface only".
        market.setFactionId("DR");
        market.addIndustry(Industries.POPULATION);
        market.addCondition(Conditions.POPULATION_4);
        market.setSize(4);
        market.addSubmarket(Submarkets.SUBMARKET_STORAGE);
        market.addSubmarket(Submarkets.SUBMARKET_BLACK);
        market.addSubmarket(Submarkets.SUBMARKET_OPEN);

        market.addCondition(Conditions.HABITABLE);
        market.addCondition(Conditions.FRONTIER);
        market.addCondition(Conditions.RUINS_WIDESPREAD);
        market.addCondition(Conditions.FARMLAND_POOR);
        market.addCondition(Conditions.DECIVILIZED_SUBPOP);
        market.addCondition(Conditions.HOT);
        market.addCondition(Conditions.RURAL_POLITY);

        market.addCondition(Conditions.LARGE_REFUGEE_POPULATION);
        //market.addCondition(Conditions.INDUSTRIAL_POLITY);
        //market.addCondition(Conditions.LOW_GRAVITY);
        market.addCondition(Conditions.ORGANIZED_CRIME);
        market.addCondition(Conditions.POLLUTION);

        // industries
        market.addIndustry(Industries.MEGAPORT);
        market.addIndustry(Industries.STARFORTRESS);
        market.addIndustry(Industries.PATROLHQ);
        market.addIndustry(Industries.HEAVYBATTERIES);
        market.addIndustry(Industries.HEAVYINDUSTRY);
        //market.addIndustry(Industries.REFINING);
        market.addIndustry(Industries.WAYSTATION);
        market.addIndustry(Industries.FARMING);
        //market.addIndustry(Industries.COMMERCE);

        Industry BurkeOrbitalWorks = market.getIndustry(Industries.HEAVYINDUSTRY);// grabs the orbital 
        BurkeOrbitalWorks.setSpecialItem(new SpecialItemData(Items.CORRUPTED_NANOFORGE, null));// adds a corrupted nano forge
        Industry BurkePATROLHQ = market.getIndustry(Industries.PATROLHQ);// grabs the orbital 
        BurkePATROLHQ.setSpecialItem(new SpecialItemData(Items.CRYOARITHMETIC_ENGINE , null));// adds a cryo engine

        Industry BurkeHEAVYBATTERIES = market.getIndustry(Industries.HEAVYBATTERIES);// grabs the heavy batteries
        BurkeHEAVYBATTERIES.setSpecialItem(new SpecialItemData(Items.DRONE_REPLICATOR , null));// adds a drone replicator
        //Those rascally Tritachyon have set their tariffs to 15%!
        market.getTariff().modifyFlat("generator", 0.15f);
        //planet.setMarket(market);
        EconomyAPI globalEconomy = Global.getSector().getEconomy();
        globalEconomy.addMarket(
                market, //The market to add obviously!
                false //The "withJunkAndChatter" flag. It will add space debris in orbit and radio chatter sound effects.*
        );
        planet.setCustomDescriptionId("DR_planet_Burke"); // adds planet description
        system.updateAllOrbits();

    }
    public void factionRelations() {
        DR_Relations.generate(Global.getSector());

    }

}