import concurrent.futures
import argparse
import requests
from concurrent.futures import ThreadPoolExecutor
import time

from get_queued_results import outputMatchGames

from file_operations import *
from constants import *


TEAM_ID_DICT, ID_TEAM_DICT = getTeamNameToIDDict()



def generateGameReqBody(oppTeamName, mapsList):
    oppTeamID = TEAM_ID_DICT[oppTeamName]
    reqBodyTeamA = {"is_ranked": False,
                    "requested_to": oppTeamID,
                    "player_order": PlayerOrder.REQUESTER_FIRST.value,
                    "map_names": mapsList}

    reqBodyTeamB = reqBodyTeamA.copy()
    reqBodyTeamB['player_order'] = PlayerOrder.REQUESTER_LAST.value

    return (reqBodyTeamA, reqBodyTeamB)

def generateGameReqsList(oppBotsList, mapsList):
    retList = []
    #Handling queuing > 10 maps at a time
    for i in range(0, len(mapsList), MAX_MAPS_PER_MATCH):
        currMapsSubsetList = mapsList[i:i+MAX_MAPS_PER_MATCH]

        for oppTeam in oppBotsList:
            retList.append(generateGameReqBody(oppTeam, currMapsSubsetList))
    return retList

def isValidRequest(response):
    # Check to make sure it's a 200s status code
    return 200 <= response.status_code < 300

def isValidRequest(response):
    # Check to make sure it's a 200s status code
    return 200 <= response.status_code < 300

def requestGamesCallback(callBackArgs):
    gameReqTeamA, gameReqTeamB = callBackArgs

    responsesList = []

    game1Response = requests.post(BATTLECODE_URL, headers=HEADERS, json=gameReqTeamA)
    response1Body = json.loads(game1Response.text)

    if not isValidRequest(game1Response):
        print(game1Response.text)
        return

    game1Info = {'oppTeamName': ID_TEAM_DICT[gameReqTeamA['requested_to']],
                 'creationDateTime': response1Body['created'],
                 'side': PlayerOrder.REQUESTER_FIRST,
                 'responseBody': response1Body,
                 'id': response1Body['id'],
                 'statusCode': game1Response.status_code,
                 'mapsList':response1Body['maps'],
                 'fullResponse': game1Response}
    responsesList.append(game1Info)

    # # Need to wait 1 second since we're using the creation_time as a key, which is only accurate up to the second
    # time.sleep(1)

    game2Response = requests.post(BATTLECODE_URL, headers=HEADERS, json=gameReqTeamB)

    response2Body = json.loads(game2Response.text)

    if not isValidRequest(game2Response):
        print(game2Response.text)
        return
    game2Info = {'oppTeamName': ID_TEAM_DICT[gameReqTeamA['requested_to']],
                 'creationDateTime': response2Body['created'],
                 'side': PlayerOrder.REQUESTER_LAST,
                 'responseBody': response2Body,
                 'id': response2Body['id'],
                 'statusCode': game2Response.status_code,
                 'mapsList':response2Body['maps'],
                 'fullResponse': game2Response}
    responsesList.append(game2Info)
    return responsesList


def main():
    parser = argparse.ArgumentParser(description='Runs Battlecode games and prints out the results')

    # Optional flag arguments
    parser.add_argument('-a', '--append', action='store_true', help='Append to end of match_links.json, default is overwriting the match_links.json')

    mapsGroup = parser.add_mutually_exclusive_group(required=False)
    mapsGroup.add_argument('-m', '--maps', nargs='+', type=str, help='Maps to play on')
    mapsGroup.add_argument('-mf', '--mapsfile', type=str, help='Path to a file containing list of maps to play on')
    mapsGroup.add_argument('-ma', '--mapsall', action='store_true', help='Select all maps to play on, as defined by ALL_MAPS in constant.py')

    # Mandatory args
    oppBotsGroup = parser.add_mutually_exclusive_group(required=True)
    oppBotsGroup.add_argument('-b', '--bots', nargs='+', type=str, help='List of bots to match against')
    oppBotsGroup.add_argument('-r', '--read',  type=str, help='Path to a file containing list of bots to match against')

    args = parser.parse_args()

    mapsList = DEFAULT_MAPS
    if args.mapsall:
        mapsList = ALL_MAPS
    if args.mapsfile:
        mapsList = loadJSON(args.mapsfile)
    elif args.maps:
        mapsList = args.maps
    oppBotsList = loadJSON(args.read) if args.read else args.bots
    gameReplaysDict = loadMatchResults() if args.append else OrderedDict()


    for oppBot in oppBotsList:
        if oppBot not in TEAM_ID_DICT:
            print(f"Selected bot {oppBot} doesn't accept unranked queue requests. "
                  f"Run `python3 update_other_team_info.py` to update the cache if you think this is wrong")

    gameReqs = generateGameReqsList(oppBotsList, mapsList)

    responsesList = []

    for gameRequest in gameReqs:
        currResponsesPair = requestGamesCallback(gameRequest)
        responsesList.append(currResponsesPair)
    print('Finished queueing all games, getting results now!\n')

    # Looping through to match the games
    # matchGamesFirstTimeTemp(responsesList, gameReplaysDict)
    outputMatchGames(responsesList, gameReplaysDict)

    print('All games completed! Saving match results to match_links.json')
    saveMatchResults(gameReplaysDict)


if __name__ == '__main__':
    main()
