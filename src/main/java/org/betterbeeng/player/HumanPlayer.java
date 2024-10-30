package org.betterbeeng.player;

import org.betterbeeng.GameState;
import org.betterbeeng.entity.Entity;

public class HumanPlayer implements Player {
	@Override
	public MoveDecision decideMove(GameState gameState, Entity entity) {
		return new MoveDecision(entity.getX(), entity.getY());
	}

	@Override
	public ActionDecision decideAction(GameState gameState, Entity entity) {
		return new ActionDecision(null, null);
	}
}
