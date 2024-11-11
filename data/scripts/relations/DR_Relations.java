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
        DR.setRelationship(Factions.LUDDIC_CHURCH, -0.65f);
	//DR.setRelationship(Factions.independent, -0.45f);
        DR.setRelationship(Factions.LUDDIC_PATH, -0.99f);
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
        DR.setRelationship("MVS", 0.4f);
        DR.setRelationship("syndicate_asp", -0.74f);	   
        DR.setRelationship("cmc", -0.2f);        
        DR.setRelationship("SCY", -0.54f);     
        DR.setRelationship("neutrinocorp", 0.25f);
        DR.setRelationship("battlefleets_forcesofchaos", -0.70f);           
        DR.setRelationship("xhanempire", -0.55f);	
        DR.setRelationship("dassault_mikoyan", 0.4f);
        DR.setRelationship("scalartech", 0.05f);     
        DR.setRelationship("blackrock_driveyards", 0.5f);	   
        DR.setRelationship("uaf", -0.70f);
        DR.setRelationship("pn_colony", 0.69f);    
        DR.setRelationship("junk_pirates", -0.55f);  
        DR.setRelationship("ORA", -0.90f);          
        DR.setRelationship("shadow_industry", -0.95f);	
        DR.setRelationship("vic", 0.25f);     
        DR.setRelationship("interstellarimperium", -0.65f);     
        DR.setRelationship("prv", 0.42f);   
        DR.setRelationship("new_galactic_order", -0.6f);	
        DR.setRelationship("battlefleets_ork_pirates", -0.5f);
        DR.setRelationship("diableavionics", -0.2f);      
        DR.setRelationship("unitedpamed", -0.25f);     
        DR.setRelationship("rb", -0.5f);    
        DR.setRelationship("JYD", 0.11f);
	DR.setRelationship("legionarry", -0.5f);      
        // DR.setRelationship("Lte", 0.0f);   
        DR.setRelationship("gmda", -0.20f);   
        // DR.setRelationship("oculus", -0.25f);     
        // DR.setRelationship("nomads", -0.25f); 
        // DR.setRelationship("thulelegacy", -0.25f); 
        // DR.setRelationship("infected", -0.99f); 
	DR.setRelationship("fantasy_manufacturing", 0.15f);
	DR.setRelationship("Goat_Aviation_Bureau", 0.30f);
	DR.setRelationship("fob", -0.10f); 
	DR.setRelationship("batavia", -0.50f); 

	
    }

}