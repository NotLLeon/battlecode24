from test_harness_internal import runGamesMain, getGameAnalytics

'''
Example code of how to use the test-harness. See TEST_HARNESS_README.MD for more details
'''
if __name__ == '__main__':
    print('Starting games!')
    retDict = runGamesMain('examplefuncsplayer',['examplefuncsplayer'], ['DefaultSmall'])
    gameAnalyticsInfo = getGameAnalytics(retDict)
    print(gameAnalyticsInfo)
