package com.fantasycloud.fantasyskilltrees.model;

import java.util.Map;

public class NodeRequirement {
    private final RequirementType type;
    private final String treeId;
    private final String nodeId;

    public NodeRequirement(RequirementType type, String treeId, String nodeId) {
        this.type = type;
        this.treeId = treeId;
        this.nodeId = nodeId;
    }

    public static NodeRequirement fromConfigValue(String currentTreeId, String raw) {
        if (raw == null || raw.trim().isEmpty()) {
            return null;
        }

        String clean = raw.trim();
        if (clean.equalsIgnoreCase("all_other_trees_complete")) {
            return new NodeRequirement(RequirementType.ALL_OTHER_TREES_FULLY_UNLOCKED, null, null);
        }
        if (clean.toLowerCase().startsWith("tree_complete:")) {
            String treeId = clean.substring("tree_complete:".length()).trim().toLowerCase();
            return new NodeRequirement(RequirementType.TREE_FULLY_UNLOCKED, treeId, null);
        }
        if (clean.contains(":")) {
            String[] split = clean.split(":", 2);
            return new NodeRequirement(RequirementType.NODE_UNLOCKED, split[0].trim().toLowerCase(), split[1].trim().toLowerCase());
        }

        return new NodeRequirement(RequirementType.NODE_UNLOCKED, currentTreeId.toLowerCase(), clean.toLowerCase());
    }

    public static NodeRequirement fromMap(String currentTreeId, Map<?, ?> map) {
        if (map == null || map.isEmpty()) {
            return null;
        }

        RequirementType type = RequirementType.fromString(asString(map.get("type")));
        if (type == null) {
            return null;
        }

        if (type == RequirementType.ALL_OTHER_TREES_FULLY_UNLOCKED) {
            return new NodeRequirement(type, null, null);
        }
        if (type == RequirementType.TREE_FULLY_UNLOCKED) {
            String treeId = normalize(asString(map.get("tree")));
            return treeId == null ? null : new NodeRequirement(type, treeId, null);
        }

        String treeId = normalize(asString(map.get("tree")));
        String nodeId = normalize(asString(map.get("node")));
        if (nodeId == null) {
            return null;
        }
        return new NodeRequirement(type, treeId != null ? treeId : currentTreeId.toLowerCase(), nodeId);
    }

    public RequirementType getType() {
        return type;
    }

    public String getTreeId() {
        return treeId;
    }

    public String getNodeId() {
        return nodeId;
    }

    private static String asString(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    private static String normalize(String raw) {
        if (raw == null || raw.trim().isEmpty()) {
            return null;
        }
        return raw.trim().toLowerCase();
    }
}
