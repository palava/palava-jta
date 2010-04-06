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

import javax.naming.Context;
import javax.naming.NamingException;
import javax.transaction.UserTransaction;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.Provider;

/**
 * {@link Provider} for {@link UserTransaction} which uses the bound {@link Context}
 * to find an instance.
 * 
 * @author Tobias Sarnowski
 * @author Willi Schoenborn
 */
final class UserTransactionProvider implements Provider<UserTransaction> {
    
    private final Provider<Context> provider;

    @Inject
    public UserTransactionProvider(Provider<Context> provider) {
        this.provider = Preconditions.checkNotNull(provider, "Provider");
    }

    @Override
    public UserTransaction get() {
        final Context context = provider.get();
        
        try {
            final Object tx = context.lookup("UserTransaction");
            assert tx instanceof UserTransaction : String.format("%s should be a UserTransaction", tx);
            return UserTransaction.class.cast(tx);
        } catch (NamingException e) {
            throw new IllegalStateException(e);
        }
    }
}
