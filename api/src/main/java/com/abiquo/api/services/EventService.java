package com.abiquo.api.services;

import java.util.Collection;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.abiquo.api.exceptions.APIError;
import com.abiquo.server.core.enterprise.Event;
import com.abiquo.server.core.enterprise.EventRep;
import com.abiquo.server.core.enterprise.User;
import com.abiquo.server.core.util.FilterOptions;

/**
 * @author vmahe
 */
@Service
@Transactional(readOnly = true)
public class EventService extends DefaultApiService
{
    private static final Logger LOGGER = LoggerFactory.getLogger(EventService.class);

    @Autowired
    private UserService userService;

    @Autowired
    private EventRep eventRep;

    public Event getEvent(final Integer eventId)
    {
        Event event = null;

        if (event == null)
        {
            addNotFoundErrors(APIError.NON_EXISTENT_EVENT);
            flushErrors();
        }

        return event;
    }

    public List<Event> getEvents(final FilterOptions filterOptions)
    {
        User currentUser = userService.getCurrentUser();

        Collection<User> listOfUsers =
            userService.getUsersByEnterprise(currentUser.getEnterprise().getId().toString(),
                filterOptions.getFilter(), filterOptions.getOrderBy(), filterOptions.getAsc());

        List<Event> listEvents = eventRep.getEventByFilter(filterOptions);

        return listEvents;
    }
}