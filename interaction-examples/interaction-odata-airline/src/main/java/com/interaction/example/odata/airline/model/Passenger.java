package com.interaction.example.odata.airline.model;

/*
 * #%L
 * interaction-example-odata-airline
 * %%
 * Copyright (C) 2012 - 2013 Temenos Holdings N.V.
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */


import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@SuppressWarnings("unused")
@Entity
public class Passenger {

	@Id
	@Basic(optional = false)
	private Long passengerNo;
	@Basic(optional = false)
	private Long flightID;
	private String name;
	@Temporal(TemporalType.TIMESTAMP)
	@Basic(optional = false)
	private java.util.Date dateOfBirth;
	
	@JoinColumn(name = "flightID", referencedColumnName = "flightID",insertable = false, updatable = false)
	@ManyToOne(optional = false)
	private Flight flight;
	
	public Passenger() {}
}