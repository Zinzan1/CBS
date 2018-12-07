package nz.ac.auckland.concert.common.jaxb;

import java.math.BigDecimal;

import javax.xml.bind.annotation.XmlAttribute;

import nz.ac.auckland.concert.common.types.PriceBand;

class MapElements {
	@XmlAttribute
	private PriceBand _priceBandKey;
	
	@XmlAttribute
	private BigDecimal _costValue;
	
	private MapElements() {
    } 

    public MapElements(PriceBand key, BigDecimal value) {
    	_priceBandKey = key;
    	_costValue = value;
    }
    
    public PriceBand getKey() {
    	return _priceBandKey;
    }
    
    public BigDecimal getValue() {
    	return _costValue;
    }
}
