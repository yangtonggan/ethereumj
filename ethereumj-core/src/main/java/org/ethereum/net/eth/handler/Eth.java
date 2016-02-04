package org.ethereum.net.eth.handler;

import org.ethereum.core.Block;
import org.ethereum.core.BlockWrapper;
import org.ethereum.core.Transaction;
import org.ethereum.net.eth.EthVersion;
import org.ethereum.net.eth.message.EthMessageCodes;
import org.ethereum.sync.state.SyncStateName;
import org.ethereum.sync.SyncStatistics;

import java.util.List;

/**
 * Describes interface required by Eth peer clients
 *
 * @see org.ethereum.net.server.Channel
 *
 * @author Mikhail Kalinin
 * @since 20.08.2015
 */
public interface Eth {

    /**
     * @return true if StatusMessage was processed, false otherwise
     */
    boolean hasStatusPassed();

    /**
     * @return true if Status has succeeded
     */
    boolean hasStatusSucceeded();

    /**
     * Executes cleanups required to be done
     * during shutdown, e.g. disconnect
     */
    void onShutdown();

    /**
     * Puts sync statistics to log output
     */
    void logSyncStats();

    /**
     * Changes Sync state to the new one
     *
     * @param newState new state
     */
    void changeState(SyncStateName newState);

    /**
     * @return true if syncState is DONE_HASH_RETRIEVING, false otherwise
     */
    boolean isHashRetrievingDone();

    /**
     * @return true if syncState is HASH_RETRIEVING, false otherwise
     */
    boolean isHashRetrieving();

    /**
     * @return true if syncState is IDLE, false otherwise
     */
    boolean isIdle();

    /**
     * @return sync statistics
     */
    SyncStatistics getStats();

    /**
     * Disables pending transaction processing
     */
    void disableTransactions();

    /**
     * Enables pending transaction processing
     */
    void enableTransactions();

    /**
     * Sends transaction to the wire
     *
     * @param tx sending transaction
     */
    void sendTransaction(List<Transaction> tx);

    /**
     * Sends new block to the wire
     */
    void sendNewBlock(Block newBlock);

    /**
     * Sends new block hashes message to the wire
     */
    void sendNewBlockHashes(Block block);

    /**
     * @return protocol version
     */
    EthVersion getVersion();

    /**
     * Fires inner logic related to long sync done or undone event
     *
     * @param done true notifies that long sync is finished,
     *             false notifies that it's enabled again
     */
    void onSyncDone(boolean done);

    /**
     * Sends {@link EthMessageCodes#STATUS} message
     */
    void sendStatus();

    /**
     * Tries to recover a gap
     *
     * @param block gap block
     */
    void recoverGap(BlockWrapper block);
}
