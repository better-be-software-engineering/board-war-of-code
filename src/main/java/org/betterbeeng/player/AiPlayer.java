package org.betterbeeng.player;

import java.util.Comparator;
import java.util.stream.Collectors;
import org.betterbeeng.GameState;
import org.betterbeeng.entity.Entity;
import org.betterbeeng.entity.Warrior;
import org.betterbeeng.entity.Mage;
import org.betterbeeng.entity.Archer;

import java.util.List;
import java.util.Random;

public class AiPlayer implements Player {
	private Random random = new Random();

	@Override
	public MoveDecision decideMove(GameState gameState, Entity entity) {
		List<Entity> enemies = gameState.getEnemies(entity);

		Entity closestEnemy = getClosestEntity(entity, enemies);

		int targetX = entity.getX();
		int targetY = entity.getY();

		boolean[][] occupied = new boolean[31][31];
		for (Entity e : gameState.getAllEntities()) {
			occupied[e.getX()][e.getY()] = true;
		}

		if (closestEnemy != null) {
			double distanceToEnemy = calculateDistance(entity, closestEnemy);
			if (distanceToEnemy <= entity.getWeapon().getRange()) {
				// Enemy is within attack range, do not move closer
				targetX = entity.getX();
				targetY = entity.getY();
			} else if (distanceToEnemy < 3) {
				// Enemy is too close, move away
				int[] newPosition = moveAway(entity.getX(), entity.getY(), closestEnemy.getX(), closestEnemy.getY(), entity.getMovementSpeed(), occupied);
				targetX = newPosition[0];
				targetY = newPosition[1];
			} else if (entity instanceof Warrior || entity instanceof Archer) {
				// Move towards the enemy
				int[] newPosition = moveTowards(entity.getX(), entity.getY(), closestEnemy.getX(), closestEnemy.getY(), entity.getMovementSpeed(), occupied);
				targetX = newPosition[0];
				targetY = newPosition[1];
			} else if (entity instanceof Mage) {
				int[] newPosition = moveAway(entity.getX(), entity.getY(), closestEnemy.getX(), closestEnemy.getY(), entity.getMovementSpeed(), occupied);
				targetX = newPosition[0];
				targetY = newPosition[1];
			}
		}

		// Add randomness
		targetX += random.nextInt(3) - 1;
		targetY += random.nextInt(3) - 1;

		return new MoveDecision(targetX, targetY);
	}

	private int[] moveTowards(int currentX, int currentY, int targetX, int targetY, int speed, boolean[][] occupied) {
		int newX = currentX;
		int newY = currentY;
		if (currentX < targetX) {
			newX = Math.min(currentX + speed / 2, targetX);
		} else if (currentX > targetX) {
			newX = Math.max(currentX - speed / 2, targetX);
		}
		if (currentY < targetY) {
			newY = Math.min(currentY + speed / 2, targetY);
		} else if (currentY > targetY) {
			newY = Math.max(currentY - speed / 2, targetY);
		}
		if (occupied[newX][newY]) {
			int[] newPosition = avoidClustering(newX, newY, occupied);
			newX = newPosition[0];
			newY = newPosition[1];
		}
		return new int[]{newX, newY};
	}

	private int[] moveAway(int currentX, int currentY, int targetX, int targetY, int speed, boolean[][] occupied) {
		int newX = currentX;
		int newY = currentY;
		if (currentX < targetX) {
			newX = Math.max(currentX - speed / 2, 0);
		} else if (currentX > targetX) {
			newX = Math.min(currentX + speed / 2, occupied.length - 1);
		}
		if (currentY < targetY) {
			newY = Math.max(currentY - speed / 2, 0);
		} else if (currentY > targetY) {
			newY = Math.min(currentY + speed / 2, occupied[0].length - 1);
		}
		if (occupied[newX][newY]) {
			int[] newPosition = avoidClustering(newX, newY, occupied);
			newX = newPosition[0];
			newY = newPosition[1];
		}
		return new int[]{newX, newY};
	}

	private int[] avoidClustering(int currentX, int currentY, boolean[][] occupied) {
		int newX = currentX;
		int newY = currentY;
		while (occupied[newX][newY]) {
			newX = (newX + 1) % occupied.length;
			newY = (newY + 1) % occupied[0].length;
		}
		return new int[]{newX, newY};
	}

	private double calculateDistance(Entity entity1, Entity entity2) {
		return Math.abs(entity1.getX() - entity2.getX()) +
				Math.abs(entity1.getY() - entity2.getY());
	}

	@Override
	public ActionDecision decideAction(GameState gameState, Entity entity) {
		List<Entity> enemies = gameState.getEnemies(entity);
		List<Entity> friends = gameState.getFriends(entity);

		Entity target = getClosestEntity(entity, enemies);

		if (entity instanceof Warrior warrior) {
			if (target != null && !target.isStunned() && warrior.hasEnoughRageForStun() && entity.inRange(target)
					&& target.isAlive()) {
				return new ActionDecision(ActionType.STUN, target);
			} else if (target != null && entity.inRange(target) && target.isAlive()) {
				return new ActionDecision(ActionType.ATTACK, target);
			}
		} else if (entity instanceof Mage mage) {
			Entity friendToHeal = getLowestHealthEntity(friends);
			if (friendToHeal != null && mage.hasEnoughManaForHeal() && entity.inRange(friendToHeal) && friendToHeal.isAlive()) {
				return new ActionDecision(ActionType.HEAL, friendToHeal);
			} else if (target != null && mage.hasEnoughManaForBolt() && entity.inRange(target) && target.isAlive()) {
				return new ActionDecision(ActionType.BOLT, target);
			} else if (target != null && entity.inRange(target) && target.isAlive()) {
				return new ActionDecision(ActionType.ATTACK, target);
			}
		} else if (entity instanceof Archer) {
			if (target != null && entity.inRange(target) && target.isAlive()) {
				return new ActionDecision(ActionType.ATTACK, target);
			}
		}

		return new ActionDecision(null, null);
	}

	private Entity getClosestEntity(Entity entity, List<Entity> entities) {
		List<Entity> sortedEntities = entities.stream()
				.sorted((e1, e2) -> Double.compare(calculateDistance(e1, entity), calculateDistance(e2, entity)))
				.toList();

		int limit = Math.min(3, sortedEntities.size());
		return sortedEntities.get(random.nextInt(limit));
	}

	private Entity getLowestHealthEntity(List<Entity> entities) {
		List<Entity> sortedEntities = entities.stream()
				.sorted(Comparator.comparingInt(Entity::getHealth))
				.toList();

		int limit = Math.min(3, sortedEntities.size());
		return sortedEntities.get(random.nextInt(limit));
	}
}