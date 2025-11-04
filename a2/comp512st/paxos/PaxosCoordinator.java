package paxos;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Logger;

class PaxosCoordinator {

    private Logger logger;

    private ReentrantLock lock = new ReentrantLock();
    private Condition roundOver = lock.newCondition();
    private boolean roundCompleted = false;
    private Long highestConfirmedBallot;

    PaxosCoordinator(Logger logger) {
        this.logger = logger;

        highestConfirmedBallot = -1L;
    }

    void awaitRoundCompletion() throws InterruptedException {
        lock.lock();
        try {
            while (!roundCompleted) {
                roundOver.await();
            }
            logger.fine("Round completed. Resetting 'roundCompleted' flag.");
            roundCompleted = false; // reset for next round
        } finally {
            lock.unlock();
        }
    }

    void signalRoundCompletion(Long confirmedBID) {
        lock.lock();
        try {
            logger.info("Lock acquired. Signaling round completion.");

            if (Long.compare(highestConfirmedBallot, confirmedBID) < 0) {
                highestConfirmedBallot = confirmedBID;
                logger.info(
                    "Updated highestKnownBallot to " +
                        this.highestConfirmedBallot
                );
            }

            roundCompleted = true;
            roundOver.signalAll();
            logger.info(
                "Condition 'roundOver' signaled to all waiting threads."
            );
        } finally {
            lock.unlock();
        }
    }

    Long getHighestConfirmedBallot() {
        lock.lock();
        try {
            return highestConfirmedBallot;
        } finally {
            lock.unlock();
        }
    }
}
