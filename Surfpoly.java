import java.util.*;

public class Surfpoly {
    // Player class representing each player's details
    static class Player {
        private final String name;
        private int position;
        private int money;
        private final List<String> properties;
        private boolean inJail;
        private int jailTurns;
        private final String token;

        public Player(String name, String token) {
            this.name = name;
            this.position = 0;
            this.money = 1500; // Starting money
            this.properties = new ArrayList<>();
            this.inJail = false;
            this.jailTurns = 0;
            this.token = token;
        }

        public String getName() {
            return name;
        }

        public int getPosition() {
            return position;
        }

        public int getMoney() {
            return money;
        }

        public List<String> getProperties() {
            return properties;
        }

        public boolean isInJail() {
            return inJail;
        }

        public String getToken() {
            return token;
        }

        public void move(int steps, int boardSize) {
            if (!inJail) {
                position = (position + steps) % boardSize;
            }
        }

        public void goToJail() {
            inJail = true;
            jailTurns = 3;
            position = 3; // Jail position on the board
        }

        public void leaveJail() {
            inJail = false;
            jailTurns = 0;
        }

        public void payMoney(int amount) {
            money -= amount;
        }

        public void earnMoney(int amount) {
            money += amount;
        }

        public void buyProperty(String propertyName, int price) {
            money -= price;
            properties.add(propertyName);
        }
    }

    // Property class representing properties on the board
    static class Property {
        private final String name;
        private final int price;
        private final int rent;
        private Player owner;

        public Property(String name, int price, int rent) {
            this.name = name;
            this.price = price;
            this.rent = rent;
            this.owner = null; // Unowned initially
        }

        public String getName() {
            return name;
        }

        public int getPrice() {
            return price;
        }

        public int getRent() {
            return rent;
        }

        public Player getOwner() {
            return owner;
        }

        public void setOwner(Player owner) {
            this.owner = owner;
        }
    }

    // ChanceCard class representing chance cards on the board
    static class ChanceCard {
        private final String description;
        private final int moneyEffect;

        public ChanceCard(String description, int moneyEffect) {
            this.description = description;
            this.moneyEffect = moneyEffect;
        }

        public void applyEffect(Player player) {
            player.earnMoney(moneyEffect);
            System.out.println(description + " Effect: " + (moneyEffect >= 0 ? "+$" : "-$") + Math.abs(moneyEffect));
        }
    }

    // Function to display the game board
    static void displayBoard(Object[] board, List<Player> players) {
        StringBuilder boardDisplay = new StringBuilder("Board:\n");
        for (int i = 0; i < board.length; i++) {
            boardDisplay.append("| ");
            boolean playerPresent = false;
            for (Player player : players) {
                if (player.getPosition() == i) {
                    boardDisplay.append(player.getToken());
                    if (player.isInJail()) boardDisplay.append("(J)");
                    playerPresent = true;
                }
            }
            if (!playerPresent) {
                if (board[i] instanceof Property) {
                    boardDisplay.append(((Property) board[i]).getName());
                } else if (board[i] instanceof ChanceCard) {
                    boardDisplay.append("Chance");
                } else {
                    boardDisplay.append(board[i]);
                }
            }
            boardDisplay.append(" ");
        }
        boardDisplay.append("|");
        System.out.println(boardDisplay);
    }

    // Function to display the leaderboard
    static void displayLeaderboard(List<Player> players) {
        players.sort((p1, p2) -> (p2.getMoney() + p2.getProperties().size() * 200) - (p1.getMoney() + p1.getProperties().size() * 200));
        System.out.println("\nLeaderboard:");
        for (int i = 0; i < players.size(); i++) {
            Player player = players.get(i);
            int netWorth = player.getMoney() + player.getProperties().size() * 200;
            System.out.println((i + 1) + ". " + player.getName() + " (" + player.getToken() + ") - Net Worth: $" + netWorth);
        }
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        // Define the board
        Object[] board = {
            "Go",
            new Property("Pipeline, Hawaii", 200, 50),
            new Property("Teahupo'o, Tahiti", 220, 55),
            "Jail",
            new Property("Jeffreys Bay, South Africa", 250, 60),
            new Property("Bells Beach, Australia", 280, 70),
            "Surf Station",
            new Property("Trestles, California", 300, 75),
            new Property("Snapper Rocks, Australia", 320, 80),
            new ChanceCard("You found a sponsorship deal!", 200),
            new Property("Rincon, Puerto Rico", 230, 60),
            new Property("Hossegor, France", 260, 65),
            new Property("El Porto, California", 290, 75),
            new Property("Waimea Bay, Hawaii", 310, 85),
            new Property("Fistral Beach, England", 270, 70),
        };

        // Define chance cards
        List<ChanceCard> chanceDeck = new ArrayList<>(Arrays.asList(
            new ChanceCard("You bought new surfing gear.", -150),
            new ChanceCard("You won a local surfing competition!", 300),
            new ChanceCard("You had to repair your surfboard.", -100),
            new ChanceCard("You earned royalties from a surf movie appearance.", 250),
            new ChanceCard("You paid a fine for surfing in a restricted area.", -200)
        ));
        Collections.shuffle(chanceDeck);

        // Initialize players
        System.out.println("Enter number of players:");
        int numPlayers = scanner.nextInt();
        scanner.nextLine(); // Consume newline
        List<Player> players = new ArrayList<>();
        
        // Token management
        String[] tokens = {"\uD83C\uDF0A", "\uD83C\uDFD6", "\uD83C\uDF34", "\uD83C\uDFC4", "\uD83C\uDFAF"};

        for (int i = 0; i < numPlayers; i++) {
            System.out.println("Enter name of player " + (i + 1) + ":");
            String name = scanner.nextLine();
            String token = tokens[i % tokens.length]; // Assign tokens in a cyclic manner
            players.add(new Player(name, token));
        }

        Random dice = new Random();
        boolean gameOn = true;

        while (gameOn) {
            // Use iterator for safe modification of the list
            Iterator<Player> iterator = players.iterator();
            while (iterator.hasNext()) {
                Player player = iterator.next();

                if (player.isInJail()) {
                    player.jailTurns--;
                    if (player.jailTurns == 0) {
                        player.leaveJail();
                        System.out.println(player.getName() + " is now free from jail!");
                    } else {
                        System.out.println(player.getName() + " is in jail for " + player.jailTurns + " more turns.");
                        continue;
                    }
                }

                System.out.println("\n" + player.getName() + "'s turn. Press Enter to roll the dice.");
                scanner.nextLine();
                int roll = dice.nextInt(6) + 1;
                System.out.println(player.getName() + " rolled a " + roll + ".");
                player.move(roll, board.length);

                Object landedField = board[player.getPosition()];
                if (landedField instanceof Property) {
                    Property landedProperty = (Property) landedField;
                    System.out.println(player.getName() + " landed on " + landedProperty.getName() + ".");
                    if (landedProperty.getOwner() == null) {
                        System.out.println("This property is unowned. Price: $" + landedProperty.getPrice());
                        if (player.getMoney() >= landedProperty.getPrice()) {
                            System.out.println("Do you want to buy it? (yes/no)");
                            String choice = scanner.nextLine();
                            if (choice.equalsIgnoreCase("yes")) {
                                player.buyProperty(landedProperty.getName(), landedProperty.getPrice());
                                landedProperty.setOwner(player);
                                System.out.println("You bought " + landedProperty.getName() + "!");
                            } else {
                                System.out.println("You chose not to buy.");
                            }
                        } else {
                            System.out.println("You don't have enough money to buy this property.");
                        }
                    } else if (landedProperty.getOwner() != player) {
                        System.out.println("This property is owned by " + landedProperty.getOwner().getName() + ". Rent is $" + landedProperty.getRent());
                        player.payMoney(landedProperty.getRent());
                        landedProperty.getOwner().earnMoney(landedProperty.getRent());
                        System.out.println("You paid $" + landedProperty.getRent() + " to " + landedProperty.getOwner().getName());
                    } else {
                        System.out.println("You own this property.");
                    }
                } else if (landedField instanceof ChanceCard) {
                    ChanceCard chanceCard = (ChanceCard) landedField;
                    chanceCard.applyEffect(player);
                    chanceDeck.add(chanceDeck.remove(0)); // Move used card to the end of the deck
                } else if (landedField.equals("Jail")) {
                    System.out.println("Go to Jail! You are now in jail for 3 turns.");
                    player.goToJail();
                } else if (landedField.equals("Go")) {
                    System.out.println("You passed Go! Collect $200.");
                    player.earnMoney(200);
                }

                System.out.println(player.getName() + " has $" + player.getMoney() + " and owns: " + player.getProperties());
                if (player.getMoney() <= 0) {
                    System.out.println(player.getName() + " is bankrupt! Game over for them.");
                    iterator.remove(); // Remove player using iterator
                }

                displayBoard(board, players);
                displayLeaderboard(players);

                if (players.size() == 1) {
                    System.out.println(players.get(0).getName() + " is the winner!");
                    gameOn = false;
                    break;
                }
            }
        }

        scanner.close();
    }
}
