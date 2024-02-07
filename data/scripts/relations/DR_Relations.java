package data.scripts.relations;

import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.campaign.SectorAPI;
import com.fs.starfarer.api.campaign.SectorGeneratorPlugin;
import com.fs.starfarer.api.campaign.FactionAPI;
import  data.scripts.DR_ModPlugin;
import com.fs.starfarer.api.impl.campaign.shared.SharedData;

public class DR_Relations implements SectorGeneratorPlugin {
    @Override
    public static void generate(SectorAPI sector) {
        FactionAPI DR = sector.getFaction("DR"); // assigns a faction variable
        SharedData.getData().getPersonBountyEventData().addParticipatingFaction("DR");
        DR.setRelationship(Factions.LUDDIC_CHURCH, -0.55f);
	//DR.setRelationship(Factions.independent, -0.50f);
        DR.setRelationship(Factions.LUDDIC_PATH, -1.0f);
        DR.setRelationship(Factions.PERSEAN, -0.8f);
        DR.setRelationship(Factions.PIRATES, -0.5f);
        DR.setRelationship("sindrian_diktat", -0.69f);
        DR.setRelationship("hegemony", 0.9f);
        DR.setRelationship("tritachyon", -0.72f); 
        // mods
        DR.setRelationship("hcok", -0.7f);    
        DR.setRelationship("ironshell", 0.75f);       
        DR.setRelationship("battlefleets_imperium", -0.54f); 
        DR.setRelationship("sevencorp", -0.5f);	 
        DR.setRelationship("xlu", -0.75f);	 
        DR.setRelationship("GKSec", -.15f);	 
        DR.setRelationship("MVS", 0.5f);
        DR.setRelationship("syndicate_asp", -0.74f);	   
        DR.setRelationship("cmc", -0.2f);        
        DR.setRelationship("SCY", 0.5f);     
        DR.setRelationship("neutrinocorp", 0.25f);
        DR.setRelationship("battlefleets_forcesofchaos", -0.70f);           
        DR.setRelationship("xhanempire", -0.55f);	
        DR.setRelationship("dassault_mikoyan", 0.5f);
        DR.setRelationship("scalartech", 0.05f);     
        DR.setRelationship("blackrock_driveyards", 0.5f);	   
        DR.setRelationship("uaf", -0.10f);
        DR.setRelationship("pn_colony", 0.69f);    
        DR.setRelationship("junk_pirates", -0.25f);  
        DR.setRelationship("ORA", -0.90f);          
        DR.setRelationship("shadow_industry", -0.95f);	
        DR.setRelationship("vic", 0.7f);     
        DR.setRelationship("interstellarimperium", -0.65f);     
        DR.setRelationship("prv", 0.52f);   
        DR.setRelationship("new_galactic_order", -0.6f);	
        DR.setRelationship("battlefleets_ork_pirates", -0.5f);
        DR.setRelationship("diableavionics", -0.2f);      
        DR.setRelationship("unitedpamed", -0.25f);     
        DR.setRelationship("rb", -0.5f);    
        DR.setRelationship("JYD", 0.11f);
	DR.setRelationship("legionarry", -0.5f);      
        // DR.setRelationship("Lte", 0.0f);   
        DR.setRelationship("gmda", 0.05f);   
        // DR.setRelationship("oculus", -0.25f);     
        // DR.setRelationship("nomads", -0.25f); 
        // DR.setRelationship("thulelegacy", -0.25f); 
        // DR.setRelationship("infected", -0.99f); 
	DR.setRelationship("fantasy_manufacturing", 0.15f);
	DR.setRelationship("Goat_Aviation_Bureau", 0.30f);
	DR.setRelationship("fob", -0.10f); 
	DR.setRelationship("batavia", -0.50f); 

	// relations for whiteskye
	FactionAPI WS = sector.getFaction("Whitesky"); // assigns a faction variable
        SharedData.getData().getPersonBountyEventData().addParticipatingFaction("DR");
        WS.setRelationship(Factions.LUDDIC_CHURCH, -0.55f);
	//WS.setRelationship(Factions.independent, -0.50f);
        WS.setRelationship(Factions.LUDDIC_PATH, -1.0f);
        WS.setRelationship(Factions.PERSEAN, -0.8f);
        WS.setRelationship(Factions.PIRATES, -0.5f);
        WS.setRelationship("sindrian_diktat", -0.69f);
        WS.setRelationship("hegemony", 0.9f);
        WS.setRelationship("tritachyon", -0.72f); 
        // mods
        WS.setRelationship("hcok", -0.7f);    
        WS.setRelationship("ironshell", 0.75f);       
        WS.setRelationship("battlefleets_imperium", -0.54f); 
        WS.setRelationship("sevencorp", -0.5f);	 
        WS.setRelationship("xlu", -0.75f);	 
        WS.setRelationship("GKSec", -.15f);	 
        WS.setRelationship("MVS", 0.5f);
        WS.setRelationship("syndicate_asp", -0.74f);	   
        WS.setRelationship("cmc", -0.2f);        
        WS.setRelationship("SCY", 0.5f);     
        WS.setRelationship("neutrinocorp", 0.25f);
        WS.setRelationship("battlefleets_forcesofchaos", -0.70f);           
        WS.setRelationship("xhanempire", -0.55f);	
        WS.setRelationship("dassault_mikoyan", 0.5f);
        WS.setRelationship("scalartech", 0.05f);     
        WS.setRelationship("blackrock_Driveyards", 0.5f);	   
        WS.setRelationship("uaf", -0.10f);
        WS.setRelationship("pn_colony", 0.69f);    
        WS.setRelationship("junk_pirates", -0.25f);  
        WS.setRelationship("ORA", -0.90f);          
        WS.setRelationship("shadow_industry", -0.95f);	
        WS.setRelationship("vic", 0.7f);     
        WS.setRelationship("interstellarimperium", -0.65f);     
        WS.setRelationship("prv", 0.52f);   
        WS.setRelationship("new_galactic_order", -0.6f);	
        WS.setRelationship("battlefleets_ork_pirates", -0.5f);
        WS.setRelationship("diableavionics", -0.2f);      
        WS.setRelationship("unitedpamed", -0.25f);     
        WS.setRelationship("rb", -0.5f);    
        WS.setRelationship("JYD", 0.11f);
	WS.setRelationship("legionarry", -0.5f);      
        // WS.setRelationship("Lte", 0.0f);   
        WS.setRelationship("gmda", 0.05f);   
        // WS.setRelationship("oculus", -0.25f);     
        // WS.setRelationship("nomads", -0.25f); 
        // WS.setRelationship("thulelegacy", -0.25f); 
        // WS.setRelationship("infected", -0.99f); 
	WS.setRelationship("fantasy_manufacturing", 0.15f);
	WS.setRelationship("Goat_Aviation_Bureau", 0.30f);
	WS.setRelationship("fob", -0.10f); 
	WS.setRelationship("batavia", -0.50f);   
    }

}