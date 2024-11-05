package org.betterbeeng.player;

import org.betterbeeng.GameState;
import org.betterbeeng.entity.Entity;

public interface Player {
    int getTeamId();
    void turnStarts(GameState gameState);
    MoveDecision decideMove(GameState gameState, Entity entity);
    ActionDecision decideAction(GameState gameState, Entity entity);
}