package org.pricelessfestival.crossoff.server.api;

import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.hibernate.Session;
import org.pricelessfestival.crossoff.server.service.Transaction;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import jakarta.ws.rs.BadRequestException;
import java.util.List;

/**
 * Created by ivan on 5/12/18.
 */
@AllArgsConstructor
@Log4j2
public class TransactGetAllTickets implements Transaction<List<Ticket>> {
    String orderBy;

    @Override
    public List<Ticket> apply(Session session) {
        CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
        CriteriaQuery<Ticket> ticketQuery = criteriaBuilder.createQuery(Ticket.class);
        Root<Ticket> root = ticketQuery.from(Ticket.class);
        if (orderBy != null) {
            try {
                ticketQuery.orderBy(criteriaBuilder.asc(criteriaBuilder.lower(root.get(orderBy))));
            } catch (IllegalArgumentException e) {
                log.warn("tried to order by unknown column");
                throw new BadRequestException("cannot order by unknown column");
            }
        }
        return session.createQuery(ticketQuery).getResultList();
    }
}
