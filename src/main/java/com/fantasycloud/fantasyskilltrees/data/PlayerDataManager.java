package com.fantasycloud.fantasyskilltrees.data;

import com.fantasycloud.fantasyskilltrees.model.PlayerSkillData;

import java.util.HashSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class PlayerDataManager {
    private final PlayerDataStorage storage;
    private final Map<UUID, PlayerSkillData> cache = new HashMap<UUID, PlayerSkillData>();

    public PlayerDataManager(PlayerDataStorage storage) {
        this.storage = storage;
    }

    public PlayerSkillData getOrLoad(UUID uuid) {
        PlayerSkillData data = cache.get(uuid);
        if (data == null) {
            data = storage.load(uuid);
            cache.put(uuid, data);
        }
        return data;
    }

    public void save(UUID uuid) {
        PlayerSkillData data = cache.get(uuid);
        if (data != null) {
            storage.save(data);
        }
    }

    public void saveAndUnload(UUID uuid) {
        PlayerSkillData data = cache.remove(uuid);
        if (data != null) {
            storage.save(data);
        }
    }

    public void saveAll() {
        for (PlayerSkillData data : cache.values()) {
            storage.save(data);
        }
    }

    public Set<UUID> getKnownPlayerUuids() {
        Set<UUID> uuids = new HashSet<UUID>(storage.getStoredPlayerUuids());
        uuids.addAll(cache.keySet());
        return uuids;
    }
}
