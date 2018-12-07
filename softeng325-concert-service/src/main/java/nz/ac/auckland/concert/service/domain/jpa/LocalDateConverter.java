package nz.ac.auckland.concert.service.domain.jpa;

import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
/**
 * AttributeConverter implementation that allows Java's time.LocalDate
 * objects to be persisted, via JPA, in a database. 
 *
 */
@Converter
public class LocalDateConverter implements AttributeConverter<LocalDate, Date> {
	
    
    public Date convertToDatabaseColumn(LocalDate locDateTime) {
    	return (locDateTime == null ? null : Date.valueOf(locDateTime));
    }

    
    public LocalDate convertToEntityAttribute(Date sqlTimestamp) {
    	return (sqlTimestamp == null ? null : sqlTimestamp.toLocalDate());
    }
}
