package org.betterbeeng.player;

import org.betterbeeng.entity.Entity;
import lombok.Value;

@Value
public class ActionDecision {
	ActionType actionType;
	Entity target;
}
