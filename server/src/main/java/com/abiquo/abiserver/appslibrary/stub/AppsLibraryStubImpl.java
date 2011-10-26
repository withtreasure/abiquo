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
package com.abiquo.abiserver.appslibrary.stub;

import static java.lang.String.valueOf;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.apache.wink.client.ClientResponse;
import org.apache.wink.client.Resource;
import org.apache.wink.client.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.abiquo.abiserver.business.authentication.TokenUtils;
import com.abiquo.abiserver.business.hibernate.pojohb.user.UserHB;
import com.abiquo.abiserver.commands.impl.AppsLibraryCommandImpl;
import com.abiquo.abiserver.commands.stub.AbstractAPIStub;
import com.abiquo.abiserver.config.AbiConfigManager;
import com.abiquo.abiserver.persistence.DAOFactory;
import com.abiquo.abiserver.persistence.hibernate.HibernateDAOFactory;
import com.abiquo.abiserver.pojo.authentication.UserSession;
import com.abiquo.abiserver.pojo.result.BasicResult;
import com.abiquo.abiserver.pojo.result.DataResult;
import com.abiquo.abiserver.pojo.virtualimage.Icon;
import com.abiquo.abiserver.pojo.virtualimage.OVFPackage;
import com.abiquo.abiserver.pojo.virtualimage.OVFPackageList;
import com.abiquo.model.transport.error.ErrorDto;
import com.abiquo.model.transport.error.ErrorsDto;
import com.abiquo.ovfmanager.ovf.section.DiskFormat;
import com.abiquo.server.core.appslibrary.IconDto;
import com.abiquo.server.core.appslibrary.IconsDto;
import com.abiquo.server.core.appslibrary.OVFPackageDto;
import com.abiquo.server.core.appslibrary.OVFPackageListDto;
import com.abiquo.server.core.appslibrary.OVFPackageListsDto;
import com.abiquo.server.core.appslibrary.OVFPackagesDto;

public class AppsLibraryStubImpl extends AbstractAPIStub implements AppsLibraryStub
{

    private static final Logger logger = LoggerFactory.getLogger(AppsLibraryStubImpl.class);

    private RestClient client;

    public static final String OVF_PACKAGE_LISTS_PATH = "appslib/ovfpackagelists";

    public static final String ENTERPRISES_PATH = "admin/enterprises";

    public String baseUri;

    final String user;

    final String password;

    final String authType;

    public static final String OVF_PACKAGE_PATH = "appslib/ovfpackages";

    private final static String defaultRepositorySpace =
        AbiConfigManager.getInstance().getAbiConfig().getDefaultRepositorySpace();

    public AppsLibraryStubImpl(final UserSession session)
    {
        client = new RestClient();
        baseUri = AbiConfigManager.getInstance().getAbiConfig().getApiLocation();

        DAOFactory factory = HibernateDAOFactory.instance();
        factory.beginConnection();
        UserHB user =
            factory.getUserDAO().getUserByLoginAuth(session.getUser(), session.getAuthType());
        factory.endConnection();

        this.user = user.getUser();
        this.password = user.getPassword();
        this.authType = user.getAuthType();
    }

    private void setAuthCookie(final Resource resource)
    {
        long tokenExpiration = System.currentTimeMillis() + 1000L * 1800;
        String signature = TokenUtils.makeTokenSignature(tokenExpiration, user, password);
        String authType = this.authType;
        String[] tokens;
        if (authType != null)
        {
            tokens = new String[] {user, valueOf(tokenExpiration), signature, authType};
        }
        else
        {
            tokens = new String[] {user, valueOf(tokenExpiration), signature};
        }
        String cookieValue = StringUtils.join(tokens, ":");

        cookieValue = new String(Base64.encodeBase64(cookieValue.getBytes()));

        resource.cookie(new Cookie("auth", cookieValue));
    }

    @Override
    public DataResult<Icon> createIcon(final Integer idEnterprise, final IconDto icon)
    {
        DataResult<Icon> result = new DataResult<Icon>();

        String uri = createIconsLink();

        ClientResponse response = post(uri, icon);

        if (response.getStatusCode() / 200 == 1)
        {
            result.setSuccess(Boolean.TRUE);
            result.setData(createFlexIconObject(response.getEntity(IconDto.class)));
        }
        else
        {
            populateErrors(response, result, "createIcon");
        }

        return result;
    }

    public Resource createResourceOVFPackageLists(final Integer idEnterprise)
    {
        final String path =
            ENTERPRISES_PATH + '/' + String.valueOf(idEnterprise) + '/' + OVF_PACKAGE_LISTS_PATH;
        Resource reso = client.resource(baseUri + "/" + path);

        setAuthCookie(reso);

        return reso;
    }

    public Resource createResourceOVFPackageList(final Integer idEnterprise,
        final Integer idOvfpackageList)
    {
        final String path =
            ENTERPRISES_PATH + '/' + String.valueOf(idEnterprise) + '/' + OVF_PACKAGE_LISTS_PATH
                + '/' + String.valueOf(idOvfpackageList);

        Resource reso = client.resource(baseUri + "/" + path);

        setAuthCookie(reso);

        return reso;
    }

    @Override
    public DataResult<OVFPackageList> createOVFPackageList(final Integer idEnterprise,
        final String ovfpackageListURL)
    {
        DataResult<OVFPackageList> result = new DataResult<OVFPackageList>();

        Resource resource = createResourceOVFPackageLists(idEnterprise);
        // resource.queryParam("ovfindexURL", ovfpackageListURL);

        ClientResponse response =
            resource.contentType(MediaType.TEXT_PLAIN).accept(MediaType.APPLICATION_XML).post(
                ovfpackageListURL);

        final Integer httpStatus = response.getStatusCode();

        if (httpStatus / 200 != 1)
        {
            populateErrors(response, result, "createOVFPackageList");
        }
        else
        {
            result.setSuccess(Boolean.TRUE);
            result.setData(createFlexOVFPackageListObject(response
                .getEntity(OVFPackageListDto.class)));
        }

        return result;
    }

    @Override
    public BasicResult deleteOVFPackageList(final Integer idEnterprise,
        final String nameOvfpackageList)
    {

        BasicResult result = new BasicResult();
        final Integer idOvfPackageList =
            getOVFPackageListIdFromName(idEnterprise, nameOvfpackageList);

        String uri = createOVFPackageListLink(idEnterprise.toString(), idOvfPackageList.toString());
        ClientResponse response = delete(uri);

        final Integer httpStatus = response.getStatusCode();

        if (httpStatus / 200 != 1)
        {
            populateErrors(response, result, "deleteOVFPackageList");
        }
        else
        {
            result.setSuccess(Boolean.TRUE);
        }
        return result;
    }

    @Override
    public DataResult<OVFPackageList> getOVFPackageList(final Integer idEnterprise,
        final String nameOVFPackageList)
    {

        DataResult<OVFPackageList> result = new DataResult<OVFPackageList>();
        final Integer idOvfPackageList =
            getOVFPackageListIdFromName(idEnterprise, nameOVFPackageList);
        String uri = createOVFPackageListLink(idEnterprise.toString(), idOvfPackageList.toString());

        ClientResponse response = get(uri);

        if (response.getStatusCode() / 200 == 1)
        {
            result.setSuccess(Boolean.TRUE);
            result.setData(createFlexOVFPackageListObject(response
                .getEntity(OVFPackageListDto.class)));
        }
        else
        {
            populateErrors(response, result, "getOVFPackageList");
        }

        return result;
    }

    private Integer getOVFPackageListIdFromName(final Integer idEnterprise, final String packageName)
    {
        OVFPackageListsDto packageLists = getOVFPackageLists(idEnterprise);

        for (OVFPackageListDto list : packageLists.getCollection())
        {
            final String listName = list.getName();
            if (packageName.equalsIgnoreCase(listName))
            {
                return list.getId();
            }
        }

        final String cause =
            String.format("Can not locat OVFPackageList named [%s] for enterprise [%s]",
                packageName, idEnterprise);
        final Response response = Response.status(Status.NOT_FOUND).entity(cause).build();
        throw new WebApplicationException(response);
    }

    @Override
    public DataResult<List<String>> getOVFPackageListName(final Integer idEnterprise)
    {
        DataResult<List<String>> result = new DataResult<List<String>>();

        List<String> packageNameList = new LinkedList<String>();

        OVFPackageListsDto packageLists = new OVFPackageListsDto();
        try
        {
            packageLists = getOVFPackageLists(idEnterprise);
        }
        catch (WebApplicationException e)
        {
            result.setSuccess(Boolean.FALSE);
            result.setMessage(e.getMessage());
            return result;
        }

        if (packageLists == null || packageLists.getCollection().size() == 0)
        {
            DataResult<OVFPackageList> defaultList = addDefaultOVFPackageList(idEnterprise);
            if (defaultList == null)
            {
                result.setSuccess(Boolean.FALSE);
                return result;
            }
            packageNameList.add(defaultList.getData().getName());
        }
        for (OVFPackageListDto list : packageLists.getCollection())
        {
            packageNameList.add(list.getName());
        }
        result.setSuccess(Boolean.TRUE);
        result.setData(packageNameList);

        return result;
    }

    private DataResult<OVFPackageList> addDefaultOVFPackageList(final Integer idEnterprise)
    {
        if (defaultRepositorySpace == null || defaultRepositorySpace.isEmpty())
        {
            logger.debug("There aren't any default repository space defined");
            return null;
        }
        else
        {
            logger.debug("Adding default repository space at [{}]", defaultRepositorySpace);
            DataResult<OVFPackageList> list =
                createOVFPackageList(idEnterprise, defaultRepositorySpace);
            return list;

        }
    }

    // XXX not used on the AppsLibraryCommand
    private OVFPackageListsDto getOVFPackageLists(final Integer idEnterprise)
    {
        Resource resource = createResourceOVFPackageLists(idEnterprise);
        ClientResponse response = resource.accept(MediaType.APPLICATION_XML).get();

        final Integer httpStatus = response.getStatusCode();
        if (httpStatus != 200)
        {
            throw new WebApplicationException(response(response));
        }

        return response.getEntity(OVFPackageListsDto.class);
    }

    @Override
    public DataResult<OVFPackageList> refreshOVFPackageList(final Integer idEnterprise,
        final String nameOvfpackageList)
    {
        DataResult<OVFPackageList> result = new DataResult<OVFPackageList>();

        final Integer idList = getOVFPackageListIdFromName(idEnterprise, nameOvfpackageList);
        OVFPackageListDto list = new OVFPackageListDto();
        try
        {
            list = refreshOVFPackageList(idEnterprise, idList);
        }
        catch (WebApplicationException e)
        {
            result.setSuccess(Boolean.FALSE);
            result.setMessage(e.getMessage());
        }
        result.setSuccess(Boolean.TRUE);
        result.setData(createFlexOVFPackageListObject(list));

        return result;

    }

    private OVFPackageListDto refreshOVFPackageList(final Integer idEnterprise, final Integer idList)
    {
        Resource resource = createResourceOVFPackageList(idEnterprise, idList);
        ClientResponse response =
            resource.accept(MediaType.APPLICATION_XML).contentType(MediaType.TEXT_PLAIN).put(null);

        final Integer httpStatus = response.getStatusCode();
        if (httpStatus != 200)
        {
            throw new WebApplicationException(response(response));
        }

        return response.getEntity(OVFPackageListDto.class);
    }

    private static Response response(final ClientResponse response)
    {
        String cause = new String();
        try
        {
            ErrorsDto errors = response.getEntity(ErrorsDto.class);
            for (ErrorDto e : errors.getCollection())
            {
                cause = cause.concat(e.getMessage());
            }
        }
        catch (Exception e)
        {
            cause = response.getEntity(String.class);
        }

        return Response.status(response.getStatusCode()).entity(cause).build();
    }

    @Override
    public OVFPackagesDto getOVFPackages(final Integer idEnterprise, final String nameOVFPackageList)
    {
        final Integer idOvfPackageList =
            getOVFPackageListIdFromName(idEnterprise, nameOVFPackageList);

        Resource resource = createResourceOVFPackages(idEnterprise, idOvfPackageList);
        ClientResponse response = resource.accept(MediaType.APPLICATION_XML).get();

        final Integer httpStatus = response.getStatusCode();

        if (httpStatus != 200)
        {
            throw new WebApplicationException(response(response));
        }

        return response.getEntity(OVFPackagesDto.class);
    }

    public Resource createResourceOVFPackages(final Integer idEnterprise,
        final Integer idOvfpackageList)
    {
        final String path =
            ENTERPRISES_PATH + '/' + String.valueOf(idEnterprise) + '/' + OVF_PACKAGE_PATH + '/'
                + String.valueOf(idOvfpackageList);

        Resource reso = client.resource(baseUri + "/" + path);

        setAuthCookie(reso);

        return reso;
    }

    @Override
    public BasicResult deleteIcon(final Integer idIcon)
    {
        BasicResult result = new BasicResult();

        final String uri = createIconLink(idIcon);

        ClientResponse response = delete(uri);

        if (response.getStatusCode() / 200 == 1)
        {
            result.setSuccess(Boolean.TRUE);
        }
        else
        {
            populateErrors(response, result, "deleteIcon");
        }
        return result;
    }

    @Override
    public BasicResult editIcon(final Icon icon)
    {
        DataResult<Icon> result = new DataResult<Icon>();

        final String uri = createIconLink(icon.getId());

        IconDto iconDto = new IconDto();

        iconDto.setId(icon.getId());
        iconDto.setName(icon.getName());
        iconDto.setPath(icon.getPath());

        ClientResponse response = put(uri, iconDto);

        if (response.getStatusCode() / 200 == 1)
        {
            result.setSuccess(Boolean.TRUE);
            result.setData(createFlexIconObject(response.getEntity(IconDto.class)));
        }
        else
        {
            populateErrors(response, result, "editIcon");
        }
        return result;

    }

    @Override
    public DataResult<List<Icon>> getIcons(final Integer idEnterprise)
    {

        DataResult<List<Icon>> result = new DataResult<List<Icon>>();

        final String uri = createIconsLink();

        ClientResponse response = get(uri);

        if (response.getStatusCode() / 200 == 1)
        {
            result.setSuccess(Boolean.TRUE);
            IconsDto icons = response.getEntity(IconsDto.class);
            List<Icon> listIcon = new ArrayList<Icon>();
            for (IconDto icon : icons.getCollection())
            {
                listIcon.add(createFlexIconObject(icon));
            }
            result.setData(listIcon);
        }
        else
        {
            populateErrors(response, result, "getIcons");
        }

        return result;

    }

    public static Icon createFlexIconObject(final IconDto iconDto)
    {
        Icon icon = new Icon();
        icon.setName(iconDto.getName());
        icon.setPath(iconDto.getPath());
        icon.setId(iconDto.getId());
        return icon;
    }

    protected OVFPackageList createFlexOVFPackageListObject(final OVFPackageListDto listDto)
    {
        OVFPackageList list = new OVFPackageList();
        list.setName(listDto.getName());
        list.setUrl("unused URL"); // TODO missing URL

        List<OVFPackage> packs = new LinkedList<OVFPackage>();

        if (listDto.getOvfPackages() != null)
        {
            for (OVFPackageDto packDto : listDto.getOvfPackages())
            {
                packs.add(createFlexOVFPackageObject(packDto));
            }
        }

        list.setOvfpackages(packs);
        return list;
    }

    protected OVFPackage createFlexOVFPackageObject(final OVFPackageDto packDto)
    {
        OVFPackage pack = new OVFPackage();
        if (packDto.getName() != null)
        {
            pack.setCategory(packDto.getName());
        }
        else
        {
            pack.setCategory("Others");
        }
        pack.setDescription(packDto.getDescription());
        pack.setDiskFormat(DiskFormat.fromValue(packDto.getDiskFormatTypeUri()).name());
        pack.setDiskSizeMb(packDto.getDiskSizeMb());
        pack.setIconUrl(packDto.getIconPath());
        pack.setIdOVFPackage(packDto.getId());
        pack.setName(packDto.getProductName()); // XXX duplicated name
        pack.setProductName(packDto.getProductName());
        pack.setProductUrl(packDto.getProductUrl());
        pack.setProductVendor(packDto.getProductVendor());
        pack.setProductVersion(packDto.getProductVersion());
        pack.setUrl(packDto.getUrl());

        return pack;
    }
}
