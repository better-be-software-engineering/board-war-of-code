package org.betterbeeng.player.human;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.tuple.Pair;
import org.betterbeeng.GameState;
import org.betterbeeng.entity.Archer;
import org.betterbeeng.entity.Entity;
import org.betterbeeng.entity.Mage;
import org.betterbeeng.entity.Warrior;
import org.betterbeeng.entity.WeaponType;
import org.betterbeeng.player.ActionDecision;
import org.betterbeeng.player.ActionType;
import org.betterbeeng.player.MoveDecision;
import org.betterbeeng.player.Player;

@RequiredArgsConstructor
public class HumanPlayer implements Player {
	@Getter
	private final int teamId;

	private List<Entity> primaryTargetsForMelee = new ArrayList<>();
	private List<Entity> primaryTargetsForDistance = new ArrayList<>();
	private List<Entity> primaryTargetsForHealing = new ArrayList<>();
	private boolean shouldWarriorBackOff = false;

	@Override
	public void turnStarts(GameState gameState) {
		primaryTargetsForMelee = getPrimaryTargets(gameState, entity -> entity instanceof Warrior, true);
		primaryTargetsForDistance = getPrimaryTargets(gameState, entity -> !(entity instanceof Warrior), false);
		primaryTargetsForHealing = getPrimaryTargetsForHealing(gameState);
		shouldWarriorBackOff = shouldStartBackoffStrategy(gameState);
	}

	private boolean shouldStartBackoffStrategy(GameState gameState) {
		int warriorsLeft = (int) gameState.getFriends(getTeamId()).stream()
				.filter(entity -> entity instanceof Warrior)
				.filter(Entity::isAlive)
				.count();
		int enemiesLeft = (int) gameState.getEnemies(getTeamId()).stream()
				.filter(Entity::isAlive)
				.count();

		if (warriorsLeft < 7 && enemiesLeft > 8) {
			return true;
		}
		return false;
	}

	@Override
	public MoveDecision decideMove(GameState gameState, Entity entity) {
		if (entity instanceof Warrior) {
			for (Entity primaryTarget : primaryTargetsForMelee) {
				Optional<Position> position = tryMovingTowards(entity, primaryTarget, gameState, entity.getMovementSpeed());
				if (position.isPresent()) {
					return new MoveDecision(position.get().getX(), position.get().getY());
				}
			}
		} else {
			for (Entity primaryAttacker : getPrimaryAttackers(gameState, otherEntity -> entity == otherEntity, entity)) {
				Optional<Position> position = backOffFromWhileInRange(entity, primaryAttacker, gameState);
				if (position.isPresent()) {
					return new MoveDecision(position.get().getX(), position.get().getY());
				}
			}
		}
		return new MoveDecision(entity.getX(), entity.getY());
	}

	private Optional<Position> backOffFromWhileInRange(Entity entity, Entity target, GameState gameState) {
		int distanceToEntity = distanceTo(entity, target);
		if (distanceToEntity > entity.getWeapon().getRange()) {
			int maxMovementAllowed = Math.min(entity.getMovementSpeed(), distanceToEntity - entity.getWeapon().getRange() + 2);
			return tryMovingTowards(entity, target, gameState, maxMovementAllowed);
		}
		if (distanceToEntity == entity.getWeapon().getRange()) {
			return Optional.of(new Position(entity.getX(), entity.getY()));
		}
		return Optional.ofNullable(getAsFarAsPossibleFrom(entity, new Position(target.getX(), target.getY()), gameState, false));
	}

	private Optional<Position> tryMovingTowards(Entity entity, Entity target, GameState gameState, int maxMovementAllowed) {
		int distanceToEntity = distanceTo(entity, target);
		if (distanceToEntity <= entity.getWeapon().getRange()) {
			// no movement in this case
			if (shouldWarriorBackOff && entity instanceof Warrior) {
				return Optional.ofNullable(getAsFarAsPossibleFrom(entity, new Position(target.getX(), target.getY()), gameState, true));
			}
			return Optional.of(new Position(entity.getX(), entity.getY()));
		}
		if (shouldWarriorBackOff && entity instanceof Warrior) {
			return Optional.ofNullable(getAsFarAsPossibleFrom(entity, new Position(target.getX(), target.getY()), gameState, true));
		}
		int maxDistanceInX = Math.min(maxMovementAllowed, Math.abs(entity.getX() - target.getX()));
		int leftMovementInY = Math.max(0, maxMovementAllowed - maxDistanceInX);
		int targetX = entity.getX() + Math.max(maxDistanceInX, maxMovementAllowed) * directionMultiplierX(entity, target);
		int targetY = entity.getY() + leftMovementInY * directionMultiplierY(entity, target);
		return getAsCloseAsPossibleFrom(entity, new Position(targetX, targetY), gameState);
	}

	private static int directionMultiplierX(Entity entity, Entity target) {
		return (target.getX() - entity.getX()) > 0 ? 1 : -1;
	}

	private static int directionMultiplierY(Entity entity, Entity target) {
		return (target.getY() - entity.getY()) > 0 ? 1 : -1;
	}

	private Optional<Position> getAsCloseAsPossibleFrom(Entity entity, Position target, GameState gameState) {
		int attemptedX = target.getX();
		int attemptedY = target.getY();
		attemptedX = moveOffXAxis(entity, target, attemptedX);
		for (int i = 0; i < 2; i ++) {
			if (isPositionFree(new Position(attemptedX, attemptedY), gameState)) {
				return Optional.of(new Position(attemptedX, attemptedY));
			}
			attemptedY = moveOffYAxis(entity, new Position(attemptedX, attemptedY), attemptedY);
			if (isPositionFree(new Position(attemptedX, attemptedY), gameState)) {
				return Optional.of(new Position(attemptedX, attemptedY));
			}
			attemptedX = moveOffXAxis(entity, new Position(attemptedX, attemptedY), attemptedX);
		}
		return Optional.empty();
	}

	private Position getAsFarAsPossibleFrom(Entity entity, Position target, GameState gameState, boolean ignoreRange) {
		int attemptedX = target.getX();
		int attemptedY = target.getY();
		Position lastWorkingPosition = new Position(entity.getX(), entity.getY());
		while (shouldCheckNextPositionForBackingOff(entity, target, new Position(attemptedX, attemptedY), ignoreRange)) {
			attemptedX = moveOffXAxis(entity, target, attemptedX);
			if (canBackOffWhileInRangeTo(entity, target, gameState, new Position(attemptedX, attemptedY), ignoreRange)) {
				lastWorkingPosition = new Position(attemptedX, attemptedY);
			}
			attemptedY = moveOffYAxis(entity, new Position(attemptedX, attemptedY), attemptedY);
			if (canBackOffWhileInRangeTo(entity, target, gameState, new Position(attemptedX, attemptedY), ignoreRange)) {
				 lastWorkingPosition = new Position(attemptedX, attemptedY);
			}
		}
		return lastWorkingPosition;
	}

	private boolean shouldCheckNextPositionForBackingOff(Entity entity, Position target, Position position, boolean ignoreRange) {
		if (ignoreRange) {
			return distanceTo(target, position) <= entity.getMovementSpeed();
		}
		return distanceTo(target, position) <= entity.getWeapon().getRange();
	}

	private boolean canBackOffWhileInRangeTo(Entity entity, Position target, GameState gameState, Position attemptedPosition, boolean ignoreRange) {
		return isPositionFree(attemptedPosition, gameState)
				&& distanceTo(entity, attemptedPosition) <= entity.getMovementSpeed()
				&& (ignoreRange || distanceTo(target, attemptedPosition) <= entity.getWeapon().getRange())
				&& entity.getX() <= 80 && entity.getY() <= 80 && entity.getX() > 0 && entity.getY() > 0;
	}

	private boolean isPositionFree(Position position, GameState gameState) {
		for (Entity entity : gameState.getAllEntities()) {
			if (entity.getX() == position.getX() && entity.getY() == position.getY()) {
				return false;
			}
		}
		return position.getX() <= 80 && position.getY() <= 80 && position.getX() > 0 && position.getY() > 0;
	}

	private static int moveOffXAxis(Entity entity, Position target, int attemptedX) {
		if (entity.getX() < target.getX()) {
			return attemptedX - 1;
		} else {
			return attemptedX + 1;
		}
	}

	private static int moveOffYAxis(Entity entity, Position target, int attemptedY) {
		if (entity.getY() < target.getY()) {
			return attemptedY - 1;
		} else {
			return attemptedY + 1;
		}
	}


	@Override
	public ActionDecision decideAction(GameState gameState, Entity entity) {
		if (entity instanceof Warrior warrior) {
			Entity meleeTarget = primaryTargetsForMelee.stream()
					.filter(primaryTarget -> distanceTo(entity, primaryTarget) <= entity.getWeapon().getRange())
					.filter(primaryTarget -> primaryTarget.isAlive())
					.findFirst()
					.orElse(null);
			if (meleeTarget != null) {
				if (!meleeTarget.isStunned() && warrior.hasEnoughRageForStun()) {
					return new ActionDecision(ActionType.STUN, meleeTarget);
				} else {
					return new ActionDecision(ActionType.ATTACK, meleeTarget);
				}
			}
		}
		if (entity instanceof Mage mage) {
			Entity healingTarget = primaryTargetsForHealing.stream()
					.filter(Entity::isAlive)
					.filter(target -> (double) target.getHealth() / (double) target.getMaxHealth() < 0.6)
					.findFirst()
					.orElse(null);
			if (healingTarget != null && mage.hasEnoughManaForHeal()) {
				return new ActionDecision(ActionType.HEAL, healingTarget);
			}
			Entity distanceTarget = primaryTargetsForDistance.stream()
					.filter(primaryTarget -> distanceTo(entity, primaryTarget) <= entity.getWeapon().getRange())
					.filter(Entity::isAlive)
					.findFirst()
					.orElse(null);
			if (distanceTarget != null) {
				if (mage.hasEnoughManaForBolt() && distanceTarget.getHealth() > WeaponType.FIREBOLT.getMaxDamage() - 2) {
					return new ActionDecision(ActionType.BOLT, distanceTarget);
				} else {
					return new ActionDecision(ActionType.ATTACK, distanceTarget);
				}
			}
		} else if (entity instanceof Archer) {
			Entity distanceTarget = primaryTargetsForDistance.stream()
					.filter(primaryTarget -> distanceTo(entity, primaryTarget) <= entity.getWeapon().getRange())
					.filter(Entity::isAlive)
					.findFirst()
					.orElse(null);
			if (distanceTarget != null) {
				return new ActionDecision(ActionType.ATTACK, distanceTarget);
			}
		}
		return new ActionDecision(null, null);
	}

	private int distanceTo(Entity entity, Entity other) {
		return Math.abs(entity.getX() - other.getX()) + Math.abs(entity.getY() - other.getY());
	}

	private int distanceTo(Entity entity, Position other) {
		return Math.abs(entity.getX() - other.getX()) + Math.abs(entity.getY() - other.getY());
	}
	private int
	distanceTo(Position entity, Position other) {
		return Math.abs(entity.getX() - other.getX()) + Math.abs(entity.getY() - other.getY());
	}

	private List<Entity> getPrimaryAttackers(GameState gameState, Predicate<Entity> predicate, Entity currentEntity) {
		List<Entity> warriorTargets = getPrimaryTargets(gameState, predicate, entity -> entity instanceof Warrior);
		List<Entity> mageTargets = getPrimaryTargets(gameState, predicate, entity -> entity instanceof Mage);
		List<Entity> archerTargets = getPrimaryTargets(gameState, predicate, entity -> entity instanceof Archer);
		List<Entity> primaryAttackers = new ArrayList<>();
		if (warriorTargets.stream().anyMatch(entity -> distanceTo(entity, currentEntity) <= entity.getWeapon().getRange())) {
			primaryAttackers.addAll(warriorTargets);
		}
		if (mageTargets.stream().anyMatch(entity -> distanceTo(entity, currentEntity) <= entity.getWeapon().getRange())) {
			primaryAttackers.addAll(mageTargets);
		}
		primaryAttackers.addAll(archerTargets);

		if (primaryAttackers.isEmpty()) {
			primaryAttackers.addAll(warriorTargets);
			primaryAttackers.addAll(mageTargets);
		}
		return primaryAttackers;
	}

	private List<Entity> getPrimaryTargets(GameState gameState, Predicate<Entity> predicate, boolean isMelee) {
		List<Entity> mageTargets = getPrimaryTargets(gameState, predicate, entity -> entity instanceof Mage);
		List<Entity> warriorTargets = getPrimaryTargets(gameState, predicate, entity -> entity instanceof Warrior);
		List<Entity> archerTargets = getPrimaryTargets(gameState, predicate, entity -> entity instanceof Archer);
		List<Entity> primaryTargets = new ArrayList<>();
		if (isMelee) {
			primaryTargets.addAll(warriorTargets);
			primaryTargets.addAll(mageTargets);
			primaryTargets.addAll(archerTargets);
		}
		else {
			primaryTargets.addAll(mageTargets);
			primaryTargets.addAll(warriorTargets);
			primaryTargets.addAll(archerTargets);
		}
		return primaryTargets;
	}

	private List<Entity> getPrimaryTargets(GameState gameState, Predicate<Entity> friendPredicate,
	                                       Predicate<Entity> enemyPredicate) {
		return gameState.getEnemies(getTeamId()).stream()
				.filter(Entity::isAlive)
				.filter(enemyPredicate)
				.map(entity -> Pair.of(entity,
						gameState.getFriends(getTeamId())
								.stream()
								.filter(friendPredicate)
								.map(friend -> distanceTo(entity, friend))
								.collect(Collectors.averagingInt(Integer::intValue))))
				.sorted(Comparator.comparingDouble(pair -> ((Pair<Entity, Double>) pair).getRight()))
				.map(Pair::getLeft)
				.toList();
	}


	private List<Entity> getPrimaryTargetsForHealing(GameState gameState) {
		return gameState.getFriends(getTeamId()).stream()
				.filter(entity -> entity.getHealth() * 3 < entity.getMaxHealth())
				.map(entity -> Pair.of(entity, (double) entity.getHealth() / (double) entity.getMaxHealth()))
				.sorted(Comparator.comparingDouble(Pair::getRight))
				.map(Pair::getLeft)
				.limit(3)
				.toList();
	}
}
