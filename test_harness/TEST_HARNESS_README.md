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
We can now have multiple users calling this script at once without issue! 

Call `queue_games.py` as a console command


Usage: `queue_games.py [-m MAPS [MAPS ...] | -mf MAPSFILE] (-b BOTS [BOTS ...] | -r READ) [-a]`

Example usages:  
`python3 queue_games.py -m DefaultSmall DefaultLarge -b "Teh Devs" "camel_case" `

`python3 queue_games.py -m DefaultSmall DefaultLarge -r ./SampleTeams.json `

`python3 queue_games.py -mf ./SampleMaps.json -b "Teh Devs" "camel_case" `



The arguments are as follows:

`-m` `-mf` are mutually exclusive, and is optional. Either pass in a list of maps (with -m flag) or a path to a json file that contains a list of maps (with -mf flag). See what `SampleMaps.json` looks like for passing in a list of maps 
If neither argument is used, then it defaults to DEFAULT_MAPS, which is a variable in the constants.py file.
There is a maximum of 10 maps that can be chosen at once.

`-b` and `-r` are mutually exclusive, and are required. Either pass in a list of bot names (with double quotes), or pass
in a json file path that contains a list of team names. See `SampleTeams.json` for what this file should look like.

`-a` is "append-mode", for the replay links. All replays are stored in `match_links.json`,
and by default this file will be overwritten with each call to this script. Append mode just appends
new replays to the end of the json, instead of overwriting it.
