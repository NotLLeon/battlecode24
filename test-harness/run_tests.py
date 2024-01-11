import os
import subprocess
import multiprocessing
import re
from collections import defaultdict

def get_winner_info(input_string):
    match = re.search(r'(\w+) \(A\) wins \(round (\d+)\)|(\w+) \(B\) wins \(round (\d+)\)', input_string)
    if not match:
        return None
    if match.group(1):
        return {'winningBot': match.group(1), 'winner': 'A', 'round': int(match.group(2))}
    elif match.group(3):
        return {'winningBot': match.group(3), 'winner': 'B', 'round': int(match.group(4))}


def getGameInfo(logStr: str):
    # Only care about lines that start with `[server]`
    importantLogLines = [line.strip() for line in logStr.split('\n') if line.strip().startswith('[server]')]

    for line in importantLogLines:
        # Assuming that there is a single instance of the word `wins`
        if 'wins' in line:
            return get_winner_info(line)

    return None

# Expected arguments: (gameMap, teamA, teamB)
def runGames(args: tuple) -> dict:
    gameMap, teamA, teamB, currBotLabel = args

    command = ["./gradlew", "run", f"-Pmaps={gameMap}", f"-PteamA={teamA}",
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
    if error:
        print(f"error is: {error}")
        gameResInfo['error'] = error
    return gameResInfo

def generateGameConfigs(currBot, otherBotsList, mapsList):
    # Returns a triple of the currBot against every other bot, on every map. Also stores which one is the 'current bot'
    # returns (currMap, botA, botB, currBotLabel)
    matchupsList = [(currBot, otherBot, 'A') for otherBot in otherBotsList]
    matchupsListRevOrder = [(otherBot, currBot, 'B') for otherBot in otherBotsList]
    matchupsList.extend(matchupsListRevOrder)

    retList = []
    for currMap in mapsList:
        for botA, botB, currBotLabel in matchupsList:
            retList.append((currMap, botA, botB, currBotLabel))
    return retList

def runGamesMain(currBot, otherBotsList, mapsList):
    gamesConfigsToRun = generateGameConfigs(currBot, otherBotsList, mapsList)

    with multiprocessing.Pool() as pool:
        results = pool.map(runGames, gamesConfigsToRun)

    retDict = defaultdict(dict)
    for resultDict in results:
        currMap = resultDict['map']
        currBotLabel = resultDict['currBotLabel']
        retDict[currMap][f"currBotAsPlayer{currBotLabel}"] = resultDict
    return retDict






if __name__ == '__main__':
    #TODO error with running multiple gradle jobs in parallel
    retDict = runGamesMain('examplefuncsplayer',['examplefuncsplayer'], ['DefaultSmall'])
    print(retDict)

    # print(results)


