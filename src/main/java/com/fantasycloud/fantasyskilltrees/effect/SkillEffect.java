package com.fantasycloud.fantasyskilltrees.effect;

public class SkillEffect {
    private final EffectType type;
    private final double value;
    private final EffectOperation operation;

    public SkillEffect(EffectType type, double value, EffectOperation operation) {
        this.type = type;
        this.value = value;
        this.operation = operation;
    }

    public EffectType getType() {
        return type;
    }

    public double getValue() {
        return value;
    }

    public EffectOperation getOperation() {
        return operation;
    }
}
