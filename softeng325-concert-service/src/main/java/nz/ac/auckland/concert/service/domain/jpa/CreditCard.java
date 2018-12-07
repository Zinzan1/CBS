package nz.ac.auckland.concert.service.domain.jpa;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import nz.ac.auckland.concert.common.dto.CreditCardDTO;
import nz.ac.auckland.concert.common.dto.CreditCardDTO.Type;
import nz.ac.auckland.concert.common.dto.UserDTO;
import nz.ac.auckland.concert.common.jaxb.LocalDateAdapter;

/**
 * Persistence class to represent credit cards. 
 * 
 * A CreditCard describes a credit card in terms of:
 * 
 * _type       	type of credit card, Visa or Mastercard.
 * 
 * _name       	the name of the person who owns the credit card.
 * 
 * _cardnumber 	16-digit credit card number. 
 * 
 * _expiryDate 	the credit card's expiry date. 
 *
 * _user 		the set of users that this creditcard is registered to
 */

@Entity
@Table(name = "CREDITCARD")
public class CreditCard {
	
	@Enumerated(EnumType.STRING)
	private Type _type;
	
	private String _name;
	
	@Id
	private String _cardnumber;
	
	@Convert(converter = LocalDateConverter.class)
	private LocalDate _expiryDate;
	
	@OneToMany(mappedBy = "_creditCard")
	private Set<User> _user = new HashSet<User>();
	
	public CreditCard() {}
	
	public CreditCard(CreditCardDTO dtoObject) {
		_type = dtoObject.getType();
		_name = dtoObject.getName();
		_cardnumber = dtoObject.getNumber();
		_expiryDate = dtoObject.getExpiryDate();
	}
	
	public CreditCard(CreditCardDTO dtoObject, User ccUser) {
		_type = dtoObject.getType();
		_name = dtoObject.getName();
		_cardnumber = dtoObject.getNumber();
		_expiryDate = dtoObject.getExpiryDate();
		_user.add(ccUser);
	}
	
	public CreditCard(Type type, String name, String number, LocalDate expiryDate) {
		_type = type;
		_name = name;
		_cardnumber = number;
		_expiryDate = expiryDate;
	}
	
	public Type getType() {
		return _type;
	}
	
	public String getName() {
		return _name;
	}
	
	public String getNumber() {
		return _cardnumber;
	}

	public LocalDate getExpiryDate() {
		return _expiryDate;
	}
	
	public Set<User> getUsers() {
		return _user;
	}
	
	public void addCreditCardUser(User ccUser) {
		_user.add(ccUser);
	}
	
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof CreditCard))
            return false;
        if (obj == this)
            return true;

        CreditCard rhs = (CreditCard) obj;
        return new EqualsBuilder().
            append(_type, rhs._type).
            append(_name, rhs._name).
            append(_cardnumber, rhs._cardnumber).
            append(_expiryDate, rhs._expiryDate).
            isEquals();
	}
	
	@Override
	public int hashCode() {
		return new HashCodeBuilder(17, 31). 
	            append(_type).
	            append(_name).
	            append(_cardnumber).
	            append(_expiryDate).
	            hashCode();
	}
	
	public CreditCardDTO toDTO() {
		return new CreditCardDTO(_type, _name, _cardnumber, _expiryDate);
	}
}
