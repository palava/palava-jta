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

import java.util.Map;

import javax.transaction.NotSupportedException;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;

import de.cosmocode.palava.ipc.IpcCall;
import de.cosmocode.palava.ipc.IpcCallFilter;
import de.cosmocode.palava.ipc.IpcCallFilterChain;
import de.cosmocode.palava.ipc.IpcCommand;
import de.cosmocode.palava.ipc.IpcCommandExecutionException;

/**
 * TODO
 * 
 * @author Tobias Sarnowski
 */
@Singleton
public final class JtaFilter implements IpcCallFilter {
    
    private static final Logger LOG = LoggerFactory.getLogger(JtaFilter.class);
    
    private Provider<UserTransaction> provider;

    @Inject
    public JtaFilter(Provider<UserTransaction> provider) {
        this.provider = provider;
    }

    @Override
    public Map<String, Object> filter(IpcCall call, IpcCommand command, IpcCallFilterChain chain) 
        throws IpcCommandExecutionException {
        
        final UserTransaction utx = provider.get();
        final Map<String, Object> response;

        // begin transaction
        try {
            utx.begin();
        } catch (NotSupportedException e) {
            throw new UnsupportedOperationException(e);
        } catch (SystemException e) {
            throw new IllegalStateException(e);
        }

        try {
            // proceed with the chain
            response = chain.filter(call, command);
            // commit transaction
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
