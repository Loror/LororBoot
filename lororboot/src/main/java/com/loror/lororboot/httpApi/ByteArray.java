package com.loror.lororboot.httpApi;

public class ByteArray {
    public byte[] result;

    public ByteArray(byte[] result) {
        this.result = result;
    }

    @Override
    public String toString() {
        return new String(result);
    }
}
