package org.betterbeeng.entity;

import lombok.Getter;

@Getter
public enum WeaponType {
    SWORD(1, 8, 0.8, DamageType.PHYSICAL, 2),  // minDamage, maxDamage, hitChance, category, range
    BOW(1, 5, 0.9, DamageType.PHYSICAL, 30),
    MAGIC(3, 6, 0.7, DamageType.MAGIC, 20),
    FIREBOLT(6, 12, 0.8, DamageType.MAGIC, 20);

    private final int minDamage;
    private final int maxDamage;
    private final double hitChance;
    private final DamageType damageType;
    private final int range;

    WeaponType(int minDamage, int maxDamage, double hitChance, DamageType damageType, int range) {
        this.minDamage = minDamage;
        this.maxDamage = maxDamage;
        this.hitChance = hitChance;
        this.damageType = damageType;
        this.range = range;
    }

    public int getRandomDamage() {
        return (int)(Math.random() * (maxDamage - minDamage + 1)) + minDamage;
    }
}
