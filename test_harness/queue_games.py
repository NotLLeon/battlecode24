import concurrent.futures
import argparse
import requests
from concurrent.futures import ThreadPoolExecutor
import time
from enum import Enum
from read_files import *

DEFAULT_BOTS = ['Teh Devs']
DEFAULT_MAPS = ["DefaultMedium", "DefaultSmall", "DefaultHuge", "DefaultLarge"]

BATTLECODE_URL = "https://api.battlecode.org/api/compete/bc24/request/"
HEADERS = {"authority": "api.battlecode.org",
           "accept": "application/json, text/javascript, */*; q=0.01",
           "accept-language": "en-US,en;q=0.9",
           "authorization": f"Bearer {getJWTToken()}",
           "content-type": "application/json",
           "origin": "https://play.battlecode.org",
           "referer": "https://play.battlecode.org/",
           "sec-fetch-mode": "cors",
           "sec-fetch-site": "same-site",
           "user-agent": "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"}

TEAM_ID_DICT = getTeamNameToIDDict()

class PlayerOrder(Enum):
    REQUESTER_FIRST = '+' # This is teamA
    REQUESTER_LAST = '-'  # This is teamB
    ALTERNATING = '?'



def requestGamesCallback(callBackArgs):
    oppTeamName, mapsList = callBackArgs
    oppTeamID = TEAM_ID_DICT[oppTeamName]

    responsesList = []
    reqBody = {"is_ranked": False,
               "requested_to": oppTeamID,
               "player_order": PlayerOrder.REQUESTER_FIRST.value,
               "map_names": mapsList}

    game1Response = requests.post(BATTLECODE_URL, headers=HEADERS, json=reqBody)
    response1Body = json.loads(game1Response.text)
    game1Info = {'oppTeamName': oppTeamName,
                 'creationDateTime': response1Body['created'],
                 'responseBody': response1Body,
                 'statusCode': game1Response.status_code,
                 'fullResponse': game1Response}
    responsesList.append(game1Info)

    # Need to wait 1 second since we're using the creation_time as a key, which is only accurate up to the second
    time.sleep(1)

    reqBody['player_order'] = PlayerOrder.REQUESTER_LAST.value
    game2Response = requests.post(BATTLECODE_URL, headers=HEADERS, json=reqBody)

    response2Body = json.loads(game2Response.text)
    game2Info = {'oppTeamName': oppTeamName,
                 'creationDateTime': response2Body['created'],
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


    with ThreadPoolExecutor(max_workers=5) as executor:
        callBackArgs = [(oppID, mapsList) for oppID in oppBotsList]
        futures = [executor.submit(requestGamesCallback, currArg) for currArg in callBackArgs]
        concurrent.futures.wait(futures)
        responseList = [future.result() for future in futures]

    print(responseList)


if __name__ == '__main__':
    main()