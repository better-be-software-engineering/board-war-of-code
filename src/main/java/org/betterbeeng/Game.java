package org.betterbeeng;// Game.java
import javax.swing.JFrame;
import lombok.SneakyThrows;
import org.betterbeeng.player.ActionDecision;
import org.betterbeeng.player.ActionType;
import org.betterbeeng.player.Player;
import org.betterbeeng.player.AiPlayer;
import org.betterbeeng.player.human.HumanPlayer;
import org.betterbeeng.player.MoveDecision;
import org.betterbeeng.entity.Archer;
import org.betterbeeng.entity.Entity;
import org.betterbeeng.entity.Mage;
import org.betterbeeng.entity.Warrior;
import java.util.ArrayList;
import java.util.List;

public class Game {
    private List<Entity> teamAEntities;
    private List<Entity> teamBEntities;
    private Player playerA;
    private Player playerB;
    private GameDisplay gameDisplay;

    public Game(Player playerA, Player playerB) {
        this.playerA = playerA;
        this.playerB = playerB;

        teamAEntities = new ArrayList<>();
        teamBEntities = new ArrayList<>();

        for (int i = 0; i < 10; i++) {
            teamAEntities.add(new Mage(20, i + 20, 1));
            teamAEntities.add(new Mage(20, i + 20, 1));
            teamAEntities.add(new Archer(20, i + 20, 1));
            teamAEntities.add(new Archer(20, i + 20, 1));
            teamAEntities.add(new Warrior(20, i + 20, 1));
            teamAEntities.add(new Warrior(20, i + 20, 1));

            teamBEntities.add(new Warrior(55, i + 20, 2));
            teamBEntities.add(new Warrior(55, i + 20, 2));
            teamBEntities.add(new Archer(58, i + 20, 2));
            teamBEntities.add(new Archer(58, i + 20, 2));
            teamBEntities.add(new Mage(60, i + 20, 2));
            teamBEntities.add(new Mage(60, i + 20, 2));
        }

        for (int i = 0; i < 7; i++) {
            teamBEntities.add(new Warrior(56, i + 20, 2));
            teamBEntities.add(new Archer(57, i + 20, 2));
            teamBEntities.add(new Mage(59, i + 20, 2));
            teamBEntities.add(new Warrior(56, i + 20, 2));
            teamBEntities.add(new Archer(57, i + 20, 2));
            teamBEntities.add(new Mage(59, i + 20, 2));
        }


        JFrame frame = new JFrame("Game Display");
        gameDisplay = new GameDisplay(teamAEntities, teamBEntities);
        frame.add(gameDisplay);
        frame.setSize(600, 600);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }

    @SneakyThrows
    public void start() {
        Thread.sleep(3000);
        while (!teamAEntities.isEmpty() && !teamBEntities.isEmpty()) {
            GameState gameState = new GameState(teamAEntities, teamBEntities);
            playerA.turnStarts(gameState);
            playerB.turnStarts(gameState);

            for (Entity entity : teamAEntities) {
                MoveDecision moveDecision = playerA.decideMove(gameState, entity);
                performMove(entity, moveDecision);
                gameDisplay.updateEntities(teamAEntities, teamBEntities);
            }
            Thread.sleep(10);
            for (Entity entity : teamBEntities) {
                MoveDecision moveDecision = playerB.decideMove(gameState, entity);
                performMove(entity, moveDecision);
                gameDisplay.updateEntities(teamAEntities, teamBEntities);
            }
            Thread.sleep(10);

            displayStatus();

            for (Entity entity : teamAEntities) {
                ActionDecision actionDecision = playerA.decideAction(gameState, entity);
                performAction(entity, actionDecision);
            }
            for (Entity entity : teamBEntities) {
                ActionDecision actionDecision = playerB.decideAction(gameState, entity);
                performAction(entity, actionDecision);
            }
            for (int i = 0; i < teamAEntities.size(); i++) {
                if (!teamAEntities.get(i).isAlive()) {
                    teamAEntities.remove(i);
                    i--;
                } else {
                    teamAEntities.get(i).endOfTurn();
                }
            }
            for (int i = 0; i < teamBEntities.size(); i++) {
                if (!teamBEntities.get(i).isAlive()) {
                    teamBEntities.remove(i);
                    i--;
                } else {
                    teamBEntities.get(i).endOfTurn();
                }
            }
            Thread.sleep(100);
            gameDisplay.updateEntities(teamAEntities, teamBEntities);
            Thread.sleep(100);
            displayStatus();
        }
        System.out.println("Game Over!");
    }

    private void performMove(Entity entity, MoveDecision moveDecision) {
        for (Entity otherEntity : teamAEntities) {
            if (otherEntity.getX() == moveDecision.getMoveToX() && otherEntity.getY() == moveDecision.getMoveToY()) {
                logAction(entity, "tried to move to a cell occupied by another entity - not moving as a result");
                return;
            }
        }
        for (Entity otherEntity : teamBEntities) {
            if (otherEntity.getX() == moveDecision.getMoveToX() && otherEntity.getY() == moveDecision.getMoveToY()) {
                logAction(entity, "tried to move to a cell occupied by another entity - not moving as a result");
                return;
            }
        }
        entity.setPosition(moveDecision.getMoveToX(), moveDecision.getMoveToY());
        logAction(entity, "moved to (" + moveDecision.getMoveToX() + ", " + moveDecision.getMoveToY() + ")");
    }

    private void performAction(Entity entity, ActionDecision actionDecision) {
        if (actionDecision.getActionType() != null) {

            if (actionDecision.getActionType().equals(ActionType.ATTACK)) {
                int damage = entity.attack(actionDecision.getTarget());
                logAction(entity, "attacked " + actionDecision.getTarget().getClass().getSimpleName()
                        + " dealing " + damage + " damage. Entity is down to "
                        + actionDecision.getTarget().getHealth() + "HP.");
            }
            if (actionDecision.getActionType().equals(ActionType.HEAL)) {
                if (entity instanceof Mage) {
                    int healingPoints = ((Mage) entity).heal(actionDecision.getTarget());
                    logAction(entity, "healed " + actionDecision.getTarget().getClass().getSimpleName()
                            + " for " + healingPoints + " health to " + actionDecision.getTarget().getHealth() + "HP.");
                } else {
                    throw new IllegalArgumentException("Only Mages can heal");
                }
            }
            if (actionDecision.getActionType().equals(ActionType.BOLT)) {
                if (entity instanceof Mage) {
                    int damage = ((Mage) entity).bolt(actionDecision.getTarget());
                    logAction(entity, "bolted " + actionDecision.getTarget().getClass().getSimpleName()
                            + " dealing " + damage + " damage. Entity is down to "
                            + actionDecision.getTarget().getHealth() + "HP.");
                } else {
                    throw new IllegalArgumentException("Only Mages can bolt");
                }
            }
            if (actionDecision.getActionType().equals(ActionType.STUN)) {
                if (entity instanceof Warrior) {
                    int damage = ((Warrior) entity).stun(actionDecision.getTarget());
                    logAction(entity, "stunned " + actionDecision.getTarget().getClass().getSimpleName()
                            + " dealing " + damage + " damage. Entity is down to "
                            + actionDecision.getTarget().getHealth() + "HP. "
                            + (actionDecision.getTarget().isStunned() ? " Target is stunned." : " Target wasn't stunned."));
                } else {
                    throw new IllegalArgumentException("Only Warriors can stun");

                }
            }
        }
    }

    private void logAction(Entity entity, String action) {
        System.out.println(entity.getClass().getSimpleName() + " team " + entity.getTeamId()
                + " at (" + entity.getX() + ", " + entity.getY() + ") " + action);
    }

    private void displayStatus() {
        System.out.println("Current Status:");
        for (Entity entity : teamAEntities) {
            System.out.println("A: " + entity.getClass().getSimpleName() + " - Health: " + entity.getHealth());
        }
        for (Entity entity : teamBEntities) {
            System.out.println("B: " + entity.getClass().getSimpleName() + " - Health: " + entity.getHealth());
        }
        System.out.println();


        //oldschoolCommandLineDisplay();
    }

    private void oldschoolCommandLineDisplay() {
        // Log the whole game field
        String[][] grid = new String[80][80];
        for (int i = 0; i < 80; i++) {
            for (int j = 0; j < 80; j++) {
                grid[i][j] = "  ";
            }
        }

        for (Entity entity : teamAEntities) {
            String symbol = getEntitySymbol(entity, 1);
            grid[entity.getX() - 1][entity.getY() - 1] = symbol;
        }
        for (Entity entity : teamBEntities) {
            String symbol = getEntitySymbol(entity, 2);
            grid[entity.getX() - 1][entity.getY() - 1] = symbol;
        }

        for (int i = 0; i < 80; i++) {
            for (int j = 0; j < 80; j++) {
                System.out.print(grid[i][j] + " ");
            }
            System.out.println();
        }
    }

    public static void main(String[] args) {
        Player playerA = new HumanPlayer(1);
        Player playerB = new AiPlayer(2);
        Game game = new Game(playerA, playerB);
        game.start();
    }


    private String getEntitySymbol(Entity entity, int team) {
        String symbol = "";
        if (entity instanceof Warrior) {
            symbol = "W" + team;
        } else if (entity instanceof Mage) {
            symbol = "M" + team;
        } else if (entity instanceof Archer) {
            symbol = "A" + team;
        }
        return symbol;
    }
}
