import math
import requests
import time
from collections import defaultdict
from constants import *
from datetime import datetime
from tabulate import tabulate
import os

NUM_RESULTS_PER_PAGE = 10
EXTRA_PAGE_LOADED = 1  #Used in case other ppl queue some games in the meanwhile
BASE_URL = 'https://api.battlecode.org/api/compete/bc24/match/scrimmage/?team_id=766'
class GameStatus(Enum):
    FINISHED = 'OK!'


def getDateTimeToNearestSecond(dateTimeStrFull):
    parsed_datetime = datetime.fromisoformat(dateTimeStrFull)


    rounded_datetime = parsed_datetime.replace(second= 0, microsecond=0)

    rounded_datetime_str = rounded_datetime.strftime('%Y-%m-%dT%H:%M')
    return rounded_datetime_str



def getAllResults(numGames):
    retList = []
    numIters = math.ceil(numGames / NUM_RESULTS_PER_PAGE) + EXTRA_PAGE_LOADED

    for i in range(numIters):
        pageNum = i + 1
        currURL = BASE_URL + f"&page={pageNum}"
        currPageAsJSON = requests.get(currURL, headers=HEADERS).json()
        retList.extend(currPageAsJSON['results'])

    return retList

def gamesAreMatched(queuedGameResponse, currentGameStatus):
    queuedGameDateTime, currentGameStatusDateTime = getDateTimeToNearestSecond(queuedGameResponse['creationDateTime']), getDateTimeToNearestSecond(currentGameStatus['created'])
    if queuedGameDateTime != currentGameStatusDateTime:
        return False

    oppBotExpectedIndex = 1 if queuedGameResponse['side'] == PlayerOrder.REQUESTER_FIRST else 0
    expectedOppBotInfo = currentGameStatus['participants'][oppBotExpectedIndex]


    return expectedOppBotInfo['teamname'] == queuedGameResponse['oppTeamName']


def setGameResultString(gameProgInfo, currentGameStatus, oppBotInfo):
    oppBotName = oppBotInfo['teamname']
    oppBotPlayerIndex = oppBotInfo['player_index'] # either 0 or 1
    gameID = currentGameStatus['id']

    if currentGameStatus['status'] != 'OK!':
        gameProgInfo[oppBotName][oppBotPlayerIndex] = currentGameStatus['status']

    else:
        numGames = len(currentGameStatus['maps'])
        numGamesLost = oppBotInfo['score']
        numGamesWon = numGames - numGamesLost


        victoryStatus = 'T' if numGamesWon == numGamesLost else ('W' if numGamesWon > numGamesLost else 'L')


        gameProgInfo[oppBotName][oppBotPlayerIndex] = f"{victoryStatus}  {numGamesWon}-{numGamesLost} {gameID}"

# Maps [oppBot][side] = ResultString
def displayGames(gameProgInfo):
    dataMatrix = []
    for oppBot in gameProgInfo:
        requesterFirstGameStr = gameProgInfo[oppBot][0]
        requesterLastGameStr = gameProgInfo[oppBot][1]

        rowStr = [oppBot, requesterFirstGameStr, requesterLastGameStr]
        dataMatrix.append(rowStr)

    print(tabulate(dataMatrix, headers= ['Opponent bots', 'Requester First', 'Requester Last'], tablefmt="grid"))



# Matches the games requested with the games in the queue
# matches based on opposingBotName, side, and creation date
def matchGames(requestedGamesResponse, gameReplaysDict):
    numGamesPlayed = len(requestedGamesResponse) * 2 # List of pairs
    allGamesInfo = getAllResults(numGamesPlayed) # Can parallelize this if needed


    # Maps [oppBot][side] = ResultString
    gameProgInfo = defaultdict(lambda: [None,None])


    completedGamesSet = set() # Is a set of all completed games
    numGamesCompleted = 0
    clearCommand = 'cls' if os.name == 'nt' else 'clear'
    while numGamesCompleted < numGamesPlayed:

        for game1Info, game2Info in requestedGamesResponse:
            # Yes this is inefficient and I can change into map, but this doesn't actually affect runtime so whatevs

            for currGameResult in allGamesInfo:
                gameMatched = False

                # Match games based on opponent name, player_index, creation time
                if gamesAreMatched(game1Info, currGameResult):
                    setGameResultString(gameProgInfo,currGameResult, currGameResult['participants'][1])
                    gameMatched = True

                elif gamesAreMatched(game2Info, currGameResult):
                    setGameResultString(gameProgInfo,currGameResult, currGameResult['participants'][0])
                    gameMatched = True

                # Check if match is competed, and save the replay url (only once though!)
                if currGameResult['status'] == 'OK!' and gameMatched and currGameResult['id'] not in completedGamesSet:
                    numGamesCompleted += 1
                    completedGamesSet.add(currGameResult['id'])
                    gameReplaysDict[currGameResult['id']] = REPLAYS_WATCH_BASE_URL+ currGameResult['replay_url']

        # Clearing the terminal of prev games
        os.system(clearCommand)
        displayGames(gameProgInfo)
        # Wait for 5 seconds in between refreshing for new list of games
        print(f'{numGamesCompleted} out of {numGamesPlayed} games completed')
        allGamesInfo = getAllResults(numGamesPlayed)
        time.sleep(5)





