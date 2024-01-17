import concurrent.futures
import argparse
import requests
from concurrent.futures import ThreadPoolExecutor
import time

from get_queued_results import matchGames

from read_files import *
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
    return [generateGameReqBody(oppTeam, mapsList) for oppTeam in oppBotsList]

# TODO: test this
def requestGamesCallback(callBackArgs):
    gameReqTeamA, gameReqTeamB = callBackArgs

    responsesList = []

    game1Response = requests.post(BATTLECODE_URL, headers=HEADERS, json=gameReqTeamA)
    response1Body = json.loads(game1Response.text)
    game1Info = {'oppTeamName': ID_TEAM_DICT[gameReqTeamA['requested_to']],
                 'creationDateTime': response1Body['created'],
                 'side': PlayerOrder.REQUESTER_FIRST,
                 'responseBody': response1Body,
                 'statusCode': game1Response.status_code,
                 'fullResponse': game1Response}
    responsesList.append(game1Info)

    # Need to wait 1 second since we're using the creation_time as a key, which is only accurate up to the second
    time.sleep(1)

    game2Response = requests.post(BATTLECODE_URL, headers=HEADERS, json=gameReqTeamB)

    response2Body = json.loads(game2Response.text)
    game2Info = {'oppTeamName': ID_TEAM_DICT[gameReqTeamA['requested_to']],
                 'creationDateTime': response2Body['created'],
                 'side': PlayerOrder.REQUESTER_LAST,
                 'responseBody': response2Body,
                 'statusCode': game2Response.status_code,
                 'fullResponse': game2Response}
    responsesList.append(game2Info)
    return responsesList


def main():
    parser = argparse.ArgumentParser(description='Runs Battlecode games and prints out the results')

    # Optional flag arguments
    parser.add_argument('-m', '--maps', nargs='+', type=str, default=DEFAULT_MAPS, help='Maps to play on')

    # Mandatory args
    oppBotsGroup = parser.add_mutually_exclusive_group(required=True)
    oppBotsGroup.add_argument('-b', '--bots', nargs='+', type=str, help='List of bots to match against')
    oppBotsGroup.add_argument('-r', '--read',  type=str, help='Path to a file containing list of bots to match against')

    args = parser.parse_args()

    mapsList = args.maps
    oppBotsList = loadJSON(args.read) if args.read else args.bots

    gameReqs = generateGameReqsList(oppBotsList, mapsList)


    with ThreadPoolExecutor(max_workers=5) as executor:
        futures = [executor.submit(requestGamesCallback, gameReq) for gameReq in gameReqs]
        concurrent.futures.wait(futures)
        responseList = [future.result() for future in futures]

    print(responseList)
    matchGames(responseList)


if __name__ == '__main__':
    main()
    # resList = generateGameReqsList(['camel_case','Teh Devs'], ['DefaultLarge', 'DefaultSmall'])
    # print('got here')