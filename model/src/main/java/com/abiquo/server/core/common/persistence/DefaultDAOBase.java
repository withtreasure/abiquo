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

package com.abiquo.server.core.common.persistence;

import java.io.Serializable;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import com.abiquo.server.core.common.GenericEnityBase;
import com.softwarementors.bzngine.dao.hibernate.JpaHibernateDaoBase;
import com.softwarementors.bzngine.entities.PersistentEntity;

public abstract class DefaultDAOBase<I extends Serializable, T extends GenericEnityBase<I>> extends
    JpaHibernateDaoBase<T, I>
{

    private boolean managed;

    @PersistenceContext
    @Override
    protected void setEntityManager(final EntityManager entityManager)
    {
        super.setEntityManager(entityManager);
    }

    /* default constructor for Spring */
    protected DefaultDAOBase(final Class<T> persistentClass)
    {
        super(persistentClass);
        this.managed = true;
    }

    /*
     * @SuppressWarnings("deprecation") public Connection getConnection() { return
     * getSession().connection(); }
     */
    protected DefaultDAOBase(final Class<T> persistentClass, final EntityManager entityManager)
    {
        super(persistentClass, entityManager);
    }

    // TODO: PAG, fix this by changing parameter type for base class isManaged
    public boolean isManaged2(final PersistentEntity< ? > entity)
    {
        assert entity != null;
        assert isOpen();
        assert isInTransaction();

        return entity.getId() != null && getEntityManager().contains(entity);
    }

    @Override
    public boolean isInTransaction()
    {
        if (!managed)
        {
            return super.isInTransaction();
        }

        return TransactionSynchronizationManager.isActualTransactionActive();
    }

    @Override
    public boolean isInReadWriteTransaction()
    {
        if (!managed)
        {
            return super.isInReadWriteTransaction();
        }

        return !TransactionSynchronizationManager.isCurrentTransactionReadOnly();
    }

    @Override
    public boolean isInRollbackTransaction()
    {
        if (!managed)
        {
            return super.isInRollbackTransaction();
        }

        return !TransactionSynchronizationManager.isSynchronizationActive();
    }

    public T findUniqueByProperty(final String propertyName, final String value)
    {
        Criterion criterion = Restrictions.eq(propertyName, value);
        return findUniqueByCriterions(criterion);
    }

    protected Criteria createNestedCriteria(final String... propertyNames)
    {
        Criteria crit = getSession().createCriteria(getPersistentClass());
        return createNestedCriteria(crit, propertyNames);
    }

    protected Criteria createNestedCriteria(final Order order, final String... propertyNames)
    {
        Criteria crit = getSession().createCriteria(getPersistentClass());
        crit.addOrder(order);
        return createNestedCriteria(crit, propertyNames);
    }

    private Criteria createNestedCriteria(final Criteria baseCriteria,
        final String... propertyNames)
    {
        Criteria crit = baseCriteria;
        for (String property : propertyNames)
        {
            crit = crit.createCriteria(property);
        }
        return crit;
    }

    protected Number count()
    {
        Criteria criteria = getSession().createCriteria(getPersistentClass());
        return count(criteria);
    }

    protected Number count(final Criteria criteria)
    {
        criteria.setProjection(Projections.rowCount());
        return (Number) criteria.uniqueResult();
    }

    protected Number count(final Query query, final Map< ? , ? > parameters)
    {
        String original = query.getQueryString();

        StringBuffer countQuery = new StringBuffer();
        countQuery.append("select count(*) ");
        countQuery.append(original.substring(original.toLowerCase().indexOf("from")));

        Query count = getSession().createQuery(countQuery.toString());
        count.setProperties(parameters);
        return (Number) count.uniqueResult();
    }

    protected Number countSQL(final SQLQuery query, final Map< ? , ? > parameters)
    {
        String original = query.getQueryString();

        StringBuffer countQuery = new StringBuffer();
        countQuery.append("select count(*) ");
        countQuery.append(original.substring(original.toLowerCase().indexOf("from")));

        SQLQuery count = getSession().createSQLQuery(countQuery.toString());
        count.setProperties(parameters);
        return (Number) count.uniqueResult();
    }

    public void detach(final T entity)
    {
        this.getEntityManager().detach(entity);
    }
}
