import os
import subprocess
import multiprocessing
import re
from itertools import combinations

def get_winner_info(input_string):
    match = re.search(r'(\w+) \(A\) wins \(round (\d+)\)|(\w+) \(B\) wins \(round (\d+)\)', input_string)
    if not match:
        return None
    if match.group(1):
        return {'player': match.group(1), 'winner': 'A', 'round': int(match.group(2))}
    elif match.group(3):
        return {'player': match.group(3), 'winner': 'B', 'round': int(match.group(4))}


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
    gameMap, teamA, teamB = args

    command = ["./gradlew", "run", f"-Pmaps={gameMap}", f"-PteamA={teamA}",
               f"-PteamB={teamB}"]
    # cwd is battlecode24/test-harness, cd to battlecode24/ instead
    process = subprocess.Popen(command, stdout=subprocess.PIPE,stderr=subprocess.PIPE, text=True, cwd=os.pardir)
    output, error = process.communicate()
    gameResInfo = getGameInfo(output)
    gameResInfo['fullOutput'] = output
    if error:
        print(f"error is: {error}")
        gameResInfo['error'] = error
    return gameResInfo

def generateStringPairs(botsList):
    pairsList = list(combinations)
def runGamesMain(mapsList, botsList):



if __name__ == '__main__':
    argsList = [('DefaultSmall', 'examplefuncsplayer', 'examplefuncsplayer')]
    with multiprocessing.Pool() as pool:
        results = pool.map(runGames, argsList)

    print(results)


