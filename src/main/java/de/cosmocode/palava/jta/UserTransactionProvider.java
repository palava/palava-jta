/**
 * palava - a java-php-bridge
 * Copyright (C) 2007-2010  CosmoCode GmbH
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA  02110-1301, USA.
 */

package de.cosmocode.palava.jta;

import com.google.inject.Inject;
import com.google.inject.Provider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.transaction.UserTransaction;

/**
 * @author Tobias Sarnowski
 */
final class UserTransactionProvider implements Provider<UserTransaction> {
    private static final Logger LOG = LoggerFactory.getLogger(UserTransactionProvider.class);
    private Provider<Context> contextProvider;

    @Inject
    public UserTransactionProvider(Provider<Context> contextProvider) {
        this.contextProvider = contextProvider;
    }


    @Override
    public UserTransaction get() {
        Context ctx = contextProvider.get();
        try {
            return UserTransaction.class.cast(ctx.lookup("UserTransaction"));
        } catch (NamingException e) {
            throw new IllegalStateException(e);
        }
    }
}
