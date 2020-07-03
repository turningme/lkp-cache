package org.lkpnotice.infra.lock;

import java.util.concurrent.atomic.AtomicReference;

/**
 * <p>This spin lock is a lock designed to protect VERY short sections
 * of critical code.  Threads attempting to take the lock will spin
 * forever until the lock is available, thus it is important that
 * the code protected by this lock is extremely simple and non
 * blocking. The reason for this lock is that it prevents a thread
 * from giving up a CPU core when contending for the lock.</p>
 * <pre>
 * try(SpinLock.Lock lock = spinlock.lock())
 * {
 *   // something very quick and non blocking
 * }
 * </pre>
 * <p>Further analysis however, shows that spin locks behave really
 * bad under heavy contention and where the number of threads
 * exceeds the number of cores, which are common scenarios for a
 * server, so this class was removed from usage, preferring
 * standard locks instead.</p>
 * @deprecated Do not use it anymore, prefer normal locks
 */
public class SpinLock
{
    private final AtomicReference<Thread> _lock = new AtomicReference<>(null);
    private final Lock _unlock = new Lock();

    public Lock lock()
    {
        Thread thread = Thread.currentThread();
        while(true)
        {
            if (!_lock.compareAndSet(null,thread))
            {
                if (_lock.get()==thread)
                    throw new IllegalStateException("SpinLock is not reentrant");
                continue;
            }
            return _unlock;
        }
    }

    public boolean isLocked()
    {
        return _lock.get()!=null;
    }

    public boolean isLockedThread()
    {
        return _lock.get()==Thread.currentThread();
    }

    public class Lock implements AutoCloseable
    {
        @Override
        public void close()
        {
            _lock.set(null);
        }
    }
}