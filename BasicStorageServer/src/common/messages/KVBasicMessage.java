/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package common.messages;

/**
 *
 * @author Maximilian
 */
public class KVBasicMessage implements KVMessage {

    private static String ExtractString(byte[] message, int start) {
        String r = "";
        while (message[start] != 0 && start < message.length) {
            r += (char) message[start];
            start++;
        }
        return r;
    }

    private final String key;
    private final String value;
    private final StatusType status;

    public KVBasicMessage(String key, String value, StatusType status) {
        this.key = key;
        this.value = value;
        this.status = status;
    }

    public KVBasicMessage(byte[] data) {
        this.status = (KVMessage.StatusType.values())[data[0]];
        this.key = KVBasicMessage.ExtractString(data, 1);
        this.value = KVBasicMessage.ExtractString(data, 1 + this.key.length() + 1); //Skip StatusByte, Key and zero terminator
    }

    public byte[] GetData() {
        byte[] b_key = this.key == null ? new byte[0] : this.key.getBytes();
        byte[] b_value = this.value == null ? new byte[0] : this.value.getBytes();
        byte[] data = new byte[b_key.length + b_value.length + 3];
        data[0] = (byte) this.status.ordinal();
        System.arraycopy(b_key, 0, data, 1, b_key.length);
        System.arraycopy(b_value, 0, data, 2 + b_key.length, b_value.length);
        return data;
    }

    @Override
    public String getKey() {
        return this.key;
    }

    @Override
    public String getValue() {
        return this.value;
    }

    @Override
    public StatusType getStatus() {
        return this.status;
    }

    @Override
    public String toString() {
        return "kvbm[ " + this.status.toString() + ": " + (this.key == null ? "null" : this.key) + " = " + (this.value == null ? "NULL" : "'" + this.value + "'") + "]";
    }
}
