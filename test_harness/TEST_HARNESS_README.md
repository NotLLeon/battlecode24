## How to use the testing harness:
NOTE: Need to pip install `tabulate` as a dependency.

Call `run_tests_script.py` as a console command


Usage: `python3 run_tests_script.py  curr_bot [-b BOTS [BOTS ...]] [-m MAPS [MAPS ...]] `

It has a mandatory argument of the current bot you want to test (`curr_bot`), 
(optionally) followed up by a list of bots to play against, then (optionally) followed up by
all the maps to play on. 

The default for bots to play against is V0 and examplefuncsplayer, and the default maps are all the maps

Example usage: `python3 run_tests_script.py v1 -b v0 examplefuncsplayer -m DefaultSmall DefaultLarge`

Plays the v1 bot against v0, examplefuncsplayer on maps DefaultSmall, DefaultLarge

Additionally, I currently have some basic analytics about the game that might be of help. 
These are printed to the console upon being called
1) How many games were won, and what the win percentage was.
2) Which games did the currBot lose on
3) Which games did the outcome change based on the side? This is prob helpful for detecting symmetry.

