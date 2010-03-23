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
import com.google.inject.Singleton;
import de.cosmocode.palava.ipc.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.transaction.NotSupportedException;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;
import java.util.Map;

/**
 * @author Tobias Sarnowski
 */
@Singleton
final class JtaFilter implements IpcCallFilter {
    private static final Logger LOG = LoggerFactory.getLogger(JtaFilter.class);
    private Provider<UserTransaction> userTransactionProvider;

    @Inject
    public JtaFilter(Provider<UserTransaction> userTransactionProvider) {
        this.userTransactionProvider = userTransactionProvider;
    }

    @Override
    public Map<String, Object> filter(IpcCall call, IpcCommand command, IpcCallFilterChain chain) throws IpcCommandExecutionException {
        UserTransaction utx = userTransactionProvider.get();
        final Map<String, Object> response;

        // begin transaction
        try {
            utx.begin();
        } catch (NotSupportedException e) {
            throw new UnsupportedOperationException(e);
        } catch (SystemException e) {
            throw new IllegalStateException(e);
        }

        // proceed with the chain
        try {
            response = chain.filter(call, command);
        } catch (Exception e) {
            try {
                utx.rollback();
            } catch (Exception se) {
                LOG.error("Failed to rollback UserTransaction " + utx + ": {}", se);
            }
            throw new IpcCommandExecutionException(e);
        }

        // commit transaction
        try {
            utx.commit();
        } catch (Exception e) {
            try {
                utx.rollback();
            } catch (Exception se) {
                LOG.error("Failed to rollback UserTransaction " + utx + ": {}", se);
            }
            throw new IpcCommandExecutionException(e);
        }

        return response;
    }
}
