package game;

import java.util.*;

import static game.InOutUtils.readStringsFromInputStream;
import static game.ProcessUtils.UTF_8;

/**
 * Main samplegame class.
 */
public class Main {

    public static void main(String[] args) {
        List<String> input = readStringsFromInputStream(System.in, UTF_8);
        if (!input.isEmpty()) {
            Round round = new Round(input);
            printMovingGroups(makeMove(round));
        }
        System.exit(0);
    }

    private static List<MovingGroup> makeMove(Round round) {
        List<MovingGroup> movingGroups = new ArrayList<>();

        List<Planet> myPlanets = round.getOwnPlanets();
        List<Planet> noMenPlanets = round.getNoMansPlanets();
        int teamId = myPlanets.get(0).getId();

        if (round.getCurrentStep() == 0) {
            for (int i = 0; i < 3; i++) { //отправка кораблей на 3 ближайшие планеты
                if (teamId == 0)
                    movingGroups.add(new MovingGroup(teamId, teamId + i + 1, noMenPlanets.get(teamId + i).getPopulation() + 1));
                else
                    movingGroups.add(new MovingGroup(teamId, teamId - i + 1, noMenPlanets.get(teamId + i).getPopulation() + 1));
            }
            int idPlanetX5;
            if (teamId == 0) //вычисление 4ой ближайшей планеты
                idPlanetX5 = teamId + 4;
            else idPlanetX5 = teamId - 4;
            movingGroups.add(new MovingGroup(teamId, idPlanetX5, noMenPlanets.get(idPlanetX5).getPopulation() + 1)); //отправка кораблей на 4ю ближайшую планету
        }
        List<MovingGroup> enemyShips = new ArrayList<>();
        List<MovingGroup> myShips = new ArrayList<>();
        try {
            enemyShips = round.getAdversarysMovingGroups(); //корабли противника
            myShips = round.getOwnMovingGroups(); //наши корабли
        } catch (Exception e) {

        }
        for (int i = 0; i < enemyShips.size(); i++) {
            MovingGroup mgEnemy = enemyShips.get(i); //конкретный корабль противника
            for (int j = 0; j < myShips.size(); j++) {
                MovingGroup mgMy = myShips.get(j); //конкретный наш корабль
                for (int g = 0; g < myPlanets.size(); g++) {
                    if (mgEnemy.getTo() == mgMy.getTo() || (mgEnemy.getTo() == myPlanets.get(g).getId())) { //если противник летит на ту же планету, что и мы
                        if (!Main.isShipsEqual(enemyShips, myShips, myPlanets.get(g).getId())) {
                            movingGroups.add(new MovingGroup(teamId, mgEnemy.getTo(), mgEnemy.getCount()));
                            g = myPlanets.size();
                            j = myShips.size();
                        }
                    }
                }
            }
        }
        int minDistance = 100;
        int ownPop = 0;
        int enemyPop = 0;
        int idOwnPlanet = 0;
        int idEnemyPlanet = 0;
        int enemyReproduction = 0;
        List<Planet> enemyPlanets = round.getAdversarysPlanets(); //планеты противника
        for (Planet myPlanet : myPlanets) {
            for (Planet enemyPlanet : enemyPlanets) {
                int dist = round.getDistanceMap()[myPlanet.getId()][enemyPlanet.getId()];
                if (dist < minDistance) {
                    minDistance = dist;
                    ownPop = myPlanet.getPopulation();
                    enemyPop = enemyPlanet.getPopulation() + 1;
                    idOwnPlanet = myPlanet.getId();
                    idEnemyPlanet = enemyPlanet.getId();
                    enemyReproduction = enemyPlanet.getReproduction();
                }
            }
            int curEnemyPop = enemyPop + minDistance*enemyReproduction;

            if (ownPop > curEnemyPop) {
                movingGroups.add(new MovingGroup(idOwnPlanet, idEnemyPlanet, curEnemyPop));
            }
            else{
                int minDist2 = 100;
                for (Planet planet : myPlanets) {
                    int betweenDist = round.getDistanceMap()[planet.getId()][idEnemyPlanet];
                    int curEnemyPop2 = enemyPop + minDist2*enemyReproduction;
                    if ((betweenDist < minDist2) && (curEnemyPop2 < planet.getPopulation())){
                        minDist2 = betweenDist;
                        idOwnPlanet = planet.getId();
                        ownPop = planet.getPopulation();
                        curEnemyPop = curEnemyPop2;
                    }
                }
                if (ownPop > curEnemyPop) {
                    movingGroups.add(new MovingGroup(idOwnPlanet, idEnemyPlanet, curEnemyPop));
                }
            }
            minDistance = 100;
        }
        return movingGroups;
    }

    private static void printMovingGroups(List<MovingGroup> moves) {
        System.out.println(moves.size());
        moves.forEach(move -> System.out.println(move.getFrom() + " " + move.getTo() + " " + move.getCount()));
    }

    private static boolean isShipsEqual(List<MovingGroup> group1, List<MovingGroup> group2, int idPlanet) {
        int count1 = 0, count2 = 0;
        for (MovingGroup movingGroup : group1) {
            if (movingGroup.getTo() == idPlanet) {
                count1++;
            }
        }
        for (MovingGroup movingGroup : group2) {
            if (movingGroup.getTo() == idPlanet) {
                count2++;
            }
        }
        return count1 == count2;
    }
}
