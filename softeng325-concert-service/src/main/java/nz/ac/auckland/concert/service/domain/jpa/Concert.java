package nz.ac.auckland.concert.service.domain.jpa;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.MapKeyColumn;
import javax.persistence.MapKeyEnumerated;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import java.util.Map;

import nz.ac.auckland.concert.common.dto.ConcertDTO;
import nz.ac.auckland.concert.common.jaxb.LocalDateTimeAdapter;
import nz.ac.auckland.concert.common.types.PriceBand;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * Persistence class to represent concerts. 
 * 
 * A Concert describes a concert in terms of:
 * _id           the unique identifier for a concert.
 * 
 * _title        the concert's title.
 * 
 * _dates        the concert's scheduled dates and times (represented as a 
 *               Set of LocalDateTime instances).
 *               
 * _tariff       concert pricing - the cost of a ticket for each price band 
 *               (A, B and C) is set individually for each concert. 
 *               
 * _performerIds identification of each performer playing at a concert 
 *               (represented as a set of performer identifiers).
 *
 */
@Entity
@Table(name = "CONCERTS")
public class Concert {
	
	@Id
	@GeneratedValue
	private Long _id;
	
	private String _title;
	
	@ElementCollection
	@CollectionTable(
			name = "CONCERT_DATES",
			joinColumns = @JoinColumn(name = "CONCERT_ID")
			)
	@Column(name = "DATE")
	@Convert(converter = LocalDateTimeConverter.class)
	private Set<LocalDateTime> _dates;
	
	@ElementCollection
	@CollectionTable(name = "CONCERT_TARIFS",
						joinColumns = @JoinColumn(name = "CONCERT_ID")
					)
	@MapKeyColumn(name = "PRICEBAND")
	@MapKeyEnumerated(EnumType.STRING)
	@Column(name = "PRICE")
	private Map<PriceBand, BigDecimal> _tariff;
	
	@ManyToMany(cascade = CascadeType.PERSIST, fetch = FetchType.LAZY)
	@JoinTable(
			name = "CONCERT_PERFORMER",
			joinColumns = @JoinColumn(name = "CONCERT_ID"),
			inverseJoinColumns = @JoinColumn(name = "PERFORMER_ID")
			)
	private Set<Performer> _performers;

	public Concert() {
	}

	public Concert(	String title, Set<LocalDateTime> dates,
					Map<PriceBand, BigDecimal> ticketPrices, Set<Performer> performers) {
		_title = title;
		_dates = new HashSet<LocalDateTime>(dates);
		_tariff = new HashMap<PriceBand, BigDecimal>(ticketPrices);
		_performers = new HashSet<Performer>(performers);
	}

	public Long getId() {
		return _id;
	}

	public String getTitle() {
		return _title;
	}
	
	public void setTitle(String title) {
		_title = title;
	}

	public Set<LocalDateTime> getDates() {
		return Collections.unmodifiableSet(_dates);
	}

	public BigDecimal getTicketPrice(PriceBand seatType) {
		return _tariff.get(seatType);
	}

	public Set<Performer> getPerformers() {
		return Collections.unmodifiableSet(_performers);
	}
	
	public Set<Long> getPerformerIDs() {
		Set<Long> returnedSet = new HashSet<Long>();
		for (Performer p :Collections.unmodifiableSet(_performers)) {
			returnedSet.add(p.getId());
		}
		
		return returnedSet;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Concert))
            return false;
        if (obj == this)
            return true;

        Concert rhs = (Concert) obj;
        return new EqualsBuilder().
            append(_id, rhs._id).
            append(_dates, rhs._dates).
            append(_tariff, rhs._tariff).
            isEquals();
	}
	
	@Override
	public int hashCode() {
		return new HashCodeBuilder(17, 31). 
	            append(_id).
	            append(_dates).
	            append(_tariff).
	            hashCode();
	}
	
	public ConcertDTO toDTO() {
		return new ConcertDTO(_id, _title, _dates, _tariff, getPerformerIDs());
	}
}
