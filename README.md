# numeral-pursuit-game
An android game for two-player sudoku

The game is a two-player version of sudoku. A player attempts to place a tile. 

When placed, the app checks if that placement is a valid solution for the current grid using normal sudoku rules. 

A point is scored for each successfully placed tile. The players take turns to attempt tile placements

Once the whole board is filled, the winner is the one with the most points.

The game is played using an appengine server as an intermediary between the two players. The server is located in a separate repository
