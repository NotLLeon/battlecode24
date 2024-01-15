import argparse
from test_harness_internal import runGamesMain, getGameAnalytics
from table_script import transformGameDataAsNestedMap, printTabulateAsGrid


DEFAULT_BOTS = ['v0', 'examplefuncsplayer']
DEFAULT_MAPS = ['DefaultSmall', 'DefaultMedium', 'DefaultLarge', 'DefaultHuge']

def main():
    parser = argparse.ArgumentParser(description='Runs Battlecode games and prints out the results')


    # Optional flag arguments
    parser.add_argument('-b', '--bots', nargs='+', type=str, default=DEFAULT_BOTS, help='Bots to match up against')
    parser.add_argument('-m', '--maps', nargs='+', type=str, default=DEFAULT_MAPS, help='Maps to play on')

    # Mandatory args
    parser.add_argument('curr_bot', type=str, help='Name of the current bot you want to test')

    args = parser.parse_args()

    # Accessing the values
    currBot = args.curr_bot
    oppBots = args.bots
    mapsList = args.maps

    # Your script logic here
    print('Starting games!')
    gamesResultsList = runGamesMain(currBot=currBot,
                           botsVersusList=oppBots,
                           mapsList=mapsList)
    gameAnalyticsInfo = getGameAnalytics(gamesResultsList)
    print(gameAnalyticsInfo)

    resDictTransformed = transformGameDataAsNestedMap(gamesResultsList)
    printTabulateAsGrid(resDictTransformed, mapsList=mapsList)




if __name__ == '__main__':
    main()