package io.github.dailystruggle.rtp.common.factory;

import io.github.dailystruggle.rtp.common.RTP;
import io.github.dailystruggle.rtp.common.configuration.ConfigParser;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.Enumeration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

/**
 * On request, find a stored object with the correct name, clone it, and return it
 * @param <T> type of values this factory will hold
 */
public class Factory<T extends FactoryValue<?>> {
    public final ConcurrentHashMap<String,T> map = new ConcurrentHashMap<>();

    public void add(String name, T value) {
        map.put(name.toUpperCase(), value);
    }

    public Enumeration<String> list() {
        return map.keys();
    }

    public boolean contains(String name) {
        name = name.toUpperCase();
        if(!name.endsWith(".YML")) name = name + ".YML";
        return map.containsKey(name);
    }

    /**
     * @param name name of item
     * @return mutable copy of item
     */
    @Nullable
    public FactoryValue<?> construct(String name) {
        String comparableName = name.toUpperCase();
        if(!comparableName.endsWith(".YML")) comparableName = comparableName + ".YML";
        //guard constructor
        T value = map.get(comparableName);
        if(value == null) {
            if(map.containsKey("DEFAULT.YML")) {
                value = map.get("DEFAULT.YML");
                T clone = (T) value.clone();
                clone.name = (StringUtils.endsWithIgnoreCase(name,".yml")) ? name : name + ".yml";

                if(clone instanceof ConfigParser) {
                    ConfigParser<?> configParser = (ConfigParser<?>) clone;
                    configParser.check(configParser.version,configParser.pluginDirectory,null);
                }
                value = clone;
            }
            else return null;
        }
        return value;
    }

    @Nullable
    public FactoryValue<?> get(String name) {
        return map.get(name.toUpperCase());
    }

    @NotNull
    public FactoryValue<?> getOrDefault(String name) {
        //guard constructor
        T value = (T) get(name);
        if(value == null) {
            if(map.containsKey("DEFAULT.YML")) {
                value = (T) construct(name);
//                map.put(name, value);
            }
            else return new ArrayList<>(map.values()).get(0);
        }
        return value;
    }
}
