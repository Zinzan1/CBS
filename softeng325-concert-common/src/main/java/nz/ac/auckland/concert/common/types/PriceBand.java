package nz.ac.auckland.concert.common.types;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * Enumerated type for classifying seats according to price bands.
 *
 */
@XmlRootElement
@XmlEnum(String.class)
public enum PriceBand {
	PriceBandA, PriceBandB, PriceBandC;
	
	public String value() {
		return name();
	}
	
	public static PriceBand fromValue(String string) {
		return valueOf(string);
	}
}
