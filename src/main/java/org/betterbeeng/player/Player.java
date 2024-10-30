package org.betterbeeng.player;

import org.betterbeeng.GameState;
import org.betterbeeng.entity.Entity;

public interface Player {
    MoveDecision decideMove(GameState gameState, Entity entity);
    ActionDecision decideAction(GameState gameState, Entity entity);
}