/**
 * Copyright 2010 CosmoCode GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.cosmocode.palava.jta;

import java.io.Serializable;

import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;

import com.google.common.collect.ForwardingObject;

/**
 * Abstract decorator for {@link UserTransaction}s.
 * 
 * @author Willi Schoenborn
 * @author Tobias Sarnowski
 */
public abstract class ForwardingUserTransaction extends ForwardingObject implements UserTransaction, Serializable {
    
    private static final long serialVersionUID = 8578402136500067868L;

    @Override
    protected abstract UserTransaction delegate();
    
    @Override
    public void begin() throws NotSupportedException, SystemException {
        delegate().begin();
    }

    @Override
    public void commit() throws HeuristicMixedException, HeuristicRollbackException,
        RollbackException, SystemException {
        delegate().commit();
    }

    @Override
    public int getStatus() throws SystemException {
        return delegate().getStatus();
    }

    @Override
    public void rollback() throws IllegalStateException, SecurityException, SystemException {
        delegate().rollback();
    }

    @Override
    public void setRollbackOnly() throws IllegalStateException, SystemException {
        delegate().setRollbackOnly();
    }

    @Override
    public void setTransactionTimeout(int i) throws SystemException {
        delegate().setTransactionTimeout(i);
    }

}
