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
import com.google.inject.name.Named;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.transaction.TransactionManager;
import javax.transaction.UserTransaction;

/**
 * @author Tobias Sarnowski
 */
public class TransactionManagerProvider implements Provider<TransactionManager> {
    private static final Logger LOG = LoggerFactory.getLogger(TransactionManagerProvider.class);

    private Provider<Context> contextProvider;
    private String manager = JtaConfig.DEFAULT_MANAGER;

    @Inject
    public TransactionManagerProvider(Provider<Context> contextProvider) {
        this.contextProvider = contextProvider;
    }

    @Inject(optional = true)
    public void setManager(@Named(JtaConfig.MANAGER) String manager) {
        this.manager = manager;
    }

    @Override
    public TransactionManager get() {
        final Context context = contextProvider.get();

        try {
            final Object tm = context.lookup(manager);
            assert tm instanceof TransactionManager : String.format("%s should be a TransactionManager", tm);
            return TransactionManager.class.cast(tm);
        } catch (NamingException e) {
            throw new IllegalStateException(e);
        }
    }
}