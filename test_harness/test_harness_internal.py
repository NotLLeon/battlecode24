import os
import subprocess
import multiprocessing
import re
from collections import defaultdict

# NOTE: Try not to modify this file!
# If you must, then only modify the if __name__ == '__main__' part.


def get_winner_info(input_string, retDict):
    match = re.search(r'(\w+) \(A\) wins \(round (\d+)\)|(\w+) \(B\) wins \(round (\d+)\)', input_string)
    if not match:
        # This should NOT happen
        retDict['Cant determine winner'] = True
    if match.group(1):
        retDict['winningBot'] = match.group(1)
        retDict['winner'] = 'A'
        retDict['round'] = int(match.group(2))
    elif match.group(3):
        retDict['winningBot'] = match.group(3)
        retDict['winner'] = 'B'
        retDict['round'] = int(match.group(4))

def extractInsideBrackets(input_string):
    match = re.search(r'\((.*?)\)', input_string)
    if match:
        return match.group(1)
    else:
        return 'Tiebreak, but couldnt get reason'
def setWinningReason(reasonStr, retDict):
    if 'team captured all flags' in reasonStr:
        retDict['winningReason'] = 'Captured All Flags'
    elif 'tiebreakers' in reasonStr:
        retDict['tieBreaker'] = True
        # need to get tiebreak reason
        retDict['winningReason'] = extractInsideBrackets(reasonStr)

def getGameInfo(logStr: str):
    # Only care about lines that start with `[server]`
    importantLogLines = [line.strip() for line in logStr.split('\n') if line.strip().startswith('[server]')]
    retDict = {'tieBreaker' : False}
    for line in importantLogLines:
        # Assuming that there is a single instance of the word `wins`
        if 'wins' in line:
            get_winner_info(line, retDict)
            continue
        if 'Reason:' in line:
            setWinningReason(line, retDict)
            continue

    return retDict

# Expected arguments: (gameMap, teamA, teamB)
def runGames(args: tuple) -> dict:
    gameMap, teamA, teamB, currBotLabel = args

    command = ["./gradlew", "--parallel", "run", f"-Pmaps={gameMap}", f"-PteamA={teamA}",
               f"-PteamB={teamB}"]
    # cwd is battlecode24/test-harness, cd to battlecode24/ instead
    process = subprocess.Popen(command, stdout=subprocess.PIPE,stderr=subprocess.PIPE, text=True, cwd=os.pardir)
    output, error = process.communicate()
    gameResInfo = getGameInfo(output)

    gameResInfo['fullOutput'] = output
    gameResInfo['map'] = gameMap
    gameResInfo['teamA'] = teamA
    gameResInfo['teamB'] = teamB
    gameResInfo['currBotLabel'] = currBotLabel
    gameResInfo['opposingBotLabel'] = 'A' if currBotLabel == 'B' else 'B'
    gameResInfo['opposingBotName'] = teamA if currBotLabel == 'B' else teamB
    gameResInfo['matchup'] = f"Map:{gameMap}, TeamA:{teamA}, TeamB:{teamB}"
    if error:
        # print(f"error is: {error}")
        gameResInfo['error'] = error
    return gameResInfo

def generateGameConfigs(currBot, otherBotsList, mapsList):
    # Returns a triple of the currBot against every other bot, on every map. Also stores which one is the 'current bot'
    # returns (currMap, botA, botB, currBotLabel)
    matchupsList = [(currBot, otherBot, 'A') for otherBot in otherBotsList]
    matchupsListSwitchedSides = [(otherBot, currBot, 'B') for otherBot in otherBotsList]
    matchupsList.extend(matchupsListSwitchedSides)

    retList = []
    for currMap in mapsList:
        for botA, botB, currBotLabel in matchupsList:
            retList.append((currMap, botA, botB, currBotLabel))
    return retList

def runGamesMain(currBot, botsVersusList, mapsList):
    gamesConfigsToRun = generateGameConfigs(currBot, botsVersusList, mapsList)

    with multiprocessing.Pool() as pool:
        results = pool.map(runGames, gamesConfigsToRun)

    return results


def getGameAnalytics(gamesResList):
    if not gamesResList:
        raise ValueError('There are no maps played!')

    mapsWhereSideAltersOutcomeInfo, matchupResDict = [], defaultdict(dict)
    #matchupResDict maps: [mapName][opposingBotName] -> winningBot
    numGamesLost, numGamesTotal = 0,len(gamesResList)
    losingGamesInfo = []

    for currMatchup in gamesResList:
        currMap = currMatchup['map']
        opposingBotName = currMatchup['opposingBotName']
        if opposingBotName not in matchupResDict[currMap]:
            matchupResDict[currMap][opposingBotName] = (currMatchup['winningBot'], currMatchup['winner'])
        else:
            prevWinningBot, prevWinnerLabel = matchupResDict[currMap][opposingBotName]
            # If the previous winning bot isn't our current bot, or the winning label hasn't switched
            if prevWinningBot != currMatchup['winningBot'] or prevWinnerLabel == currMatchup['winner']:
                # Don't know which side cause the currBot to lose, but doesn't matter bc can check the losing games
                infoStr = f"Inconsistent result on map {currMap}, with one side as {currMatchup['teamA']} and other side as {currMatchup['teamB']}"
                mapsWhereSideAltersOutcomeInfo.append(infoStr)

        if currMatchup['winner'] != currMatchup['currBotLabel']:
            losingGamesInfo.append(f"currBot lost to {currMatchup['winningBot']} on map {currMap}")
            numGamesLost += 1

    numGamesWon = numGamesTotal - numGamesLost

    return {'GamesWonInfo': f"Percentage of games won: {numGamesWon/numGamesTotal}. There are {numGamesWon} games won out of {numGamesTotal} total games.",
            'losingGamesInfo': losingGamesInfo,
            'mapsWhereSideAltersOutcomeInfo': mapsWhereSideAltersOutcomeInfo}



# Example of running the test harness
if __name__ == '__main__':
    #TODO error with running multiple gradle jobs in parallel, but it still runs
    retList = runGamesMain(currBot='examplefuncsplayer',
                           botsVersusList=['examplefuncsplayer'],
                           mapsList=['DefaultSmall'])

    print('got here')
    gamesInfo = getGameAnalytics(retList)
    print(gamesInfo)



