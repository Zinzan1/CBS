package nz.ac.auckland.concert.common.jaxb;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.adapters.XmlAdapter;

import nz.ac.auckland.concert.common.types.PriceBand;

public class MapAdapter extends XmlAdapter<MapElements[], Map<PriceBand, BigDecimal>> {
	
	@Override
	public Map<PriceBand, BigDecimal> unmarshal(MapElements[] mapAsArray) throws Exception {
		
		Map<PriceBand, BigDecimal> returnedMap = new HashMap<PriceBand, BigDecimal>();
		for (MapElements mapElement : mapAsArray) {
        returnedMap.put(mapElement.getKey(), mapElement.getValue());
		}
		return returnedMap;
	}

	@Override
	public MapElements[] marshal(Map<PriceBand, BigDecimal> date) throws Exception {
		MapElements[] mapAsArray = new MapElements[date.size()];
		int i = 0;
		
		for(Map.Entry<PriceBand, BigDecimal> entry : date.entrySet()) {
			mapAsArray[i] = new MapElements(entry.getKey(), entry.getValue());
			i++;
		}
		
		return mapAsArray;
	}
}
