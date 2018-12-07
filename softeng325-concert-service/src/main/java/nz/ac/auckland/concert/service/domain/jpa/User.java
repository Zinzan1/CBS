package nz.ac.auckland.concert.service.domain.jpa;

import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import nz.ac.auckland.concert.common.dto.ConcertDTO;
import nz.ac.auckland.concert.common.dto.UserDTO;

/**
 * Persistence class to represent users. 
 * 
 * A User describes a user in terms of:
 * 
 * _username 	the user's unique username.
 * 
 * _password  	the user's password.
 * 
 * _firstname 	the user's first name.
 * 
 * _lastname  	the user's family name.
 * 
 * _creditCard 	the user's registered credit card
 * 
 * _bookings	the bookings associated with the User (confirmed reservations)
 *
 */

@Entity
@Table(name = "USER")
public class User {
	@Id
	@GeneratedValue
	private Long _id;

	private String _username;
	private String _password;
	private String _firstname;
	private String _lastname;
	
	@ManyToOne(fetch = FetchType.EAGER, cascade = {CascadeType.PERSIST, CascadeType.REMOVE})
	@JoinTable(name = "CREDITCARD_USERS",
			joinColumns = @JoinColumn(name = "USER_ID"),
			inverseJoinColumns = @JoinColumn(name = "CreditCardNumber")
	)
	private CreditCard _creditCard;
	
	@OneToMany(mappedBy="_userBelongsTo" ,cascade=CascadeType.REMOVE, orphanRemoval=true)
	private Set<Booking> _bookings;
	
	protected User() {}
	
	public User(UserDTO user) {
		_username = user.getUsername();
		_password = user.getPassword();
		_lastname = user.getLastname();
		_firstname = user.getFirstname();
	}
	
	public User(String username, String password, String lastname, String firstname, CreditCard cc) {
		_username = username;
		_password = password;
		_lastname = lastname;
		_firstname = firstname;
		_creditCard = cc;
	}
	
	public Long getID() {
		return _id;
	}
	
	public String getUsername() {
		return _username;
	}
	
	public String getPassword() {
		return _password;
	}
	
	public String getFirstname() {
		return _firstname;
	}
	
	public String getLastname() {
		return _lastname;
	}
	
	public CreditCard getCreditCard() {
		return _creditCard;
	}
	
	public void setCreditCard(CreditCard cc) {
		_creditCard = cc;
	}
	
	public void addBooking(Booking newBooking) {
		_bookings.add(newBooking);
	}
	
	public boolean hasNullValues() {
		
		if(_username == null) {
			return true;
		}
		if(_password == null) {
			return true;
		}
		if(_lastname == null) {
			return true;
		}
		if(_firstname == null) {
			return true;
		}
		
		return false;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof User))
            return false;
        if (obj == this)
            return true;

        User rhs = (User) obj;
        return new EqualsBuilder().
            append(_username, rhs._username).
            append(_password, rhs._password).
            append(_firstname, rhs._firstname).
            append(_lastname, rhs._lastname).
            isEquals();
	}
	
	@Override
	public int hashCode() {
		return new HashCodeBuilder(17, 31). 
	            append(_username).
	            append(_password).
	            append(_firstname).
	            append(_password).
	            hashCode();
	}
	
	public UserDTO toDTO() {
		return new UserDTO(_username,  _password, _lastname, _firstname);
	}
}
