from test_harness_internal import runGamesMain, getGameAnalytics
from table_script import transformGameDataAsRows, printTabulate, transformGameDataAsNestedMap, printTabulateAsGrid
'''
DONT USE THIS! 
'''
if __name__ == '__main__':
    print('Starting games!')
    # retDict = runGamesMain(currBot='v1',
    #                        botsVersusList=['v0','examplefuncsplayer'],
    #                        mapsList=['DefaultSmall', 'DefaultMedium', 'DefaultLarge', 'DefaultHuge'])
    retDict = runGamesMain(currBot='v1',
                           botsVersusList=['v0', 'examplefuncsplayer'],
                           mapsList=['DefaultSmall', 'DefaultMedium', 'DefaultLarge'])
    print('got here')
    gameAnalyticsInfo = getGameAnalytics(retDict)
    print(gameAnalyticsInfo)
    resDictTransformed = transformGameDataAsNestedMap(retDict)
    print('got here')
    printTabulateAsGrid(resDictTransformed, ['DefaultSmall', 'DefaultMedium', 'DefaultLarge'])
    # printTabulate(transformGameDataAsRows(retDict), ['DefaultSmall', 'DefaultMedium'])

