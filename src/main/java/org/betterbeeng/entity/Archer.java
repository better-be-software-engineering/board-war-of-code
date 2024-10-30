package org.betterbeeng.entity;

public class Archer extends Entity {
    public Archer(int x, int y, int teamId) {
        super(x, y, 18, 1, 1, 10, 10, WeaponType.BOW, teamId);
    }
}