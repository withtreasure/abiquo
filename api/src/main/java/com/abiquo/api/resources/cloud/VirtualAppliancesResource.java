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

package com.abiquo.api.resources.cloud;

import static com.abiquo.api.resources.cloud.VirtualApplianceResource.createTransferObject;

import java.util.List;

import javax.validation.constraints.Min;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.lang.StringUtils;
import org.apache.wink.common.annotations.Parent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import com.abiquo.api.resources.AbstractResource;
import com.abiquo.api.resources.TaskResourceUtils;
import com.abiquo.api.services.cloud.VirtualApplianceService;
import com.abiquo.api.util.IRESTBuilder;
import com.abiquo.server.core.cloud.VirtualAppliance;
import com.abiquo.server.core.cloud.VirtualApplianceDto;
import com.abiquo.server.core.cloud.VirtualAppliancesDto;
import com.abiquo.server.core.task.Task;
import com.abiquo.server.core.task.TasksDto;
import com.abiquo.server.core.util.FilterOptions;
import com.abiquo.server.core.util.PagedList;

@Parent(VirtualDatacenterResource.class)
@Path(VirtualAppliancesResource.VIRTUAL_APPLIANCES_PATH)
@Controller
public class VirtualAppliancesResource extends AbstractResource
{
    public static final String VIRTUAL_APPLIANCES_PATH = "virtualappliances";

    @Autowired
    VirtualApplianceService service;

    @GET
    public VirtualAppliancesDto getVirtualAppliances(
        @PathParam(VirtualDatacenterResource.VIRTUAL_DATACENTER) final Integer vdcId,
        @QueryParam(START_WITH) @DefaultValue("0") @Min(0) final Integer startwith,
        @QueryParam(LIMIT) @DefaultValue(DEFAULT_PAGE_LENGTH_STRING) @Min(1) final Integer limit,
        @QueryParam(BY) @DefaultValue("name") final String orderBy,
        @QueryParam(FILTER) @DefaultValue("") final String filter,
        @QueryParam(ASC) @DefaultValue("true") final Boolean asc,
        @QueryParam("expand") final String expand, @Context final UriInfo uriInfo,
        @Context final IRESTBuilder restBuilder) throws Exception
    {
        FilterOptions filterOptions = new FilterOptions(startwith, limit, filter, orderBy, asc);

        List<VirtualAppliance> all =
            service.getVirtualAppliancesByVirtualDatacenter(vdcId, filterOptions);
        VirtualAppliancesDto vappsDto = new VirtualAppliancesDto();

        if (all != null && !all.isEmpty())
        {
            for (VirtualAppliance v : all)
            {
                VirtualApplianceDto dto = createTransferObject(v, restBuilder);
                expandNodes(vdcId, v.getId(), expand, uriInfo, dto);
                vappsDto.getCollection().add(dto);
            }
        }

        if (all.isEmpty() == false)
        {
            vappsDto.setTotalSize(((PagedList< ? >) all).getTotalResults());
        }

        return vappsDto;
    }

    private void expandNodes(final Integer vdcId, final Integer vappId, final String expand,
        final UriInfo uriInfo, final VirtualApplianceDto dto)
    {
        String[] expands = StringUtils.split(expand, ",");
        if (expands != null)
        {
            for (String e : expands)
            {
                if ("last_task".equalsIgnoreCase(e))
                {
                    List<Task> lastTasks = service.getAllNodesLastTask(vdcId, vappId);
                    if (lastTasks != null && !lastTasks.isEmpty())
                    {
                        TasksDto dtos = TaskResourceUtils.transform(lastTasks, uriInfo);
                        dto.setLastTasks(dtos);
                    }
                }
            }
        }
    }

    @POST
    @Consumes(MediaType.APPLICATION_XML)
    public VirtualApplianceDto createVirtualAppliance(
        @PathParam(VirtualDatacenterResource.VIRTUAL_DATACENTER) final Integer vdcId,
        final VirtualApplianceDto dto, @Context final IRESTBuilder restBuilder) throws Exception
    {
        VirtualAppliance response = service.createVirtualAppliance(vdcId, dto);

        return createTransferObject(response, restBuilder);
    }

}
