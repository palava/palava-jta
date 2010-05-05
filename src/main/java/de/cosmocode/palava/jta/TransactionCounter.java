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
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author Tobias Sarnowski
 */
public class TransactionCounter implements TransactionCounterMBean, Serializable {

    private final AtomicLong pending = new AtomicLong();
    private final AtomicLong committed = new AtomicLong();
    private final AtomicLong rolledbackSuccess = new AtomicLong();
    private final AtomicLong rolledbackFailed = new AtomicLong();


    @Override
    public AtomicLong getRolledbackFailed() {
        if (rolledbackFailed.get() < 0) {
            rolledbackFailed.set(0);
        }
        return rolledbackFailed;
    }

    @Override
    public AtomicLong getRolledbackSuccess() {
        if (rolledbackSuccess.get() < 0) {
            rolledbackSuccess.set(0);
        }
        return rolledbackSuccess;
    }

    public long getRolledback() {
        return getRolledbackSuccess().get() + getRolledbackFailed().get();
    }

    @Override
    public AtomicLong getCommitted() {
        if (committed.get() < 0) {
            committed.set(0);
        }
        return committed;
    }

    @Override
    public AtomicLong getPending() {
        if (pending.get() < 0) {
            pending.set(0);
        }
        return pending;
    }
}