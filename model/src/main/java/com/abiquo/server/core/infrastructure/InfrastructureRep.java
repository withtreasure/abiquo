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

package com.abiquo.server.core.infrastructure;

import java.net.URISyntaxException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.persistence.EntityManager;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.abiquo.model.enumerator.HypervisorType;
import com.abiquo.model.enumerator.RemoteServiceType;
import com.abiquo.server.core.cloud.Hypervisor;
import com.abiquo.server.core.cloud.HypervisorDAO;
import com.abiquo.server.core.cloud.VirtualMachine;
import com.abiquo.server.core.cloud.VirtualMachineDAO;
import com.abiquo.server.core.common.DefaultRepBase;
import com.abiquo.server.core.enterprise.DatacenterLimits;
import com.abiquo.server.core.enterprise.DatacenterLimitsDAO;
import com.abiquo.server.core.enterprise.Enterprise;
import com.abiquo.server.core.infrastructure.network.Network;
import com.abiquo.server.core.infrastructure.network.NetworkDAO;
import com.abiquo.server.core.infrastructure.storage.StorageRep;
import com.abiquo.server.core.infrastructure.storage.Tier;
import com.abiquo.server.core.util.PagedList;

@Repository
public class InfrastructureRep extends DefaultRepBase
{
    /* package: test only */static final String BUG_INSERT_NAME_MUST_BE_UNIQUE =
        "ASSERT- insert: datacenter name must be unique";

    /* package: test only */static final String BUG_UPDATE_NAME_MUST_BE_UNIQUE =
        "ASSERT- update: datacenter name must be unique";

    /* package: test only */static final String BUG_INSERT_RACK_NAME_MUST_BE_UNIQUE =
        "ASSERT- rack insert: racks on the same datacenter must have a unique name";

    /* package: test only */static final String BUG_UPDATE_RACK_NAME_MUST_BE_UNIQUE =
        "ASSERT- rack update: racks on the same datacenter must have a unique name";

    /* package: test only */static final String BUG_INSERT_MACHINE_NAME_MUST_BE_UNIQUE =
        "ASSERT- machine insert: machines on the same datacenter must have a unique name";

    /* package: test only */static final String BUG_UPDATE_MACHINE_NAME_MUST_BE_UNIQUE =
        "ASSERT- machine update: machines on the same datacenter must have a unique name";

    @Autowired
    private DatacenterDAO dao;

    @Autowired
    private RackDAO rackDao;

    @Autowired
    private UcsRackDAO ucsRackDao;

    @Autowired
    private MachineDAO machineDao;

    @Autowired
    private HypervisorDAO hypervisorDao;

    @Autowired
    private DatastoreDAO datastoreDao;

    @Autowired
    private RemoteServiceDAO remoteServiceDao;

    @Autowired
    private NetworkDAO networkDao;

    @Autowired
    private RepositoryDAO repositoryDao;

    @Autowired
    private VirtualMachineDAO virtualMachineDao;

    @Autowired
    private StorageRep storageRep;

    @Autowired
    private DatacenterLimitsDAO datacenterLimitDao;

    public InfrastructureRep()
    {

    }

    public InfrastructureRep(final EntityManager entityManager)
    {
        assert entityManager != null;
        assert entityManager.isOpen();

        this.entityManager = entityManager;

        this.dao = new DatacenterDAO(entityManager);
        this.rackDao = new RackDAO(entityManager);
        this.ucsRackDao = new UcsRackDAO(entityManager);
        this.machineDao = new MachineDAO(entityManager);
        this.hypervisorDao = new HypervisorDAO(entityManager);
        this.datastoreDao = new DatastoreDAO(entityManager);
        this.remoteServiceDao = new RemoteServiceDAO(entityManager);
        this.repositoryDao = new RepositoryDAO(entityManager);
        this.networkDao = new NetworkDAO(entityManager);
        this.datacenterLimitDao = new DatacenterLimitsDAO(entityManager);
        this.storageRep = new StorageRep(entityManager);
    }

    public Datacenter findById(final Integer id)
    {
        assert id != null;

        return this.dao.findById(id);
    }

    public Collection<Datacenter> findAll()
    {
        return this.dao.findAll();
    }

    public void insert(final Datacenter datacenter)
    {
        assert datacenter != null;
        assert !this.dao.isManaged(datacenter);
        assert !existsAnyDatacenterWithName(datacenter.getName()) : BUG_INSERT_NAME_MUST_BE_UNIQUE;

        this.dao.persist(datacenter);
        this.dao.flush();
    }

    /*
     * public boolean hasVirtualMachines(Datacenter datacenter) { assert datacenter != null; assert
     * this.dao.isManaged(datacenter); List<Machine> machines = findMachines(datacenter); if(
     * machines.isEmpty()) return false; for( Machine machine : machines ) { } return
     * this.virtualMachineDao.haveVirtualMachines(machines); }
     */

    public void delete(final Datacenter datacenter)
    {
        assert datacenter != null;
        assert this.dao.isManaged(datacenter);
        // assert !hasVirtualMachines(datacenter);

        this.dao.remove(datacenter);
        this.dao.flush();
    }

    public void update(final Datacenter datacenter)
    {
        assert datacenter != null;
        assert this.dao.isManaged(datacenter);
        assert !existsAnyOtherWithName(datacenter, datacenter.getName()) : BUG_UPDATE_NAME_MUST_BE_UNIQUE;

        this.dao.flush();
    }

    public boolean existsAnyDatacenterWithName(final String name)
    {
        assert !StringUtils.isEmpty(name);

        return this.dao.existsAnyWithName(name);
    }

    public boolean existsAnyOtherWithName(final Datacenter datacenter, final String name)
    {
        assert datacenter != null;
        assert this.dao.isManaged(datacenter);
        assert !StringUtils.isEmpty(name);

        return this.dao.existsAnyOtherWithName(datacenter, name);
    }

    public List<Rack> findRacks(final Datacenter datacenter)
    {
        assert datacenter != null;
        assert this.dao.isManaged(datacenter);

        return this.rackDao.findRacks(datacenter);
    }

    public List<Machine> findMachines(final Datacenter datacenter)
    {
        assert datacenter != null;
        assert this.dao.isManaged(datacenter);

        return this.machineDao.findMachines(datacenter);
    }

    public List<Machine> findRackMachines(final Rack rack)
    {
        assert rack != null;
        assert this.rackDao.isManaged(rack);

        return this.machineDao.findRackMachines(rack);
    }

    public Set<HypervisorType> findHypervisors(final Datacenter datacenter)
    {
        Set<HypervisorType> types = new HashSet<HypervisorType>();

        for (Machine machine : findMachines(datacenter))
        {
            if (machine.getHypervisor() != null)
            {
                types.add(machine.getHypervisor().getType());
            }
        }

        return types;
    }

    public List<Enterprise> findEnterprisesByDataCenter(final Datacenter datacenter,
        final Boolean network, final Integer firstElem, final Integer numElem)
    {
        PagedList<Enterprise> enterprises = new PagedList<Enterprise>();

        enterprises =
            (PagedList<Enterprise>) this.dao.findEnterprisesByDatacenters(datacenter, firstElem,
                numElem, network);

        return enterprises;
    }

    public DatacenterLimits findDatacenterLimits(final Enterprise enterprise,
        final Datacenter datacenter)
    {
        return datacenterLimitDao.findByEnterpriseAndDatacenter(enterprise, datacenter);
    }

    public boolean existsAnyRackWithName(final Datacenter datacenter, final String name)
    {
        assert datacenter != null;
        assert !StringUtils.isEmpty(name);

        return this.rackDao.existsAnyWithDatacenterAndName(datacenter, name);
    }

    public boolean existsAnyOtherRackWithName(final Rack rack, final String name)
    {
        assert rack != null;
        assert !StringUtils.isEmpty(name);

        return this.rackDao.existsAnyOtherWithDatacenterAndName(rack, name);
    }

    public boolean existsAnyUcsRackWithIp(String ip)
    {
        return this.ucsRackDao.existAnyOtherWithIP(ip);
    }
    
    public boolean existsAnyMachineWithName(final Datacenter datacenter, final String name)
    {
        assert datacenter != null;
        assert !StringUtils.isEmpty(name);

        return this.machineDao.existsAnyWithDatacenterAndName(datacenter, name);
    }

    public boolean existsAnyOtherMachineWithName(final Machine machine, final String name)
    {
        assert machine != null;
        assert !StringUtils.isEmpty(name);

        return this.machineDao.existsAnyOtherWithDatacenterAndName(machine, name);
    }

    public Rack findRackById(final Integer id)
    {
        assert id != null;

        return this.rackDao.findById(id);
    }

    public void insertUcsRack(final UcsRack UcsRack)
    {
        this.ucsRackDao.persist(UcsRack);
        this.ucsRackDao.flush();
    }

    public UcsRack findUcsRackById(Integer rackId)
    {
        return ucsRackDao.findById(rackId);
    }

    public void insertRack(final Rack rack)
    {
        assert rack != null;
        assert !this.rackDao.isManaged(rack);
        assert this.rackDao.isManaged2(rack.getDatacenter());
        assert !existsAnyRackWithName(rack.getDatacenter(), rack.getName()) : BUG_INSERT_RACK_NAME_MUST_BE_UNIQUE;

        this.rackDao.persist(rack);
        this.rackDao.flush();
    }

    public void updateRack(final Rack rack)
    {
        assert rack != null;
        assert this.rackDao.isManaged(rack);
        assert !existsAnyOtherRackWithName(rack, rack.getName()) : BUG_UPDATE_RACK_NAME_MUST_BE_UNIQUE;

        this.rackDao.flush();
    }

    public void deleteRack(final Rack rack)
    {
        assert rack != null;
        assert this.rackDao.isManaged(rack);

        this.machineDao.deleteRackMachines(rack);
        this.machineDao.flush();
        this.rackDao.remove(rack);
        this.rackDao.flush();
    }

    public Machine findMachineById(final Integer id)
    {
        assert id != null;

        return this.machineDao.findById(id);
    }

    public Machine findMachineByIds(Integer datacenterId, Integer rackId, Integer machineId)
    {
        return this.machineDao.findByIds(datacenterId, rackId, machineId);
    }

    public void insertMachine(final Machine machine)
    {
        assert machine != null;
        assert !this.machineDao.isManaged(machine);
        assert !existsAnyMachineWithName(machine.getDatacenter(), machine.getName()) : BUG_INSERT_MACHINE_NAME_MUST_BE_UNIQUE;

        this.machineDao.persist(machine);
        this.machineDao.flush();
    }

    public void updateMachine(final Machine machine)
    {
        assert machine != null;
        assert this.machineDao.isManaged(machine);
        assert !existsAnyOtherMachineWithName(machine, machine.getName()) : BUG_UPDATE_MACHINE_NAME_MUST_BE_UNIQUE;

        this.machineDao.flush();
    }

    public void deleteMachine(final Machine machine)
    {
        assert machine != null;
        assert this.machineDao.isManaged(machine);

        this.machineDao.remove(machine);
        this.machineDao.flush();
    }

    public void insertTier(final Tier tier)
    {
        storageRep.insertTier(tier);
    }

    public void insertHypervisor(final Hypervisor hypervisor)
    {
        assert hypervisor != null;
        assert !hypervisorDao.isManaged(hypervisor);
        assert !existAnyHypervisorWithIp(hypervisor.getIp());
        assert !existAnyHypervisorWithIpService(hypervisor.getIpService());

        hypervisorDao.persist(hypervisor);
        hypervisorDao.flush();

        Machine machine = hypervisor.getMachine();

        machine.setHypervisor(hypervisor);
        updateMachine(machine);
    }

    public boolean existAnyHypervisorWithIp(final String ip)
    {
        assert !StringUtils.isEmpty(ip);

        return hypervisorDao.existsAnyWithIp(ip);
    }

    public boolean existAnyHypervisorWithIpService(final String ipService)
    {
        assert !StringUtils.isEmpty(ipService);

        return hypervisorDao.existsAnyWithIpService(ipService);
    }

    public List<Datastore> findMachineDatastores(final Machine machine)
    {
        assert machine != null;

        return datastoreDao.findMachineDatastores(machine);
    }

    public Datastore findDatastoreById(final Integer id)
    {
        assert id != null;

        return datastoreDao.findById(id);
    }

    public void insertDatastore(final Datastore datastore)
    {
        assert datastore != null;
        assert !datastoreDao.isManaged(datastore);
        assert !existAnyDatastoreWithName(datastore.getName()) : "ASSERT - datastore duplicated name";
        assert !existAnyDatastoreWithDirectory(datastore.getDirectory()) : "ASSERT - datastore duplicated directory";

        datastoreDao.persist(datastore);
        datastoreDao.flush();
    }

    public void updateDatastore(final Datastore datastore)
    {
        assert datastore != null;
        assert datastoreDao.isManaged(datastore);
        assert !existAnyOtherDatastoreWithName(datastore, datastore.getName()) : "ASSERT - datastore duplicated name";
        assert !existAnyOtherDatastoreWithDirectory(datastore, datastore.getDirectory()) : "ASSERT - datastore duplicated directory";

        datastoreDao.flush();
    }

    public boolean existAnyDatastoreWithName(final String name)
    {
        assert !StringUtils.isEmpty(name);
        return datastoreDao.existsAnyWithName(name);
    }

    public boolean existAnyOtherDatastoreWithName(final Datastore datastore, final String name)
    {
        assert !StringUtils.isEmpty(name);
        return datastoreDao.existsAnyOtherWithName(datastore, name);
    }

    public boolean existAnyDatastoreWithDirectory(final String directory)
    {
        assert !StringUtils.isEmpty(directory);
        return datastoreDao.existsAnyWithDirectory(directory);
    }

    public boolean existAnyOtherDatastoreWithDirectory(final Datastore datastore,
        final String directory)
    {
        assert !StringUtils.isEmpty(directory);
        return datastoreDao.existsAnyOtherWithDirectory(datastore, directory);
    }

    public void insertRemoteService(final RemoteService remoteService)
    {
        this.remoteServiceDao.persist(remoteService);
        this.remoteServiceDao.flush();
    }

    public void insertNetwork(final Network network)
    {
        this.networkDao.persist(network);
        this.networkDao.flush();
    }

    public void updateRemoteService(final RemoteService remoteService)
    {
        remoteServiceDao.flush();
    }

    public void deleteRemoteService(final RemoteService remoteService)
    {
        assert remoteService != null;
        assert remoteServiceDao.isManaged(remoteService);

        this.remoteServiceDao.remove(remoteService);
        this.remoteServiceDao.flush();
    }

    public boolean existAnyRemoteServiceWithUri(final String uri) throws URISyntaxException
    {
        return remoteServiceDao.existRemoteServiceUri(uri);
    }

    public List<RemoteService> findRemoteServicesByDatacenter(final Datacenter datacenter)
    {
        return remoteServiceDao.findByDatacenter(datacenter);
    }

    public List<RemoteService> findAllRemoteServices()
    {
        return remoteServiceDao.findAll();
    }

    public RemoteService findRemoteServiceById(final int id)
    {
        return remoteServiceDao.findById(id);
    }

    public List<RemoteService> findRemoteServiceWithTypeInDatacenter(final Datacenter datacenter,
        final RemoteServiceType type)
    {
        return remoteServiceDao.findByDatacenterAndType(datacenter, type);
    }

    public boolean existAnyRemoteServiceWithTypeInDatacenter(final Datacenter datacenter,
        final RemoteServiceType type)
    {
        return !findRemoteServiceWithTypeInDatacenter(datacenter, type).isEmpty();
    }

    public List<Machine> findCandidateMachines(final Integer idRack,
        final Integer idVirtualDatacenter, final Long hdRequiredOnDatastore,
        final Enterprise enterprise)
    {
        return machineDao.findCandidateMachines(idRack, idVirtualDatacenter, hdRequiredOnDatastore,
            enterprise);
    }

    public List<Machine> findCandidateMachines(Integer idRack, Integer idVirtualDatacenter,
        Enterprise enterprise, String datastoreUuid, Integer originalHypervisorId)
    {
        return machineDao.findCandidateMachines(idRack, idVirtualDatacenter, enterprise,
            datastoreUuid, originalHypervisorId);
    }

    public List<Integer> getRackIdByMinVLANCount(final int idDatacenter)
    {
        return rackDao.getRackIdByMinVLANCount(idDatacenter);
    }

    // Populate requireds
    public Datacenter findByName(final String name)
    {
        return dao.findUniqueByProperty(Datacenter.NAME_PROPERTY, name);
    }

    public Rack findRackByName(final String name)
    {
        return rackDao.findUniqueByProperty(Rack.NAME_PROPERTY, name);
    }

    public Machine findMachineByName(final String name)
    {
        return machineDao.findUniqueByProperty(Machine.NAME_PROPERTY, name);
    }

    public Long getNumberOfDeployedVlanNetworksByRack(final Integer rackId)
    {
        return rackDao.getNumberOfDeployedVlanNetworks(rackId);
    }

    public boolean isRepositoryBeingUsed(final Datacenter datacenter)
    {
        return repositoryDao.isBeingUsed(datacenter);
    }

    public void updateRepositoryLocation(final Datacenter datacenter, final String url)
    {
        repositoryDao.updateRepositoryLocation(datacenter, url);
    }

    public void deleteRepository(final Datacenter datacenter)
    {
        repositoryDao.removeByDatacenter(datacenter);
    }

    public boolean existRepositoryInOtherDatacenter(final Datacenter datacenter,
        final String repositoryLocation)
    {
        return repositoryDao.existRepositoryInOtherDatacenter(datacenter, repositoryLocation);
    }

    public boolean existRepositoryInSameDatacenter(Datacenter datacenter, String repositoryLocation)
    {
        return repositoryDao.existRepositoryInSameDatacenter(datacenter, repositoryLocation);
    }

    public void createRepository(final Datacenter datacenter, final String repositoryLocation)
    {
        com.abiquo.server.core.infrastructure.Repository repo =
            new com.abiquo.server.core.infrastructure.Repository(datacenter, repositoryLocation);

        repositoryDao.persist(repo);
    }

    public com.abiquo.server.core.infrastructure.Repository findRepositoryByDatacenter(
        final Datacenter datacenter)
    {
        return repositoryDao.findByDatacenter(datacenter);
    }

    public boolean existDeployedVirtualMachines(final Datacenter datacenter)
    {
        assert datacenter != null;
        List<VirtualMachine> vmachinesInDC =
            virtualMachineDao.findVirtualMachinesByDatacenter(datacenter.getId());
        for (Iterator iterator = vmachinesInDC.iterator(); iterator.hasNext();)
        {
            VirtualMachine virtualMachine = (VirtualMachine) iterator.next();
            // We can ignore CRASHED state: it means the VM is actually not deployed
            if (!(virtualMachine.getState().equals("NOT_DEPLOYED") || virtualMachine.getState()
                .equals("CRASHED")))
            {
                return true;
            }
        }
        return false;
    }

    public Rack findRackByIds(Integer datacenterId, Integer rackId)
    {
        return rackDao.findByIds(datacenterId, rackId);
    }

    public List<Rack> findRacksWithHAEnabled(Datacenter dc)
    {
        return rackDao.findRacksWithHAEnabled(dc);
    }

    public List<Machine> findRackEnabledForHAMachines(Rack rack)
    {
        return machineDao.findRackEnabledForHAMachines(rack);
    }


    /**
     * Return all {@links UcsRack} associated to a
     * 
     * @param datacenterId id.
     * @return List<UcsRack> with all {@links UcsRack} associated to the given {@link Datacenter}.
     */
    public List<UcsRack> findAllUcsRacksByDatacenter(final Datacenter datacenter)
    {
        return this.ucsRackDao.findAllUcsRacksByDatacenter(datacenter);
    }

    /**
     * Return all not managed {@link Rack} associated to a
     * 
     * @param datacenterId id.
     * @return List<UcsRack> with all {@links UcsRack} associated to the given {@link Datacenter}.
     */
    public List<Rack> findAllNotManagedRacksByDatacenter(final Integer datacenterId)
    {
        return this.rackDao.findAllNotManagedRacksByDatacenter(datacenterId);
    }

    public boolean existAnyHypervisorWithIpInDatacenter(String ip, Integer datacenterId)
    {
        return hypervisorDao.existsAnyWithIpAndDatacenter(ip, datacenterId);
    }

}
