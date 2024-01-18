## How to use the local testing harness:
NOTE: Need to pip install `tabulate` as a dependency.

Both scripts have a `--help` argument you can use to get more info.

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

## How to use the queuing tool
NOTE: Do NOT have multiple people queueing up games at the same time! This script can tolerate up to 10 additional games being queued
while it's running, before it doesn't get the output of the initially queued games correctly! Only have a single person use this tool at once.
Carlos will fix this in the future if it becomes a problem.

Call `queue_games.py` as a console command


Usage: `python3 queue_games.py [-m MAPS [MAPS ...]] (-b BOTS [BOTS ...] | -r READ) [-a]`

Example usages:  
`python3 queue_games.py -m DefaultSmall DefaultLarge -b "Teh Devs" "camel_case" `

`python3 queue_games.py -m DefaultSmall DefaultLarge -r ./SampleTeams.json `



The arguments are as follows:

`-m` is followed up by a list of maps that you wish to play. Have a max of 3 maps, otherwise it randomly selects 3 maps for you.

`-b` and `-r` are mutually exclusive, and are required. Either pass in a list of bot names (with double quotes), or pass
in a json that contains a list of team names. See `SampleTeams.json` for what this file should look like.

`-a` is "append-mode", for the replay links. All replays are stored in `match_links.json`,
and by default this file will be overwritten with each call to this script. Append mode just appends
new replays to the end of the json, instead of overwriting it.