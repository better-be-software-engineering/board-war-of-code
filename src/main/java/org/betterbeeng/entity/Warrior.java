package org.betterbeeng.entity;

public class Warrior extends Entity {
    private int rage = 2;


    public Warrior(int x, int y, int teamId) {
        super(x, y, 25, 3, 0, 6, 6, WeaponType.SWORD, teamId);
    }

    public int stun(Entity entity) {
        if (rage < 1) {
            System.out.println(this + "doesn't have enough rage to stun " + entity);
            return 0;
        }
        int damages = attack(entity, WeaponType.SWORD);
        rage--;
        if (damages > 0 && Math.random() < 0.5) {
            entity.stunned();
        }
        return damages;
    }

    public boolean hasEnoughRageForStun() {
        return rage > 0;
    }
}
