package org.betterbeeng.entity;

import lombok.Getter;

@Getter
public abstract class Entity {
    private int x, y;
    private int health;
    private final int maxHealth;
    private int armor;
    private int armorAgainstMagic;
    private int agility;
    private int movementSpeed;
    private WeaponType weapon;
    private int teamId;
    private boolean isStunned = false;
    private boolean hasMovedTooMuch = false;

    public Entity(int x, int y, int health, int armor, int armorAgainstMagic, int agility, int movementSpeed,
                  WeaponType weapon, int teamId) {
        this.x = x;
        this.y = y;
        this.health = health;
        this.maxHealth = health;
        this.armor = armor;
        this.agility = agility;
	    this.movementSpeed = movementSpeed;
	    this.armorAgainstMagic = armorAgainstMagic;
	    this.weapon = weapon;
	    this.teamId = teamId;
    }

    public void setPosition(int x, int y) {
        if (isStunned()) {
            System.out.println("Cannot move  " + this + "while stunned");
            return;
        }
        int distanceMoved = Math.abs(this.x - x) + Math.abs(this.y - y);
        if (distanceMoved > movementSpeed) {
            System.out.println("Cannot move " + this + " more than " + movementSpeed + " units");
            return;
        }
        if (distanceMoved > movementSpeed / 2) {
            hasMovedTooMuch = true;
        }
        if (x < 1 || x > 80 || y < 1 || y > 80) {
            System.out.println("Cannot move " + this + " outside the battlefield");
            return;
        }
        this.x = x;
        this.y = y;
    }

    public void takeDamage(int damage, WeaponType weaponType) {
        int effectiveArmor = weaponType.getDamageType() == DamageType.MAGIC ? armorAgainstMagic : armor;
        int effectiveDamage = Math.max(0, damage - effectiveArmor);
        health -= effectiveDamage;
        if (health < 0) {
            health = 0;
        }
    }

    public int attack(Entity target) {
        return attack(target, weapon);
    }

    public int attack(Entity target, WeaponType weapon) {
        if (isStunned) {
            System.out.println(this + " cannot attack while stunned");
            return 0;
        }
        if (!inRange(target)) {
            System.out.println("Target " + target + "is out of range for " + this + " to attack");
            return 0;
        }
        if (!target.isAlive()) {
            System.out.println(this + " cannot attack a dead entity " + target);
            return 0;
        }
        if (hasMovedTooMuch) {
            System.out.println(this + " cannot attack after moving");
            return 0;
        }
        if (isStunned || (Math.random() < weapon.getHitChance() * (1 - target.getAgility() / 100.0))) {
            int damage = weapon.getRandomDamage();
            target.takeDamage(damage, weapon);
            return damage;
        }
        return 0;
    }

    public boolean inRange(Entity target) {
        int distanceToTarget = Math.abs(x - target.x) + Math.abs(y - target.y);
        return distanceToTarget <= weapon.getRange();
    }

    public boolean isAlive() {
        return health > 0;
    }

    protected void healed(double healingPoints) {
        health += healingPoints;
    }

    protected void stunned() {
        isStunned = true;
    }

    public boolean isEnemy(Entity entity) {
        return teamId == entity.teamId;
    }

    public void endOfTurn() {
        if (isStunned && Math.random() < 0.5) {
            isStunned = false;
        }
        hasMovedTooMuch = false;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " at (" + x + ", " + y + ")";
    }
}
