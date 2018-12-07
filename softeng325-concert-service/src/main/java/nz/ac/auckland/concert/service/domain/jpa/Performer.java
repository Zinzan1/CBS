package nz.ac.auckland.concert.service.domain.jpa;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import javax.persistence.Transient;

import nz.ac.auckland.concert.common.dto.ConcertDTO;
import nz.ac.auckland.concert.common.dto.PerformerDTO;
import nz.ac.auckland.concert.common.types.Genre;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * Persistence class to represent performers. 
 * 
 * A Performer describes a performer in terms of:
 * 
 * _id        	the unique identifier for a performer.
 * 
 * _genre      	the performer's genre
 * 
 * _imageName  	the name of an image file for the performer.
 * 
 * _name      	the performer's name.
 * 
 * _concerts 	identification of each concert in which the performer is 
 *            	 playing. 
 *             
 */

@Entity
@Table(name = "PERFORMERS")
public class Performer {

	@Id
	@GeneratedValue
	private Long _id;
	
	@Enumerated(EnumType.STRING)
	private Genre _genre;
	
	private String _imageName;
	
	private String _name;
	
	@ManyToMany(mappedBy = "_performers", fetch = FetchType.LAZY)
	private Set<Concert> _concerts;
	
	public Performer() {}
	
	public Performer(String name, String imageName, Genre genre, Set<Concert> concerts) {
		_name = name;
		_imageName = imageName;
		_genre = genre;
		_concerts = new HashSet<Concert>(concerts);
	}
	
	public Long getId() {
		return _id;
	}
	
	public Genre getGenre() {
		return _genre;
	}
	
	public void setGenre(Genre genre) {
		_genre = genre;
	}
	
	public String getName() {
		return _name;
	}
	
	public void setName(String name) {
		_name = name;
	}
	
	public String getImageName() {
		return _imageName;
	}
	
	public void setImageName(String imageName) {
		_imageName = imageName;
	}
	
	public Set<Concert> getConcerts() {
		return Collections.unmodifiableSet(_concerts);
	}
	
	public Set<Long> getConcertIDs() {
		Set<Long> returnedSet = new HashSet<Long>();
		for (Concert p :Collections.unmodifiableSet(_concerts)) {
			returnedSet.add(p.getId());
		}
		
		return returnedSet;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Performer))
            return false;
        if (obj == this)
            return true;

        Performer rhs = (Performer) obj;
        return new EqualsBuilder().
            append(_id, rhs._id).
            append(_imageName, rhs._imageName).
            append(_genre, rhs._genre).
            isEquals();
	}
	
	@Override
	public int hashCode() {
		return new HashCodeBuilder(17, 31). 
	            append(_id).
	            append(_imageName).
	            append(_genre).
	            hashCode();
	}
	
	public PerformerDTO toDTO() {
		return new PerformerDTO(_id, _name, _imageName, _genre, getConcertIDs());
	}
}
