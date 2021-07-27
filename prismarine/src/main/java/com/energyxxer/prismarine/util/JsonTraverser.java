package com.energyxxer.prismarine.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

public class JsonTraverser {
    public static final JsonTraverser INSTANCE = new JsonTraverser(null);
    public static final ThreadLocal<JsonTraverser> THREAD_SAFE_INSTANCE = ThreadLocal.withInitial(() -> new JsonTraverser(null));

    private final JsonElement root;
    private JsonElement neck;
    private JsonElement head;
    boolean createOnTraversal;

    boolean missingHead = false;
    Object lastTraversalKey = null;

    public JsonTraverser(JsonElement root) {
        this.root = root;
        this.head = root;
        createOnTraversal = false;
    }

    public JsonTraverser reset() {
        this.head = root;
        createOnTraversal = false;
        return this;
    }

    public JsonTraverser reset(JsonElement newHead) {
        this.head = newHead;
        createOnTraversal = false;
        missingHead = false;
        return this;
    }

    public JsonTraverser get(String key) {
        restoreHead(JsonObject::new);

        if(head != null && head.isJsonObject() && head.getAsJsonObject().has(key)) {
            neck = head;
            head = head.getAsJsonObject().get(key);
        } else {
            deadEnd();
        }
        lastTraversalKey = key;
        return this;
    }

    public JsonTraverser get(int index) {
        restoreHead(JsonArray::new);

        if(head != null && index >= 0 && head.isJsonArray() && head.getAsJsonArray().size() > index) {
            neck = head;
            head = head.getAsJsonArray().get(index);
        } else {
            deadEnd();
        }
        lastTraversalKey = index;
        return this;
    }

    public JsonObject asJsonObject() {
        restoreHead(JsonObject::new);
        if(head != null && head.isJsonObject()) {
            JsonObject head = (JsonObject) this.head;
            reset();
            return head;
        }
        reset();
        return null;
    }

    public JsonArray asJsonArray() {
        restoreHead(JsonArray::new);
        if(head != null && head.isJsonArray()) {
            JsonArray head = (JsonArray) this.head;
            reset();
            return head;
        }
        reset();
        return null;
    }

    public Iterable<Map.Entry<String, JsonElement>> iterateAsObject() {
        restoreHead(JsonObject::new);
        if(head != null && head.isJsonObject()) {
            Set<Map.Entry<String, JsonElement>> entries = head.getAsJsonObject().entrySet();
            reset();
            return entries;
        }
        reset();
        return Collections.emptyList();
    }

    public Iterable<JsonElement> iterateAsArray() {
        restoreHead(JsonArray::new);
        if(head != null && head.isJsonArray()) {
            JsonArray asJsonArray = head.getAsJsonArray();
            reset();
            return asJsonArray;
        }
        reset();
        return Collections.emptyList();
    }

    public String asString() {
        return asString(null);
    }

    public String asString(String defaultValue) {
        restoreHead(() -> defaultValue != null ? new JsonPrimitive(defaultValue) : null);
        if(head != null && head.isJsonPrimitive() && head.getAsJsonPrimitive().isString()) {
            String asString = head.getAsString();
            reset();
            return asString;
        }
        reset();
        return defaultValue;
    }

    public String asNonEmptyString() {
        return asNonEmptyString(null);
    }

    public String asNonEmptyString(String defaultValue) {
        restoreHead(() -> defaultValue != null ? new JsonPrimitive(defaultValue) : null);
        if(head != null && head.isJsonPrimitive() && head.getAsJsonPrimitive().isString() && !head.getAsString().isEmpty()) {
            String asString = head.getAsString();
            reset();
            return asString;
        }
        reset();
        return defaultValue;
    }

    public boolean asBoolean() {
        return asBoolean(false);
    }

    public boolean asBoolean(boolean defaultValue) {
        restoreHead(() -> new JsonPrimitive(defaultValue));
        if(head != null && head.isJsonPrimitive() && head.getAsJsonPrimitive().isBoolean()) {
            boolean asBoolean = head.getAsBoolean();
            reset();
            return asBoolean;
        }
        reset();
        return defaultValue;
    }

    public Boolean asBoolean(Boolean defaultValue) {
        restoreHead(() -> new JsonPrimitive(defaultValue));
        if(head != null && head.isJsonPrimitive() && head.getAsJsonPrimitive().isBoolean()) {
            boolean asBoolean = head.getAsBoolean();
            reset();
            return asBoolean;
        }
        reset();
        return defaultValue;
    }

    public byte asByte() {
        return asByte((byte) 0);
    }

    public byte asByte(byte defaultValue) {
        restoreHead(() -> new JsonPrimitive(defaultValue));
        if(head != null && head.isJsonPrimitive() && head.getAsJsonPrimitive().isNumber()) {
            byte asByte = head.getAsByte();
            reset();
            return asByte;
        }
        reset();
        return defaultValue;
    }

    public Byte asByte(Byte defaultValue) {
        restoreHead(() -> new JsonPrimitive(defaultValue));
        if(head != null && head.isJsonPrimitive() && head.getAsJsonPrimitive().isNumber()) {
            byte asByte = head.getAsByte();
            reset();
            return asByte;
        }
        reset();
        return defaultValue;
    }

    public short asShort() {
        return asShort((short) 0);
    }

    public short asShort(short defaultValue) {
        restoreHead(() -> new JsonPrimitive(defaultValue));
        if(head != null && head.isJsonPrimitive() && head.getAsJsonPrimitive().isNumber()) {
            short asShort = head.getAsShort();
            reset();
            return asShort;
        }
        reset();
        return defaultValue;
    }

    public Short asShort(Short defaultValue) {
        restoreHead(() -> new JsonPrimitive(defaultValue));
        if(head != null && head.isJsonPrimitive() && head.getAsJsonPrimitive().isNumber()) {
            short asShort = head.getAsShort();
            reset();
            return asShort;
        }
        reset();
        return defaultValue;
    }

    public int asInt() {
        return asInt(0);
    }

    public int asInt(int defaultValue) {
        restoreHead(() -> new JsonPrimitive(defaultValue));
        if(head != null && head.isJsonPrimitive() && head.getAsJsonPrimitive().isNumber()) {
            int asInt = head.getAsInt();
            reset();
            return asInt;
        }
        reset();
        return defaultValue;
    }

    public Integer asInt(Integer defaultValue) {
        restoreHead(() -> new JsonPrimitive(defaultValue));
        if(head != null && head.isJsonPrimitive() && head.getAsJsonPrimitive().isNumber()) {
            int asInt = head.getAsInt();
            reset();
            return asInt;
        }
        reset();
        return defaultValue;
    }

    public long asLong() {
        return asLong(0);
    }

    public long asLong(long defaultValue) {
        restoreHead(() -> new JsonPrimitive(defaultValue));
        if(head != null && head.isJsonPrimitive() && head.getAsJsonPrimitive().isNumber()) {
            long asLong = head.getAsLong();
            reset();
            return asLong;
        }
        reset();
        return defaultValue;
    }

    public Long asLong(Long defaultValue) {
        restoreHead(() -> new JsonPrimitive(defaultValue));
        if(head != null && head.isJsonPrimitive() && head.getAsJsonPrimitive().isNumber()) {
            long asLong = head.getAsLong();
            reset();
            return asLong;
        }
        reset();
        return defaultValue;
    }

    public float asFloat() {
        return asFloat(0);
    }

    public float asFloat(float defaultValue) {
        restoreHead(() -> new JsonPrimitive(defaultValue));
        if(head != null && head.isJsonPrimitive() && head.getAsJsonPrimitive().isNumber()) {
            float asFloat = head.getAsFloat();
            reset();
            return asFloat;
        }
        reset();
        return defaultValue;
    }

    public Float asFloat(Float defaultValue) {
        restoreHead(() -> new JsonPrimitive(defaultValue));
        if(head != null && head.isJsonPrimitive() && head.getAsJsonPrimitive().isNumber()) {
            float asFloat = head.getAsFloat();
            reset();
            return asFloat;
        }
        reset();
        return defaultValue;
    }

    public double asDouble() {
        return asDouble(0);
    }

    public double asDouble(double defaultValue) {
        restoreHead(() -> new JsonPrimitive(defaultValue));
        if(head != null && head.isJsonPrimitive() && head.getAsJsonPrimitive().isNumber()) {
            double asDouble = head.getAsDouble();
            reset();
            return asDouble;
        }
        reset();
        return defaultValue;
    }

    public Double asDouble(Double defaultValue) {
        restoreHead(() -> new JsonPrimitive(defaultValue));
        if(head != null && head.isJsonPrimitive() && head.getAsJsonPrimitive().isNumber()) {
            double asDouble = head.getAsDouble();
            reset();
            return asDouble;
        }
        reset();
        return defaultValue;
    }

    public BigInteger asBigInt() {
        return asBigInt(BigInteger.ZERO);
    }

    public BigInteger asBigInt(BigInteger defaultValue) {
        restoreHead(() -> new JsonPrimitive(defaultValue));
        if(head != null && head.isJsonPrimitive() && head.getAsJsonPrimitive().isNumber()) {
            BigInteger asBigInteger = head.getAsBigInteger();
            reset();
            return asBigInteger;
        }
        reset();
        return defaultValue;
    }

    public BigDecimal asBigDecimal() {
        return asBigDecimal(BigDecimal.ZERO);
    }

    public BigDecimal asBigDecimal(BigDecimal defaultValue) {
        restoreHead(() -> new JsonPrimitive(defaultValue));
        if(head != null && head.isJsonPrimitive() && head.getAsJsonPrimitive().isNumber()) {
            BigDecimal asBigDecimal = head.getAsBigDecimal();
            reset();
            return asBigDecimal;
        }
        reset();
        return defaultValue;
    }

    private void deadEnd() {
        neck = head;
        head = null;
        missingHead = true;
    }

    public JsonElement getHead() {
        return head;
    }

    public JsonTraverser createOnTraversal() {
        createOnTraversal = true;
        return this;
    }

    private void restoreHead(Supplier<JsonElement> newHeadSupplier) {
        if(createOnTraversal && missingHead && neck != null) {
            JsonElement newHead = newHeadSupplier.get();
            if(newHead == null) return;
            head = newHead;
            missingHead = false;
            if (lastTraversalKey instanceof String) { //Last traversal should have been via a key
                neck.getAsJsonObject().add((String) lastTraversalKey, head);
            } else { //Last traversal should have been via an index
                JsonArray neckArr = neck.getAsJsonArray();
                while (neckArr.size() <= ((int) lastTraversalKey)) {
                    neckArr.add(0);
                }
                neckArr.set((int) lastTraversalKey, head);
            }
        }
    }

    public static JsonTraverser getThreadInstance() {
        return THREAD_SAFE_INSTANCE.get();
    }
}
