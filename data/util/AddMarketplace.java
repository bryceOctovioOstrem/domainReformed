/**
 * Shorthand function for adding a market -- this is derived from tahlan mod
 */
package data.scripts.world;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.PlanetAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.econ.EconomyAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import java.util.ArrayList;
import java.util.List;

public class addMarketplace{

    public static MarketAPI addMarketplace(String factionID, SectorEntityToken primaryEntity, ArrayList<SectorEntityToken> connectedEntities, String name, 
                                    int size, ArrayList<String> marketConditions, ArrayList<String> Industries, ArrayList<String> submarkets, float tariff) {  
        EconomyAPI globalEconomy = Global.getSector().getEconomy();  
        String planetID = primaryEntity.getId();  
        String marketID = planetID;
              
        MarketAPI newMarket = Global.getFactory().createMarket(marketID, name, size);  
        newMarket.setFactionId(factionID);  
        newMarket.setPrimaryEntity(primaryEntity);  
        newMarket.getTariff().modifyFlat("generator", tariff);  
              
        if (null != submarkets){  
            for (String market : submarkets){  
                newMarket.addSubmarket(market);  
            }  
        }  
              
        for (String condition : marketConditions) {  
            newMarket.addCondition(condition);  
        }
        
        for (String industry : Industries) {
            newMarket.addIndustry(industry);
        }
              
        if (null != connectedEntities) {  
            for (SectorEntityToken entity : connectedEntities) {  
                newMarket.getConnectedEntities().add(entity);  
            }  
        }  
            
        globalEconomy.addMarket(newMarket, true);  
        primaryEntity.setMarket(newMarket);
        primaryEntity.setFaction(factionID);
              
        if (null != connectedEntities) {  
            for (SectorEntityToken entity : connectedEntities) {  
                entity.setMarket(newMarket);
                entity.setFaction(factionID);
            }  
        }
            
        return newMarket;
    }
}