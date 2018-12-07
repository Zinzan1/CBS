package nz.ac.auckland.concert.service.domain.jpa;

import java.sql.Timestamp;
import java.time.LocalDateTime;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import nz.ac.auckland.concert.common.types.PriceBand;

@Converter
public class PriceBandConverter implements AttributeConverter<PriceBand, String>{
	public String convertToDatabaseColumn(PriceBand locDateTime) {
	    return (locDateTime == null ? null : locDateTime.toString());
	}

	    
	public PriceBand convertToEntityAttribute(String sqlTimestamp) {
	    return (sqlTimestamp == null ? null : PriceBand.valueOf(sqlTimestamp));
	}
}
