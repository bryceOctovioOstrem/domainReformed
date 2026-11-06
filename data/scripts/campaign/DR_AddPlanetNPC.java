package data.scripts.campaign; 
// Defines the package path. Must match the folder structure.

import com.fs.starfarer.api.Global; 
// Provides access to the game world (Sector), factories, settings, etc.

import com.fs.starfarer.api.characters.FullName; 
// Gives access to gender enums and name structure.

import com.fs.starfarer.api.characters.PersonAPI; 
// Interface used for creating and modifying NPC characters.

import com.fs.starfarer.api.campaign.econ.MarketAPI; 
// Interface representing a colony/market (planets & stations).

public class DR_AddPlanetNPC { 
// Defines a utility class to hold our NPC creation code.

    public static void addNPC() { 
    // Static method so it can be called from the ModPlugin.

        // Fetch the market (planet) by its ID
        MarketAPI market = Global.getSector()        // Get the current game sector
                .getEconomy()                       // Access the economic layer
                .getMarket("newTerra");              // Look up a market by ID (CHANGE THIS)

        if (market == null) return; 
        // Safety check: prevents crashes if the ID is wrong.

        // Create a new character object
        PersonAPI npc = Global.getFactory().createPerson(); 
        // Uses the game factory to create a valid PersonAPI instance.

        npc.setFaction("hegemony");             
        // Sets the faction the NPC belongs to (affects dialog & hostility).

        npc.setRankId("citizen");               
        // Sets rank title (purely flavor text in most cases).

        npc.setPostId("administrator");         
        // Sets role/post — determines where they appear in UI & dialog.

        npc.getName().setFirst("Darius");       
        // Set first name.

        npc.getName().setLast("Rhett");         
        // Set last name.

        npc.setGender(FullName.Gender.MALE);    
        // Sets gender (affects pronouns & name formatting).

        npc.setPortraitSprite("graphics/portraits/portrait_hegemony04.png");  
        // Path to portrait image.

        npc.setPersonality("steady");           
        // Personality affects dialog tone and AI behavior.

        market.addPerson(npc);                  
        // Adds them to the market’s personnel list.

        market.setAdmin(npc);                   
        // OPTIONAL: assigns them as colony administrator.
        // Remove this line if you don't want that.

        // Add NPC to the market's comm directory
        market.getCommDirectory().addPerson(npc);  
        // REQUIRED: makes them visible in the comm directory.

    }
}
