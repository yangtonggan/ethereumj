package org.ethereum.sync.strategy;

import org.ethereum.net.server.Channel;
import org.ethereum.sync.SyncQueue;
import org.ethereum.sync.state.SyncStateName;
import org.springframework.stereotype.Component;

import static org.ethereum.sync.state.SyncStateName.BLOCK_RETRIEVING;
import static org.ethereum.sync.state.SyncStateName.HASH_RETRIEVING;
import static org.ethereum.sync.state.SyncStateName.IDLE;

/**
 * Implements long sync algorithm <br/>
 *
 * It starts to act each time VM is started
 * and tries to reach the end of block chain <br/>
 *
 * After it reaches the end or block near to it
 * it's switched to {@link ShortSync}
 *
 * @author Mikhail Kalinin
 * @since 02.02.2016
 */
@Component
public class LongSync extends AbstractSyncStrategy {

    private static final int ROTATION_LIMIT = 100;

    private SyncStateName state = HASH_RETRIEVING;

    protected void doWork() {
        maintainState();
    }

    private void maintainState() {

        logger.trace("Maintain {} state", state);

        switch (state) {
            case HASH_RETRIEVING:   doHeaders(); break;
            case BLOCK_RETRIEVING:  doBodies(); break;
            case IDLE:              doIdle(); break;
        }
    }

    private void doHeaders() {

        if (queue.isLimitExceeded()) {
            logger.info("Queue limit exceeded, process blocks");
            changeState(BLOCK_RETRIEVING);
            return;
        }

        Channel master = null;
        for (Channel peer : pool) {
            // if hash retrieving is done all we need to do is just change state and quit
            if (peer.isHashRetrievingDone()) {
                changeState(BLOCK_RETRIEVING);
                return;
            }

            // master is found
            if (peer.isHashRetrieving()) {
                master = peer;
                break;
            }
        }

        if (master == null) {

            logger.trace("HASH_RETRIEVING is in progress, start master peer");

            master = pool.getMaster();

            if (master == null) return;

            master.changeSyncState(HASH_RETRIEVING);
        } else {

            // master peer rotation
            if (master.getSyncStats().getHeaderBunchesCount() > ROTATION_LIMIT) {
                logger.debug("Peer {}: rotating", master.getPeerIdShort());
                changeState(BLOCK_RETRIEVING);
            }
        }
    }

    private void doBodies() {
        if (queue.isHeadersEmpty()) changeState(IDLE);
    }

    private void doIdle() {
        if (queue.isMoreBlocksNeeded()) changeState(HASH_RETRIEVING);
    }

    private void changeState(SyncStateName newState) {
        logger.info("Change state from {} to {}", state, newState);

        if (newState == BLOCK_RETRIEVING) {
            pool.changeState(BLOCK_RETRIEVING);
        }

        state = newState;
    }
}
