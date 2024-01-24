import argparse
from test_harness_internal import runGamesMain
from table_script import transformGameDataAsNestedMap, printTabulateAsGrid

from constants import *
from file_operations import *


DEFAULT_BOTS = ['v0', 'examplefuncsplayer']
DEFAULT_MAPS = ['DefaultSmall', 'DefaultMedium', 'DefaultLarge', 'DefaultHuge']

def main():
    parser = argparse.ArgumentParser(description='Runs Battlecode games and prints out the results')


    # Optional flag arguments
    parser.add_argument('-b', '--bots', nargs='+', type=str, default=DEFAULT_BOTS, help='Bots to match up against')

    mapsGroup = parser.add_mutually_exclusive_group(required=False)
    mapsGroup.add_argument('-m', '--maps', nargs='+', type=str, help='Maps to play on')
    mapsGroup.add_argument('-mf', '--mapsfile', type=str, help='Path to a file containing list of maps to play on')
    mapsGroup.add_argument('-ma', '--mapsall', action='store_true', help='Select all maps to play on, as defined by ALL_MAPS in constant.py')


# Mandatory args
    parser.add_argument('curr_bot', type=str, help='Name of the current bot you want to test')

    args = parser.parse_args()

    # Accessing the values
    currBot = args.curr_bot
    oppBots = args.bots

    mapsList = DEFAULT_MAPS
    if args.mapsall:
        mapsList = ALL_MAPS
    if args.mapsfile:
        mapsList = loadJSON(args.mapsfile)
    elif args.maps:
        mapsList = args.maps



    # Your script logic here
    print('Starting games!')
    gamesResultsList = runGamesMain(currBot=currBot,
                           botsVersusList=oppBots,
                           mapsList=mapsList)
    # gameAnalyticsInfo = getGameAnalytics(gamesResultsList)
    # print(gameAnalyticsInfo)

    resDictTransformed = transformGameDataAsNestedMap(gamesResultsList)
    printTabulateAsGrid(resDictTransformed, mapsList=mapsList)




if __name__ == '__main__':
    main()