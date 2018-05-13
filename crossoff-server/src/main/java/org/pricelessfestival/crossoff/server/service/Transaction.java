package org.pricelessfestival.crossoff.server.service;

import org.hibernate.Session;

import java.util.function.Function;

/**
 * Created by ivan on 5/12/18.
 */
public interface Transaction<R> extends Function<Session, R> {
}
