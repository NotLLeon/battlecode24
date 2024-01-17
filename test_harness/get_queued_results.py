import math
import requests
import time
from collections import defaultdict
from constants import *
from datetime import datetime
from tabulate import tabulate


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
        currPageAsJSON = requests.get(currURL).json()
        retList.extend(currPageAsJSON['results'])

    return retList

def gamesAreMatched(queuedGameResponse, currentGameStatus):
    queuedGameDateTime, currentGameStatusDateTime = getDateTimeToNearestSecond(queuedGameResponse['creationDateTime']), getDateTimeToNearestSecond(currentGameStatus['created'])
    if queuedGameDateTime != currentGameStatusDateTime:
        return False

    team1, team2 = currentGameStatus['participants']
    #TODO this is hella buggy
    expectedOppBotSideGame1 = 1 if queuedGameResponse['side'] == PlayerOrder.REQUESTER_FIRST else 0
    expectedOppBotSideGame2 = 0 if queuedGameResponse['side'] == PlayerOrder.REQUESTER_FIRST else 1


    if team1['teamname'] == queuedGameResponse['oppTeamName'] and expectedOppBotSideGame1 == team1['player_index']:
        return True

    if team2['teamname'] == queuedGameResponse['oppTeamName'] and expectedOppBotSideGame2 == team2['player_index']:
        return True

    return False

def setGameResultString(gameProgInfo, currentGameStatus, oppBotInfo):
    oppBotName = oppBotInfo['teamname']
    oppBotPlayerIndex = oppBotInfo['player_index'] # either 0 or 1

    if currentGameStatus['status'] != 'OK!':
        gameProgInfo[oppBotName][oppBotPlayerIndex] = currentGameStatus['status']
    else:
        numGames = len(currentGameStatus['maps'])
        numGamesLost = oppBotInfo['score']
        numGamesWon = numGames - numGamesLost


        victoryStatus = 'T' if numGamesWon == numGamesLost else ('W' if numGamesWon > numGamesLost else 'L')

        gameProgInfo[oppBotName][oppBotPlayerIndex] = f"{victoryStatus}  {numGamesWon}-{numGamesLost} {currentGameStatus['replay_url']}"

# Maps [oppBot][side] = ResultString
def displayGames(gameProgInfo):
    dataMatrix = []
    for oppBot in gameProgInfo:
        requesterFirstGameStr = gameProgInfo[oppBot][0]
        requesterSecondGameStr = gameProgInfo[oppBot][1]

        rowStr = [oppBot, f"{requesterFirstGameStr}, {requesterSecondGameStr}"]
        dataMatrix.append(rowStr)

    print(tabulate(dataMatrix, headers= [' ', "Game Results"], tablefmt="grid"))



# Matches the games requested with the games in the queue
# matches based on opposingBotName, side, and creation date
def matchGames(requestedGamesResponse):
    numGamesPlayed = len(requestedGamesResponse) * 2 # List of pairs
    allGamesInfo = getAllResults(numGamesPlayed) # Can parallelize this if needed


    # Maps [oppBot][side] = ResultString
    gameProgInfo = defaultdict(lambda: [None,None])

    # Ugly ass triple nested loop
    while True:
        numGamesCompleted = 0

        for game1Info, game2Info in requestedGamesResponse:
            # Yes this is inefficient and I can change into map, but this doesn't actually affect runtime so whatevs

            for currGameResult in allGamesInfo:
                # Match games based on opponent name, player_index, creation time
                if gamesAreMatched(game1Info, currGameResult):
                    setGameResultString(gameProgInfo,currGameResult, currGameResult['participants'][0])
                    if currGameResult['status'] == 'OK!':
                        numGamesCompleted += 1

                elif gamesAreMatched(game2Info, currGameResult):
                    setGameResultString(gameProgInfo,currGameResult, currGameResult['participants'][1])
                    if currGameResult['status'] == 'OK!':
                        numGamesCompleted += 1


        displayGames(gameProgInfo)
        # Wait for 4 seconds in between refreshing for new list of games
        print(f'{numGamesCompleted} out of {numGamesPlayed} games completed')
        allGamesInfo = getAllResults(numGamesPlayed)
        time.sleep(4)


        if numGamesCompleted == numGamesPlayed:
            break

