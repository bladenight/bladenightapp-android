package de.greencity.bladenightapp.android.network;

import de.greencity.bladenightapp.network.messages.RealTimeUpdateData;

public interface RealTimeDataConsumer {
    void consume(RealTimeUpdateData realTimeUpdateData);
}
