package org.betterbeeng.entity;

import lombok.Getter;

@Getter
public class Mage extends Entity {
    public int mana;

    public Mage(int x, int y, int teamId) {
        super(x, y, 15, 0, 4, 4, 8, WeaponType.MAGIC, teamId);
        this.mana = 100;
    }

    public int heal(Entity entity) {
        if (!entity.isAlive()) {
            System.out.println(this + " cannot heal a dead entity " + entity);
            return 0;
        }
        if (mana < 20) {
            System.out.println("Not enough mana to heal " + entity + " with " + this);
            return 0;
        }
        int healingPoints = (int) (Math.random() * 4) + 5;
        entity.healed(healingPoints);
        mana -= 20;
        return healingPoints;
    }

    public int bolt(Entity entity) {
        if (!entity.isAlive()) {
            System.out.println(this + " cannot attack a dead entity " + entity);
            return 0;
        }
        if (mana < 30) {
            System.out.println("Not enough mana to attack " + entity + " with " + this);
            return 0;
        }
        mana -= 30;
        return attack(entity, WeaponType.MAGIC);
    }

    public boolean hasEnoughManaForHeal() {
        return mana >= 20;
    }

    public boolean hasEnoughManaForBolt() {
        return mana >= 30;
    }
}