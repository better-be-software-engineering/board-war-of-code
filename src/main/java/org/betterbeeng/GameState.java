package org.betterbeeng;

import org.betterbeeng.entity.Entity;
import java.util.List;
import lombok.Value;

@Value
public class GameState {
	List<Entity> teamAEntities;
	List<Entity> teamBEntities;

	public List<Entity> getEnemies(Entity entity) {
		if (teamAEntities.contains(entity)) {
			return teamBEntities;
		} else {
			return teamAEntities;
		}
	}

	public List<Entity> getFriends(Entity entity) {
		if (teamAEntities.contains(entity)) {
			return teamAEntities;
		} else {
			return teamBEntities;
		}
	}

	public List<Entity> getAllEntities() {
		return List.of(teamAEntities, teamBEntities).stream().flatMap(List::stream).toList();
	}
}
