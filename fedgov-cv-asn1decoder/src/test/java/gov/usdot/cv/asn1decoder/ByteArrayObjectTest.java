/*
 * Copyright (C) 2026 LEIDOS.
 * Apache License 2.0
 */
package gov.usdot.cv.asn1decoder;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Tests for {@link ByteArrayObject}.
 * Covers constructor, getters, setters, and null-message handling.
 */
public class ByteArrayObjectTest {

    @Test
    public void constructor_setsTypeAndMessage() {
        byte[] data = {0x01, 0x02, 0x03};
        ByteArrayObject obj = new ByteArrayObject("TIM", data);
        assertEquals("TIM", obj.getType());
        assertArrayEquals(data, obj.getMessage());
    }

    @Test
    public void constructor_nullMessage_getMessageReturnsNull() {
        ByteArrayObject obj = new ByteArrayObject("MAP", null);
        assertEquals("MAP", obj.getType());
        assertNull(obj.getMessage());
    }

    @Test
    public void setType_updatesType() {
        ByteArrayObject obj = new ByteArrayObject("TIM", null);
        obj.setType("BSM");
        assertEquals("BSM", obj.getType());
    }

    @Test
    public void setMessage_updatesMessage() {
        ByteArrayObject obj = new ByteArrayObject("TIM", null);
        byte[] newData = {0x0A, 0x0B};
        obj.setMessage(newData);
        assertArrayEquals(newData, obj.getMessage());
    }

    @Test
    public void setMessage_toNull_getMessageReturnsNull() {
        byte[] data = {0x01};
        ByteArrayObject obj = new ByteArrayObject("TIM", data);
        obj.setMessage(null);
        assertNull(obj.getMessage());
    }

    @Test
    public void getType_emptyString_returnsEmpty() {
        ByteArrayObject obj = new ByteArrayObject("", new byte[0]);
        assertEquals("", obj.getType());
    }

    @Test
    public void getMessage_emptyByteArray_returnsEmptyArray() {
        ByteArrayObject obj = new ByteArrayObject("TIM", new byte[0]);
        assertNotNull(obj.getMessage());
        assertEquals(0, obj.getMessage().length);
    }

    @Test
    public void getMessage_returnsExactReference() {
        byte[] data = {0x05, 0x06};
        ByteArrayObject obj = new ByteArrayObject("TIM", data);
        // Same array reference, not a copy
        assertSame(data, obj.getMessage());
    }
}