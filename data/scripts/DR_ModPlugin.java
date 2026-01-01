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
import data.scripts.campaign.econ.DR_industries;
//import 

public class DR_ModPlugin extends BaseModPlugin {

    @Override

    public void onNewGame() {
        newTerraGen(); // capital world
        MaistreGen();// prison world
        Ilyin();// industrial world
        Burke(); // farm world
        factionRelations(); // sets faction relations 
		TakamoriGen();
		Oshun();
		kulug();
		Mirfak();
		wangMangGen();
		Diocletian();
		wangMangTwo();
        BonaldGen();
        EvolaGen();
		
    }
    private void newTerraGen() {
        StarSystemAPI system = Global.getSector().createStarSystem("Metternich");
        PlanetAPI star = system.initStar("Metternich", "star_yellow", 1000, 7600, -4500, 250);
        //PlanetAPI star = system.initStar("Metternich", "star_yellow", 1000, -6200, -16500, 250);

        PlanetAPI planet = system.addPlanet("newTerra", star, "Confucius", "terran", -10, 180, 7000, 120);

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
        market.getTariff().modifyFlat("generator", 0.4f);
        //Planet surface/market conditions
        market.setPlanetConditionMarketOnly(false);
        market.addCondition(Conditions.HABITABLE);
        market.addCondition(Conditions.POPULATION_2);
        market.addCondition(Conditions.RUINS_VAST);
        // BREAK
        //market.addCondition(Conditions.ORGANICS_PLENTIFUL);
        market.addCondition(Conditions.FARMLAND_BOUNTIFUL);
        market.addCondition(Conditions.MILD_CLIMATE);
        market.addCondition(Conditions.REGIONAL_CAPITAL);
        market.addCondition(Conditions.HOT);
        market.addCondition(Conditions.SOLAR_ARRAY);
        market.setFactionId("DR");
        market.addIndustry(Industries.POPULATION);
        market.addIndustry(Industries.SPACEPORT);
        market.addIndustry(Industries.HIGHCOMMAND);
        //market.addIndustry(Industries.STARFORTRESS);
	    market.addIndustry(DR_industries.DR_STATION3);
        //market.addIndustry(Industries.PATROLHQ);
        market.addIndustry(Industries.HEAVYBATTERIES);
		market.addIndustry(DR_industries.DR_BLACKFLEET);


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
                true //The "withJunkAndChatter" flag. It will add space debris in orbit and radio chatter sound effects.*
        );

        //make asteroid belt surround it
        system.addAsteroidBelt(star, 150, 4800f, 250f, 1200, 250, Terrain.ASTEROID_BELT, "Astroid Belt");
		system.addRingBand(star, "misc", "rings_asteroids0", 256f, 0, Color.white, 256f, 4800f, 226f, null, null);
		system.addAsteroidBelt(star, 100, 2000f, 250f, 1200, 250, Terrain.ASTEROID_BELT, "Astroid Belt");
		system.addRingBand(star, "misc", "rings_asteroids0", 256f, 0, Color.white, 256f, 2000f, 226f, null, null);
		system.addAsteroidBelt(star, 150, 6900f, 250f, 1200, 250, Terrain.ASTEROID_BELT, "Astroid Belt");
		system.addRingBand(star, "misc", "rings_asteroids0", 256f, 0, Color.white, 256f, 6900f, 226f, null, null);
		system.addAsteroidBelt(star, 100, 9000f, 250f, 1200, 250, Terrain.ASTEROID_BELT, "Astroid Belt");
		system.addRingBand(star, "misc", "rings_asteroids0", 256f, 0, Color.white, 256f, 9000f, 226f, null, null);
        system.autogenerateHyperspaceJumpPoints(true, true);

        planet.setInteractionImage("illustrations", "confucius_planet");//adds illustration
		
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
        market.addCondition(Conditions.ORGANICS_COMMON);
        market.addCondition(Conditions.HOT);
		market.addCondition(Conditions.DISSIDENT);
        market.addCondition(Conditions.RUINS_SCATTERED);
        market.addCondition(Conditions.ORE_ULTRARICH);
        market.addCondition(Conditions.DECIVILIZED_SUBPOP);
		market.addCondition(Conditions.POLLUTION);
        market.addCondition(Conditions.RARE_ORE_ULTRARICH);
        market.addCondition(Conditions.INIMICAL_BIOSPHERE);
        market.addCondition(Conditions.FRONTIER);
        market.addCondition(Conditions.RARE_ORE_ULTRARICH);
        

        // industries
        market.addIndustry(Industries.MEGAPORT);
        //market.addIndustry(Industries.STARFORTRESS);
	    market.addIndustry(DR_industries.DR_STATION3);
        market.addIndustry(Industries.PATROLHQ);
        market.addIndustry(Industries.HEAVYBATTERIES);
        market.addIndustry(Industries.MINING);
        //market.addIndustry(Industries.HEAVYINDUSTRY);
		//market.addIndustry(DR_industries.WHITESKYE);
	    market.addIndustry(DR_industries.DR_WENDIGO);


        Industry MaistrePATROLHQ = market.getIndustry(Industries.PATROLHQ);// grabs the orbital 
        MaistrePATROLHQ.setSpecialItem(new SpecialItemData(Items.CRYOARITHMETIC_ENGINE , null));// adds a cryo engine
        Industry MaistreHEAVYBATTERIES = market.getIndustry(Industries.HEAVYBATTERIES);// grabs the heavy batteries
        MaistreHEAVYBATTERIES.setSpecialItem(new SpecialItemData(Items.DRONE_REPLICATOR , null));// adds a drone replicator

        //Industry MaistreWorks = market.getIndustry(Industries.HEAVYINDUSTRY);// grabs the orbital 
        //MaistreWorks.setSpecialItem(new SpecialItemData(Items.CORRUPTED_NANOFORGE, null));// adds a corrupted nano forge

        //Those rascally Tritachyon have set their tariffs to 15%!
        market.getTariff().modifyFlat("generator", 0.4f);
        //planet.setMarket(market);
        EconomyAPI globalEconomy = Global.getSector().getEconomy();
        globalEconomy.addMarket(
                market, //The market to add obviously!
                true //The "withJunkAndChatter" flag. It will add space debris in orbit and radio chatter sound effects.*
        );
        planet.setCustomDescriptionId("DR_planet_Maistre"); // adds planet description
		planet.setInteractionImage("illustrations", "Maistre_planet");//adds illustration
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
        market.addCondition(Conditions.POPULATION_4);
        market.setSize(4);
        market.addSubmarket(Submarkets.SUBMARKET_STORAGE);
        market.addSubmarket(Submarkets.SUBMARKET_BLACK);
        market.addSubmarket(Submarkets.SUBMARKET_OPEN);
        market.addCondition(Conditions.LARGE_REFUGEE_POPULATION);
        market.addCondition(Conditions.HABITABLE);
        market.addCondition(Conditions.COLD);
        market.addCondition(Conditions.RUINS_SCATTERED);
        market.addCondition(Conditions.ORE_SPARSE);
        //market.addCondition(Conditions.LARGE_REFUGEE_POPULATION);
        market.addCondition(Conditions.INDUSTRIAL_POLITY);
        market.addCondition(Conditions.ORGANIZED_CRIME);
        market.addCondition(Conditions.POLLUTION);
        market.addCondition(Conditions.INDUSTRIAL_POLITY);
		//market.addCondition(Conditions.DISSIDENT );

        // industries
        market.addIndustry(Industries.MEGAPORT);
        //market.addIndustry(Industries.STARFORTRESS);
		market.addIndustry(DR_industries.DR_STATION3);
        market.addIndustry(Industries.MILITARYBASE);
        market.addIndustry(Industries.HEAVYBATTERIES);
        market.addIndustry(Industries.ORBITALWORKS);
        //market.addIndustry(Industries.REFINING);
        market.addIndustry(Industries.WAYSTATION);
        //market.addIndustry(Industries.FUELPROD);
        //market.addIndustry(Industries.COMMERCE);
		market.addIndustry(DR_industries.DR_GENDARMERIE);

        // //add special items to industries WIP

        Industry ilynOrbitalWorks = market.getIndustry(Industries.ORBITALWORKS);// grabs the orbital 
        ilynOrbitalWorks.setSpecialItem(new SpecialItemData(Items.CORRUPTED_NANOFORGE, null));// adds a corrupted nano forge

        Industry IllyinPATROLHQ = market.getIndustry(Industries.MILITARYBASE);// grabs the orbital 
        IllyinPATROLHQ.setSpecialItem(new SpecialItemData(Items.CRYOARITHMETIC_ENGINE , null));// adds a cryo engine

        Industry ilynHEAVYBATTERIES = market.getIndustry(Industries.HEAVYBATTERIES);// grabs the heavy batteries
        ilynHEAVYBATTERIES.setSpecialItem(new SpecialItemData(Items.DRONE_REPLICATOR , null));// adds a drone replicator

        //Those rascally Tritachyon have set their tariffs to 15%!
        market.getTariff().modifyFlat("generator", 0.4f);
        //planet.setMarket(market);
        EconomyAPI globalEconomy = Global.getSector().getEconomy();
        globalEconomy.addMarket(
                market, //The market to add obviously!
                true //The "withJunkAndChatter" flag. It will add space debris in orbit and radio chatter sound effects.*
        );
        planet.setCustomDescriptionId("DR_planet_Ilyin"); // adds planet description
		planet.setInteractionImage("illustrations", "illyn_planet");//adds illustration
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
        market.addCondition(Conditions.POPULATION_3);
        market.setSize(3);
        market.addSubmarket(Submarkets.SUBMARKET_STORAGE);
        market.addSubmarket(Submarkets.SUBMARKET_BLACK);
        market.addSubmarket(Submarkets.SUBMARKET_OPEN);

        market.addCondition(Conditions.HABITABLE);
        market.addCondition(Conditions.FRONTIER);
        market.addCondition(Conditions.RUINS_WIDESPREAD);
        market.addCondition(Conditions.FARMLAND_POOR);
        market.addCondition(Conditions.HOT);
        market.addCondition(Conditions.RURAL_POLITY);
		//market.addCondition(Conditions.DISSIDENT);

        market.addCondition(Conditions.LARGE_REFUGEE_POPULATION);
        //market.addCondition(Conditions.INDUSTRIAL_POLITY);
        //market.addCondition(Conditions.LOW_GRAVITY);
        market.addCondition(Conditions.ORGANIZED_CRIME);
        market.addCondition(Conditions.POLLUTION);

        // industries
        market.addIndustry(Industries.MEGAPORT);
        //market.addIndustry(Industries.STARFORTRESS);
		market.addIndustry(DR_industries.DR_STATION3);
        market.addIndustry(Industries.MILITARYBASE );
        market.addIndustry(Industries.HEAVYBATTERIES);
       // market.addIndustry(Industries.HEAVYINDUSTRY);
        //market.addIndustry(Industries.REFINING);
        market.addIndustry(Industries.WAYSTATION);
        market.addIndustry(Industries.FARMING);
        //market.addIndustry(Industries.COMMERCE);
	//market.addIndustry(DR_industries.DR_WENDIGO);
        market.addIndustry(DR_industries.WHITESKYE);

        //Industry BurkeOrbitalWorks = market.getIndustry(Industries.HEAVYINDUSTRY);// grabs the orbital 
        //BurkeOrbitalWorks.setSpecialItem(new SpecialItemData(Items.CORRUPTED_NANOFORGE, null));// adds a corrupted nano forge
        Industry BurkePATROLHQ = market.getIndustry(Industries.MILITARYBASE);// grabs the orbital 
        BurkePATROLHQ.setSpecialItem(new SpecialItemData(Items.CRYOARITHMETIC_ENGINE , null));// adds a cryo engine

        Industry BurkeHEAVYBATTERIES = market.getIndustry(Industries.HEAVYBATTERIES);// grabs the heavy batteries
        BurkeHEAVYBATTERIES.setSpecialItem(new SpecialItemData(Items.DRONE_REPLICATOR , null));// adds a drone replicator
        //Those rascally Tritachyon have set their tariffs to 15%!
        market.getTariff().modifyFlat("generator", 0.4f);
        //planet.setMarket(market);
        EconomyAPI globalEconomy = Global.getSector().getEconomy();
		planet.setInteractionImage("illustrations", "bombard_tactical_result");//adds illustration
        globalEconomy.addMarket(
                market, //The market to add obviously!
                true //The "withJunkAndChatter" flag. It will add space debris in orbit and radio chatter sound effects.*
        );
        planet.setCustomDescriptionId("DR_planet_Burke"); // adds planet description
        system.updateAllOrbits();

    }
    public void factionRelations() {
        DR_Relations.generate(Global.getSector());

    }
		private void TakamoriGen() {
        StarSystemAPI system = Global.getSector().createStarSystem("Khomeini");

        PlanetAPI star = system.initStar("Khomeini", "star_white", 400, -5800, -17500, 200);
		
		PlanetAPI planet = system.addPlanet("takamori", star, "Takamori", "water", 120, 100, 4000, 100);
		
		MarketAPI market = Global.getFactory().createMarket(
                "Takamori_market", //market id
                planet.getName(), //market display name, usually the planet's name
                4 //market size
        );
        planet.setMarket(market);
        //Market global property settings
        market.setPrimaryEntity(planet);
		planet.setFaction("luddic_path");
		
		market.setPlanetConditionMarketOnly(false); //We are going to turn this into a proper colony and not just a "surface only".
        market.setFactionId("luddic_path");
        market.addIndustry(Industries.POPULATION);
        market.addCondition(Conditions.POPULATION_4);
        market.setSize(3);
        market.addSubmarket(Submarkets.SUBMARKET_STORAGE);
        market.addSubmarket(Submarkets.SUBMARKET_BLACK);
        market.addSubmarket(Submarkets.SUBMARKET_OPEN);

        market.addCondition(Conditions.HABITABLE);
        market.addCondition(Conditions.FRONTIER);
        market.addCondition(Conditions.LOW_GRAVITY);
        market.addCondition(Conditions.DECIVILIZED_SUBPOP);
        market.addCondition(Conditions.LUDDIC_MAJORITY);
        market.addCondition(Conditions.RURAL_POLITY);
		market.addCondition(Conditions.TECTONIC_ACTIVITY); // check first
		market.addCondition(Conditions.INIMICAL_BIOSPHERE);
        market.addCondition(Conditions.EXTREME_WEATHER);
        //market.addCondition(Conditions.LOW_GRAVITY);
        //market.addCondition(Conditions.FARMLAND_POOR);
        //market.addCondition(Conditions.DECIVILIZED_SUBPOP);

        //market.addCondition(Conditions.LARGE_REFUGEE_POPULATION);
        //market.addCondition(Conditions.INDUSTRIAL_POLITY);
        //market.addCondition(Conditions.LOW_GRAVITY);
        //market.addCondition(Conditions.ORGANIZED_CRIME);
        //market.addCondition(Conditions.POLLUTION);
		
		// industries
        market.addIndustry(Industries.AQUACULTURE);
        //market.addIndustry(Industries.STARFORTRESS);
		//market.addIndustry(DR_industries.DR_STATION3);
        market.addIndustry(Industries.PATROLHQ);
        market.addIndustry(Industries.SPACEPORT);
        //market.addIndustry(Industries.HEAVYINDUSTRY);
        //market.addIndustry(Industries.REFINING);
        //market.addIndustry(Industries.WAYSTATION);
        //market.addIndustry(Industries.FARMING);
        //market.addIndustry(Industries.COMMERCE);
		//market.addIndustry(DR_industries.DR_WENDIGO);
        //market.addIndustry(DR_industries.WHITESKYE);
		
        market.setSurveyLevel(MarketAPI.SurveyLevel.FULL); 
        //planet.setCustomDescriptionId("DR_planet_Burke"); // adds planet description
		JumpPointAPI jumpPoint = Global.getFactory().createJumpPoint("jumpOne", " Jupitor Jump-point"); // add new jump point
		jumpPoint.setCircularOrbit( system.getEntityById("Khomeini"), 120, 9000, 200);
		system.addEntity(jumpPoint);
		
		// JumpPointAPI jumpPointTwo = Global.getFactory().createJumpPoint("jumpTwo", "Neptune Jump-point"); // add new jump point
		// jumpPoint.setCircularOrbit( system.getEntityById("Khomeini"), 240 , 10000, 100);
		// system.addEntity(jumpPointTwo);
		
		// JumpPointAPI jumpPointThree = Global.getFactory().createJumpPoint("jumpThree", "Pluto Jump-point"); // add new jump point
		// jumpPoint.setCircularOrbit( system.getEntityById("Khomeini"), 360 , 9000, 100);
		// system.addEntity(jumpPointThree);
		
		system.autogenerateHyperspaceJumpPoints(false, true);
		EconomyAPI globalEconomy = Global.getSector().getEconomy();
		market.getTariff().modifyFlat("generator", 0.3f);
        globalEconomy.addMarket(
                market, //The market to add obviously!
                false //The "withJunkAndChatter" flag. It will add space debris in orbit and radio chatter sound effects.*
        );
		SectorEntityToken relay = system.addCustomEntity("Metternich_relay",null, "comm_relay_makeshift","DR");
        relay.setCircularOrbitPointingDown(star, 230, 7500, 265f);
		
		planet.setCustomDescriptionId("Takamori"); // adds planet description
		planet.setInteractionImage("illustrations", "takamori_planet");//adds illustration
        system.updateAllOrbits();
	}
	private void Oshun() {
        StarSystemAPI system = Global.getSector().getStarSystem("Khomeini");
        SectorEntityToken star = system.getStar();
        PlanetAPI planet = system.addPlanet("oshun", star, "Oshun","tundra", 300,220, 6300, 150);
        MarketAPI market = Global.getFactory().createMarket(
                "oshun_market", //market id
                planet.getName(), //market display name, usually the planet's name
                4 //market size
        );


        planet.setMarket(market);
        //Market global property settings
        market.setPrimaryEntity(planet);

        market.setSurveyLevel(MarketAPI.SurveyLevel.FULL);

        planet.setFaction("pirates");

        market.setPlanetConditionMarketOnly(false); //We are going to turn this into a proper colony and not just a "surface only".
        market.setFactionId("pirates");
        market.addIndustry(Industries.POPULATION);
        market.addCondition(Conditions.POPULATION_5);
        market.setSize(5);
        market.addSubmarket(Submarkets.SUBMARKET_STORAGE);
        market.addSubmarket(Submarkets.SUBMARKET_BLACK);
        market.addSubmarket(Submarkets.SUBMARKET_OPEN);

        market.addCondition(Conditions.HABITABLE);
        market.addCondition(Conditions.FRONTIER);
        market.addCondition(Conditions.RARE_ORE_SPARSE);
        market.addCondition(Conditions.FARMLAND_POOR);
        market.addCondition(Conditions.FREE_PORT);
        market.addCondition(Conditions.FARMLAND_POOR );
        market.addCondition(Conditions.ORE_ULTRARICH);
		market.addCondition(Conditions.IRRADIATED);
		market.addCondition(Conditions.ORGANICS_TRACE);

        market.addCondition(Conditions.INDUSTRIAL_POLITY );
        market.addCondition(Conditions.COLD);
        market.addCondition(Conditions.AI_CORE_ADMIN);
        market.addCondition(Conditions.EXTREME_WEATHER);
        //market.addCondition(Conditions.POLLUTION);

        // industries
        market.addIndustry(Industries.SPACEPORT);
        //market.addIndustry(Industries.STARFORTRESS);
		market.addIndustry(Industries.BATTLESTATION);
        market.addIndustry(Industries.MILITARYBASE);
        market.addIndustry(Industries.GROUNDDEFENSES);
        market.addIndustry(Industries.LIGHTINDUSTRY );
        market.addIndustry(Industries.MINING);
        //market.addIndustry(Industries.WAYSTATION);
        market.addIndustry(Industries.FARMING);
        //market.addIndustry(Industries.COMMERCE);
	//market.addIndustry(DR_industries.DR_WENDIGO);
        market.addIndustry(DR_industries.WHITESKYE);

		// beta cores
		market.getIndustry(Industries.MILITARYBASE).setAICoreId(Commodities.BETA_CORE);
		market.getIndustry(Industries.SPACEPORT).setAICoreId(Commodities.BETA_CORE);
		market.getIndustry(Industries.MINING).setAICoreId(Commodities.BETA_CORE);
		
		//alpha cores
		market.getIndustry(Industries.FARMING).setAICoreId(Commodities.ALPHA_CORE);
		market.getIndustry(Industries.LIGHTINDUSTRY).setAICoreId(Commodities.ALPHA_CORE);
		
		//gamma cores
		market.getIndustry(Industries.GROUNDDEFENSES).setAICoreId(Commodities.GAMMA_CORE);
		market.getIndustry(Industries.POPULATION).setAICoreId(Commodities.GAMMA_CORE);
		market.getIndustry(Industries.BATTLESTATION).setAICoreId(Commodities.GAMMA_CORE);
		
        // Industry BurkeOrbitalWorks = market.getIndustry(Industries.HEAVYINDUSTRY);// grabs the orbital 
        // BurkeOrbitalWorks.setSpecialItem(new SpecialItemData(Items.CORRUPTED_NANOFORGE, null));// adds a corrupted nano forge
        // Industry BurkePATROLHQ = market.getIndustry(Industries.PATROLHQ);// grabs the orbital 
        // BurkePATROLHQ.setSpecialItem(new SpecialItemData(Items.CRYOARITHMETIC_ENGINE , null));// adds a cryo engine

        // Industry BurkeHEAVYBATTERIES = market.getIndustry(Industries.HEAVYBATTERIES);// grabs the heavy batteries
        // BurkeHEAVYBATTERIES.setSpecialItem(new SpecialItemData(Items.DRONE_REPLICATOR , null));// adds a drone replicator
        //Those rascally Tritachyon have set their tariffs to 15%!
        market.getTariff().modifyFlat("generator", 0.4f);
        //planet.setMarket(market);
        EconomyAPI globalEconomy = Global.getSector().getEconomy();
        globalEconomy.addMarket(
                market, //The market to add obviously!
                true //The "withJunkAndChatter" flag. It will add space debris in orbit and radio chatter sound effects.*
        );
        planet.setCustomDescriptionId("OSHUN"); // adds planet description
		planet.setInteractionImage("illustrations", "oshun_planet");//adds illustration
        system.updateAllOrbits();

    }
	private void kulug() {
        StarSystemAPI system = Global.getSector().getStarSystem("Khomeini");
        SectorEntityToken star = system.getStar();
	system.addAsteroidBelt(star, 100, 2000f, 250f, 1200, 250, Terrain.ASTEROID_BELT, "Astroid Belt");
	system.addRingBand(star, "misc", "rings_asteroids0", 256f, 0, Color.white, 256f, 2000f, 226f, null, null);
	system.addAsteroidBelt(star, 150, 6900f, 250f, 1200, 250, Terrain.ASTEROID_BELT, "Astroid Belt");
	system.addRingBand(star, "misc", "rings_asteroids0", 256f, 0, Color.white, 256f, 6900f, 226f, null, null);
	system.addAsteroidBelt(star, 100, 9000f, 250f, 1200, 250, Terrain.ASTEROID_BELT, "Astroid Belt");
	system.addRingBand(star, "misc", "rings_asteroids0", 256f, 0, Color.white, 256f, 9000f, 226f, null, null);								
        PlanetAPI planet = system.addPlanet("kulug", star,"Justinian","desert", 100,180, 3000, 50);
        MarketAPI market = Global.getFactory().createMarket(
                "kulug_market", //market id
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
        market.addCondition(Conditions.POPULATION_3);
        market.setSize(3);
        market.addSubmarket(Submarkets.SUBMARKET_STORAGE);
        market.addSubmarket(Submarkets.SUBMARKET_BLACK);
        market.addSubmarket(Submarkets.SUBMARKET_OPEN);

        market.addCondition(Conditions.HABITABLE);
        market.addCondition(Conditions.FRONTIER);
        market.addCondition(Conditions.RUINS_SCATTERED);
        market.addCondition(Conditions.VOLATILES_PLENTIFUL);
		market.addCondition(Conditions.ORGANICS_TRACE);
        //market.addCondition(Conditions.RURAL_POLITY);
		
		market.addCondition(Conditions.HOT);

        market.addCondition(Conditions.URBANIZED_POLITY);
        //market.addCondition(Conditions.COLD);
        //market.addCondition(Conditions.EXTREME_WEATHER);
        market.addCondition(Conditions.ORGANIZED_CRIME);
        market.addCondition(Conditions.POLLUTION);

        // industries
        market.addIndustry(Industries.MEGAPORT);
        //market.addIndustry(Industries.STARFORTRESS);
		market.addIndustry(DR_industries.DR_STATION2);
        market.addIndustry(Industries.MILITARYBASE);
        market.addIndustry(Industries.HEAVYBATTERIES);

        market.addIndustry(Industries.MINING);
        market.addIndustry(Industries.WAYSTATION);

        market.addIndustry(DR_industries.RONIN);
		

         Industry KulugPATROLHQ = market.getIndustry(Industries.MILITARYBASE);// grabs the orbital 
         KulugPATROLHQ.setSpecialItem(new SpecialItemData(Items.CRYOARITHMETIC_ENGINE , null));// adds a cryo engine

        Industry KulugHEAVYBATTERIES = market.getIndustry(Industries.HEAVYBATTERIES);// grabs the heavy batteries
        KulugHEAVYBATTERIES.setSpecialItem(new SpecialItemData(Items.DRONE_REPLICATOR , null));// adds a drone replicator
        //Those rascally Tritachyon have set their tariffs to 15%!
        market.getTariff().modifyFlat("generator", 0.3f);
        //planet.setMarket(market);
        EconomyAPI globalEconomy = Global.getSector().getEconomy();
        globalEconomy.addMarket(
                market, //The market to add obviously!
                true //The "withJunkAndChatter" flag. It will add space debris in orbit and radio chatter sound effects.*
        );
        planet.setCustomDescriptionId("Kulug"); // adds planet description
		planet.setInteractionImage("illustrations", "Justinian_planet");//adds illustration
        system.updateAllOrbits();
	
    }
	    private void Mirfak() {
        StarSystemAPI system = Global.getSector().createStarSystem("Alpha Persei");

        PlanetAPI star = system.initStar("Alpha_Persei", "star_yellow",4000, -6000,12000,4000); 
		star.setCustomDescriptionId("alpha_persai");
		SectorEntityToken LouisStation = system.addCustomEntity("Louis_station", "Louis XVIII station", "station_hightech3", "DR");
		LouisStation.setCustomDescriptionId("LouisStation");
		LouisStation.setInteractionImage("illustrations", "Louis_lab");
		LouisStation.setCircularOrbitWithSpin(star, 220, 8000, 160, 3, 5);
		
		 JumpPointAPI jumpPointOne = Global.getFactory().createJumpPoint("jumpOne", "Alpha Jump-point"); // add new jump point
		 jumpPointOne.setCircularOrbit( system.getEntityById("Alpha_Persei"), 240 , 10000, 100);
		 system.addEntity(jumpPointOne);
		system.autogenerateHyperspaceJumpPoints(false,false);
		system.updateAllOrbits();
		
        SectorEntityToken relay = system.addCustomEntity("wangMang_relay",null, "comm_relay_makeshift","DR");// creates relay
        relay.setCircularOrbitPointingDown(star, 230, 10500, 265f); // sets relays orbit


		LouisStation.setFaction("DR");
        MarketAPI market = Global.getFactory().createMarket(
                "Louis_station", //market id
                LouisStation.getName(), //market display name, usually the planet's name
                5 //market size
        );
		
		LouisStation.setMarket(market);
        //Market global property settings
        market.setPrimaryEntity(LouisStation);

        market.setSurveyLevel(MarketAPI.SurveyLevel.FULL);

        LouisStation.setFaction("DR");
        //planet.setCustomDescriptionId("DR_planet_new_terra");
        market.setPlanetConditionMarketOnly(false); //We are going to turn this into a proper colony and not just a "surface only".
        market.setFactionId("DR");
        market.addIndustry(Industries.POPULATION);
        market.addCondition(Conditions.POPULATION_1);
        market.setSize(1);
        market.addSubmarket(Submarkets.SUBMARKET_STORAGE);
        market.addSubmarket(Submarkets.SUBMARKET_BLACK);
        market.addSubmarket(Submarkets.SUBMARKET_OPEN);
        market.addCondition(Conditions.FRONTIER);
        

        // industries
        market.addIndustry(Industries.SPACEPORT);
		//market.addIndustry(DR_industries.DR_STATION3);
        market.addIndustry(Industries.MILITARYBASE);
        market.addIndustry(Industries.HEAVYBATTERIES);
        market.addIndustry(Industries.TECHMINING);
        //market.addIndustry(Industries.FARMING);
		market.addIndustry(DR_industries.WHITESKYE);
		market.addIndustry(Industries.STARFORTRESS_HIGH);
		
        Industry LouisBase = market.getIndustry(Industries.MILITARYBASE);// grabs the orbital 
        LouisBase.setSpecialItem(new SpecialItemData(Items.CRYOARITHMETIC_ENGINE , null));// adds a cryo engine
		 
		Industry LouisHEAVYBATTERIES = market.getIndustry(Industries.HEAVYBATTERIES);// grabs the heavy batteries
        LouisHEAVYBATTERIES.setSpecialItem(new SpecialItemData(Items.DRONE_REPLICATOR , null));// adds a drone replicator
    
	    //Those rascally Tritachyon have set their tariffs to 15%!
        market.getTariff().modifyFlat("generator", 0.4f);
        //planet.setMarket(market);
        EconomyAPI globalEconomy = Global.getSector().getEconomy();
        globalEconomy.addMarket(
                market, //The market to add obviously!
                true //The "withJunkAndChatter" flag. It will add space debris in orbit and radio chatter sound effects.*
        );

        system.updateAllOrbits();	
		}

    private void wangMangGen() {
        StarSystemAPI system = Global.getSector().createStarSystem("wang mang");
        PlanetAPI star = system.initStar("Wang Mang", "star_red_dwarf", 200, -21000, -6900, 200);
		system.autogenerateHyperspaceJumpPoints(true,true);
        SectorEntityToken relay = system.addCustomEntity("wangMang_relay",null, "comm_relay_makeshift","DR");// creates relay
        relay.setCircularOrbitPointingDown(star, 230, 6500, 265f); // sets relays orbit
		system.addAsteroidBelt(star, 150, 6900f, 250f, 1200, 250, Terrain.ASTEROID_BELT, "Astroid Belt");
		system.addRingBand(star, "misc", "rings_asteroids0", 256f, 0, Color.white, 256f, 6900f, 226f, null, null);
		system.addAsteroidBelt(star, 100, 9000f, 250f, 1200, 250, Terrain.ASTEROID_BELT, "Astroid Belt");
		system.addRingBand(star, "misc", "rings_asteroids0", 256f, 0, Color.white, 256f, 9000f, 226f, null, null);
	}
	private void Diocletian() {
        StarSystemAPI system = Global.getSector().getStarSystem("wang mang");
        SectorEntityToken star = system.getStar();
		PlanetAPI planet = system.addPlanet("diocletian", star,"Diocletian","rocky_metallic", 60,60, 700, 120);
		
		planet.setFaction("DR");
        MarketAPI market = Global.getFactory().createMarket(
                "Diocletian", //market id
                planet.getName(), //market display name, usually the planet's name
                3 //market size
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
        market.addCondition(Conditions.POPULATION_2);
        market.setSize(2);
        market.addSubmarket(Submarkets.SUBMARKET_STORAGE);
        market.addSubmarket(Submarkets.SUBMARKET_BLACK);
        market.addSubmarket(Submarkets.SUBMARKET_OPEN);
        //market.addCondition(Conditions.ORGANICS_TRACE);
		market.addCondition(Conditions.ORE_SPARSE);
        market.addCondition(Conditions.HOT);
        market.addCondition(Conditions.RUINS_SCATTERED);
		market.addCondition(Conditions.POLLUTION);
        market.addCondition(Conditions.FRONTIER);
        market.addCondition(Conditions.NO_ATMOSPHERE);        
        market.addCondition(Conditions.INDUSTRIAL_POLITY);
		
        // industries
        market.addIndustry(Industries.MEGAPORT);
		market.addIndustry(DR_industries.DR_STATION3);
        market.addIndustry(Industries.MILITARYBASE);
        market.addIndustry(Industries.HEAVYBATTERIES);
        market.addIndustry(Industries.FUELPROD);
       // market.addIndustry(Industries.HEAVYINDUSTRY);
		market.addIndustry(DR_industries.WHITESKYE);
		
		
		//Industry DiocletianOrbitalWorks = market.getIndustry(Industries.HEAVYINDUSTRY);// grabs the orbital 
       // DiocletianOrbitalWorks.setSpecialItem(new SpecialItemData(Items.CORRUPTED_NANOFORGE, null));// adds a corrupted nano forge
		// adds catylatic fuel
		Industry DiocletianFUELPROD = market.getIndustry(Industries.FUELPROD);// grabs the fuel production 
        DiocletianFUELPROD.setSpecialItem(new SpecialItemData(Items.SYNCHROTRON, null));// adds a SYNCHROTRON 
    
	    //Those rascally Tritachyon have set their tariffs to 15%!
        market.getTariff().modifyFlat("generator", 0.4f);
        //planet.setMarket(market);
        EconomyAPI globalEconomy = Global.getSector().getEconomy();
        globalEconomy.addMarket(
                market, //The market to add obviously!
                true //The "withJunkAndChatter" flag. It will add space debris in orbit and radio chatter sound effects.*
        );
        //Those rascally Tritachyon have set their tariffs to 15%!
        market.getTariff().modifyFlat("generator", 0.15f);
        planet.setCustomDescriptionId("DiocletianDescription"); // adds planet description
		planet.setInteractionImage("illustrations", "vacuum_colony");//adds illustration
        system.updateAllOrbits();	
}
	private void wangMangTwo() {
        StarSystemAPI system = Global.getSector().getStarSystem("wang mang");
        SectorEntityToken star = system.getStar();
		PlanetAPI planet = system.addPlanet("wangMangII", star,"Wang Mang II","jungle", 70,70, 1600, 100);
		
		planet.setFaction("persean");
        MarketAPI market = Global.getFactory().createMarket(
                "wangMangTwo", //market id
                planet.getName(), //market display name, usually the planet's name
                5 //market size
        );
		
		planet.setMarket(market);
        //Market global property settings
        market.setPrimaryEntity(planet);

        market.setSurveyLevel(MarketAPI.SurveyLevel.FULL);

        planet.setFaction("independent");
        //planet.setCustomDescriptionId("DR_planet_new_terra");
        market.setPlanetConditionMarketOnly(false); //We are going to turn this into a proper colony and not just a "surface only".
        market.setFactionId("independent");
        market.addIndustry(Industries.POPULATION);
        market.addCondition(Conditions.POPULATION_5);
        market.setSize(5);
        market.addSubmarket(Submarkets.SUBMARKET_STORAGE);
        market.addSubmarket(Submarkets.SUBMARKET_BLACK);
        market.addSubmarket(Submarkets.SUBMARKET_OPEN);
        market.addCondition(Conditions.FARMLAND_ADEQUATE);
		market.addCondition(Conditions.ORE_SPARSE);
        market.addCondition(Conditions.VERY_HOT);
		market.addCondition(Conditions.LOW_GRAVITY);
        market.addCondition(Conditions.VOLATILES_DIFFUSE);
        market.addCondition(Conditions.FRONTIER);
		market.addCondition(Conditions.ORGANICS_COMMON);
		market.addCondition(Conditions.RURAL_POLITY);
        

        // industries
        market.addIndustry(Industries.MEGAPORT);
		//market.addIndustry(DR_industries.DR_STATION3);
        market.addIndustry(Industries.MILITARYBASE);
        market.addIndustry(Industries.GROUNDDEFENSES);
        market.addIndustry(Industries.MINING);
        market.addIndustry(Industries.FARMING);
		//market.addIndustry(DR_industries.WHITESKYE);
		market.addIndustry(Industries.BATTLESTATION_MID);
		
         Industry wangMangBase = market.getIndustry(Industries.MILITARYBASE);// grabs the orbital 
         wangMangBase.setSpecialItem(new SpecialItemData(Items.CRYOARITHMETIC_ENGINE , null));// adds a cryo engine
    
	    //Those rascally Tritachyon have set their tariffs to 15%!
        market.getTariff().modifyFlat("generator", 0.3f);
        //planet.setMarket(market);
        EconomyAPI globalEconomy = Global.getSector().getEconomy();
        globalEconomy.addMarket(
                market, //The market to add obviously!
                true //The "withJunkAndChatter" flag. It will add space debris in orbit and radio chatter sound effects.*
        );
        planet.setCustomDescriptionId("mang2desctiption"); // adds planet description
		//planet.setInteractionImage("illustrations", "diocletian_planet");//adds illustration
        system.updateAllOrbits();
        
        
	
}
        private void BonaldGen() {
        StarSystemAPI system = Global.getSector().getStarSystem("wang mang");
        SectorEntityToken star = system.getStar();
		//There are many star systems, though, and they are retrieved using their id, which happens to often be their name.
        PlanetAPI planet = system.addPlanet("Bonald", star, "Bonald", "frozen", 350, 200, 7000, 120);	
        MarketAPI market = Global.getFactory().createMarket(
			"Bonald", //market id
			planet.getName(), //market display name, usually the planet's name
			0 //market size
		);
        //planet.setFaction("DR");
        market.addCondition(Conditions.VOLATILES_DIFFUSE);
        market.addCondition(Conditions.VERY_COLD);
		//Misc.initConditionMarket(planet);	
        system.updateAllOrbits();
    }

        private void EvolaGen() {
        StarSystemAPI system = Global.getSector().getStarSystem("Khomeini");
        SectorEntityToken star = system.getStar();
		//There are many star systems, though, and they are retrieved using their id, which happens to often be their name.
        PlanetAPI planet = system.addPlanet("evola", star, "Evola", "frozen", 350, 200, 8000, 120);	
        MarketAPI market = Global.getFactory().createMarket(
			"Evola_market", //market id
			planet.getName(), //market display name, usually the planet's name
			0 //market size
		);
        //planet.setFaction("DR");
        market.addCondition(Conditions.VOLATILES_DIFFUSE);
        market.addCondition(Conditions.VERY_COLD);
		//Misc.initConditionMarket(planet);	
        system.updateAllOrbits();
    }
}