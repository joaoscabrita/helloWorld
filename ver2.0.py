import random

class Player:
    """Represents a player in the game."""
    def __init__(self, name):
        self.name = name
        self.money = 1500  # Starting money for each player
        self.position = 0  # Start at GO
        self.in_jail = False
        self.jail_turns = 0
        self.properties = []  # List of properties owned by the player

    def move_to_position(self, position):
        """Moves the player to a new board position."""
        self.position = position

    def add_money(self, amount):
        """Adds money to the player's balance."""
        self.money += amount

    def subtract_money(self, amount):
        """Subtracts money from the player's balance."""
        self.money -= amount

    def add_property(self, property):
        """Adds a property to the player's assets."""
        self.properties.append(property)

    def get_properties(self):
        """Returns a list of properties owned by the player."""
        return self.properties


class Property:
    """Represents a property on the board."""
    def __init__(self, name, price, rent, color_group):
        self.name = name
        self.price = price
        self.rent = rent
        self.color_group = color_group
        self.owner = None  # No owner when the game starts

    def calculate_rent(self):
        """Returns the rent value for the property."""
        return self.rent


# Subclasses of Property representing different property types
class Beach(Property):
    pass

class WavePool(Property):
    pass

class SurfBrand(Property):
    pass


class Card:
    """Represents a game card (Chance or Lucky Draw)."""
    def __init__(self, description, card_type, action):
        self.description = description
        self.card_type = card_type  # "CHANCE" or "LUCKY_DRAW"
        self.action = action  # Function to execute when drawn

    def execute(self, player, game):
        """Executes the effect of the card."""
        self.action(player, game)


class Game:
    """Represents the main Surfopoly game."""
    def __init__(self):
        self.players = []  # List of players in the game
        self.board = []  # The game board (list of properties and special spaces)
        self.chance_cards = []  # List of Chance cards
        self.lucky_draw_cards = []  # List of Lucky Draw cards
        self.current_player_index = 0  # Keeps track of whose turn it is
        self.random = random.Random()
        
        self.initialize_board()
        self.initialize_cards()

    def initialize_board(self):
        """Initializes the game board with properties and special spaces."""
        self.board.clear()

        # GO space (position 0)
        self.board.append(None)

        # Example property group: Brown (Beginner Surf Spots)
        self.board.append(Beach("Pipeline, Hawaii", 60, 2, "BROWN"))
        self.board.append(None)  # Community Chest
        self.board.append(Beach("Waimea Bay, Hawaii", 60, 4, "BROWN"))
        self.board.append(None)  # Income Tax
        self.board.append(WavePool("North Shore Wave Pool", 200))

        # Jail (position 10)
        self.board.append(None)

        # More properties would be added here following the same pattern...

    def initialize_cards(self):
        """Initializes the Chance and Lucky Draw card decks."""
        self.chance_cards.clear()
        self.chance_cards.extend([
            Card("Your surfboard sponsorship pays out! Collect $150", "CHANCE",
                 lambda player, game: player.add_money(150)),

            Card("Caught in a bad wipeout. Pay medical expenses $100", "CHANCE",
                 lambda player, game: player.subtract_money(100))
        ])

        self.lucky_draw_cards.clear()
        self.lucky_draw_cards.extend([
            Card("Win local surf competition. Collect $100", "LUCKY_DRAW",
                 lambda player, game: player.add_money(100))
        ])

        # Shuffle both decks
        random.shuffle(self.chance_cards)
        random.shuffle(self.lucky_draw_cards)

    def add_player(self, name):
        """Adds a player to the game."""
        self.players.append(Player(name))

    def start_game(self):
        """Starts the game by resetting the player turn index."""
        self.current_player_index = 0

    def take_turn(self):
        """Handles a player's turn."""
        player = self.players[self.current_player_index]

        if player.in_jail:
            self.handle_jail_turn(player)
            return

        # Roll two dice
        roll1 = random.randint(1, 6)
        roll2 = random.randint(1, 6)
        total_roll = roll1 + roll2

        # Move the player
        self.move_player(player, total_roll)
        self.handle_landing_space(player)

        # If doubles were not rolled, move to the next player
        if roll1 != roll2:
            self.next_player()

    def handle_jail_turn(self, player):
        """Handles a player's turn when they are in jail."""
        player.jail_turns += 1

        if player.jail_turns >= 3:
            # Pay fine and leave jail
            player.subtract_money(50)
            player.in_jail = False
            self.take_turn()
        else:
            roll1 = random.randint(1, 6)
            roll2 = random.randint(1, 6)

            if roll1 == roll2:
                # Player rolls doubles and escapes jail
                player.in_jail = False
                self.move_player(player, roll1 + roll2)
                self.handle_landing_space(player)

            self.next_player()

    def move_player(self, player, spaces):
        """Moves a player forward by a number of spaces."""
        new_position = (player.position + spaces) % len(self.board)

        if new_position < player.position:
            # Player passes GO, collect $200
            player.add_money(200)

        player.move_to_position(new_position)

    def handle_landing_space(self, player):
        """Handles the effects of landing on a space."""
        space = self.board[player.position]

        if space is None:
            self.handle_special_square(player)
            return

        if space.owner is None:
            if player.money >= space.price:
                self.purchase_property(player, space)
        elif space.owner != player:
            rent = space.calculate_rent()
            player.subtract_money(rent)
            space.owner.add_money(rent)

    def handle_special_square(self, player):
        """Handles special spaces like GO, Jail, Free Parking, and Go to Jail."""
        if player.position == 0:
            pass  # GO
        elif player.position == 10:
            pass  # Just visiting Jail
        elif player.position == 20:
            pass  # Free Parking
        elif player.position == 30:
            self.send_to_jail(player)

    def send_to_jail(self, player):
        """Sends a player to jail."""
        player.in_jail = True
        player.move_to_position(10)

    def purchase_property(self, player, property):
        """Handles the purchase of an unowned property."""
        player.subtract_money(property.price)
        player.add_property(property)
        property.owner = player

    def next_player(self):
        """Moves to the next player's turn."""
        self.current_player_index = (self.current_player_index + 1) % len(self.players)

    def is_game_over(self):
        """Checks if the game is over (only one player left with money)."""
        return sum(1 for p in self.players if p.money > 0) <= 1

    def get_current_player(self):
        """Returns the current player whose turn it is."""
        return self.players[self.current_player_index]

    def draw_card(self, card_type):
        """Draws a Chance or Lucky Draw card and applies its effect."""
        player = self.get_current_player()
        
        if card_type == "CHANCE":
            card = self.chance_cards.pop(0)
            self.chance_cards.append(card)  # Put it at the back
        else:
            card = self.lucky_draw_cards.pop(0)
            self.lucky_draw_cards.append(card)

        card.execute(player, self)

    def get_property(self, position):
        """Returns the property at a given position."""
        return self.board[position]

    def get_properties_by_color(self, color_group):
        """Returns all properties of a certain color group."""
        return [p for p in self.board if p and p.color_group == color_group]