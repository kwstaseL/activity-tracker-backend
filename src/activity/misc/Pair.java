package activity.misc;

import java.io.Serializable;

// Pair: A generic class that represents a key-value pair
public class Pair<K, V> implements Serializable
{
    private final K key;
    private final V value;

    public Pair(K key, V value)
    {
        this.key = key;
        this.value = value;
    }

    public K getKey() {
        return key;
    }

    public V getValue() {
        return value;
    }

}