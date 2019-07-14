package org.eclipse.paho.android.service;

import android.database.Cursor;

import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

class InMemoryMessageStore implements MessageStore {


    private final static HashMap<String, ArrayList<StoredData>> MessageList = new HashMap<>();

    InMemoryMessageStore() {
    }

    /**
     * Store an MQTT message
     *
     * @param clientHandle identifier for the client storing the message
     * @param topic        The topic on which the message was published
     * @param message      the arrived MQTT message
     * @return an identifier for the message, so that it can be removed when appropriate
     */
    @Override
    public String storeArrived(String clientHandle, String topic, MqttMessage message) {

        String id = java.util.UUID.randomUUID().toString();
        StoredData data = new StoredData(id, clientHandle, topic, message);

        ArrayList<StoredData> messages = null;
        if (MessageList.containsKey(clientHandle)) {
            messages = MessageList.get(clientHandle);
        } else {
            messages = new ArrayList<>();
            MessageList.put(clientHandle, messages);
        }
        messages.add(data);
        return id;
    }

    /**
     * Delete an MQTT message.
     *
     * @param clientHandle identifier for the client which stored the message
     * @param id           the identifying string returned when the message was stored
     * @return true if the message was found and deleted
     */
    @Override
    public boolean discardArrived(String clientHandle, String id) {

        if (!MessageList.containsKey(clientHandle)) {
            return false;
        }

        ArrayList<StoredData> messages = MessageList.get(clientHandle);

        Iterator iterator = messages.iterator();
        while (iterator.hasNext()) {
            StoredData data = (StoredData) iterator.next();
            if (data.getMessageId() == id) {
                iterator.remove();
                break;
            }
        }

        return true;
    }

    /**
     * Get an iterator over all messages stored (optionally for a specific client)
     *
     * @param clientHandle identifier for the client.<br>
     *                     If null, all messages are retrieved
     * @return iterator of all the arrived MQTT messages
     */
    @Override
    public Iterator<StoredMessage> getAllArrivedMessages(final String clientHandle) {

        ArrayList<StoredData> messages = null;
        if (!MessageList.containsKey(clientHandle)) {
            return new ArrayList<StoredMessage>().iterator();
        }

        messages = MessageList.get(clientHandle);
        final Iterator<StoredData> iterator = messages.iterator();

        return new Iterator<StoredMessage>() {
            private Cursor c;

            @Override
            public boolean hasNext() {
                return iterator.hasNext();
            }

            @Override
            public StoredMessage next() {

                return iterator.next();
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }

//            @Override
//            protected void finalize() throws Throwable {
//                super.finalize();
//            }
        };
    }

    /**
     * Delete all messages (optionally for a specific client)
     *
     * @param clientHandle identifier for the client.<br>
     *                     If null, all messages are deleted
     */
    @Override
    public void clearArrivedMessages(String clientHandle) {

        MessageList.remove(clientHandle);
    }


    @Override
    public void close() {
    }


    private class StoredData implements StoredMessage {
        private final String messageId;
        private final String clientHandle;
        private final String topic;
        private final MqttMessage message;

        StoredData(String messageId, String clientHandle, String topic, MqttMessage message) {
            this.messageId = messageId;
            this.clientHandle = clientHandle;
            this.topic = topic;
            this.message = message;
        }

        @Override
        public String getMessageId() {
            return messageId;
        }

        @Override
        public String getClientHandle() {
            return clientHandle;
        }

        @Override
        public String getTopic() {
            return topic;
        }

        @Override
        public MqttMessage getMessage() {
            return message;
        }
    }

}