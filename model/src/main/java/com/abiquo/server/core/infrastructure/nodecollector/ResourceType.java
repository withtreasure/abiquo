/**
 * Abiquo community edition
 * cloud management application for hybrid clouds
 * Copyright (C) 2008-2010 - Abiquo Holdings S.L.
 *
 * This application is free software; you can redistribute it and/or
 * modify it under the terms of the GNU LESSER GENERAL PUBLIC
 * LICENSE as published by the Free Software Foundation under
 * version 3 of the License
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * LESSER GENERAL PUBLIC LICENSE v.3 for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the
 * Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA 02111-1307, USA.
 */

//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vJAXB 2.1.10 in JDK 6 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2011.10.27 at 11:30:26 AM CEST 
//

package com.abiquo.server.core.infrastructure.nodecollector;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;

/**
<<<<<<< HEAD
 *  This class represents a resource allocated in a computer system,
 *                 whatever is virtual or physical.
 *             
 * 
 * <p>Java class for ResourceType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
=======
 * This class represents a resource allocated in a computer system, whatever is virtual or physical.
 * <p>
 * Java class for ResourceType complex type.
 * <p>
 * The following schema fragment specifies the expected content contained within this class.
>>>>>>> stable
 * 
 * <pre>
 * &lt;complexType name="ResourceType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="elementName" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="address" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="resourceType" type="{http://abiquo.com/server/core/infrastructure/nodecollector}ResourceEnumType"/>
 *         &lt;element name="resourceSubType" type="{http://www.w3.org/2001/XMLSchema}anySimpleType"/>
 *         &lt;element name="units" type="{http://www.w3.org/2001/XMLSchema}long"/>
 *         &lt;element name="availableUnits" type="{http://www.w3.org/2001/XMLSchema}long"/>
 *         &lt;element name="connection" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="label" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="attachment" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
<<<<<<< HEAD
@XmlType(name = "ResourceType", propOrder = {
    "elementName",
    "address",
    "resourceType",
    "resourceSubType",
    "units",
    "availableUnits",
    "connection",
    "label",
    "attachment"
})
public class ResourceType {
=======
@XmlType(name = "ResourceType", propOrder = {"elementName", "address", "resourceType",
"resourceSubType", "units", "availableUnits", "connection"})
public class ResourceType
{
>>>>>>> stable

    @XmlElement(required = true)
    protected String elementName;

    @XmlElement(required = true)
    protected String address;

    @XmlElement(required = true)
    protected ResourceEnumType resourceType;

    @XmlElement(required = true, nillable = true)
    @XmlSchemaType(name = "anySimpleType")
    protected Object resourceSubType;

    @XmlElement(required = true, type = Long.class, nillable = true)
    protected Long units;

    @XmlElement(required = true, type = Long.class, nillable = true)
    protected Long availableUnits;

    @XmlElement(required = true, nillable = true)
    protected String connection;
    @XmlElement(required = true, nillable = true)
    protected String label;
    @XmlElement(required = true, nillable = true)
    protected String attachment;

    /**
     * Gets the value of the elementName property.
     * 
     * @return possible object is {@link String }
     */
    public String getElementName()
    {
        return elementName;
    }

    /**
     * Sets the value of the elementName property.
     * 
     * @param value allowed object is {@link String }
     */
    public void setElementName(String value)
    {
        this.elementName = value;
    }

    /**
     * Gets the value of the address property.
     * 
     * @return possible object is {@link String }
     */
    public String getAddress()
    {
        return address;
    }

    /**
     * Sets the value of the address property.
     * 
     * @param value allowed object is {@link String }
     */
    public void setAddress(String value)
    {
        this.address = value;
    }

    /**
     * Gets the value of the resourceType property.
     * 
     * @return possible object is {@link ResourceEnumType }
     */
    public ResourceEnumType getResourceType()
    {
        return resourceType;
    }

    /**
     * Sets the value of the resourceType property.
     * 
     * @param value allowed object is {@link ResourceEnumType }
     */
    public void setResourceType(ResourceEnumType value)
    {
        this.resourceType = value;
    }

    /**
     * Gets the value of the resourceSubType property.
     * 
     * @return possible object is {@link Object }
     */
    public Object getResourceSubType()
    {
        return resourceSubType;
    }

    /**
     * Sets the value of the resourceSubType property.
     * 
     * @param value allowed object is {@link Object }
     */
    public void setResourceSubType(Object value)
    {
        this.resourceSubType = value;
    }

    /**
     * Gets the value of the units property.
     * 
     * @return possible object is {@link Long }
     */
    public Long getUnits()
    {
        return units;
    }

    /**
     * Sets the value of the units property.
     * 
     * @param value allowed object is {@link Long }
     */
    public void setUnits(Long value)
    {
        this.units = value;
    }

    /**
     * Gets the value of the availableUnits property.
     * 
     * @return possible object is {@link Long }
     */
    public Long getAvailableUnits()
    {
        return availableUnits;
    }

    /**
     * Sets the value of the availableUnits property.
     * 
     * @param value allowed object is {@link Long }
     */
    public void setAvailableUnits(Long value)
    {
        this.availableUnits = value;
    }

    /**
     * Gets the value of the connection property.
     * 
     * @return possible object is {@link String }
     */
    public String getConnection()
    {
        return connection;
    }

    /**
     * Sets the value of the connection property.
     * 
     * @param value allowed object is {@link String }
     */
    public void setConnection(String value)
    {
        this.connection = value;
    }

    /**
     * Gets the value of the label property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLabel() {
        return label;
    }

    /**
     * Sets the value of the label property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLabel(String value) {
        this.label = value;
    }

    /**
     * Gets the value of the attachment property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAttachment() {
        return attachment;
    }

    /**
     * Sets the value of the attachment property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAttachment(String value) {
        this.attachment = value;
    }

}
