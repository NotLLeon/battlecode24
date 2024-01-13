from test_harness_internal import runGamesMain, getGameAnalytics

'''
Example code of how to use the test-harness. See TEST_HARNESS_README.MD for more details
'''
if __name__ == '__main__':
    print('Starting games!')
    retDict = runGamesMain(currBot='v1',
                           botsVersusList=['v0','examplefuncsplayer'],
                           mapsList=['DefaultSmall'])

    print('got here')
    gameAnalyticsInfo = getGameAnalytics(retDict)
    print(gameAnalyticsInfo)
