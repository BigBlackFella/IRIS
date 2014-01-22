package com.interaction.example.odata.embedded.model;

/*
 * #%L
 * interaction-example-odata-embedded
 * %%
 * Copyright (C) 2012 - 2014 Temenos Holdings N.V.
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


import java.util.Collection;

import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;

@Entity
@SuppressWarnings("unused")
public class Airport {

	@Id
	@Basic(optional = false)
	private String code;

	private String name;
	private String country;

    @OneToMany(mappedBy="departureAirportCode")
    private Collection<FlightSchedule> departures;
    @OneToMany(mappedBy="arrivalAirportCode")
    private Collection<FlightSchedule> arrivals;

	public Airport() {
	}
}