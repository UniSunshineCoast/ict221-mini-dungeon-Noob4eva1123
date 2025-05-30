import java.util. *;

public class DungeonEngine{
    interface Cell {
    char getSymbol();

    boolean isPassable();

    void onEnter(Player player);
}

static class Floor implements Cell {
    public char getSymbol() {
        return '.';
    }

    public boolean isPassable() {
        return true;
    }

    public void onEnter(Player player) {
}
}

static class Wall implements Cell {
    public char getSymbol() {
        return '#';
    }

    public boolean isPassable() {
        return false;
    }

    public void onEnter(Player player) {
}
}

static class Entry implements Cell {
    public char getSymbol() {
        return 'E';
    }

    public boolean isPassable() {
        return true;
    }

    public void onEnter(Player player) {
}
}

static class Ladder implements Cell {
    public char getSymbol() {
        return 'L';
    }

    public boolean isPassable() {
        return true;
    }

    public void onEnter(Player player) {
    System.out.println(" you reached the ladder! ");
}
}

static class Trap implements Cell {
    public char getSymbol() {
        return 'T';
    }

    public boolean isPassable() {
        return true;
    }

    public void onEnter(Player player) {
    System.out.println(" you reached the trap! -2HP");
    player.decreaseHP(2);
}
}

static class Gold implements Cell {
    public char getSymbol() {
        return 'G';
    }

    public boolean isPassable() {
        return true;
    }

    public void onEnter(Player player) {
    System.out.println(" You picked up gold. +2 score");
    player.increaseScore(2);
}
}

static class HealthPotion implements Cell {
    public char getSymbol() {
        return 'H';
    }

    public boolean isPassable() {
        return true;
    }


    public void onEnter(Player player) {
    System.out.println(" You used a health potion. +4 HP");
    player.restoreHP(4);
}
}

static class MeleeMutant implements Cell {
    public char getSymbol() {
        return 'M';
    }

    public boolean isPassable() {
        return true;
    }

    public void onEnter(Player player) {
    System.out.println(" You fought a melee mutant! -2 HP, +2 score");
    player.decreaseHP(2);
    player.increaseScore(2);
}
}

static class RangedMutant implements Cell {
    public char getSymbol() {
        return 'R';
    }

    public boolean isPassable() {
        return true;
    }

    public void onEnter(Player player) {
    System.out.println("You defeated a ranged mutant. +2 score");
    player.increaseScore(2);
}
}

static class Player {
    public int x, y;
    private int hp = 10;
    private int score = 0;

    public void moveTo(int newX, int newY) {
    x = newX;
    y = newY;
}

public void decreaseHP(int amt) {
    hp = Math.max(0, hp - amt);
    System.out.println("HP restored to: " + hp);
}

public void restoreHP(int amt) {
    hp = Math.min(10, hp + amt);
    System.out.println("HP restored to: " + hp);
}

public void increaseScore(int amt) {
    score += amt;
    System.out.println("Score: " + score);
}

public boolean isAlive() {
    return hp > 0;
}

public int getHP() {
    return hp;
}

public int getScore() {
    return score;
}
}

static class GameEngine {
    private final int width = 10, height = 10, maxSteps = 100;
    private int steps = 0, level = 1, difficulty;
    private Cell[][] map;
    private Player player = new Player();
    private Random rand = new Random();

    public GameEngine(int difficulty) {
    this.difficulty = difficulty < 0 || difficulty > 10 ? 3 : difficulty;
    player.moveTo(height - 1, 0);
    generateMap();
}

private void generateMap() {
    map = new Cell[height][width];
    for (int i = 0; i < height; i++)
    for (int j = 0; j < width; j++)
    map[i][j] = new Floor();

    for (int i = 0; i < height; i++) {
        map[i][0] = new Wall();
        map[i][width - 1] = new Wall();
    }

    for (int j = 0; j < width; j++) {
        map[0][j] = new Wall();
        map[height - 1][j] = new Wall();
    }

    map[height - 1][0] = level == 2 ? new Ladder() : new Entry();

    if (level == 1) {
        int x, y;
        do {
            x = rand.nextInt(height - 2) + 1;
            y = rand.nextInt(width - 2) + 1;
        } while (x == height - 1 && y == 0);
        map[x][y] = new Ladder();
    }

    placeRandom(new Trap(), 5 + difficulty);
    placeRandom(new Gold(), 5 + difficulty);
    placeRandom(new MeleeMutant(), 3 + difficulty);
    placeRandom(new RangedMutant(), difficulty);
    placeRandom(new HealthPotion(), 2);
}

private void placeRandom(Cell cell, int count) {
    for (int i = 0; i < count; ) {
        int x = rand.nextInt(height - 2) + 1;
        int y = rand.nextInt(width - 2) + 1;
        if (map[x][y] instanceof Floor && (x != player.x || y != player.y)) {
            map[x][y] = cell;
            i++;
        }
    }
}

public String movePlayer(char dir) {
    if (!player.isAlive()) return "You're dead";
    if (steps >= maxSteps) return "Too many steps. Game over.";
    int nx = player.x, ny = player.y;
    switch (dir) {
        case 'u':
            nx--;
            break;
        case 'd':
            nx++;
            break;
        case 'l':
            ny--;
            break;
        case 'r':
            ny++;
            break;
        default:
            return "Invalid input.";
    }
    if (nx < 0 || nx >= height || ny < 0 || ny >= width) return "Out of bounds";
    Cell cell = map[nx][ny];
    if (!cell.isPassable()) return "Blocked!";
    player.moveTo(nx, ny);
    steps++;
    cell.onEnter(player);
    if (cell instanceof Gold || cell instanceof HealthPotion || cell instanceof MeleeMutant)
        map[nx][ny] = new Floor();

    attackFromRangedMutant();

    if (!player.isAlive()) return "You're dead";
    if (cell instanceof Ladder) {
        if (level == 1) {
            level = 2;
            steps = 0;
            difficulty += 2;
            player.moveTo(height - 1, 0);
            generateMap();
            return "level up !";
        } else {
            return "Victory! Score: " + player.getScore();
        }
    }
    return "Moved " + dir + ". Steps: " + steps + "HP: " + player.getHP() + "Score: " + player.getScore();
}

private void attackFromRangedMutant() {
    for (int i = 0; i < height; i++) {
        for (int j = 0; j < width; j++) {
            if (map[i][j] instanceof RangedMutant) {
                int dx = Math.abs(i - player.x), dy = Math.abs(j - player.y);
                if (rand.nextBoolean()) {
                    System.out.println("Ranged mutant hit! -2 HP");
                    player.decreaseHP(2);
                } else {
                    System.out.println("Ranged mutant missed");
                }
            }
        }
    }
}


public void printMap() {
    for (int i = 0; i < height; i++) {
        for (int j = 0; j < width; j++) {
            if (player.x == i && player.y == j) System.out.print('P');
            else System.out.print(map[i][j].getSymbol());
        }
        System.out.println();
    }
}

public boolean isGameOver() {
    return !player.isAlive() || steps >= maxSteps;
}
}

public static void main(String[] args) {
    Scanner scan = new Scanner(System.in);
    System.out.print("Enter difficulty: (0-10): ");
    int diff = 3;
    try {
        diff = Integer.parseInt(scan.nextLine());
    } catch (Exception ignored) {
    }

    GameEngine game = new GameEngine(diff);
    System.out.println("Use, 'u', 'd', 'l', 'r' to move. 'q' to quit");
    game.printMap();

    while (true) {
        System.out.print("> ");
        String cmd = scan.nextLine().toLowerCase();
        if (cmd.equals("q")) break;
        if (cmd.isEmpty()) continue;
        char move = cmd.charAt(0);
        String result = game.movePlayer(move);
        System.out.println(result);
        game.printMap();
        if (result.contains("Victory") || result.contains("dead") || result.contains("Game Over")) break;
    }
    scan.close();
    System.out.println("Game Ended.");
}
}

