import argparse
from test_harness_internal import runGamesMain
from table_script import transformGameDataAsNestedMap, printTabulateAsGrid

from constants import *
from file_operations import *
from pathlib import Path
import time

def checkBotsExist(botsList):
    for botName in botsList:
        botDirPath = Path(f"../src/{botName}")
        if not botDirPath.exists():
            print(f"{botName} is not found!")
            return False
    return True


def main():





    parser = argparse.ArgumentParser(description='Runs Battlecode games and prints out the results')


    # Mandatory arg, which bots to play against
    parser.add_argument('-b', '--bots', nargs='+', type=str, required=True, help='Bots to match up against')

    # Optional args
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

    if not checkBotsExist([currBot]):
        return
    if not checkBotsExist(oppBots):
        return

    # Record the start time
    start_time = time.time()

    # Your script logic here
    print('Starting games!')
    gamesResultsList = runGamesMain(currBot=currBot,
                           botsVersusList=oppBots,
                           mapsList=mapsList)
    # gameAnalyticsInfo = getGameAnalytics(gamesResultsList)
    # print(gameAnalyticsInfo)
    # Record the end time
    end_time = time.time()

    # Calculate the elapsed time in seconds
    elapsed_time = end_time - start_time

    print(f"Time taken: {elapsed_time} seconds")

    resDictTransformed = transformGameDataAsNestedMap(gamesResultsList)
    printTabulateAsGrid(resDictTransformed, botsVersusList=oppBots)




if __name__ == '__main__':
    main()