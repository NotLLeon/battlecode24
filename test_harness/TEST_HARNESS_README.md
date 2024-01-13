## How to use the testing harness:
Don't modify test_harness_internal.py, but instead create another python file and call it from there. 

If you have issues with python, then modify the `if __name__=='__main__` part of the `test_harness_internal.py` to your liking.

See sample_test.py for an example of what the usage looks like.

Function params:

`runGamesMain(currBot, botsVersusList, mapsList)`

The currBot indicates the name of the current bot that you would like to test on,
and botsVersusList is a list of bots that your current bot will be paired against.
Generally have this be set to all the previous bots we've implemented. 
mapsList is a list of maps that these matches will happen on.

Every matchup is run twice, since we need to switch sides.

`getGameAnalytics(gamesResultsDict)`

gamesResultsDict is just the output of runGamesMain().
getGameAnalytics() outputs a info about how the games went.

Current it returns:
1) How many games were won, and what the win percentage was.
2) Which games did the currBot lose on
3) Which games did the outcome change based on the side? This is prob helpful for detecting symmetry.

I'd recommend just using the debugger if you want to see the results, but printing it or piping it into a file works too.
